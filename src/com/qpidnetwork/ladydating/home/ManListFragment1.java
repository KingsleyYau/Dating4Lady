package com.qpidnetwork.ladydating.home;

import java.util.ArrayList;
import java.util.List;

import android.content.Intent;
import android.graphics.Point;
import android.os.Bundle;
import android.os.Message;
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
import com.qpidnetwork.ladydating.base.BaseGridViewFragment;
import com.qpidnetwork.ladydating.customized.view.HeaderableGridView;
import com.qpidnetwork.ladydating.customized.view.MaterialDropDownMenu;
import com.qpidnetwork.ladydating.customized.view.RetriableListFooter;
import com.qpidnetwork.ladydating.man.ManProfileActivity;
import com.qpidnetwork.ladydating.utility.Converter;

public class ManListFragment1 extends BaseGridViewFragment implements GridView.OnItemClickListener,
																	 View.OnClickListener,
																	 MaterialDropDownMenu.OnClickCallback,
																	 GridView.OnScrollListener{
	 	
	public ManListAdapter adpater;
	public List<ManListAdapter.ManItem> manList;
	private HeaderableGridView gridView;
	private MaterialDropDownMenu categoryMenu;
	private View gridViewHeader;
	private RetriableListFooter gridViewFooter;
	private HomeActivity homeActivity;
	private ManSearcherWindow manSearchWindow;
	
	
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
		
		Point point = new Point();
		point.x = Converter.dp2px(240);
		point.y = LayoutParams.WRAP_CONTENT;
		
		String[] categoryText = new String[]{getString(R.string.online),
				getString(R.string.all),
				getString(R.string.recent_visitors)
		};
		
		categoryMenu = new MaterialDropDownMenu(getActivity(), categoryText, this, point);
		manSearchWindow = new ManSearcherWindow(this.getActivity());
	}
	
	@Override
	protected void setupGridView(HeaderableGridView gridView) {
		// TODO Auto-generated method stub
		this.gridView = gridView;
		manList = new ArrayList<ManListAdapter.ManItem>();
		adpater = new ManListAdapter(getActivity(), manList);
		
		gridView.addHeaderView(creaHeaderView(), null, false);
		gridView.addFooterView(createFooterView(), null, false);
		gridView.setAdapter(adpater);
		gridView.setOnItemClickListener(this);
		gridView.setOnScrollListener(this);
		getRefreshLayout().setSwipeableChildren(R.id.gridView, R.id.emptyView);
		loadData();
	}

	
	private View creaHeaderView(){
		gridViewHeader = LayoutInflater.from(getActivity()).inflate(R.layout.view_home_manlist_header, null);
		TextView category = (TextView)gridViewHeader.findViewById(R.id.category);
		category.setOnClickListener(this);
		return gridViewHeader;
		
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
				
				if (manList.size() > 70){
					sendUiMessage(2);
					return;
				}
				
				for (int i=0; i < 26; i++){
					ManListAdapter.ManItem item = new ManListAdapter.ManItem("", "Micheal", "19", "United States");
					manList.add(item);
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
			categoryMenu.showAsDropDownAtVerticalCenter(gridViewHeader);
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
	
	/**
	 * GridView onClick
	 */
	@Override
	public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
		// TODO Auto-generated method stub
		Log.v(this.getClass().getName(), arg2 + "");
		startActivity(new Intent(this.getActivity(), ManProfileActivity.class));
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
	public boolean onOptionsItemSelected(MenuItem item){
		
		switch(item.getItemId()){
		case R.id.search:
			manSearchWindow.showAsDropDown(homeActivity.getToolBar());
			break;
		}
		
		return super.onOptionsItemSelected(item);
		
	}
	
	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
	    inflater.inflate(R.menu.man_list, menu);
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
