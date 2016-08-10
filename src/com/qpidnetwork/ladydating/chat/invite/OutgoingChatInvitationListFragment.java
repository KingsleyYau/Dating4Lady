package com.qpidnetwork.ladydating.chat.invite;

import java.util.ArrayList;
import java.util.List;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import com.qpidnetwork.ladydating.R;
import com.qpidnetwork.ladydating.base.BaseListViewFragment;
import com.qpidnetwork.ladydating.customized.view.ExtendableListView;
import com.qpidnetwork.ladydating.man.ManProfileActivity;
import com.qpidnetwork.livechat.LCUserItem;
import com.qpidnetwork.livechat.LiveChatManager;

public class OutgoingChatInvitationListFragment extends BaseListViewFragment implements ListView.OnItemClickListener{
	 	
	public OutgoingChatInvitationListAdapter adpater;
	public List<LCUserItem> manList;
	private LiveChatManager mLiveChatManager;
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		return super.onCreateView(inflater, container, savedInstanceState);
	}
	
	@Override
	protected void onFragmentCreated(Bundle savedInstanceState) {

		
	}
	
	@SuppressLint("NewApi") @Override
	protected void setupListView(ExtendableListView listView) {
		// TODO Auto-generated method stub
		manList = new ArrayList<LCUserItem>();
		adpater = new OutgoingChatInvitationListAdapter(getActivity(), manList);
		listView.setAdapter(adpater);
		listView.setOnItemClickListener(this);
		
		mLiveChatManager = LiveChatManager.getInstance();
		
		//关闭上拉刷新功能
		getRefreshLayout().setCanPullUp(false);
		getRefreshLayout().setCanPullDown(false);
		getProgressBar().setVisibility(View.GONE);
		setEmptyText(getResources().getString(R.string.outing_invite_list_null));
		
		updateInviteList();
	}
	
	private void updateInviteList(){
		manList.clear();
		List<LCUserItem> inviteList = mLiveChatManager.GetWomanInviteUsers();
		if(inviteList != null){
			for(LCUserItem item : inviteList){
				manList.add(item);
			}
		}
		adpater.notifyDataSetChanged();
	}
	
	@Override
	public void onResume() {
		super.onResume();
		updateInviteList();
	}
	
	@Override
	public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
		LCUserItem item = manList.get(arg2);
		ManProfileActivity.launchManProfileActivity(getActivity(), item.userId, item.userName, item.imgUrl);
	}


	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
	    super.onCreateOptionsMenu(menu, inflater);
	}

	@Override
	protected void handleUiMessage(Message msg) {
		// TODO Auto-generated method stub
		
	}
}
