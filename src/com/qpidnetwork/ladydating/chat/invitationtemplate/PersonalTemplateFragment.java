package com.qpidnetwork.ladydating.chat.invitationtemplate;

import java.util.ArrayList;
import java.util.List;

import android.os.Bundle;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import com.qpidnetwork.framework.util.StringUtil;
import com.qpidnetwork.ladydating.R;
import com.qpidnetwork.ladydating.base.BaseListViewFragment;
import com.qpidnetwork.ladydating.bean.RequestBaseResponse;
import com.qpidnetwork.ladydating.chat.invitationtemplate.CustomTemplateAdapter.OnCustomTemplateStatusClickListener;
import com.qpidnetwork.ladydating.chat.invitationtemplate.InviteTemplateManager.InviteTemplateMode;
import com.qpidnetwork.ladydating.common.activity.SimpleTextPreviewerActivity;
import com.qpidnetwork.ladydating.customized.view.ExtendableListView;
import com.qpidnetwork.ladydating.customized.view.MaterialDialogAlert;
import com.qpidnetwork.ladydating.customized.view.MaterialDialogSingleChoice;
import com.qpidnetwork.ladydating.customized.view.MaterialDropDownMenu;
import com.qpidnetwork.request.OnLCCustomTemplateCallback;
import com.qpidnetwork.request.OnRequestCallback;
import com.qpidnetwork.request.item.LiveChatInviteTemplateListItem;
import com.qpidnetwork.request.item.LiveChatInviteTemplateListItem.TemplateStatus;

