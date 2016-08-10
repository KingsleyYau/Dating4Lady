package com.qpidnetwork.ladydating.auth;

import android.content.Intent;
import android.os.Bundle;
import android.os.Message;
import android.text.InputType;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageButton;
import android.widget.Toast;

import com.qpidnetwork.framework.util.StringUtil;
import com.qpidnetwork.ladydating.QpidApplication;
import com.qpidnetwork.ladydating.R;
import com.qpidnetwork.ladydating.authorization.IAuthorizationCallBack;
import com.qpidnetwork.ladydating.authorization.KickOffNotification;
import com.qpidnetwork.ladydating.authorization.LoginManager;
import com.qpidnetwork.ladydating.authorization.LoginManager.LoginStatus;
import com.qpidnetwork.ladydating.authorization.LoginParam;
import com.qpidnetwork.ladydating.base.BaseFragmentActivity;
import com.qpidnetwork.ladydating.bean.RequestBaseResponse;
import com.qpidnetwork.ladydating.customized.view.MaterialDialogAlert;
import com.qpidnetwork.ladydating.customized.view.MaterialEditText;
import com.qpidnetwork.ladydating.customized.view.MaterialRaisedButton;
import com.qpidnetwork.ladydating.home.HomeActivity;
import com.qpidnetwork.livechat.LCUserItem;
import com.qpidnetwork.livechat.LiveChatManager;
import com.qpidnetwork.livechat.LiveChatManagerOtherListener;
import com.qpidnetwork.livechat.jni.LiveChatClientListener.KickOfflineType;
import com.qpidnetwork.livechat.jni.LiveChatClientListener.LiveChatErrType;
import com.qpidnetwork.livechat.jni.LiveChatClientListener.TalkEmfNoticeType;
import com.qpidnetwork.livechat.jni.LiveChatTalkUserListItem;
import com.qpidnetwork.request.ConfigManagerJni;
import com.qpidnetwork.request.OnConfigManagerCallback;
import com.qpidnetwork.request.RequestJni;
import com.qpidnetwork.request.item.LoginItem;
import com.qpidnetwork.request.item.SynConfigItem;

