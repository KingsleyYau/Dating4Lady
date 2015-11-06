package com.qpidnetwork.ladydating.chat.invitationtemplate;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;

import com.qpidnetwork.ladydating.R;
import com.qpidnetwork.ladydating.base.BaseFragmentPagerAdapter;
import com.qpidnetwork.ladydating.base.BaseTabbableActionbarActivity;
import com.qpidnetwork.ladydating.bean.RequestBaseResponse;
import com.qpidnetwork.ladydating.chat.invitationtemplate.InviteTemplateManager.InviteTemplateMode;
import com.qpidnetwork.ladydating.common.activity.SimpleTextEditorActivity;
import com.qpidnetwork.ladydating.customized.view.MaterialDialogAlert;
import com.qpidnetwork.request.OnRequestCallback;

public class ChatInvitationTemplateActivity extends BaseTabbableActionbarActivity implements OnClickListener{
	
	public static final String TEMPLATE_MODE = "templateMode";
	private static final int SUMMIT_TEMPLATE_CALLBACK = 0;
	
	
	BaseFragmentPagerAdapter pageAdapter;
	private String mAddTemplateContent;
	private InviteTemplateManager mInviteTemplateManager;
	private InviteTemplateMode mTemplateMode = InviteTemplateMode.EDIT_MODE;//定义模板的使用场景
	
	public static void launchInviteTemplateActivity(Context context, InviteTemplateMode mode){
		Intent intent = new Intent(context, ChatInvitationTemplateActivity.class);
		intent.putExtra(TEMPLATE_MODE, mode.ordinal());
		context.startActivity(intent);
	}
	
	public static void launchInviteTemplateActivityForResult(Activity context, InviteTemplateMode mode, int requestCode){
		Intent intent = new Intent(context, ChatInvitationTemplateActivity.class);
		intent.putExtra(TEMPLATE_MODE, mode.ordinal());
		context.startActivityForResult(intent, requestCode);
	}
	
	@Override
	public void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		Bundle bundle = getIntent().getExtras();
		if((bundle!=null)&&(bundle.containsKey(TEMPLATE_MODE))){
			mTemplateMode = InviteTemplateMode.values()[bundle.getInt(TEMPLATE_MODE)];
		}
		
		this.setActionbarTitle(getString(R.string.invitation_template), getResources().getColor(R.color.text_color_dark));
		this.requestBackIcon(R.drawable.ic_arrow_back_grey600_24dp);
		mInviteTemplateManager = InviteTemplateManager.newInstance();
	}
	
	
	/**
	 * Simulate fake post.
	 * @param string
	 */
	private void doSubmitTemplate(String tempContent){
		this.showProgressDialog(getString(R.string.loading));
		mInviteTemplateManager.addCustomTemplate(tempContent, new OnRequestCallback() {
			
			@Override
			public void OnRequest(boolean isSuccess, String errno, String errmsg) {
				RequestBaseResponse response = new RequestBaseResponse(isSuccess, errno, errmsg, null);
				Message msg = Message.obtain();
				msg.what = SUMMIT_TEMPLATE_CALLBACK;
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
		case SUMMIT_TEMPLATE_CALLBACK:{
			dismissProgressDialog();
			if (response .isSuccess){
				showDoneToast(getString(R.string.done));
				if((pageAdapter != null) && (pageAdapter.getItem(0) != null)){
					((PersonalTemplateFragment)(pageAdapter.getItem(0))).getPersonalTemplates();
				}
			}else{
				MaterialDialogAlert dialog = new MaterialDialogAlert(this);
				dialog.setMessage(getString(R.string.failed_to_submit_template_would_you_like_to_retry));
				dialog.addButton(dialog.createButton(getString(R.string.ok), this, android.R.id.button1));
				dialog.addButton(dialog.createButton(getString(R.string.cancel), null));
				if(isActivityVisible()){
					dialog.show();
				}
			}
		}break;

		default:
			break;
		}
	}
	
	@Override
	protected int setupActionbarColor() {
		// TODO Auto-generated method stub
		return R.color.white;
	}

	@Override
	protected int setupTabbarColor() {
		// TODO Auto-generated method stub
		return R.color.white;
	}
	
	@Override
	public void onPageScrollStateChanged(int arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onPageScrolled(int arg0, float arg1, int arg2) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onPageSelected(int arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	protected ViewPager setupViewPager(ViewPager viewPager) {
		// TODO Auto-generated method stub
		pageAdapter = new BaseFragmentPagerAdapter(this.getSupportFragmentManager());
		Bundle bundle = new Bundle();
		bundle.putInt(TEMPLATE_MODE, mTemplateMode.ordinal());
		Fragment personalFragment = new PersonalTemplateFragment();
		personalFragment.setArguments(bundle);
		Fragment systemFragment = new SystemTemplateFragment();
		systemFragment.setArguments(bundle);
		pageAdapter.addFragment(personalFragment, getString(R.string.personal_templeates));
		pageAdapter.addFragment(systemFragment, getString(R.string.system_templates));
		viewPager.setAdapter(pageAdapter);
		return viewPager;
	}

	@Override
	protected Drawable setupTabIcon(int position) {
		// TODO Auto-generated method stub
		return null;
	}



	@Override
	protected void onMenuItemSelected(MenuItem menu) {
		// TODO Auto-generated method stub
		switch(menu.getItemId()){
		case android.R.id.home:
			finish();
			break;
		case R.id.add:
			SimpleTextEditorActivity.InputParams params = new SimpleTextEditorActivity.InputParams();
			params.setTitle(getString(R.string.add_new_template));
			params.setMinLength(20);
			params.setMaxLength(160);
			params.setHint(getString(R.string.type_your_template_here));
			SimpleTextEditorActivity.launch(this, params);
			break;
		}
	}

	@Override
	protected boolean onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		// TODO Auto-generated method stub
		return true;
	}


	private void onSimpleTextEditorActivityResult(Intent data){
		if (data.getExtras() == null ||
				!data.getExtras().containsKey(SimpleTextEditorActivity.OutputParams.KEY_OUTPUT_PARAMS)) return;
		SimpleTextEditorActivity.OutputParams item = (SimpleTextEditorActivity.OutputParams)data.getSerializableExtra(SimpleTextEditorActivity.OutputParams.KEY_OUTPUT_PARAMS);
		mAddTemplateContent = item.getOutputText();
//		doSubmitTemplate(item.getOutputText());
		
	}
	
	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		if(!TextUtils.isEmpty(mAddTemplateContent)){
			doSubmitTemplate(mAddTemplateContent);
		}
	}
	
	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
		mAddTemplateContent = "";
	}
	
	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data){
		super.onActivityResult(requestCode, resultCode, data);
		if (resultCode != RESULT_OK) 
			return;
		
		if (SimpleTextEditorActivity.ACTIVITY_CODE == requestCode){
			onSimpleTextEditorActivityResult(data);
			return;
		}
	}
	
	/**
	 * 模板选择后回调
	 * @param tempContent
	 */
	public void onTemplateChoosed(String tempContent){
		Intent intent = new Intent();
		intent.putExtra("tempcontent", tempContent);
		setResult(RESULT_OK, intent);
	}


	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case android.R.id.button1:
			doSubmitTemplate(mAddTemplateContent);
			break;

		default:
			break;
		}
	}
}
