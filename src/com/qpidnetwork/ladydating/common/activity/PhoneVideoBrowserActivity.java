package com.qpidnetwork.ladydating.common.activity;

import java.io.File;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;

import com.qpidnetwork.ladydating.R;
import com.qpidnetwork.ladydating.album.VideoItem;
import com.qpidnetwork.ladydating.base.BaseFragmentActivity;
import com.qpidnetwork.ladydating.utility.ActivityCodeUtil;

public class PhoneVideoBrowserActivity extends BaseFragmentActivity{

	public static final int ACTIVITY_CODE = ActivityCodeUtil.PHONO_VIDEO_BROWSER_ACTIVITY_CODE;
	
	public static void launch(Context context){
		Intent intent = new Intent(context, PhoneVideoBrowserActivity.class);
		((FragmentActivity) context).startActivityForResult(intent, ACTIVITY_CODE);
	}
	
	@Override
	public void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_phone_photo_browser);
		
		Toolbar toolbar = (Toolbar) findViewById(R.id.baseToolbar);
		View adaptiveAcitionbarShawdow = (View) findViewById(R.id.adaptiveTabbarShawdow);
		if (Build.VERSION.SDK_INT >= 21) 
			adaptiveAcitionbarShawdow.setVisibility(View.GONE);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        toolbar.setBackgroundColor(getResources().getColor(setupThemeColor()));
        actionBar.setHomeAsUpIndicator(R.drawable.ic_arrow_back_grey600_24dp);
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setTitle(getString(R.string.video_browser));
        toolbar.setTitleTextColor(getResources().getColor(R.color.text_color_dark));
        
		FragmentTransaction ft = this.getSupportFragmentManager().beginTransaction();
		ft.replace(R.id.fragmentReplacement, new PhoneVideoBrowserFragment()).commit();
	}

	private void captureVideo() {
	    Intent takeVideoIntent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
	    takeVideoIntent.putExtra(MediaStore.EXTRA_SIZE_LIMIT, 40*1000*1000);//限制最大40M
	    takeVideoIntent.putExtra(MediaStore.EXTRA_DURATION_LIMIT, 15);//限制最长录制15秒
	    if (takeVideoIntent.resolveActivity(getPackageManager()) != null) {
	        startActivityForResult(takeVideoIntent, ActivityCodeUtil.CAPTURE_VIDEO_ACTIVITY_CODE);
	    }
	}
	
	private void onVideoCaptureActivityResult(Intent data){
		if (data.getData() == null) 
			return;
		Uri videoUri = data.getData();
		
		String videoPath = getVideoCapturePath(videoUri);
		if(TextUtils.isEmpty(videoPath) 
				|| !new File(videoPath).exists()){
			return;
		}
		
		VideoItem videoItem = new VideoItem();
		videoItem.videoUri = videoPath;
		
		Uri mediaContentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
		String[] projections = new String[]{
				MediaStore.Video.VideoColumns.DATA,
				MediaStore.Video.VideoColumns.DURATION,
				MediaStore.Video.VideoColumns.SIZE
		};
		String whereCase = projections[0] + " = ? ";
		String[] whereCaseArray = new String[]{videoPath};
		
		Cursor cursor = null;
		try{
			cursor = this.getContentResolver().query(mediaContentUri, projections, whereCase, whereCaseArray, null);
			if (cursor.moveToFirst()){
				videoItem.videoUri = cursor.getString(cursor.getColumnIndex(MediaStore.Video.VideoColumns.DATA));
				videoItem.duraion = cursor.getInt(cursor.getColumnIndex(MediaStore.Video.VideoColumns.DURATION))/1000;
				videoItem.size = cursor.getInt(cursor.getColumnIndex(MediaStore.Video.VideoColumns.SIZE));
			}else{
				MediaPlayer mp = MediaPlayer.create(this, Uri.parse(videoPath));
				videoItem.duraion = mp.getDuration()/1000;
				mp.release();
			}
		}catch(Exception e){
			e.printStackTrace();
		}finally{
			if(cursor != null){
				cursor.close();
			}
		}
		
		 Bundle mBundle = new Bundle();  
	     mBundle.putParcelable(PhoneVideoBrowserFragment.SELECT_VIDEO_ITEM, videoItem); 
	     getIntent().putExtras(mBundle);
	     setResult(Activity.RESULT_OK, getIntent());
	     finish();
		
	}
	
	/**
	 * 解析返回VideoUri获取文件路径
	 * @param videoUri
	 */
	private String getVideoCapturePath(Uri videoUri){
		String videoPath = "";
		if(videoUri != null 
				&& !TextUtils.isEmpty(videoUri.getScheme())){
			if(videoUri.getScheme().equals("file")){
				videoPath = videoUri.getPath();
			}else if(videoUri.getScheme().equals("content")){
				Cursor cursor = null;
				String[] projections = new String[]{
						MediaStore.Video.VideoColumns.DATA
				};
				try{
					cursor = this.getContentResolver().query(videoUri, projections, null, null, null);
					if(cursor != null){
						if (cursor.moveToFirst()){
							videoPath =  cursor.getString(cursor.getColumnIndex(MediaStore.Video.VideoColumns.DATA));
						}
					}
				}catch(Exception e){
					e.printStackTrace();
				}finally{
					if(cursor != null){
						cursor.close();
					}
				}
			}
		}
		return videoPath;
	}
	
