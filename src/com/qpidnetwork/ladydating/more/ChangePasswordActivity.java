package com.qpidnetwork.ladydating.more;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Message;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.qpidnetwork.framework.util.StringUtil;
import com.qpidnetwork.ladydating.R;
import com.qpidnetwork.ladydating.authorization.LoginManager;
import com.qpidnetwork.ladydating.authorization.IAuthorizationCallBack.OperateType;
import com.qpidnetwork.ladydating.base.BaseActionbarActivity;
import com.qpidnetwork.ladydating.bean.RequestBaseResponse;
import com.qpidnetwork.ladydating.home.HomeActivity;
import com.qpidnetwork.request.OnRequestCallback;
import com.qpidnetwork.request.RequestJniOther;
import com.qpidnetwork.view.MaterialTextField;

public class ChangePasswordActivity extends BaseActionbarActivity{
	
	private MaterialTextField etCurPwd;
	private MaterialTextField etNewPwd;
	private MaterialTextField etConfPwd;
	
	private boolean isModifySuccess = false;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		this.setActionbarTitle(R.string.more_title_change_psw, getResources().getColor(R.color.text_color_dark));
		this.requestBackIcon(R.drawable.ic_arrow_back_grey600_24dp);
		initViews();
	}
	
	private void initViews(){
		etCurPwd = (MaterialTextField)findViewById(R.id.etCurPwd);
		etNewPwd = (MaterialTextField)findViewById(R.id.etNewPwd);
		etConfPwd = (MaterialTextField)findViewById(R.id.etConfPwd);
		
		etCurPwd.setNoPredition();
		etCurPwd.setHint(getString(R.string.more_current_password));

		etNewPwd.setNoPredition();
		etNewPwd.setHint(getString(R.string.more_new_password));
		
		etConfPwd.setNoPredition();
		etConfPwd.setHint(getString(R.string.more_confirm_password));
	}
	
	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		//防止更改过程中Home键返回后台，异步问题
		if(isModifySuccess){
			jumpToLoginActivity();
		}
	}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		
	}
	@Override
	protected int setupContentVew() {
		return R.layout.activity_change_password;
	}
	@Override
	protected int setupThemeColor() {
		return R.color.white;
	}
	@Override
	protected void onMenuItemSelected(MenuItem menu) {
		switch(menu.getItemId()){
		case R.id.done:
			doSubmit();
			break;
		case android.R.id.home:
			finish();
			break;
		default:
			break;
		}		
	}
	@Override
	protected boolean onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		inflater.inflate(R.menu.done, menu);
		return true;
	}
	
	@Override
	protected void handleUiMessage(Message msg) {
		super.handleUiMessage(msg);
		dismissProgressDialog();
		RequestBaseResponse response = (RequestBaseResponse)msg.obj;
		String errMsg = StringUtil.getErrorMsg(this, response.errno, response.errmsg);
		if(response.isSuccess){
			if(isActivityVisible()){
				jumpToLoginActivity();
			}else{
				isModifySuccess = true;
			}
		}else{
			Toast.makeText(this, getString(R.string.modify_password_fail_tips), Toast.LENGTH_SHORT).show();
		}
	}
	
	/**
	 * 修改密码成功，跳转回登陆界面
	 */
	private void jumpToLoginActivity(){
		isModifySuccess = false;
		Toast.makeText(this, getString(R.string.modify_password_success_tips), Toast.LENGTH_SHORT).show();
		LoginManager.getInstance().Logout(OperateType.MANUAL);
		finish();
		Intent jumpIntent = new Intent(ChangePasswordActivity.this, HomeActivity.class);
		jumpIntent.putExtra(HomeActivity.NEW_INTENT_LOGOUT, true);
		jumpIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		startActivity(jumpIntent);
	}
	
	private void doSubmit(){
		String curPwd = etCurPwd.getText().toString();
		String newPwd = etNewPwd.getText().toString();
		String confPwd = etConfPwd.getText().toString();
		
		if (curPwd.length() < 3){
			etCurPwd.setError(Color.RED, true);
			return;
		}
		
		if (newPwd.length() < 3){
			etNewPwd.setError(Color.RED, true);
			return;
		}
		
		if (confPwd.length() < 3){
			etConfPwd.setError(Color.RED, true);
			return;
		}
		if(!newPwd.equals(confPwd)){
			etNewPwd.setError(Color.RED, true);
			return;
		}
		
		setProgressDialogCanCancel(false);
		showProgressDialog(getString(R.string.common_wait_tips));
		RequestJniOther.ModifyPassword(curPwd, newPwd, new OnRequestCallback() {
			
			@Override
			public void OnRequest(boolean isSuccess, String errno, String errmsg) {
				RequestBaseResponse response = new RequestBaseResponse(isSuccess, errno, errmsg, null);
				Message msg = Message.obtain();
				msg.obj = response;
				sendUiMessage(msg);
			}	
		});
	}
}	
