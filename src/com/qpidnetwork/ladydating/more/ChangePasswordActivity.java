package com.qpidnetwork.ladydating.more;

import android.graphics.Color;
import android.os.Bundle;
import android.os.Message;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;

import com.qpidnetwork.ladydating.R;
import com.qpidnetwork.ladydating.base.BaseActionbarActivity;
import com.qpidnetwork.ladydating.bean.RequestBaseResponse;
import com.qpidnetwork.request.OnRequestCallback;
import com.qpidnetwork.request.RequestJniOther;
import com.qpidnetwork.view.MaterialTextField;

public class ChangePasswordActivity extends BaseActionbarActivity{
	
	private MaterialTextField etCurPwd;
	private MaterialTextField etNewPwd;
	private MaterialTextField etConfPwd;
	
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
		if(response.isSuccess){
			showDoneToast(getString(R.string.done));
			finish();
		}else{
			showFailedToast(response.errmsg);
		}
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
