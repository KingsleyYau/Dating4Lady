package com.qpidnetwork.ladydating.home;

import me.tangke.slidemenu.SlideMenu;
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
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.RelativeLayout;

import com.qpidnetwork.ladydating.R;
import com.qpidnetwork.ladydating.base.BaseFragmentActivity;
import com.qpidnetwork.ladydating.bean.RequestBaseResponse;
import com.qpidnetwork.ladydating.customized.view.MaterialDialogAlert;
import com.qpidnetwork.ladydating.more.ApkUpdateService;
import com.qpidnetwork.ladydating.utility.Converter;
import com.qpidnetwork.manager.MultiLanguageManager;
import com.qpidnetwork.request.OnVersionCheckCallback;
import com.qpidnetwork.request.RequestJniOther;
import com.qpidnetwork.request.item.VersionCheckItem;

public class HomeActivity extends BaseFragmentActivity{
	
	private static final int CHECK_VERSION_CALLBACK = 0;
	
	private RelativeLayout masterView;
	private SlideMenu slideMenu;
	private HomeViewController homeViewController;
	private HomeLivechatViewController homeLivechatViewController;
	protected int liveChatUnreadCount = 0;
	
	private BroadcastReceiver mLanguageSetReceiver; 
	
	@Override
	protected void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_home);
		masterView = (RelativeLayout) findViewById(R.id.masterView);
		slideMenu = (SlideMenu) findViewById(R.id.slideMenu);
		slideMenu.setSecondaryShadowWidth(Converter.dp2px(4.0f));
		homeViewController = new HomeViewController(this);
		homeLivechatViewController = new HomeLivechatViewController(this);

		setupSlideMenu();
		setActionbarColor();
		setActionbarTitleColor();
		initLanguageSetBroadCast();
	}
	
	/**
	 * 注册语言设置接收广播
	 */
	private void initLanguageSetBroadCast(){
		mLanguageSetReceiver = new BroadcastReceiver(){
			@Override
			public void onReceive(Context context, Intent intent) {
				Intent it = new Intent(context, HomeActivity.class);
				it.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
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

	}
	
	private void setActionbarColor(){
		masterView.setBackgroundResource(R.color.app_theme_cd);
		homeViewController.setActionbarColorResource(R.color.app_theme_cd);
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
    	RequestBaseResponse response = (RequestBaseResponse)msg.obj;
    	switch (msg.what) {
		case CHECK_VERSION_CALLBACK:
			if(response.isSuccess){
				cancelToast();
				VersionCheckItem item = (VersionCheckItem)response.body;
				onVersionChecked(item);
			}else{
				showFailedToast(response.errmsg);
			}
			break;
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

}
