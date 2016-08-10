package com.qpidnetwork.ladydating.home;

import java.io.File;

import me.tangke.slidemenu.SlideMenu;
import me.tangke.slidemenu.SlideMenu.OnSlideStateChangeListener;
import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageInfo;
import android.os.Build;
import android.os.Bundle;
import android.os.Message;
import android.support.v7.widget.Toolbar;
import android.telephony.TelephonyManager;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.RelativeLayout;

import com.qpidnetwork.framework.util.StringUtil;
import com.qpidnetwork.ladydating.MainActivity;
import com.qpidnetwork.ladydating.QpidApplication;
import com.qpidnetwork.ladydating.R;
import com.qpidnetwork.ladydating.auth.LoginActivity;
import com.qpidnetwork.ladydating.authorization.LoginManager;
import com.qpidnetwork.ladydating.base.BaseFragmentActivity;
import com.qpidnetwork.ladydating.bean.RequestBaseResponse;
import com.qpidnetwork.ladydating.chat.contact.ContactManager;
import com.qpidnetwork.ladydating.chat.contact.OnUnreadCountUpdateListener;
import com.qpidnetwork.ladydating.customized.view.MaterialDialogAlert;
import com.qpidnetwork.ladydating.more.ApkUpdateService;
import com.qpidnetwork.ladydating.utility.Converter;
import com.qpidnetwork.manager.FileCacheManager;
import com.qpidnetwork.manager.MultiLanguageManager;
import com.qpidnetwork.request.OnRequestCallback;
import com.qpidnetwork.request.OnVersionCheckCallback;
import com.qpidnetwork.request.RequestJni;
import com.qpidnetwork.request.RequestJniOther;
import com.qpidnetwork.request.item.SynConfigItem;
import com.qpidnetwork.request.item.VersionCheckItem;
import com.qpidnetwork.tool.CrashPerfence;
import com.qpidnetwork.tool.CrashPerfence.CrashParam;

public class HomeActivity extends BaseFragmentActivity implements OnUnreadCountUpdateListener, OnSlideStateChangeListener{
	
	private static final int CHECK_VERSION_CALLBACK = 0;
	private static final int UNREAD_COUNT_UPDATE = 1;
	
	public static final String START_LIVECHAT_LIST = "start_livechat_list";
	public static final String NEW_INTENT_LOGOUT = "logout";
	
	private RelativeLayout masterView;
	private SlideMenu slideMenu;
	private HomeViewController homeViewController;
	private HomeLivechatViewController homeLivechatViewController;
	protected int liveChatUnreadCount = 0;
	
	private BroadcastReceiver mLanguageSetReceiver; 
	
	//crashLog 相关
	private long mRequestId = RequestJni.InvalidRequestId;
	private MaterialDialogAlert mCrashDialog = null;
	
	@Override
	protected void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_home);