//	private VideoItem getVideoInfo(Intent data) {
//		// TODO Auto-generated method stub
//		VideoItem videoItem = null;
//		
//		Uri uri = data.getData();
//		Cursor cursor = this.getContentResolver().query(uri, null, null,null, null);
//		if (cursor != null && cursor.moveToNext()) {
//			int id = cursor.getInt(cursor.getColumnIndex(VideoColumns._ID));
//			String videoUri = cursor.getString(cursor.getColumnIndex(VideoColumns.DATA));
//			Long videoDuration = cursor.getLong(cursor.getColumnIndex(VideoColumns.DURATION));
//			Bitmap bitmap = Thumbnails.getThumbnail(getContentResolver(),id, Thumbnails.MICRO_KIND, null);
//			String localPath = FileCacheManager.getInstance().CacheVideoThumbnailFromVideoUri(videoUri);
//			ImageUtil.saveBitmapToFile(localPath, bitmap, CompressFormat.JPEG, 100);
//			videoItem = new VideoItem(videoUri,videoDuration);
//			//ThumbnailUtils类2.2以上可用
//			//Bitmap bitmap = ThumbnailUtils.createVideoThumbnail(filePath, Thumbnails.MICRO_KIND);
//			//Bitmap bitmap = ThumbnailUtils.extractThumbnail(bitmap,size,size,ThumbnailUtils.OPTIONS_RECYCLE_INPUT);  
//			cursor.close();
//		}
//		return videoItem;
//		
//	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (resultCode != RESULT_OK) return;
		
	    if (requestCode == ActivityCodeUtil.CAPTURE_VIDEO_ACTIVITY_CODE) {
	    	onVideoCaptureActivityResult(data);
	    }
	}
	

	protected int setupThemeColor() {
		// TODO Auto-generated method stub
		return R.color.white;
	}
	
	protected void onMenuItemSelected(MenuItem menu) {
		// TODO Auto-generated method stub
		switch (menu.getItemId()) {
		case android.R.id.home:
			finish();
			break;
		case R.id.camera:
			captureVideo();
			break;
		default:
			break;
		}
	}

	protected boolean onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		// TODO Auto-generated method stub
		inflater.inflate(R.menu.camera, menu);
		return true;
	}
	
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
    	onMenuItemSelected(item);

        return super.onOptionsItemSelected(item);
    }
	
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
       return onCreateOptionsMenu(menu, getMenuInflater());
    }

}
