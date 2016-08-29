package com.qpidnetwork.ladydating.man;

import java.util.ArrayList;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Message;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Toast;

import com.qpidnetwork.framework.util.StringUtil;
import com.qpidnetwork.ladydating.QpidApplication;
import com.qpidnetwork.ladydating.R;
import com.qpidnetwork.ladydating.base.BaseWebViewActivity;
import com.qpidnetwork.ladydating.bean.RequestBaseResponse;
import com.qpidnetwork.ladydating.chat.ChatActivity;
import com.qpidnetwork.ladydating.chat.invitationtemplate.ChatInvitationTemplateActivity;
import com.qpidnetwork.ladydating.chat.invitationtemplate.InviteTemplateManager.InviteTemplateMode;
import com.qpidnetwork.livechat.LCMessageItem;
import com.qpidnetwork.livechat.LCMessageItem.MessageType;
import com.qpidnetwork.livechat.LCUserItem.CanSendErrType;
import com.qpidnetwork.livechat.LiveChatManager;
import com.qpidnetwork.livechat.LiveChatManagerMessageListener;
import com.qpidnetwork.livechat.jni.LiveChatClientListener.LiveChatErrType;
import com.qpidnetwork.manager.WebsiteManager;
import com.qpidnetwork.request.OnQueryManDetailCallback;
import com.qpidnetwork.request.OnRequestCallback;
import com.qpidnetwork.request.RequestJniMan;
import com.qpidnetwork.request.item.ManDetailItem;

public class ManProfileActivity extends BaseWebViewActivity implements LiveChatManagerMessageListener{
	
	private static final String MAN_PROFILE_MAN_ID = "manId";
	private static final String MAN_PROFILE_MAN_NAME = "manName";
	private static final String MAN_PROFILE_MAN_PHOTOURL = "manPhotoUrl";
	
	private static final String CHAT_CLICK_URL = "qpidnetwork://app/chat";
	private static final String ADD_FAVORITE_URL = "qpidnetwork://app/addfavorite";
	private static final String DEL_FAVORITE_URL = "qpidnetwork://app/delfavorite";
	private static final String VIEW_MANPHOTO_URL = "qpidnetwork://app/manphoto";
	
	private static final int ADD_FAVORITE_CALLBACK = 1;
	private static final int REMOVE_FAVORITE_CALLBACK = 2;
	private static final int SEND_INVITE_CALLBACK = 3;
	private static final int QUERY_MAN_DETAIL_CALLBACK = 4;
	
	private static final int RESULT_INVITE_TEMPLATE = 1001;
	
	private String mManId = "";
	private String mManName = "";
	private String mManPhotoUrl = "";
	private LiveChatManager mLivechChatManager;
	private ManDetailItem mManDetail;
	
	public static void launchManProfileActivity(Context context, String manId, String manName, String photoUrl){
		Intent intent = new Intent(context, ManProfileActivity.class);
		intent.putExtra(MAN_PROFILE_MAN_ID, manId);
		intent.putExtra(MAN_PROFILE_MAN_NAME, manName);
		intent.putExtra(MAN_PROFILE_MAN_PHOTOURL, photoUrl);
		context.startActivity(intent);
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		initData();
		mLivechChatManager = LiveChatManager.getInstance();
		loadManProfile();
		QueryManDetail();
	}
	
	private void initData(){
		Bundle bundle = getIntent().getExtras();
		if(bundle != null){
			if(bundle.containsKey(MAN_PROFILE_MAN_ID)){
				mManId = bundle.getString(MAN_PROFILE_MAN_ID);
			}
			if(bundle.containsKey(MAN_PROFILE_MAN_NAME)){
				mManName = bundle.getString(MAN_PROFILE_MAN_NAME);
			}
			if(bundle.containsKey(MAN_PROFILE_MAN_PHOTOURL)){
				mManPhotoUrl = bundle.getString(MAN_PROFILE_MAN_PHOTOURL);
			}
		}
		this.setActionbarTitle(mManName, getResources().getColor(R.color.text_color_dark));
		this.requestBackIcon(R.drawable.ic_arrow_back_grey600_24dp);
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		mLivechChatManager.RegisterMessageListener(this);
	}
	