//		masterView.setBackgroundColor(getResources().getColor(R.color.blue));
		masterView = (RelativeLayout) findViewById(R.id.masterView);
		slideMenu = (SlideMenu) findViewById(R.id.slideMenu);
		slideMenu.setSecondaryShadowWidth(Converter.dp2px(4.0f));
		homeViewController = new HomeViewController(this);
		homeLivechatViewController = new HomeLivechatViewController(this);

		setupSlideMenu();
		setActionbarColor();
		setActionbarTitleColor();
		initLanguageSetBroadCast();
		
		//初始化未读数目
		liveChatUnreadCount = ContactManager.getInstance().getAllUnreadCount();
		
		//绑定联系人manager，监控未读消息更新
		ContactManager.getInstance().RegisterUnreadCountChangeListener(this);
		
		// 根据消息弹出界面
		StartFromNotification(getIntent());
		
		//检测版本更新
		checkVersionOrCheckCrashLog();
	}
	
	@Override
	protected void onNewIntent(Intent intent) {
		// TODO Auto-generated method stub
		super.onNewIntent(intent);
		StartFromNotification(intent);
		logout(intent);
	}
	
	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		//查看收藏列表是否需要刷新
		if(QpidApplication.updataFavIfNeed){
			homeViewController.refreshFaveriteList();
		}
		//查看相册列表是否需要刷新
		if(QpidApplication.updateAlbumsNeed){
			homeViewController.refreshAlbumsList();
		}
	}
	
	/**
	 * 检测版本升级，如果不需要弹出升级提示，直接检测是否Crash上传
	 */
	private void checkVersionOrCheckCrashLog(){
		boolean isNeedUpgrade = false;
		SynConfigItem configItem = LoginManager.getInstance().getSynConfigItem();
		if(configItem != null){
			try{
				PackageInfo packetInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
				if(configItem.apkVersionCode > packetInfo.versionCode){
					isNeedUpgrade = true;
					VersionCheckItem item = new VersionCheckItem(configItem.apkVersionCode, configItem.apkVersionName, configItem.apkVersionUrl);
					onVersionChecked(item);
				}
			}catch(Exception e){
				
			}
		}
		if(!isNeedUpgrade){
			//检测上传CrashLog
			CheckCrashLog();
		}
	}
	
	/**
	 * 注册语言设置接收广播
	 */
	private void initLanguageSetBroadCast(){
		mLanguageSetReceiver = new BroadcastReceiver(){
			@Override
			public void onReceive(Context context, Intent intent) {
				Intent it = new Intent(context, MainActivity.class);
				context.startActivity(it);
				HomeActivity.this.finish();
			}
		};
		IntentFilter filter = new IntentFilter();
		filter.addAction(MultiLanguageManager.ACTION_CHANGE_LOACL_LANGUAGE);
		registerReceiver(mLanguageSetReceiver, filter);
	}
	
	
	private void setupSlideMenu(){
		slideMenu.addView(homeLivechatViewController.getView(), new SlideMenu.LayoutParams(
				homeLivechatViewController.getSize().x,
				android.view.ViewGroup.LayoutParams.MATCH_PARENT,
				SlideMenu.LayoutParams.ROLE_SECONDARY_MENU));

		slideMenu.addView(homeViewController.getView(), new SlideMenu.LayoutParams(
				android.view.ViewGroup.LayoutParams.MATCH_PARENT,
				android.view.ViewGroup.LayoutParams.MATCH_PARENT,
				SlideMenu.LayoutParams.ROLE_CONTENT));
		
		slideMenu.setSlideMode(SlideMenu.MODE_SLIDE_CONTENT);
		slideMenu.setOnSlideStateChangeListener(this);
	}
	
	private void setActionbarColor(){
		masterView.setBackgroundResource(R.color.app_themeColor);
		homeViewController.setActionbarColorResource(R.color.app_themeColor);
	}
	
	private void setActionbarTitleColor(){
		homeViewController.setActionbarTitleColorResource(R.color.white);
	}
	
	
	protected Toolbar getToolBar(){
		return homeViewController.getToolbar();
	}
	
	@SuppressLint("NewApi") 
	private void pushLiveChatBadge(int unreadCount){
		liveChatUnreadCount = unreadCount;
		
		if (Build.VERSION.SDK_INT < 11 ){
			this.supportInvalidateOptionsMenu();
		}else{
			this.invalidateOptionsMenu();
		}
	}
	
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
    	
        //int id = item.getItemId();
        
        switch(item.getItemId()){
        case R.id.search:
        	break;
        case R.id.contacts:
        	slideMenu.open(true, true);
        	onAnalyticsPageSelected(1);	// 统计LiveChat联系人
        	break;
        	//return false;
        }

        return super.onOptionsItemSelected(item);
    }
    
    public void scrollToLeftEdge() {
		if (homeLivechatViewController.getOffset() == 0) {
			homeLivechatViewController.setOffset(homeLivechatViewController.getStaticOffset());
			slideMenu.smoothScrollContentTo(0- homeLivechatViewController.getSize().x);
		}else{
			homeLivechatViewController.setOffset(0);
			slideMenu.smoothScrollContentTo(0- homeLivechatViewController.getSize().x);
		}
	}
    
    @Override
    public void onDestroy() {
    	// TODO Auto-generated method stub
    	super.onDestroy();
    	if(mLanguageSetReceiver != null){
    		unregisterReceiver(mLanguageSetReceiver);
    	}
    	ContactManager.getInstance().UnregisterUnreadCountChangeListener(this);
    }
    
	/*
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
    	
    	MenuItem item = menu.findItem(R.id.contacts); 
    	MenuItemCompat.setActionView(item, R.layout.badge_view_circle_solid_red_strok_white); 
    	//notifCount = (Button) MenuItemCompat.getActionView(item);
    	
    	homeViewController.setActionbarMenu(menu);
        return true;
    }*/
	
    public void CheckVersion(){
    	showProgressToast(getString(R.string.more_check_version_progress));
    	RequestJniOther.VersionCheck(new OnVersionCheckCallback() {
			@Override
			public void OnVersionCheck(boolean isSuccess, String errno, String errmsg,
					VersionCheckItem item) {
				RequestBaseResponse response = new RequestBaseResponse(isSuccess, errno, errmsg, item);
				Message msg = Message.obtain();
				msg.what = CHECK_VERSION_CALLBACK;
				msg.obj = response;
				sendUiMessage(msg);
			}
		});
    }
    
    @Override
    protected void handleUiMessage(Message msg) {
    	// TODO Auto-generated method stub
    	super.handleUiMessage(msg);
    	switch (msg.what) {
		case CHECK_VERSION_CALLBACK:{
			RequestBaseResponse response = (RequestBaseResponse)msg.obj;
	    	String errMsg = StringUtil.getErrorMsg(this, response.errno, response.errmsg);
			if(response.isSuccess){
				cancelToast();
				VersionCheckItem item = (VersionCheckItem)response.body;
				onVersionChecked(item);
			}else{
				showFailedToast(errMsg);
			}
		}break;
		case UNREAD_COUNT_UPDATE:{
			int unreadCount = msg.arg1;
			pushLiveChatBadge(unreadCount);
		}break;
		default:
			break;
		}
    }
    
    private void onVersionChecked(final VersionCheckItem item){
    	try{
			PackageInfo packetInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
			if(item.verCode > packetInfo.versionCode){
				//需要更新
				MaterialDialogAlert dialog = new MaterialDialogAlert(this);
				dialog.setMessage(getString(R.string.more_check_version_needupgrade_tips));
				dialog.addButton(dialog.createButton(getString(R.string.common_btn_upgrade), new OnClickListener(){
					@Override
					public void onClick(View v) {
						Intent intent = new Intent(HomeActivity.this, ApkUpdateService.class);
						intent.putExtra(ApkUpdateService.APk_UPDATE_VERSION_ITEM, item);
						startService(intent);
					}
				}));
				dialog.addButton(dialog.createButton(getString(R.string.cancel), null));
				dialog.show();
			}else{
				//最新版本，无需升级
				MaterialDialogAlert alert = new MaterialDialogAlert(this);
				alert.setMessage(getString(R.string.more_check_version_isnewest_tips));
				alert.addButton(alert.createButton(getString(R.string.ok), null));
				if(isActivityVisible()){
					alert.show();
				}
			}
		}catch(Exception e){
			e.printStackTrace();
		}
    }
    
    /**
	 * 从消息中心进入
	 * 
	 * @param intent
	 */
	public void StartFromNotification(Intent intent) {

		Bundle bundle = intent.getExtras();
		if (bundle != null) {
			 if (bundle.containsKey(START_LIVECHAT_LIST)
					&& bundle.getBoolean(START_LIVECHAT_LIST)) {
				// 收到最近联系人的消息（包括邀请消息）, 打开右边界面
				mHandler.postDelayed(new Runnable() {

					@Override
					public void run() {
						// TODO Auto-generated method stub
						slideMenu.open(true, false);
						onAnalyticsPageSelected(1);	// 统计LiveChat联系人
					}
				}, 1000);
			} 
		}
	}
	
	/**
	 * 注销逻辑跳回登陆界面
	 * @param intent
	 */
	private void logout(Intent newIntent){
		Bundle bundle = newIntent.getExtras();
		if (bundle != null){
			 if (bundle.containsKey(NEW_INTENT_LOGOUT)
						&& bundle.getBoolean(NEW_INTENT_LOGOUT)){
				 Intent intent = new Intent(this, LoginActivity.class);
				 startActivity(intent);
				 finish();
			 }
		}
	}
	
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) { 
			if(slideMenu.getCurrentState() == SlideMenu.STATE_OPEN_RIGHT){
				slideMenu.close(true);
				return true;
			}
			//处理返回键模仿Home键功能
	        Intent intent = new Intent(Intent.ACTION_MAIN); 
	        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK); 
	        intent.addCategory(Intent.CATEGORY_HOME); 
	        startActivity(intent); 
	        return true; 
	    }  
		return super.onKeyDown(keyCode, event);
	}
	
	/**
	 * 判断是否需要弹出CrashLog上传窗口，若是则弹出
	 */
	public void CheckCrashLog() {
		File file = new File(FileCacheManager.getInstance().GetCrashInfoPath());
		boolean bFlag = false;
		if (file.exists() && file.isDirectory() && file.list() != null
				&& file.list().length > 0) {
			for (File item : file.listFiles()) {
				if (item != null && item.isFile()) {
					// 有文件需要上传
					bFlag = true;
					break;
				}
			}
		}

		if (bFlag) {
			boolean bUploadCrash = false;
			boolean bRemember = false;

			CrashParam param = CrashPerfence.GetCrashParam(this);
			if (param != null) {
				bUploadCrash = param.bUploadCrash;
				bRemember = param.bRemember;
			}

			// 登录成功并且不在上传中
			if ((mRequestId == RequestJni.InvalidRequestId)) {
				// 已经记住
				if (bRemember) {
					if (bUploadCrash) {
						// 直接上传
						UploadCrashLog();
					}
				} else {
					if (mCrashDialog == null) {
						mCrashDialog = new MaterialDialogAlert(this);
						mCrashDialog.setTitle(getResources().getString(R.string.Crash_report_detected));
						mCrashDialog.setMessage(getResources().getString(
												R.string.Do_you_want_report_an_incident_to_help_us_for_app_improvement));
						mCrashDialog.setCheckBox(getResources().getString(R.string.Remember_my_choise), false);

						mCrashDialog.addButton(mCrashDialog.createButton(getResources().getString(R.string.common_btn_yes),
								new OnClickListener() {

									@Override
									public void onClick(View v) {
										// TODO Auto-generated method stub
										boolean checked = mCrashDialog.getCheckBox().isChecked();
										CrashPerfence.SaveCrashParam(HomeActivity.this, new CrashParam(true, checked));
										UploadCrashLog();
									}
								}));

						mCrashDialog.addButton(mCrashDialog.createButton(getResources().getString(R.string.common_btn_no),
								new OnClickListener() {

									@Override
									public void onClick(View v) {
										// TODO Auto-generated method stub
										boolean checked = mCrashDialog.getCheckBox().isChecked();
										CrashPerfence.SaveCrashParam(HomeActivity.this, new CrashParam(false, checked));
									}

								}));
					}
					mCrashDialog.show();
				}
			}
		}
	}
	
	public void UploadCrashLog() {
		TelephonyManager tm = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
		mRequestId = RequestJniOther.UploadCrashLog(RequestJni.GetDeviceId(tm),
				FileCacheManager.getInstance().GetCrashInfoPath(),
				FileCacheManager.getInstance().GetTempPath(),
				new OnRequestCallback() {

					@Override
					public void OnRequest(boolean isSuccess, String errno,
							String errmsg) {
						// TODO Auto-generated method stub
						if (isSuccess) {
							FileCacheManager.getInstance().ClearCrashLog();
						}
						mRequestId = RequestJni.InvalidRequestId;
					}
				});
	}

	@Override
	public void onUnreadUpdate(int unreadCount) {
		Message msg = Message.obtain();
		msg.what = UNREAD_COUNT_UPDATE;
		msg.arg1 = unreadCount;
		sendUiMessage(msg);
	}

	@Override
	public void onSlideStateChange(int slideState) 
	{
		// TODO Auto-generated method stub
		if (SlideMenu.STATE_CLOSE == slideState)
		{
			this.onAnalyticsPageSelected(0);
		}
	}

	@Override
	public void onSlideOffsetChange(float offsetPercent) {
		// TODO Auto-generated method stub
		
	}

}
