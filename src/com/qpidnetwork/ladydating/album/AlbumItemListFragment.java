package com.qpidnetwork.ladydating.album;

import java.util.ArrayList;

import android.os.Bundle;
import android.os.Message;
import android.view.View;

import com.qpidnetwork.ladydating.base.BaseListViewFragment;
import com.qpidnetwork.ladydating.customized.view.ExtendableListView;

public class AlbumItemListFragment extends BaseListViewFragment{

	private AlbumItemListAdapter adapter;
	private ArrayList<AlbumItemListAdapter.AlbumItem> albumList;
	private AlbumDetailActivity homeActivity;
	
//	@Override
//	public void onRefresh() {
//		// TODO Auto-generated method stub
//		sendUiMessage(0);
//	}

	@Override
	protected void onFragmentCreated(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		homeActivity = (AlbumDetailActivity) this.getActivity();
		
		albumList = new ArrayList<AlbumItemListAdapter.AlbumItem>();
		adapter = new AlbumItemListAdapter(homeActivity, albumList);
	}
	
	@Override
	protected void setupListView(ExtendableListView listView) {
		// TODO Auto-generated method stub
		listView.setAdapter(adapter);
		createFakeData();
	}
	
	private void createFakeData(){
		ArrayList<PhotoItemAdapter.PhotoItem> underReviewList = new ArrayList<PhotoItemAdapter.PhotoItem>();
		underReviewList.add(new PhotoItemAdapter.PhotoItem("My eyes can kill you", PhotoItemAdapter.ReviewStatus.UNDER_REVIEW));
		underReviewList.add(new PhotoItemAdapter.PhotoItem("My beauty can kill you", PhotoItemAdapter.ReviewStatus.UNDER_REVIEW));
		underReviewList.add(new PhotoItemAdapter.PhotoItem("My love can kill you", PhotoItemAdapter.ReviewStatus.UNDER_REVIEW));
		underReviewList.add(new PhotoItemAdapter.PhotoItem("My kiss can kill you", PhotoItemAdapter.ReviewStatus.UNDER_REVIEW));
		
		ArrayList<PhotoItemAdapter.PhotoItem> pastList = new ArrayList<PhotoItemAdapter.PhotoItem>();
		pastList.add(new PhotoItemAdapter.PhotoItem("My eyes can see you", PhotoItemAdapter.ReviewStatus.PAST));
		pastList.add(new PhotoItemAdapter.PhotoItem("My beauty can see you", PhotoItemAdapter.ReviewStatus.PAST));
		pastList.add(new PhotoItemAdapter.PhotoItem("My love can see you", PhotoItemAdapter.ReviewStatus.PAST));
		pastList.add(new PhotoItemAdapter.PhotoItem("My kiss can see you", PhotoItemAdapter.ReviewStatus.PAST));
		
		ArrayList<PhotoItemAdapter.PhotoItem> editList = new ArrayList<PhotoItemAdapter.PhotoItem>();
		editList.add(new PhotoItemAdapter.PhotoItem("My eyes can feel you", PhotoItemAdapter.ReviewStatus.REQUIRED_EDIT));
		editList.add(new PhotoItemAdapter.PhotoItem("My beauty can feel you", PhotoItemAdapter.ReviewStatus.REQUIRED_EDIT));
		editList.add(new PhotoItemAdapter.PhotoItem("My love can feel you", PhotoItemAdapter.ReviewStatus.REQUIRED_EDIT));
		editList.add(new PhotoItemAdapter.PhotoItem("My kiss can feel you", PhotoItemAdapter.ReviewStatus.REQUIRED_EDIT));
		
		ArrayList<PhotoItemAdapter.PhotoItem> rejectedList = new ArrayList<PhotoItemAdapter.PhotoItem>();
		rejectedList.add(new PhotoItemAdapter.PhotoItem("My eyes can love you", PhotoItemAdapter.ReviewStatus.REJECTED));
		rejectedList.add(new PhotoItemAdapter.PhotoItem("My beauty can hug you", PhotoItemAdapter.ReviewStatus.REJECTED));
		rejectedList.add(new PhotoItemAdapter.PhotoItem("My love can warm you", PhotoItemAdapter.ReviewStatus.REJECTED));
		rejectedList.add(new PhotoItemAdapter.PhotoItem("My kiss can melt you", PhotoItemAdapter.ReviewStatus.REJECTED));
		
		albumList.add(new AlbumItemListAdapter.AlbumItem(AlbumItemListAdapter.Category.UNDER_REVIEW, underReviewList));
		albumList.add(new AlbumItemListAdapter.AlbumItem(AlbumItemListAdapter.Category.PAST, pastList));
		albumList.add(new AlbumItemListAdapter.AlbumItem(AlbumItemListAdapter.Category.REQUIRED_EDIT, editList));
		albumList.add(new AlbumItemListAdapter.AlbumItem(AlbumItemListAdapter.Category.REJECTED, rejectedList));
		
		sendUiMessage(0);
		
	}

	@Override
	protected void handleUiMessage(Message msg) {
		// TODO Auto-generated method stub
		if (msg.what == 0){
			adapter.notifyDataSetChanged();
			getRefreshLayout().setRefreshing(false);
			getProgressBar().setVisibility(View.GONE);
		}
	}

}
