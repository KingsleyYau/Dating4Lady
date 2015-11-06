package com.qpidnetwork.ladydating.common.activity;

import java.io.File;
import java.util.ArrayList;

import android.app.Activity;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Message;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;

import com.qpidnetwork.ladydating.R;
import com.qpidnetwork.ladydating.base.BaseGridViewFragment;
import com.qpidnetwork.ladydating.customized.view.HeaderableGridView;
import com.qpidnetwork.ladydating.customized.view.MaterialDialogAlert;
import com.qpidnetwork.ladydating.utility.Converter;

public class PhoneVideoBrowserFragment extends BaseGridViewFragment implements OnItemClickListener{

	private ArrayList<PhoneVideoBrowserAdapter.VideoItem> videoList;
	private PhoneVideoBrowserAdapter adapter;
	
	@Override
	public void onRefresh() {
		// TODO Auto-generated method stub
		
	}

	@Override
	protected void onFragmentCreated(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		videoList = new ArrayList<PhoneVideoBrowserAdapter.VideoItem>();
		adapter = new PhoneVideoBrowserAdapter(getActivity(), videoList);
		getProgressBar().setVisibility(View.GONE);
		queryVidoelList();
		getRefreshLayout().setEnabled(false);
	}

	@Override
	protected void setupGridView(HeaderableGridView gridView) {
		// TODO Auto-generated method stub
		gridView.setNumColumns(2);
		gridView.setVerticalSpacing(Converter.dp2px(2));
		gridView.setHorizontalSpacing(Converter.dp2px(1));
		gridView.setSelector(new ColorDrawable(Color.TRANSPARENT));
		gridView.setOnItemClickListener(this);
		gridView.setAdapter(adapter);

		
	}
	
	private void queryVidoelList(){
		String[] projection = {
			MediaStore.Video.VideoColumns.DATA,
			MediaStore.Video.VideoColumns.DURATION,
			MediaStore.Video.VideoColumns._ID
		};
		
		String selection = projection[1] + " > ?";
		String[] selectionArrs = new String[]{"500"};
		Cursor cursor = getActivity().getContentResolver().query(MediaStore.Video.Media.EXTERNAL_CONTENT_URI,  projection, selection, selectionArrs, null );
		
		if (cursor.getCount() == 0) {
			cursor.close();
			return;
		}
		
		
		while(cursor.moveToNext()){
			
			String videoUri = cursor.getString(cursor.getColumnIndex(projection[0]));
			Long videoDuration = cursor.getLong(cursor.getColumnIndex(projection[1]));
			int videoId = cursor.getInt(cursor.getColumnIndex(projection[2]));
			String thumbUri = queryVideoThumbnail(videoId);
			videoList.add(new PhoneVideoBrowserAdapter.VideoItem(videoUri, thumbUri, videoDuration));
		}
		
		cursor.close();
		adapter.notifyDataSetChanged();

		
	}
	
	private String queryVideoThumbnail(int videoId){
		String thumbUri = "";
		String[] projection = {
				MediaStore.Video.Thumbnails.DATA
		};
		String selection = MediaStore.Video.Thumbnails.VIDEO_ID + "= ?";
		String[] selectionArr = new String[]{videoId + ""};
		Cursor cursor = getActivity().getContentResolver().query(MediaStore.Video.Thumbnails.EXTERNAL_CONTENT_URI,  projection, selection, selectionArr, null );
		if (cursor.moveToFirst()) thumbUri = cursor.getString(cursor.getColumnIndex(projection[0]));
		cursor.close();
		
		return thumbUri;
	}

	@Override
	protected void handleUiMessage(Message msg) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
		// TODO Auto-generated method stub
		PhoneVideoBrowserAdapter.VideoItem videoItem = videoList.get(arg2);
		if (videoItem.duraion == 0 || videoItem.thumbnailUri.length() == 0){
			MaterialDialogAlert dialog = new MaterialDialogAlert(getActivity());
			dialog.setMessage(getString(R.string.this_video_maybe_corrupted_or_is_malformed_please_choose_another_video));
			dialog.addButton(dialog.createButton(getString(R.string.ok), null));
			dialog.show();
			return;
		}
		
		getActivity().getIntent().setData(Uri.fromFile(new File(videoItem.videoUri)));
		getActivity().setResult(Activity.RESULT_OK, getActivity().getIntent());
		getActivity().finish();
		
	}

}
