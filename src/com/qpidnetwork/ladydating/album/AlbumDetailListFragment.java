package com.qpidnetwork.ladydating.album;

import java.util.ArrayList;

import android.content.Intent;
import android.os.Bundle;
import android.os.Message;
import android.util.Log;
import android.view.View;

import com.qpidnetwork.ladydating.R;
import com.qpidnetwork.ladydating.album.AlbumDetailListAdapter.OnItemClickCallback;
import com.qpidnetwork.ladydating.base.BaseListViewFragment;
import com.qpidnetwork.ladydating.customized.view.ExtendableListView;
import com.qpidnetwork.ladydating.customized.view.MaterialDialogAlert;

public class AlbumDetailListFragment extends BaseListViewFragment implements OnItemClickCallback,
																			 View.OnClickListener{

	private AlbumDetailListAdapter adapter;
	private ArrayList<AlbumDetailListAdapter.AlbumItem> albumList;
	private AlbumDetailActivity homeActivity;
	private String inputAlbumName;
	private AlbumDetailActivity.AlbumType inputAlbumType;
	
	private static class IDs{
		public final static int dialog_retry_button = 0X0001F;
		public final static int dialog_cancel_button = 0X0002F;
	}

	@Override
	protected void onFragmentCreated(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		homeActivity = (AlbumDetailActivity) this.getActivity();
		inputAlbumName = getActivity().getIntent().getExtras().getString(AlbumDetailActivity.INPUT_ALBUM_NAME);
		inputAlbumType = AlbumDetailActivity.AlbumType.valueOf(getActivity().getIntent().getExtras().getString(AlbumDetailActivity.INPUT_ALBUM_TYPE));
		
		albumList = new ArrayList<AlbumDetailListAdapter.AlbumItem>();
		adapter = new AlbumDetailListAdapter(homeActivity, albumList, this);
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
		underReviewList.get(3).uploadState = PhotoItemAdapter.UploadState.UPLOAD_FAILED;
		
		
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
		
		albumList.add(new AlbumDetailListAdapter.AlbumItem(AlbumDetailListAdapter.Category.UNDER_REVIEW, underReviewList));
		albumList.add(new AlbumDetailListAdapter.AlbumItem(AlbumDetailListAdapter.Category.PAST, pastList));
		albumList.add(new AlbumDetailListAdapter.AlbumItem(AlbumDetailListAdapter.Category.REQUIRED_EDIT, editList));
		albumList.add(new AlbumDetailListAdapter.AlbumItem(AlbumDetailListAdapter.Category.REJECTED, rejectedList));
		
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
	
	protected void onAddPhotoActivityCallback(Intent data){
		if (data == null ||
				data.getExtras() == null||
				!data.getExtras().containsKey(EditPhotoActivity.OUTPUT_PHOTO_URI)||
				!data.getExtras().containsKey(EditPhotoActivity.OUTPUT_PHOTO_DESCRIPTION)){
			return;
		}
		
		String description = data.getExtras().getString(EditPhotoActivity.OUTPUT_PHOTO_DESCRIPTION);
		String photoUri = data.getExtras().getString(EditPhotoActivity.OUTPUT_PHOTO_URI);
		
		ArrayList<PhotoItemAdapter.PhotoItem> underReviewList = albumList.get(0).photoItems;
		PhotoItemAdapter.PhotoItem photoItem = new PhotoItemAdapter.PhotoItem(description, PhotoItemAdapter.ReviewStatus.UNDER_REVIEW);
		photoItem.photoUri = photoUri;
		photoItem.uploadState = PhotoItemAdapter.UploadState.UPLOADING;
		underReviewList.add(0, photoItem);
		
		adapter.notifyDataSetChanged();
	}



	@Override
	public void onItemClick(View adapterView, View viewClickOn, int listViewPosition, int gridViewPosition) {
		// TODO Auto-generated method stub
		PhotoItemAdapter.PhotoItem photoItem = albumList.get(listViewPosition).photoItems.get(gridViewPosition);
		if (photoItem.uploadState == PhotoItemAdapter.UploadState.UPLOAD_FAILED){
			MaterialDialogAlert dialog = new MaterialDialogAlert(getActivity());
			dialog.setMessage(getString(R.string.this_item_did_not_successfully_uploaded));
			dialog.addButton(dialog.createButton(getString(R.string.retry), this, IDs.dialog_retry_button));
			dialog.addButton(dialog.createButton(getString(R.string.cancel), this, IDs.dialog_cancel_button));
			dialog.show();
			return;
		}
		
		if (photoItem.reviewStatus == PhotoItemAdapter.ReviewStatus.REQUIRED_EDIT){
			if (inputAlbumType == AlbumDetailActivity.AlbumType.PHOTO){
				EditPhotoActivity.launchWithEditPhotoMode(getActivity(), inputAlbumName, photoItem.title, "");
			}else{
				EditVideoActivity.launchWithEditVideoMode(getActivity(), inputAlbumName, photoItem.title, EditVideoActivity.RequireEditType.BOTH);
			}
			
			
		}else{
			//launch photo preview activity
		}
		
	}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		switch(v.getId()){
		case IDs.dialog_retry_button:
			break;
		case IDs.dialog_cancel_button:
			break;
		}
	}
	
	
	

}
