package com.qpidnetwork.ladydating.more;

import android.os.Bundle;
import android.os.Message;
import android.text.Html;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.qpidnetwork.framework.util.StringUtil;
import com.qpidnetwork.ladydating.R;
import com.qpidnetwork.ladydating.base.BaseActionbarActivity;
import com.qpidnetwork.ladydating.bean.RequestBaseResponse;
import com.qpidnetwork.ladydating.customized.view.MaterialRaisedButton;
import com.qpidnetwork.request.OnOtherGetAgentInfoCallback;
import com.qpidnetwork.request.RequestJniOther;
import com.qpidnetwork.request.item.AgentInfoItem;

public class ContactAgentActivity extends BaseActionbarActivity{
	
	private static final int GET_AGENCY_DETAIL_CALLBACK = 1;
	
	private TextView tvAgenDetail;
	
	//错误页
	private View errorPage;
	private MaterialRaisedButton btnErrorRetry;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		this.setActionbarTitle(R.string.contact_agency, getResources().getColor(R.color.text_color_dark));
		this.requestBackIcon(R.drawable.ic_arrow_back_grey600_24dp);
		
		tvAgenDetail = (TextView)findViewById(R.id.tvAgenDetail);
		
		//error page
		errorPage = (View)findViewById(R.id.errorPage);
		btnErrorRetry = (MaterialRaisedButton)findViewById(R.id.btnErrorRetry);
		btnErrorRetry.setButtonTitle(getString(R.string.retry));
		btnErrorRetry.setOnClickListener(this);
		btnErrorRetry.requestFocus();
		
		getAgencyDetail();
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.btnErrorRetry:{
			errorPage.setVisibility(View.GONE);
			getAgencyDetail();
		}break;

		default:
			break;
		}
	}

	@Override
	protected int setupContentVew() {
		// TODO Auto-generated method stub
		return R.layout.activity_contact_agency;
	}

	@Override
	protected int setupThemeColor() {
		// TODO Auto-generated method stub
		return R.color.white;
	}

	@Override
	protected void onMenuItemSelected(MenuItem menu) {
		switch(menu.getItemId()){
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
	
	/**
	 * 获取Agency info
	 */
	private void getAgencyDetail(){
		showProgressDialog(getString(R.string.loading));
		RequestJniOther.GetAgentInfo(new OnOtherGetAgentInfoCallback() {
			
			@Override
			public void OnOtherGetAgentInfo(long requestId, boolean isSuccess,
					String errno, String errmsg, AgentInfoItem item) {
				Message msg = Message.obtain();
				msg.what = GET_AGENCY_DETAIL_CALLBACK;
				RequestBaseResponse response = new RequestBaseResponse(isSuccess, errno, errmsg, item);
				msg.obj = response;
				sendUiMessage(msg);
			}
		});
	}
	
	@Override
	protected void handleUiMessage(Message msg) {
		// TODO Auto-generated method stub
		super.handleUiMessage(msg);
		dismissProgressDialog();
		RequestBaseResponse response = (RequestBaseResponse)msg.obj;
		String errMsg = StringUtil.getErrorMsg(this, response.errno, response.errmsg);
		switch (msg.what) {
		case GET_AGENCY_DETAIL_CALLBACK:{
			if(response.isSuccess){
				AgentInfoItem item = (AgentInfoItem)response.body;
				tvAgenDetail.setText(Html.fromHtml(getResources().getString(R.string.contact_agency_description, item.name, item.tel)));
			}else{
				errorPage.setVisibility(View.VISIBLE);
			}
		}break;
		default:
			break;
		}
	}

}
