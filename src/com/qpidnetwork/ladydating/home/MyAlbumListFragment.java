package com.qpidnetwork.ladydating.home;

import java.util.ArrayList;
import java.util.List;

import android.content.Intent;
import android.graphics.Point;
import android.os.Bundle;
import android.os.Message;
import android.support.v4.view.MenuItemCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.TextView;

import com.qpidnetwork.ladydating.R;
import com.qpidnetwork.ladydating.album.AlbumDetailActivity;
import com.qpidnetwork.ladydating.album.AlbumEditActivity;
import com.qpidnetwork.ladydating.base.BaseGridViewFragment;
import com.qpidnetwork.ladydating.customized.view.HeaderableGridView;
import com.qpidnetwork.ladydating.customized.view.MaterialDropDownMenu;
import com.qpidnetwork.ladydating.customized.view.RetriableListFooter;
import com.qpidnetwork.ladydating.utility.Converter;

public class MyAlbumListFragment extends BaseGridViewFragment implements GridView.OnItemClickListener,
																	 View.OnClickListener,
																	 MaterialDropDownMenu.OnClickCallback,
																	 GridView.OnScrollListener{
	 	
	public MyAlbumListAdapter adpater;
	public List<MyAlbumListAdapter.AlbumItem> albumList;
	private HeaderableGridView gridView;
	private RetriableListFooter gridViewFooter;
	private HomeActivity homeActivity;
	
	
	private boolean isLoading = false;
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		return super.onCreateView(inflater, container, savedInstanceState);
	}
	
	@Override
	protected void onFragmentCreated(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		homeActivity = (HomeActivity)getActivity();
		getRefreshLayout().setRefreshing(true);

	}
	
	@Override
	protected void setupGridView(HeaderableGridView gridView) {
		// TODO Auto-generated method stub
		this.gridView = gridView;
		albumList = new ArrayList<MyAlbumListAdapter.AlbumItem>();
		adpater = new MyAlbumListAdapter(getActivity(), albumList);

		gridView.addFooterView(createFooterView(), null, false);
		gridView.setAdapter(adpater);
		gridView.setOnItemClickListener(this);
		gridView.setOnScrollListener(this);
		getRefreshLayout().setSwipeableChildren(R.id.gridView, R.id.emptyView);
		loadData();
	}


	
	private View createFooterView(){
		gridViewFooter = new RetriableListFooter(getActivity());
		gridViewFooter.showView(RetriableListFooter.ViewType.LOADING);
		gridViewFooter.setVisibility(View.GONE);
		return gridViewFooter;
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
				
				if (albumList.size() > 70){
					sendUiMessage(2);
					return;
				}
				
				if (albumList.size() == 0){
					albumList.add(new MyAlbumListAdapter.AlbumItem("", "", MyAlbumListAdapter.AlbumItem.AlbumType.PHOTO, 0));
					albumList.add(new MyAlbumListAdapter.AlbumItem("", "", MyAlbumListAdapter.AlbumItem.AlbumType.EDIT, 5));
				}
				
				MyAlbumListAdapter.AlbumItem item = new MyAlbumListAdapter.AlbumItem("", "This is my album", MyAlbumListAdapter.AlbumItem.AlbumType.PHOTO, 0);
				albumList.add(item);
				item = new MyAlbumListAdapter.AlbumItem("", "This is my album", MyAlbumListAdapter.AlbumItem.AlbumType.VIDEO, 5);
				albumList.add(item);
				item = new MyAlbumListAdapter.AlbumItem("", "This is my album", MyAlbumListAdapter.AlbumItem.AlbumType.PHOTO, 5);
				albumList.add(item);
				item = new MyAlbumListAdapter.AlbumItem("", "This is my album", MyAlbumListAdapter.AlbumItem.AlbumType.VIDEO, 0);
				albumList.add(item);

				
				for (int i=0; i < 26; i++){
					item = new MyAlbumListAdapter.AlbumItem("", "This is my album", MyAlbumListAdapter.AlbumItem.AlbumType.PHOTO, 0);
					albumList.add(item);
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
		if (arg2 == 0){
			AlbumEditActivity.launch(homeActivity);
			return;
		}
		
		MyAlbumListAdapter.AlbumItem albumItem = albumList.get(arg2);
		switch(albumItem.albumType){
			case EDIT:
				break;
			case PHOTO:
				AlbumDetailActivity.launch(homeActivity, albumItem.albumName, "albumid001", AlbumDetailActivity.AlbumType.PHOTO, albumItem.albumDescription);
				break;
			case VIDEO:
				AlbumDetailActivity.launch(homeActivity, albumItem.albumName, "albumid001", AlbumDetailActivity.AlbumType.VIDEO, albumItem.albumDescription);
				break;
			default:
				break;
		}
	}
	
	@Override
	public void onRefresh() {
		// TODO Auto-generated method stub
		sendUiMessage(0);
	}
	

	
	@Override
	public void handleUiMessage(Message msg) {
		// TODO Auto-generated method stub
		
		isLoading = false;
		
		switch (msg.what){
		
		case 0:  //success
			gridViewFooter.setVisibility(View.GONE);
			this.getProgressBar().setVisibility(View.GONE);
			this.getRefreshLayout().setRefreshing(false);
			adpater.notifyDataSetChanged();
			break;
		case 1:  //no data
			setEmptyText("");  //set a message to the user
			break;
		case 2:  //error on loading data
			boolean ifErrorOnLoadingMoreItems = true;
			if (ifErrorOnLoadingMoreItems) gridViewFooter.showView(RetriableListFooter.ViewType.RETRY);
			break;
		}
		
	}


	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
	    inflater.inflate(R.menu.other_list, menu);
	    MenuItem item = menu.findItem(R.id.contacts); 
    	//MenuItemCompat.setActionView(item, R.layout.badge_view_circle_solid_red_strok_white); 
    	if (homeActivity.liveChatUnreadCount > 0){
    		item.setIcon(R.drawable.ic_man_list_white_24dp_badged);
    	}else{
    		item.setIcon(R.drawable.ic_man_list_white_24dp);
    	}
    	
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
				gridViewFooter.showView(RetriableListFooter.ViewType.LOADING);
			}else{
				gridViewFooter.showView(RetriableListFooter.ViewType.NO_MORE);
			}
			
			gridViewFooter.setVisibility(View.VISIBLE);
			
		}
	}

	@Override
	public void onScrollStateChanged(AbsListView view, int scrollState) {
		// TODO Auto-generated method stub
		
	}




	
	
	
}