public class LoginActivity extends BaseFragmentActivity implements
		View.OnClickListener, IAuthorizationCallBack, OnConfigManagerCallback, LiveChatManagerOtherListener{
	
	public static final String LIVE_CHAT_KICK_OFF = "kickoff";
	private static final int SYNCONFIG_CALLBACK = 1;
	private static final int HTTP_LOGIN__CALLBACK = 2;
	private static final int LIVECHAT_CLIENT__CALLBACK = 3;

	private MaterialEditText profileId;
	private MaterialEditText password;
	private MaterialRaisedButton login;
	private ImageButton passwordVisibility;
	
	private LoginManager mLoginManager;
	/**
     * 保存当前登陆需要的账号密码
     */
    private String loginEmail = "";
	private String loginPassword = "";

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_auth_login);
		profileId = (MaterialEditText) findViewById(R.id.profile_id);
		password = (MaterialEditText) findViewById(R.id.password);
		login = (MaterialRaisedButton) findViewById(R.id.login);
		passwordVisibility = (ImageButton) findViewById(R.id.passwordVisibility);

		passwordVisibility.setOnClickListener(this);
		profileId.setNoPredition();
		login.setButtonTitle(getString(R.string.login));
		login.setOnClickListener(this);
		login.requestFocus();
		
		mLoginManager = LoginManager.getInstance();
		
		ConfigManagerJni.AddCallback(this);
		mLoginManager.AddListenner(this);
		LiveChatManager.getInstance().RegisterOtherListener(this);
		
		//自动登陆逻辑
		if((!AutoLogin()) && (mLoginManager.getSynConfigItem() == null)){
			//同步配置
			ConfigManagerJni.Sync();
		}
		
	}
	
	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		
		int kickoffInterval = ((int)(System.currentTimeMillis()/1000)) - QpidApplication.lastestKickoffTime;
        if(QpidApplication.isKickOff && kickoffInterval <= 60*60){
        	//被踢且被踢的时间差在1消失以内，弹出提示显示被踢
        	MaterialDialogAlert dialog = new MaterialDialogAlert(this);
			if(QpidApplication.kickOffType == KickOfflineType.Maintain){
				dialog.setMessage(getString(R.string.livechat_kickoff_by_sever_update));
			}
			else{
				dialog.setMessage(getString(R.string.livechat_kickoff_by_other));
			}
		
			dialog.addButton(dialog.createButton(getString(R.string.ok), null));
			dialog.show();
        }
        KickOffNotification.newInstance(this).Cancel();
        QpidApplication.isKickOff = false;
	}

	private void doLogin() {
		password.setErrorEnabled(false);
		profileId.setErrorEnabled(false);

		String loginId = profileId.getEditText().getText().toString();
		String loginPwd = password.getEditText().getText().toString();

		if (loginId.length() == 0) {
			profileId.setError(getString(R.string.required));
			profileId.setErrorEnabled(true);
			this.shakeView(profileId, true);
			return;
		}

		if (loginPwd.length() == 0) {
			password.setError(getString(R.string.required));
			password.setErrorEnabled(true);
			this.shakeView(password, true);
			return;
		}

		Login(loginId, loginPwd);
	}

	@Override
	protected void handleUiMessage(Message msg) {
		// TODO Auto-generated method stub
		super.handleUiMessage(msg);
		RequestBaseResponse response = (RequestBaseResponse)msg.obj;
		if((!response.isSuccess) && (mLoginManager.GetLoginStatus() == LoginStatus.LOGINING)){
			dismissProgressDialog();
			String errMsg = StringUtil.getErrorMsg(this, response.errno, response.errmsg);
			mLoginManager.setLoginnStatus(LoginStatus.NONE);
			Toast.makeText(this, errMsg, Toast.LENGTH_SHORT).show();
			return;
		}
		switch (msg.what) {
		case SYNCONFIG_CALLBACK:{
			SynConfigItem config = (SynConfigItem)response.body;
			mLoginManager.setSynConfigItem(config);
			if(mLoginManager.GetLoginStatus() == LoginStatus.LOGINING &&
						(!TextUtils.isEmpty(loginEmail) && (!TextUtils.isEmpty(loginPassword)))){
				/*登陆过程中，同步配置成功后http登陆*/
				mLoginManager.Login(loginEmail, loginPassword);
			}
		}break;
		case HTTP_LOGIN__CALLBACK:
			/*Livechat 自动登录*/
			break;
		case LIVECHAT_CLIENT__CALLBACK:{
			/*登陆成功，跳转到home页*/
			dismissProgressDialog();
			mLoginManager.setLoginnStatus(LoginStatus.LOGINED);
			startActivity(new Intent(this, HomeActivity.class));
			finish();	
		}break;
		default:
			break;
		}
	}

	/**
	 * toggle visibility of password
	 */
	public void onClickVisiblePassword() {
		if (password.getEditText().getInputType() == InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD) {
			password.setPassword();
			passwordVisibility
					.setImageResource(R.drawable.ic_visible_grey600_24dp);
		} else {
			password.setVisiblePassword();
			passwordVisibility
					.setImageResource(R.drawable.ic_invisible_grey600_24dp);
		}

		password.getEditText().setSelection(
				password.getEditText().getText().length());

	}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		switch (v.getId()) {
		case R.id.login:
			doLogin();
			break;
		case R.id.passwordVisibility:
			onClickVisiblePassword();
			break;
		default:
			break;
		}
	}
	
	@Override
	public void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		ConfigManagerJni.RemoveCallback(this);
		mLoginManager.RemoveListenner(this);
		LiveChatManager.getInstance().UnregisterOtherListener(this);
	}
	
	
	private void Login(String loginId, String loginPwd){
		// 同步配置成功, 这里是主线程
		setProgressDialogCanCancel(false);
		showProgressDialog(getResources().getString(R.string.logining));
		mLoginManager.setLoginnStatus(LoginStatus.LOGINING);
		this.loginEmail = loginId;
		this.loginPassword = loginPwd;
		if(mLoginManager.getSynConfigItem() != null){
			//同步配置成功直接登录
			mLoginManager.Login(loginId, loginPwd);
		}else{
			ConfigManagerJni.Sync();
		}
	}
	
	private boolean AutoLogin(){
		boolean isAutoLogin = false;
		LoginParam params = mLoginManager.GetLoginParam();
		if((params!= null) && (!TextUtils.isEmpty(params.email))){
			profileId.getEditText().setText(params.email);
		}
		if((params!= null) && (!TextUtils.isEmpty(params.password))){
			password.getEditText().setText(params.password);
		}
		if((params != null) && (!TextUtils.isEmpty(params.email))
				&& (!TextUtils.isEmpty(params.password))){
			isAutoLogin = true;
			Login(params.email, params.password);
		}
		return isAutoLogin;
	}

	@Override
	public void OnLogin(OperateType operateType, boolean isSuccess,
			String errno, String errmsg, LoginItem item) {
		Message msg = Message.obtain();
		msg.what = HTTP_LOGIN__CALLBACK;
		RequestBaseResponse response = new RequestBaseResponse(isSuccess, errno, errmsg, item);
		msg.obj = response;
		sendUiMessage(msg);
	}

	@Override
	public void OnLogout(OperateType operateType) {
		
	}

