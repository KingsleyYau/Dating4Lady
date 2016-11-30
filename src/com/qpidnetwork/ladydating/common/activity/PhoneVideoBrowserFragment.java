package com.qpidnetwork.ladydating.common.activity;

import java.util.ArrayList;

import android.app.Activity;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Message;
import android.provider.MediaStore;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;

import com.qpidnetwork.framework.util.FileUtil;
import com.qpidnetwork.ladydating.R;
import com.qpidnetwork.ladydating.album.VideoItem;
import com.qpidnetwork.ladydating.base.BaseGridViewFragment;
import com.qpidnetwork.ladydating.customized.view.HeaderableGridView;
import com.qpidnetwork.ladydating.customized.view.MaterialDialogAlert;
import com.qpidnetwork.ladydating.utility.Converter;

public class PhoneVideoBrowserFragment extends BaseGridViewFragment implements OnItemClickListener{

	private static final int GET_VIDEOLIST_CALLBACK = 1;
	
	public static String SELECT_VIDEO_ITEM = "SELECT_VIDEO_ITEM";
	
	private ArrayList<VideoItem> mVideoList;
	private PhoneVideoBrowserAdapter adapter;
	
	@Override
	public void onRefresh() {
		// TODO Auto-generated method stub
		
	}

	@Override
	protected void onFragmentCreated(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		mVideoList = new ArrayList<VideoItem>();
		adapter = new PhoneVideoBrowserAdapter(getActivity(), mVideoList);
		getRefreshLayout().setEnabled(false);
		//异步获取系统视频列表，最大500个
		getProgressBar().setVisibility(View.VISIBLE);
		queryVidoelList();
	}

	@Override
	protected void setupGridView(HeaderableGridView gridView) {
		// TODO Auto-generated method stub
		gridView.setNumColumns(3);
		//gridView.setPadding(0, UnitConversion.dip2px(mContext, 5), 0, 0);
		gridView.setBackgroundColor(Color.WHITE);
		gridView.setVerticalSpacing(Converter.dp2px(2));
		gridView.setHorizontalSpacing(Converter.dp2px(2));
		gridView.setSelector(new ColorDrawable(Color.TRANSPARENT));
		gridView.setOnItemClickListener(this);
		gridView.setAdapter(adapter);
	}
	
	private void queryVidoelList(){
		new Thread(new Runnable() {
			
			@Override
			public void run() {
				ArrayList<VideoItem> videoList = new ArrayList<VideoItem>();
				String[] projection = {
						MediaStore.Video.VideoColumns.DATA,
						MediaStore.Video.VideoColumns.DURATION,
						MediaStore.Video.VideoColumns.SIZE,
						MediaStore.Video.VideoColumns._ID
					};		
				String selection = projection[1] + " > ?";
				String[] selectionArrs = new String[]{"500"};
				Cursor cursor = getActivity().getContentResolver().query(MediaStore.Video.Media.EXTERNAL_CONTENT_URI,  projection, selection, selectionArrs, MediaStore.Video.VideoColumns.DATE_MODIFIED + " DESC" );
				try{
					if (cursor.getCount() == 0) {
						cursor.close();
						return;
					}
					while(cursor.moveToNext()){
						String videoUri = cursor.getString(cursor.getColumnIndex(projection[0]));
						Long videoDuration = cursor.getLong(cursor.getColumnIndex(projection[1]));
						Long videoSize = cursor.getLong(cursor.getColumnIndex(projection[2]));
						if((videoDuration >= 8*1000 
									&&videoDuration <= 15*1000)
									&& videoSize < 40 * 1000 * 1000
									&& FileUtil.isValidVideo(videoUri)){
							//视频过滤，要求视频处于8-15 大小小于40M 且格式为*.wmv;*.avi;*.mp4;*.3gp
							videoList.add(new VideoItem(videoUri, videoDuration, videoSize));
						}
					}
				}catch(Exception e){
					e.printStackTrace();
				}finally{
					Message msg = Message.obtain();
					msg.what = GET_VIDEOLIST_CALLBACK;
					msg.obj = videoList;
					sendUiMessage(msg);
					cursor.close();
				}

			}
		}).start();
	} 

	@Override
	protected void handleUiMessage(Message msg) {
		// TODO Auto-generated method stub
		getProgressBar().setVisibility(View.GONE);
		switch (msg.what) {
		case GET_VIDEOLIST_CALLBACK:{
			ArrayList<VideoItem> videoList = (ArrayList<VideoItem>)msg.obj;
			mVideoList.clear();
			mVideoList.addAll(videoList);
			adapter.notifyDataSetChanged();
		}break;
		default:
			break;
		}
	}

	@Override
	public void onItemClick(AdapterView<?> arg0, View arg1, int position, long arg3) {
		// TODO Auto-generated method stub
		VideoItem videoItem = mVideoList.get(position);
		if (videoItem.duraion == 0){
			MaterialDialogAlert dialog = new MaterialDialogAlert(getActivity());
			dialog.setMessage(getString(R.string.this_video_maybe_corrupted_or_is_malformed_please_choose_another_video));
			dialog.addButton(dialog.createButton(getString(R.string.ok), null));
			dialog.show();
			return;
		}
        Bundle mBundle = new Bundle();  
        mBundle.putParcelable(SELECT_VIDEO_ITEM, videoItem); 
        getActivity().getIntent().putExtras(mBundle);
		getActivity().setResult(Activity.RESULT_OK, getActivity().getIntent());
		getActivity().finish();
		
	}

}
