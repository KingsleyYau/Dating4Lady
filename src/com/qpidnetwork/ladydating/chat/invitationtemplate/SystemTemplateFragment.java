package com.qpidnetwork.ladydating.chat.invitationtemplate;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import android.os.Bundle;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import com.qpidnetwork.ladydating.R;
import com.qpidnetwork.ladydating.base.BaseListViewFragment;
import com.qpidnetwork.ladydating.bean.RequestBaseResponse;
import com.qpidnetwork.ladydating.chat.invitationtemplate.InviteTemplateManager.InviteTemplateMode;
import com.qpidnetwork.ladydating.common.activity.SimpleTextPreviewerActivity;
import com.qpidnetwork.ladydating.customized.view.ExtendableListView;
import com.qpidnetwork.request.OnLCSystemTemplateCallback;

public class SystemTemplateFragment extends BaseListViewFragment implements ListView.OnItemClickListener{
	
	private static final int GET_SYSTEM_TEMPLATES_CALLBACK = 0;
	 	
	public SystemTemplateAdapter adpater;
	public List<String> mSystemTemplateList;
	private ChatInvitationTemplateActivity homeActivity;
	private InviteTemplateManager mInviteTemplateManager;
	
	private InviteTemplateMode mTemplateMode = InviteTemplateMode.EDIT_MODE;//定义模板的使用场景
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		return super.onCreateView(inflater, container, savedInstanceState);
	}
	
	@Override
	protected void onFragmentCreated(Bundle savedInstanceState) {
		Bundle bundle = getArguments();
		if((bundle!=null)&&(bundle.containsKey(ChatInvitationTemplateActivity.TEMPLATE_MODE))){
			mTemplateMode = InviteTemplateMode.values()[bundle.getInt(ChatInvitationTemplateActivity.TEMPLATE_MODE)];
		}
		homeActivity = (ChatInvitationTemplateActivity)getActivity();
		mInviteTemplateManager = InviteTemplateManager.newInstance();
	}
	
	@Override
	protected void setupListView(ExtendableListView listView) {
		mSystemTemplateList = new ArrayList<String>();
		adpater = new SystemTemplateAdapter(homeActivity, mSystemTemplateList);
		listView.setAdapter(adpater);
		listView.setOnItemClickListener(this);
		
		//关闭上拉刷新功能
		getRefreshLayout().setCanPullUp(false);
		setEmptyText("No system Templates now.");
		getSystemTemplates();
	}
	
	/**
	 * ListView onClick
	 */
	@Override
	public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
		String tempContent = mSystemTemplateList.get(arg2);
		if(mTemplateMode == InviteTemplateMode.EDIT_MODE){
			SimpleTextPreviewerActivity.launch(homeActivity, getString(R.string.invitation_template), tempContent);
		}else{
			if((getActivity()!=null)&&(getActivity() instanceof ChatInvitationTemplateActivity)){
				((ChatInvitationTemplateActivity)getActivity()).onTemplateChoosed(tempContent);
			}
		}
	}
	
	@Override
	public void handleUiMessage(Message msg) {
		RequestBaseResponse response = (RequestBaseResponse)msg.obj;
		switch (msg.what) {
		case GET_SYSTEM_TEMPLATES_CALLBACK:{
			getProgressBar().setVisibility(View.GONE);
			if(response.isSuccess){
				String[] templates = (String[])response.body;
				if(templates != null && templates.length > 0){
					mSystemTemplateList.clear();
					mSystemTemplateList.addAll(Arrays.asList(templates));
					adpater.notifyDataSetChanged();
				}
			}else{
				Toast.makeText(homeActivity, response.errmsg, Toast.LENGTH_LONG).show();
			}
			onRefreshComplete();
		}break;

		default:
			break;
		}
	}
	
	private void getSystemTemplates(){
		mInviteTemplateManager.getSystemTemplate(new OnLCSystemTemplateCallback() {
			
			@Override
			public void onSystemTemplate(boolean isSuccess, String errno,
					String errmsg, String[] tempList) {
				RequestBaseResponse response = new RequestBaseResponse(isSuccess, errno, errmsg, tempList);
				sendUiMessage(GET_SYSTEM_TEMPLATES_CALLBACK, response);
			}
		});
	}
	
	@Override
	public void onPullDownToRefresh() {
		// TODO Auto-generated method stub
		super.onPullDownToRefresh();
		getSystemTemplates();
	}
}