/***************************  LiveChat相关回调   ****************************************/
	@Override
	public void OnLogin(LiveChatErrType errType, String errmsg,
			boolean isAutoLogin) {
		boolean isSuccess = false;
		String errNo = "";
		if(errType == LiveChatErrType.Success){
			isSuccess = true;
		}else{
			errNo = errType.name();
		}
		Message msg = Message.obtain();
		msg.what = LIVECHAT_CLIENT__CALLBACK;
		RequestBaseResponse response = new RequestBaseResponse(isSuccess, errNo, errmsg, null);
		msg.obj = response;
		sendUiMessage(msg);
	}

	@Override
	public void OnLogout(LiveChatErrType errType, String errmsg,
			boolean isAutoLogin) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void OnGetHistoryMessage(boolean success, String errno,
			String errmsg, LCUserItem userItem) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void OnSetStatus(LiveChatErrType errType, String errmsg) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void OnUpdateStatus(LCUserItem userItem) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void OnChangeOnlineStatus(LCUserItem userItem) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void OnRecvKickOffline(KickOfflineType kickType) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void OnReplyIdentifyCode(LiveChatErrType errType, String errmsg) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void OnRecvIdentifyCode(byte[] data) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void OnRecvTalkEvent(LCUserItem item) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void OnContactListChange() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void OnRecvEMFNotice(String fromId, TalkEmfNoticeType noticeType) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void OnSearchOnlineMan(LiveChatErrType errType, String errmsg,
			String[] userIds) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void OnGetUsersInfo(LiveChatErrType errType, String errmsg, int seq,
			LiveChatTalkUserListItem[] list) {
		// TODO Auto-generated method stub
		
	}
	
	@Override
	public void OnTransStatusChange() {
		// TODO Auto-generated method stub
		
	}

/************************** 同步配置相关回调  *****************************************/
	@Override
	public void OnSynConfig(boolean isSuccess, String errno, String errmsg,
			SynConfigItem item) {
		Message msg = Message.obtain();
		msg.what = SYNCONFIG_CALLBACK;
		RequestBaseResponse response = new RequestBaseResponse(isSuccess, errno, errmsg, item);
		msg.obj = response;
		sendUiMessage(msg);
	}

}
