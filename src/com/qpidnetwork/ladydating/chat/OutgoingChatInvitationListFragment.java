package com.qpidnetwork.ladydating.chat;

import java.util.ArrayList;
import java.util.List;

import android.annotation.SuppressLint;
import android.os.Build;
import android.os.Bundle;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ListView;

import com.qpidnetwork.ladydating.R;
import com.qpidnetwork.ladydating.base.BaseListViewFragment;
import com.qpidnetwork.ladydating.customized.view.ExtendableListView;
import com.qpidnetwork.ladydating.customized.view.MaterialDropDownMenu;
import com.qpidnetwork.ladydating.customized.view.RetriableListFooter;

public class OutgoingChatInvitationListFragment extends BaseListViewFragment implements ListView.OnItemClickListener,
																	 View.OnClickListener,
																	 MaterialDropDownMenu.OnClickCallback,
																	 ListView.OnScrollListener{
	 	
	public OutgoingChatInvitationListAdapter adpater;
	public List<OutgoingChatInvitationListAdapter.ManItem> manList;
	private ExtendableListView listView;
	private RetriableListFooter listViewFooter;
	private OutgoingChatInvitationActivity homeActivity;
	
	
	private boolean isLoading = false;
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		return super.onCreateView(inflater, container, savedInstanceState);
	}
	
	@Override
	protected void onFragmentCreated(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		homeActivity = (OutgoingChatInvitationActivity)getActivity();
		getRefreshLayout().setRefreshing(true);
		
	}
	
	@SuppressLint("NewApi") @Override
	protected void setupListView(ExtendableListView listView) {
		// TODO Auto-generated method stub
		this.listView = listView;
		manList = new ArrayList<OutgoingChatInvitationListAdapter.ManItem>();
		adpater = new OutgoingChatInvitationListAdapter(getActivity(), manList);

		listView.addFooterView(createFooterView());
		listView.setAdapter(adpater);
		listView.setOnItemClickListener(this);
		listView.setOnScrollListener(this);
		if (Build.VERSION.SDK_INT >= 21) listView.setNestedScrollingEnabled(false);
		loadData();
	}

	
	private View createFooterView(){
		listViewFooter = new RetriableListFooter(getActivity());
		listViewFooter.showView(RetriableListFooter.ViewType.LOADING);
		listViewFooter.setVisibility(View.GONE);
		return listViewFooter;
	}
	
	private void loadData(){
		
		/**
		 * Simulating an http request which data will load on a non-ui-thread
		 */
		
		if (isLoading) return;
		
		new Thread(new Runnable(){

			@Override
			public void run() {
				// TODO Auto-generated method stub
				try {
					Thread.sleep(3000);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
				if (manList.size() > 70){
					sendUiMessage(2);
					return;
				}
				
				OutgoingChatInvitationListAdapter.ManItem item = new OutgoingChatInvitationListAdapter.ManItem("", "Micheal", "How are your doing", true);
				OutgoingChatInvitationListAdapter.ManItem item1 = new OutgoingChatInvitationListAdapter.ManItem("", "Jackma", "Very good", true );
				OutgoingChatInvitationListAdapter.ManItem item2 = new OutgoingChatInvitationListAdapter.ManItem("", "Jackma", "Very good", true);
				OutgoingChatInvitationListAdapter.ManItem item3 = new OutgoingChatInvitationListAdapter.ManItem("", "Jackma", "Very good", true);
				OutgoingChatInvitationListAdapter.ManItem item4 = new OutgoingChatInvitationListAdapter.ManItem("", "Jackma", "Very good", false);
				manList.add(item);
				manList.add(item1);
				manList.add(item2);
				manList.add(item3);
				manList.add(item4);
				
				
				for (int i=0; i < 26; i++){
					OutgoingChatInvitationListAdapter.ManItem item_ = new OutgoingChatInvitationListAdapter.ManItem("", "Micheal", "How are your doing", false);
					manList.add(item_);
				}
				sendUiMessage(0);
				
			}
			
		}).start();
		
		
	}
	
	/** 
	 * View onClick
	 */
	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		switch(v.getId()){
		case R.id.category:
			break;
		default:
			break;
		}
	}

	
	/**
	 * Popup menu onClick
	 */
	@Override
	public void onClick(AdapterView<?> adptView, View v, int which) {
		// TODO Auto-generated method stub
		
	}
	
	@Override
	public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
		// TODO Auto-generated method stub
		Log.v(this.getClass().getName(), arg2 + "");
	}
	
//	@Override
//	public void onRefresh() {
//		// TODO Auto-generated method stub
//		sendUiMessage(0);
//	}
	

	
	@Override
	public void handleUiMessage(Message msg) {
		// TODO Auto-generated method stub
		
		isLoading = false;
		
		switch (msg.what){
		
		case 0:  //success
			listViewFooter.setVisibility(View.GONE);
			this.getProgressBar().setVisibility(View.GONE);
			this.getRefreshLayout().setRefreshing(false);
			adpater.notifyDataSetChanged();
			break;
		case 1:  //no data
			setEmptyText("");  //set a message to the user
			break;
		case 2:  //error on loading data
			boolean ifErrorOnLoadingMoreItems = true;
			if (ifErrorOnLoadingMoreItems) listViewFooter.showView(RetriableListFooter.ViewType.RETRY);
			break;
		}
		
	}


	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
	    super.onCreateOptionsMenu(menu, inflater);
	}

	@Override
	public void onScroll(AbsListView view, int firstVisibleItem,
			int visibleItemCount, int totalItemCount) {
		// TODO Auto-generated method stub
		
		boolean hasMore = true;  //if has more item
		//when the last 4th position is reached, 
		//it will load more items if has more.
		if (view.getLastVisiblePosition() == (totalItemCount - 5)) {

			if (hasMore){
				//run load more items
				loadData();
				listViewFooter.showView(RetriableListFooter.ViewType.LOADING);
			}else{
				listViewFooter.showView(RetriableListFooter.ViewType.NO_MORE);
			}
			
			listViewFooter.setVisibility(View.VISIBLE);
			
		}
	}

	@Override
	public void onScrollStateChanged(AbsListView view, int scrollState) {
		// TODO Auto-generated method stub
		
	}




	
	
	
}
