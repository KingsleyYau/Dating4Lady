package com.qpidnetwork.ladydating.album;

import java.util.HashMap;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.text.TextUtils;
import android.util.Log;

import com.qpidnetwork.ladydating.R;
import com.qpidnetwork.ladydating.album.VideoUploadTask.OnUploadVideoResultCallback;

public class VideoUploadManager implements OnUploadVideoResultCallback{
	
private static VideoUploadManager mVideoUploadManager;
	
	private Context mContext;
	private AtomicInteger mTaskIdCreator;
	
	private HashMap<Integer, VideoUploadTask> mTaskMap;//taskId与VideoUploadTask映射关系
	private HashMap<Integer, VideoUploadTask> mFailedTaskMap;//上传失败task Map
	private AlbumVideoUploadNotificationManager mNotificationManager;
	
	private BroadcastReceiver mBroadcastReceiver;
	
	private boolean isTimerStarted = false;
	private Handler mHandler = new Handler();
	
	public static VideoUploadManager getInstance(){
		return mVideoUploadManager;
	}
	
	public static void newInstance(Context context){
		if(mVideoUploadManager == null){
			mVideoUploadManager = new VideoUploadManager(context);
		}
	}
	
	private VideoUploadManager(Context context){
		this.mContext = context;
		mTaskIdCreator = new AtomicInteger(0);
		mTaskMap = new HashMap<Integer, VideoUploadTask>();
		mFailedTaskMap = new HashMap<Integer, VideoUploadTask>();
		mNotificationManager = new AlbumVideoUploadNotificationManager(context);
		initNotificationReceiver();
	}
	
	private void initNotificationReceiver(){
		mBroadcastReceiver = new BroadcastReceiver() {
			@Override
			public void onReceive(Context context, Intent intent) {
				// TODO Auto-generated method stub
				String action = intent.getAction();
				int taskId = intent.getExtras().getInt(AlbumVideoUploadNotificationManager.EXTRAS_TASK_ID);
				switch (action) {
				case AlbumVideoUploadNotificationManager.ACTION_SUCCESSOK_UPLOAD_VIDEO:{
					mNotificationManager.cancelNotification(taskId);
				}break;
				case AlbumVideoUploadNotificationManager.ACTION_RETRY_UPLOAD_VIDEO:{
					if(mFailedTaskMap.containsKey(Integer.valueOf(taskId))){
						VideoUploadTask task = mFailedTaskMap.remove(Integer.valueOf(taskId));
						if(task != null){
							if(!mTaskMap.containsKey(Integer.valueOf(taskId))){
								mTaskMap.put(Integer.valueOf(taskId), task);
							}
							task.retry();
						}
					}else{
						//异常取消系统通知栏
						if(mTaskMap.containsKey(Integer.valueOf(taskId))){
							mTaskMap.remove(Integer.valueOf(taskId));
						}
						mNotificationManager.cancelNotification(taskId);
					}
				}break;
				case AlbumVideoUploadNotificationManager.ACTION_CANCEL_UPLOAD_VIDEO:{
					VideoUploadTask task = null;
					if(mTaskMap.containsKey(Integer.valueOf(taskId))){
						task = mTaskMap.remove(Integer.valueOf(taskId));
					}
					
					if(mFailedTaskMap.containsKey(Integer.valueOf(taskId))){
						task = mFailedTaskMap.remove(Integer.valueOf(taskId));
					}
					if(task != null){
						task.cancelUpload();
					}
					mNotificationManager.cancelNotification(taskId);
				}break;
				default:
					break;
				}
				
			}
		};

		IntentFilter filter = new IntentFilter();
		filter.addAction(AlbumVideoUploadNotificationManager.ACTION_SUCCESSOK_UPLOAD_VIDEO);
		filter.addAction(AlbumVideoUploadNotificationManager.ACTION_RETRY_UPLOAD_VIDEO);
		filter.addAction(AlbumVideoUploadNotificationManager.ACTION_CANCEL_UPLOAD_VIDEO);
		mContext.registerReceiver(mBroadcastReceiver, filter);
	
	}
	
	/**
	 * 上传视频文件入口 
	 * @param albumId
	 * @param videoTitle
	 * @param videoPath
	 * @param thumbPath
	 */
	public void UploadVideo(String albumId, String videoTitle, String videoPath, String thumbPath){
		int taskId = mTaskIdCreator.getAndIncrement();
		VideoUploadTask task = new VideoUploadTask();
		mTaskMap.put(taskId, task);
		if(!isTimerStarted){
			isTimerStarted = true;
			mHandler.postDelayed(timer, 2000);
		}
		task.setUploadVideoResultCallback(this);
		task.VideoUpload(taskId, albumId, videoTitle, videoPath, thumbPath);
	}

