package com.qpidnetwork.ladydating.auth;

import android.content.Intent;
import android.os.Bundle;
import android.os.Message;
import android.text.InputType;
import android.view.View;
import android.widget.ImageButton;
import android.widget.Toast;

import com.qpidnetwork.ladydating.R;
import com.qpidnetwork.ladydating.authorization.IAuthorizationCallBack;
import com.qpidnetwork.ladydating.authorization.LoginManager;
import com.qpidnetwork.ladydating.base.BaseFragmentActivity;
import com.qpidnetwork.ladydating.bean.RequestBaseResponse;
import com.qpidnetwork.ladydating.customized.view.MaterialEditText;
import com.qpidnetwork.ladydating.customized.view.MaterialRaisedButton;
import com.qpidnetwork.ladydating.home.HomeActivity;
import com.qpidnetwork.request.item.LoginItem;

public class LoginActivity extends BaseFragmentActivity implements
		View.OnClickListener, IAuthorizationCallBack {

	private MaterialEditText profileId;
	private MaterialEditText password;
	private MaterialRaisedButton login;
	private ImageButton passwordVisibility;

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
		
		LoginManager.getInstance().AddListenner(this);
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

		// 同步配置成功, 这里是主线程
		LoginManager.getInstance().Login(loginId, loginPwd);
	}

	@Override
	protected void handleUiMessage(Message msg) {
		// TODO Auto-generated method stub
		super.handleUiMessage(msg);
		RequestBaseResponse response = (RequestBaseResponse)msg.obj;
		if(response.isSuccess){
			startActivity(new Intent(this, HomeActivity.class));
			finish();
		}else{
			Toast.makeText(LoginActivity.this, "Login Failed", Toast.LENGTH_SHORT).show();
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
		LoginManager.getInstance().RemoveListenner(this);
	}

	@Override
	public void OnLogin(OperateType operateType, boolean isSuccess,
			String errno, String errmsg, LoginItem item) {
		Message msg = Message.obtain();
		RequestBaseResponse response = new RequestBaseResponse(isSuccess, errno, errmsg, item);
		msg.obj = response;
		sendUiMessage(msg);
	}

	@Override
	public void OnLogout(OperateType operateType) {
		
	}

}
