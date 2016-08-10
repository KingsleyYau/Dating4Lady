package com.qpidnetwork.ladydating.more;

import java.util.ArrayList;

import android.content.Intent;
import android.os.Bundle;
import android.os.Message;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Toast;

import com.qpidnetwork.framework.util.StringUtil;
import com.qpidnetwork.ladydating.R;
import com.qpidnetwork.ladydating.authorization.LoginManager;
import com.qpidnetwork.ladydating.base.BaseWebViewActivity;
import com.qpidnetwork.ladydating.bean.RequestBaseResponse;
import com.qpidnetwork.ladydating.home.HomeActivity;
import com.qpidnetwork.manager.WebsiteManager;
import com.qpidnetwork.request.OnQueryMyProfileCallback;
import com.qpidnetwork.request.RequestJniOther;
import com.qpidnetwork.request.item.MyProfileItem;

public class LadyProfileDetailActivity extends BaseWebViewActivity {
	
	private static final String PHOTO_CLICK_URL = "qpidnetwork://app/womanphoto";
	private static final int GET_MY_PROFILE_CALLBACK = 1;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.setActionbarTitle(getString(R.string.more_title_my_profile), getResources().getColor(R.color.text_color_dark));
		this.requestBackIcon(R.drawable.ic_arrow_back_grey600_24dp);
		loadUrl();
	}
	
	protected void loadUrl() {
		synCookies();
		/*加载男士资料*/
		String domain = WebsiteManager.getInstance().mWebSite.webHost;
		String url = StringUtil.mergeMultiString(domain, "/","lady/myprofileview/");
		loadUrl(url);
	}

	@Override
	protected boolean dealOverrideUrl(String url) {
		boolean bFlag = false;
		if( url.contains(PHOTO_CLICK_URL) ){
			//点击相册列表查看
			getMyProfile();
			bFlag = true;
		}
		return bFlag;
	}
	
	@Override
	protected void handleUiMessage(Message msg) {
		super.handleUiMessage(msg);
		RequestBaseResponse response = (RequestBaseResponse)msg.obj;
		switch (msg.what) {
		case GET_MY_PROFILE_CALLBACK:{
			dismissProgressDialog();
			if(response.isSuccess){
				MyProfileItem item = (MyProfileItem)response.body;
				if((item != null) && (item.photoUrls != null)){
					ArrayList<String> photoUrls = new ArrayList<String>();
					for(String url : item.photoUrls){
						photoUrls.add(url);
					}
					if(item != null){
						NormalPhotoPreviewActivity.launchNoramlPhotoActivity(this, photoUrls, 0);
					}
				}
			}else{
				String errMsg = StringUtil.getErrorMsg(this, response.errno, response.errmsg);
				Toast.makeText(this, errMsg, Toast.LENGTH_SHORT).show();	
			}
		}break;
		default:
			break;
		}
	}
	
	/**
	 * 获取个人资料
	 */
	private void getMyProfile(){
		showProgressDialog(getString(R.string.loading));
		RequestJniOther.QueryMyProfile(new OnQueryMyProfileCallback() {
			
			@Override
			public void OnQueryMyProfileDetail(long requestId, boolean isSuccess,
					String errno, String errmsg, MyProfileItem item) {
				Message msg = Message.obtain();
				msg.what = GET_MY_PROFILE_CALLBACK;
				RequestBaseResponse response = new RequestBaseResponse(isSuccess, errno, errmsg, item);
				msg.obj = response;
				sendUiMessage(msg);
			}
		});
	}
	
	@Override
	protected void onMenuItemSelected(MenuItem menu) {
		// TODO Auto-generated method stub
		switch (menu.getItemId()){
		case R.id.logout:{
			// 注销
			LoginManager.getInstance().Logout(OperateType.MANUAL);
			Intent jumpIntent = new Intent(LadyProfileDetailActivity.this, HomeActivity.class);
			jumpIntent.putExtra(HomeActivity.NEW_INTENT_LOGOUT, true);
			jumpIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
			startActivity(jumpIntent);
			finish();
		}break;
		case android.R.id.home:
			finish();
			break;
		default:
			break;
		}
	}

	@Override
	protected boolean onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		inflater.inflate(R.menu.my_profile, menu);
		return true;
	}

	@Override
	protected void reloadDestUrl() {
		loadUrl();
	}
	
}