public class PersonalTemplateFragment extends BaseListViewFragment implements
		ListView.OnItemClickListener, ListView.OnItemLongClickListener,
		MaterialDropDownMenu.OnClickCallback,
		OnCustomTemplateStatusClickListener,
		OnClickListener{

	private static final int GET_CUSTOM_TEMPLATES_CALLBACK = 0;
	private static final int DELETE_CUSTOM_TEMPLATES_CALLBACK = 1;

	public CustomTemplateAdapter adpater;
	public List<LiveChatInviteTemplateListItem> invitationList;
	private ChatInvitationTemplateActivity homeActivity;
	
	private InviteTemplateManager mInviteTemplateManager;
	private int mCurrLongClickPos = -1;
	
	private InviteTemplateMode mTemplateMode = InviteTemplateMode.EDIT_MODE;//定义模板的使用场景
	
	public static PersonalTemplateFragment getInstance(InviteTemplateMode mode){
		PersonalTemplateFragment fragment = new PersonalTemplateFragment();
		Bundle bundle = new Bundle();
		bundle.putInt(ChatInvitationTemplateActivity.TEMPLATE_MODE, mode.ordinal());
		fragment.setArguments(bundle);
		return fragment;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		return super.onCreateView(inflater, container, savedInstanceState);
	}

	@Override
	protected void onFragmentCreated(Bundle savedInstanceState) {
		Bundle bundle = getArguments();
		if((bundle!=null)&&(bundle.containsKey(ChatInvitationTemplateActivity.TEMPLATE_MODE))){
			mTemplateMode = InviteTemplateMode.values()[bundle.getInt(ChatInvitationTemplateActivity.TEMPLATE_MODE)];
		}
		
		homeActivity = (ChatInvitationTemplateActivity) getActivity();
		mInviteTemplateManager = InviteTemplateManager.newInstance();
	}

	@Override
	protected void setupListView(ExtendableListView listView) {

		invitationList = new ArrayList<LiveChatInviteTemplateListItem>();
		adpater = new CustomTemplateAdapter(homeActivity, invitationList, mTemplateMode);
		adpater.setOnCustomTemplateStatusClickListener(this);
		
		listView.setAdapter(adpater);
		listView.setOnItemClickListener(this);
		if(mTemplateMode == InviteTemplateMode.EDIT_MODE){
			listView.setOnItemLongClickListener(this);
		}

		// 关闭下拉刷新
		getRefreshLayout().setCanPullUp(false);
		setEmptyText(getResources().getString(R.string.personal_template_list_null));
		getPersonalTemplates();
	}

	private void reviewFlagOnClick(View v) {
		if (v.getTag() == null)
			return;
		int position = (int) v.getTag();
		String reviewMessage;
		TemplateStatus reviewStatus = invitationList.get(position).tempStatus;
		switch (reviewStatus) {
		case Pending:
			reviewMessage = getString(R.string.this_message_template_is_under_review);
			break;
		case Audited:
			reviewMessage = getString(R.string.this_message_template_is_approved);
			break;
		case Rejected:
			reviewMessage = getString(R.string.this_message_template_is_rejected);
			break;
		default:
			reviewMessage = getString(R.string.this_message_template_is_under_review);
			break;
		}

		MaterialDialogAlert dialog = new MaterialDialogAlert(homeActivity);
		dialog.setMessage(reviewMessage);
		dialog.addButton(dialog.createButton(getString(R.string.ok), null));
		dialog.show();

	}

	/**
	 * Popup menu onClick
	 */
	@Override
	public void onClick(AdapterView<?> adptView, View v, int which) {
		// TODO Auto-generated method stub
	}

	/**
	 * ListView onClick
	 */
	@Override
	public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
		LiveChatInviteTemplateListItem item = invitationList.get(arg2);
		if(mTemplateMode == InviteTemplateMode.EDIT_MODE){
			SimpleTextPreviewerActivity.launch(homeActivity, getString(R.string.invitation_template), item.tempContent);
		}else{
			if((getActivity()!=null)&&(getActivity() instanceof ChatInvitationTemplateActivity)){
				((ChatInvitationTemplateActivity)getActivity()).onTemplateChoosed(item.tempContent);
			}
		}
	}

	@Override
	public void handleUiMessage(Message msg) {
		RequestBaseResponse response = (RequestBaseResponse)msg.obj;
		String errMsg = response.errmsg;
		if(getActivity() == null){
			//fragment已经not attached
			return;
		}
		errMsg = StringUtil.getErrorMsg(getActivity(), response.errno, response.errmsg);
		switch (msg.what) {
		case GET_CUSTOM_TEMPLATES_CALLBACK:{
			getProgressBar().setVisibility(View.GONE);
			if(response.isSuccess){
				LiveChatInviteTemplateListItem[] templates = (LiveChatInviteTemplateListItem[])response.body;
				if(templates != null && templates.length > 0){
					invitationList.clear();
					for(int i=0; i<templates.length; i++){
						if(mTemplateMode == InviteTemplateMode.CHOOSE_MODE){
							if((templates[i] != null) && (templates[i].tempStatus == TemplateStatus.Audited)){
								invitationList.add(templates[i]);
							}
						}else{
							if((templates[i] != null)){
								invitationList.add(templates[i]);
							}
						}
					}
					adpater.notifyDataSetChanged();
				}
			}else{
				Toast.makeText(homeActivity, errMsg, Toast.LENGTH_LONG).show();
			}
			onRefreshComplete();
		}break;
		
		case DELETE_CUSTOM_TEMPLATES_CALLBACK:{
			dismissProgressDialog();
			if(response.isSuccess){
				showDoneToast(getResources().getString(R.string.done));
				getPersonalTemplates();
			}else{
				Toast.makeText(homeActivity, errMsg, Toast.LENGTH_LONG).show();
			}
		}break;

		default:
			break;
		}
	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		super.onCreateOptionsMenu(menu, inflater);
		if(mTemplateMode == InviteTemplateMode.EDIT_MODE){
			inflater.inflate(R.menu.add, menu);
		}
	}

	@Override
	public boolean onItemLongClick(AdapterView<?> arg0, View arg1, int arg2,
			long arg3) {
		mCurrLongClickPos = arg2;
		MaterialDialogSingleChoice dialog = new MaterialDialogSingleChoice(
				getActivity(), new String[] { getString(R.string.delete),
						getString(R.string.cancel) },
				new MaterialDialogSingleChoice.OnClickCallback() {

					@Override
					public void onClick(AdapterView<?> adptView, View v,
							int which) {
						if(which == 0){
							doDeleteConfirm();
						}
					}
				});

		dialog.show();
		return true;
	}
	
	private void doDeleteConfirm(){
		MaterialDialogAlert dialog = new MaterialDialogAlert(getActivity());
		dialog.setMessage(getString(R.string.delete_custom_template_confirm_tips));
		dialog.addButton(dialog.createButton(getString(R.string.delete), this, android.R.id.button1));
		dialog.addButton(dialog.createButton(getString(R.string.cancel), null));
		dialog.show();
	}

	@Override
	public void onStatusClick(View v) {
		reviewFlagOnClick(v);
	}
	
	@Override
	public void onPullDownToRefresh() {
		// TODO Auto-generated method stub
		super.onPullDownToRefresh();
		getPersonalTemplates();
	}

	public void getPersonalTemplates() {
		//解决空指针异常
		if(mInviteTemplateManager == null){
			mInviteTemplateManager = InviteTemplateManager.newInstance();
		}
		mInviteTemplateManager.getCustomTemplate(new OnLCCustomTemplateCallback() {

			@Override
			public void onCustomTemplate(boolean isSuccess, String errno, String errmsg,
					LiveChatInviteTemplateListItem[] tempList) {
				RequestBaseResponse response = new RequestBaseResponse(isSuccess, errno, errmsg, tempList);
				sendUiMessage(GET_CUSTOM_TEMPLATES_CALLBACK, response);
			}
		});
	}
	
	/**
	 * 删除指定自定义模板
	 */
	private void deleteCustomTemp(){
		showProgressDialog(getResources().getString(R.string.deleting));
		if((mCurrLongClickPos>=0) && (mCurrLongClickPos<invitationList.size())){
			LiveChatInviteTemplateListItem item = invitationList.get(mCurrLongClickPos);
			mInviteTemplateManager.DelCustomTemplate(item.tempId, new OnRequestCallback() {
				
				@Override
				public void OnRequest(boolean isSuccess, String errno, String errmsg) {
					RequestBaseResponse response = new RequestBaseResponse(isSuccess, errno, errmsg, null);
					sendUiMessage(DELETE_CUSTOM_TEMPLATES_CALLBACK, response);
				}
			});
		}
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case android.R.id.button1:
			deleteCustomTemp();
			break;

		default:
			break;
		}
	}
}