	@Override
	public void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
		mLivechChatManager.UnregisterMessageListener(this);
	}
	
	private void loadManProfile(){
		synCookies();
		String domain = WebsiteManager.getInstance().mWebSite.webHost;
		String url = StringUtil.mergeMultiString(domain, "/",
				"lady/manprofileview/man_id/", mManId, "/versioncode/", QpidApplication.versionCode);
		loadUrl(url);
	}
	
	/**
	 * 获取男士详情，用于点击看大图，图片Url获取
	 */
	private void QueryManDetail(){
		if(!TextUtils.isEmpty(mManId)){
			RequestJniMan.QueryManDetail(mManId, new OnQueryManDetailCallback() {
				
				@Override
				public void OnQueryManDetail(boolean isSuccess, String errno,
						String errmsg, ManDetailItem item) {
					Message msg = Message.obtain();
					msg.what = QUERY_MAN_DETAIL_CALLBACK;
					RequestBaseResponse response = new RequestBaseResponse(isSuccess, errno, errmsg, item);
					msg.obj = response;
					sendUiMessage(msg);
				}
			});
		}
	}
	
	@Override
	protected boolean dealOverrideUrl(String url) {
		boolean bFlag = false;
		if( url.contains(CHAT_CLICK_URL) ){
			boolean isContact = mLivechChatManager.IsInContactList(mManId);
			if(isContact){
				ChatActivity.launchChatActivity(this, mManId, mManName, mManPhotoUrl);
			}else{
				ChatInvitationTemplateActivity.launchInviteTemplateActivityForResult(this, 
						InviteTemplateMode.CHOOSE_MODE, RESULT_INVITE_TEMPLATE);
			}
			bFlag = true;
		} else if( url.contains(ADD_FAVORITE_URL) ) {
			AddFavorite();
			bFlag = true;
		} else if( url.contains(DEL_FAVORITE_URL) ) {
			DelFavorite();
			bFlag = true;
		}else if( url.contains(VIEW_MANPHOTO_URL) ) {
			if(mManDetail != null
					&& !TextUtils.isEmpty(mManDetail.photo_big_url)){
				ManPhotoPreviewActivity.launchManPhotoPreviewActivity(this, mManDetail.photo_big_url);
			}
			bFlag = true;
		}
		return bFlag;
	}
	
	/**
	 * 添加收藏
	 */
	private void AddFavorite(){
		showProgressToast(getString(R.string.processing));
		RequestJniMan.AddFavourites(mManId, new OnRequestCallback() {
			
			@Override
			public void OnRequest(boolean isSuccess, String errno, String errmsg) {
				Message msg = Message.obtain();
				msg.what = ADD_FAVORITE_CALLBACK;
				RequestBaseResponse response = new RequestBaseResponse(isSuccess, errno, errmsg, null);
				msg.obj = response;
				sendUiMessage(msg);
			}
		});
	}
	
	/**
	 * 取消收藏
	 */
	private void DelFavorite(){
		showProgressToast(getString(R.string.deleting));
		RequestJniMan.RemoveFavourites(new String[]{mManId}, new OnRequestCallback() {
			
			@Override
			public void OnRequest(boolean isSuccess, String errno, String errmsg) {
				Message msg = Message.obtain();
				msg.what = REMOVE_FAVORITE_CALLBACK;
				RequestBaseResponse response = new RequestBaseResponse(isSuccess, errno, errmsg, null);
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
		String errMsg = StringUtil.getErrorMsg(this, response.errno, response.errmsg);
		switch (msg.what) {
		case ADD_FAVORITE_CALLBACK:{
			if(response.isSuccess){
				showDoneToast(getString(R.string.done));
				onFavoriteChangeJs(true);
			}else{
				showFailedToast(errMsg);	
			}
		}break;

		case REMOVE_FAVORITE_CALLBACK:{
			if(response.isSuccess){
				showDoneToast(getString(R.string.done));
				onFavoriteChangeJs(false);
			}else{
				showFailedToast(errMsg);
			}
		}break;
		case SEND_INVITE_CALLBACK:{
			if(!response.isSuccess){
				if(TextUtils.isEmpty(errMsg)){
					errMsg = getResources().getString(R.string.send_error_text_normal);
				}
				Toast.makeText(this, errMsg, Toast.LENGTH_SHORT).show();
			}else{
				Toast.makeText(this, getResources().getString(R.string.livechat_send_invite_success), Toast.LENGTH_SHORT).show();
			}
		}break;
		case QUERY_MAN_DETAIL_CALLBACK:{
			if(response.isSuccess){
				mManDetail = (ManDetailItem)response.body;
			}
		}break;
		default:
			break;
		}
	}
	
	/**
	 * 添加收藏或取消收藏成功后回调Js更新界面
	 * @param isFavour
	 */
	private void onFavoriteChangeJs(boolean isFavour){
		//删除或添加收藏后标记需要更新收藏列表
		QpidApplication.updataFavIfNeed = true;
		
		String url = "javascript:js_update_favorite(";
		url += "'" +  isFavour + "')";
		loadUrl(url);
	}
	
	@Override
	protected void onMenuItemSelected(MenuItem menu) {
		// TODO Auto-generated method stub
		switch (menu.getItemId()){
		case android.R.id.home:
			finish();
			break;
		default:
			break;
		}
	}

	@Override
	protected boolean onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		// TODO Auto-generated method stub
		return true;
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		// TODO Auto-generated method stub
		super.onActivityResult(requestCode, resultCode, data);
		if(resultCode == RESULT_OK){
			if(requestCode == RESULT_INVITE_TEMPLATE){
				//选择模板返回，发送邀请
				if(data != null){
					Bundle bundle = data.getExtras();
					if((bundle!=null)&&(bundle.containsKey(ChatInvitationTemplateActivity.TEMPLATE_CONTENT))){
						String msg = bundle.getString(ChatInvitationTemplateActivity.TEMPLATE_CONTENT);
						CanSendErrType errType = mLivechChatManager.CanSendMessage(mManId, MessageType.Text);
						if(errType == CanSendErrType.OK){
							mLivechChatManager.SendMessage(mManId, msg);
						}else{
							//发送频率过快
							Toast.makeText(this, getString(R.string.livechat_send_invitemsg_frequently), Toast.LENGTH_SHORT).show();
						}
					}
				}
			}
		}
	}
	
	/***********************  Livechat 发送消息回调  *************************************/
	@Override
	public void OnSendMessage(LiveChatErrType errType, String errmsg,
			LCMessageItem item) {
		if((item != null) && (item.getUserItem() != null)
				&& mManId.equals(item.getUserItem().userId)){
			//发送给当前男士邀请错误返回提示用户
			Message msg = Message.obtain();
			msg.what = SEND_INVITE_CALLBACK;
			RequestBaseResponse response = new RequestBaseResponse(errType == LiveChatErrType.Success ? true:false, errType.name(), errmsg, null);
			msg.obj = response;
			sendUiMessage(msg);
		}		
	}

	@Override
	public void OnRecvMessage(LCMessageItem item) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void OnRecvWarning(LCMessageItem item) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void OnRecvEditMsg(String fromId) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void OnRecvSystemMsg(LCMessageItem item) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void OnSendMessageListFail(LiveChatErrType errType,
			ArrayList<LCMessageItem> msgList) {
		// TODO Auto-generated method stub
		
	}

	@Override
	protected void reloadDestUrl() {
		loadManProfile();
	}
}