	@Override
	public void uploadVideoSuccess(int taskId) {
		VideoUploadTask task = null;
		if(mTaskMap.containsKey(Integer.valueOf(taskId))){
			task = mTaskMap.remove(Integer.valueOf(taskId));
		}
		if(mFailedTaskMap.containsKey(Integer.valueOf(taskId))){
			mFailedTaskMap.remove(Integer.valueOf(taskId));
		}
		if(task != null){
			String tickerText = String.format(mContext.getResources().getString(R.string.album_video_upload_success_notify), task.getVideoTitle());
			mNotificationManager.showVideoUploadSuccessNotification(R.drawable.ic_launcher, tickerText, taskId);
		}
	}

	@Override
	public void uploadVideoFailed(int taskId,  String errNo) {
		VideoUploadTask task = null;
		if(mTaskMap.containsKey(Integer.valueOf(taskId))){
			task = mTaskMap.remove(Integer.valueOf(taskId));
		}
		if(task != null){
			if(!mFailedTaskMap.containsKey(Integer.valueOf(taskId))){
				mFailedTaskMap.put(Integer.valueOf(taskId), task);
			}
			String tickerText = "";
			if(!TextUtils.isEmpty(errNo)
					&& errNo.equals("4026")){
				//4026  相册超过30张照片
				tickerText = String.format(mContext.getResources().getString(R.string.album_video_upload_overflow_failed), task.getVideoTitle());
				mNotificationManager.showVideoUploadFailConfirmNotification(R.drawable.ic_launcher, tickerText, taskId);
			}else if(!TextUtils.isEmpty(errNo)
					&& errNo.equals("4027")){
				//4027  视频封面MD5名称已存在
				tickerText = String.format(mContext.getResources().getString(R.string.album_video_upload_thumb_exist_failed), task.getVideoTitle());
				mNotificationManager.showVideoUploadFailConfirmNotification(R.drawable.ic_launcher, tickerText, taskId);
			}else if(!TextUtils.isEmpty(errNo)
					&& errNo.equals("4028")){
				//4028  视频MD5名称已存在
				tickerText = String.format(mContext.getResources().getString(R.string.album_video_upload_video_exist_failed), task.getVideoTitle());
				mNotificationManager.showVideoUploadFailConfirmNotification(R.drawable.ic_launcher, tickerText, taskId);
			}else if(!TextUtils.isEmpty(errNo)
					&& errNo.equals("4033")){
				//4033 视频封面格式不正确(大小、类型)
				tickerText = String.format(mContext.getResources().getString(R.string.album_video_upload_thumb_format_failed), task.getVideoTitle());
				mNotificationManager.showVideoUploadFailConfirmNotification(R.drawable.ic_launcher, tickerText, taskId);
			}else{
				//4032  视频封面上传失败
				//4035  添加视频失败
				//网络及其他原因导致失败，可重试
				tickerText = String.format(mContext.getResources().getString(R.string.album_video_upload_normal_failed), task.getVideoTitle());
				mNotificationManager.showVideoUploadFailNotification(R.drawable.ic_launcher, tickerText, taskId);
			}
		}
	}
	
	/**
	 * 定时更新进度
	 */
	private Runnable timer = new Runnable() {
		
		@Override
		public void run() {
			Set<Integer> taskIdSet = mTaskMap.keySet();
			for(Integer taskId : taskIdSet ){
				VideoUploadTask task = mTaskMap.get(taskId);
				String tickerText = String.format(mContext.getResources().getString(R.string.album_video_uploading_notify), task.getVideoTitle());
				mNotificationManager.showVideoUploadProgressNotification(R.drawable.ic_launcher, tickerText, taskId, task.getUploadProgress());
			}
			if(mTaskMap.size()>0){
				mHandler.postDelayed(timer, 2000);
			}else{
				isTimerStarted = false;
			}
		}
	};
	
	/**
	 * 销毁
	 */
	public void onDestroy(){
		mHandler.removeCallbacks(timer);
		mContext.unregisterReceiver(mBroadcastReceiver);
		mTaskMap.clear();
		mNotificationManager.onDestroy();
	}
	
	/**
	 * 重新登陆重新绑定广播接收器
	 */
	public void reInit(){
		initNotificationReceiver();
	}

}
