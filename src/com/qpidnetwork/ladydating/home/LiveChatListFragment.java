package com.qpidnetwork.ladydating.home;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.os.Bundle;
import android.os.Message;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;

import com.qpidnetwork.ladydating.R;
import com.qpidnetwork.ladydating.base.BaseListViewFragment;
import com.qpidnetwork.ladydating.chat.ChatActivity;
import com.qpidnetwork.ladydating.chat.contact.ContactManager;
import com.qpidnetwork.ladydating.chat.contact.OnContactListChangeListener;
import com.qpidnetwork.ladydating.customized.view.ExtendableListView;
import com.qpidnetwork.livechat.LCUserItem;
import com.qpidnetwork.livechat.LiveChatManager;

public class LiveChatListFragment extends BaseListViewFragment implements 
						OnItemClickListener, OnContactListChangeListener{
	
	private List<LCUserItem> mContactList;
	private LiveChatListAdapter mAdapter;
	private ContactManager mContactManager;
	private LiveChatManager mLivechatChatManager;

	@Override
	protected void onFragmentCreated(Bundle savedInstanceState) {
		
	}

	@Override
	protected void setupListView(ExtendableListView listView) {
		mContactList = new ArrayList<LCUserItem>();
		mAdapter = new LiveChatListAdapter(getActivity(), mContactList);
		listView.setAdapter(mAdapter);
		listView.setOnItemClickListener(this);
		
		//关闭上拉刷新功能
		getRefreshLayout().setCanPullUp(false);
		getRefreshLayout().setCanPullDown(false);
		getProgressBar().setVisibility(View.GONE);
		setEmptyText(getResources().getString(R.string.livechat_contact_list_null));	
		
		updateContactList();
	}
	
	@Override
	public void onAttach(Activity activity) {
		// TODO Auto-generated method stub
		super.onAttach(activity);
		mContactManager = ContactManager.getInstance();
		mLivechatChatManager = LiveChatManager.getInstance();
		mContactManager.RegisterContactListChangeListener(this);
	}
	
	@Override
	public void onDetach() {
		// TODO Auto-generated method stub
		super.onDetach();
		mContactManager.UnregisterContactListChangeListener(this);
	}

	@Override
	protected void handleUiMessage(Message msg) {
		
	}
	
	/**
	 * 更新联系人列表
	 */
	private void updateContactList(){
		mContactList.clear();
		List<LCUserItem> userList = mLivechatChatManager.GetContactList();
		for(LCUserItem item : userList){
			mContactList.add(item);
		}
		mAdapter.notifyDataSetChanged();
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position,
			long id) {
		LCUserItem item = mContactList.get(position);
		ChatActivity.launchChatActivity(getActivity(), item.userId, item.userName, item.imgUrl);
	}

	@Override
	public void onContactListChange() {
		/*联系人列表改变，刷新列表*/
		updateContactList();
	}

}
