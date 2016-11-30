package com.qpidnetwork.ladydating.chat.invitationtemplate;

import java.lang.ref.WeakReference;
import java.util.HashMap;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;

import com.qpidnetwork.ladydating.R;
import com.qpidnetwork.ladydating.base.BaseTabbableActionbarActivity;
import com.qpidnetwork.ladydating.bean.RequestBaseResponse;
import com.qpidnetwork.ladydating.chat.invitationtemplate.InviteTemplateManager.InviteTemplateMode;
import com.qpidnetwork.ladydating.customized.view.MaterialDialogAlert;
import com.qpidnetwork.request.OnRequestCallback;

public class ChatInvitationTemplateActivity extends BaseTabbableActionbarActivity implements OnClickListener{
	
	public static final String TEMPLATE_MODE = "templateMode";
	public static final String TEMPLATE_CONTENT = "templateContent";
	private static final int SUMMIT_TEMPLATE_CALLBACK = 0;
	
	private static final int EDIT_TEMPLATE_RESULT = 1002;
	
	ChatInvitePagerAdapter pageAdapter;
	private String mAddTemplateContent;
	private boolean isInviteAssistant = true;
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
		
		this.setActionbarTitle(getString(R.string.invitation_template), getResources().getColor(R.color.text_color_dark));
		this.requestBackIcon(R.drawable.ic_arrow_back_grey600_24dp);
		mInviteTemplateManager = InviteTemplateManager.newInstance();
		
		// 统计（默认为0页）
		onAnalyticsPageSelected(0);
	}
	
	
	/**
	 * Simulate fake post.
	 * @param string
	 */
	private void doSubmitTemplate(String tempContent, boolean isInviteAssistant){
		this.showProgressDialog(getString(R.string.loading));
		mInviteTemplateManager.addCustomTemplate(tempContent, isInviteAssistant, new OnRequestCallback() {
			
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
				if((pageAdapter != null) && (pageAdapter.getFragment(0) != null)){
					((PersonalTemplateFragment)(pageAdapter.getFragment(0))).getPersonalTemplates();
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
		// 统计
		onAnalyticsPageSelected(arg0);
	}

	@Override
	protected ViewPager setupViewPager(ViewPager viewPager) {
		Bundle bundle = getIntent().getExtras();
		if((bundle!=null)&&(bundle.containsKey(TEMPLATE_MODE))){
			mTemplateMode = InviteTemplateMode.values()[bundle.getInt(TEMPLATE_MODE)];
		}
		
		pageAdapter = new ChatInvitePagerAdapter(this, new String[]{
			getString(R.string.personal_templeates) , getString(R.string.system_templates)});
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
		case R.id.add:{
			Intent intent = new Intent(this, InviteTemplateEditActivity.class);
			startActivityForResult(intent, EDIT_TEMPLATE_RESULT);
		}break;
		}
	}

	@Override
	protected boolean onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		// TODO Auto-generated method stub
		return true;
	}


	private void onSimpleTextEditorActivityResult(Intent data){
		if (data.getExtras() == null ||
				!data.getExtras().containsKey(InviteTemplateEditActivity.EDIT_TEMPALTE_CONTENT)) 
			return;
		mAddTemplateContent = data.getStringExtra(InviteTemplateEditActivity.EDIT_TEMPALTE_CONTENT);
		isInviteAssistant = data.getBooleanExtra(InviteTemplateEditActivity.EDIT_TEMPLATE_AUTOINVITE_FLAG, true);
//		doSubmitTemplate(item.getOutputText());
		
	}
	
	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		if(!TextUtils.isEmpty(mAddTemplateContent)){
			doSubmitTemplate(mAddTemplateContent, isInviteAssistant);
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
		
		if (EDIT_TEMPLATE_RESULT == requestCode){
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
		intent.putExtra(TEMPLATE_CONTENT, tempContent);
		setResult(RESULT_OK, intent);
		finish();
	}


	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case android.R.id.button1:
			doSubmitTemplate(mAddTemplateContent, isInviteAssistant);
			break;

		default:
			break;
		}
	}
	
	private class ChatInvitePagerAdapter extends FragmentPagerAdapter {
		private String[] titles;
		private HashMap<Integer, WeakReference<Fragment>> mPageReference;

		public ChatInvitePagerAdapter(FragmentActivity activity, String[] titles) {
			super(activity.getSupportFragmentManager());
			this.titles = titles;
			mPageReference = new HashMap<Integer, WeakReference<Fragment>>();
		}

		public Fragment getFragment(int position) {
			Fragment fragment = null;
			if (mPageReference.containsKey(position)) {
				fragment = mPageReference.get(position).get();
			}
			return fragment;
		}

		@Override
		public int getCount() {
			return 2;
		}

		@Override
		public Fragment getItem(int position) {
			Fragment fragment = null;
			if (mPageReference.containsKey(position)) {
				fragment = mPageReference.get(position).get();
			}
			if (fragment == null) {
				if(position == 0){
					fragment = PersonalTemplateFragment.getInstance(mTemplateMode);
				}else{
					fragment = SystemTemplateFragment.getInstance(mTemplateMode);
				}
				fragment.setHasOptionsMenu(true);
				mPageReference.put(position, new WeakReference<Fragment>(
						fragment));
			}
			return fragment;
		}
		
	    @Override
	    public CharSequence getPageTitle(int position) {
	    	if(titles != null && titles.length > position){
	    		return titles[position];
	    	}else{
	    		return "";
	    	}
	    }
	}
}
