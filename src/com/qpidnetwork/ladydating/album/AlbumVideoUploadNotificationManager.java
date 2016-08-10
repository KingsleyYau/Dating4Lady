package com.qpidnetwork.ladydating.album;

import java.util.HashMap;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.widget.RemoteViews;

import com.qpidnetwork.ladydating.R;

public class AlbumVideoUploadNotificationManager {
	
	public static final String ACTION_CANCEL_UPLOAD_VIDEO = "action.cancel.uploadvideo";
	public static final String ACTION_RETRY_UPLOAD_VIDEO = "action.retry.uploadvideo";
	public static final String ACTION_SUCCESSOK_UPLOAD_VIDEO = "action.success.uploadvideo";
	public static final String EXTRAS_TASK_ID = "taskId";
	
	private static int  APP_VIDEO_UPLOAD_NOTIFICATION_BASEID = 10000;
	private static final int NOTIFICATION_MAX_COUNT = 200;
	private int mCurrentMaxNotificationId = APP_VIDEO_UPLOAD_NOTIFICATION_BASEID;
	
	private HashMap<Integer, Integer> mTaskNotificationMap;//存放制定VideoUploadTask taskID 对应的NotificationId
	private HashMap<Integer, Boolean> mUsingNotificationMap;//存放当前正在使用的通知Id列表，方便新通知ID获取查询
	
	private Context mContext;
	private NotificationManager mNotificationManager;
	
	public AlbumVideoUploadNotificationManager(Context context){
		mContext = context;
		mTaskNotificationMap = new HashMap<Integer, Integer>();
		mUsingNotificationMap = new HashMap<Integer, Boolean>();
		mNotificationManager = (NotificationManager)mContext.getSystemService(Context.NOTIFICATION_SERVICE);
	}
	
	/**
	 * 自定义上传进度通知栏
	 */
	public void showVideoUploadProgressNotification(int icon, String tickerText, int taskId, int progress){
		// 创建新的通知
    	NotificationCompat.Builder builder = new NotificationCompat.Builder(mContext);
        // 状态栏
    	builder.setSmallIcon(icon);
    	builder.setTicker(tickerText);
    	
    	// 自定义通知栏
    	RemoteViews contentView = new RemoteViews(mContext.getPackageName(), R.layout.notification_view_videoupload);
    	contentView.setImageViewResource(R.id.notificationImage, icon);  
    	
    	if(progress <= 100){
	        contentView.setTextViewText(R.id.notificationTitle, tickerText);
	        contentView.setProgressBar(R.id.notificationProgress, 100, progress, false);
    	}else {
			contentView.setTextViewText(R.id.notificationTitle, tickerText);
			contentView.setProgressBar(R.id.notificationProgress, 100, 100, false);
		}
    	
    	//cancel broadcast（取消上传）
    	Intent cancelIntent = new Intent(ACTION_CANCEL_UPLOAD_VIDEO);
    	cancelIntent.putExtra(EXTRAS_TASK_ID, taskId);
        PendingIntent cancelPendingIntent = PendingIntent.getBroadcast(mContext, 0, cancelIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        contentView.setOnClickPendingIntent(R.id.notificationCancel, cancelPendingIntent);
    	builder.setContent(contentView);
    	
        mNotificationManager.notify(getNotificationId(taskId), builder.build());
	}
	
	/**
	 * 自定义上传视频失败通知栏
	 */
	public void showVideoUploadFailNotification(int icon, String tickerText, int taskId){
		// 创建新的通知
    	NotificationCompat.Builder builder = new NotificationCompat.Builder(mContext);
        // 状态栏
    	builder.setSmallIcon(icon);
    	builder.setTicker(tickerText);
    	
    	// 自定义通知栏
    	RemoteViews contentView = new RemoteViews(mContext.getPackageName(), R.layout.notification_view_videoupload_failed);
    	contentView.setImageViewResource(R.id.notificationImage, icon);  
    	
	    contentView.setTextViewText(R.id.notificationTitle, tickerText);
	    
	    //Retry
    	Intent retryIntent = new Intent(ACTION_RETRY_UPLOAD_VIDEO);
    	retryIntent.putExtra(EXTRAS_TASK_ID, taskId);
        PendingIntent retryPendingIntent = PendingIntent.getBroadcast(mContext, 0, retryIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        contentView.setOnClickPendingIntent(R.id.btnRetry, retryPendingIntent);
    	
    	//cancel broadcast（取消上传）
    	Intent cancelIntent = new Intent(ACTION_CANCEL_UPLOAD_VIDEO);
    	cancelIntent.putExtra(EXTRAS_TASK_ID, taskId);
        PendingIntent cancelPendingIntent = PendingIntent.getBroadcast(mContext, 0, cancelIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        contentView.setOnClickPendingIntent(R.id.btnCancel, cancelPendingIntent);
        
    	builder.setContent(contentView);
    	
        mNotificationManager.notify(getNotificationId(taskId), builder.build());
	}
	
	/**
	 * 处理无法Retry 只能通知失败提示（处理方式和成功一致）
	 * @param icon
	 * @param tickerText
	 * @param taskId
	 */
	public void showVideoUploadFailConfirmNotification(int icon, String tickerText, int taskId){
		// 创建新的通知
    	NotificationCompat.Builder builder = new NotificationCompat.Builder(mContext);
        // 状态栏
    	builder.setSmallIcon(icon);
    	builder.setTicker(tickerText);
    	
    	// 自定义通知栏
    	RemoteViews contentView = new RemoteViews(mContext.getPackageName(), R.layout.notification_view_videoupload_success);
    	contentView.setImageViewResource(R.id.notificationImage, icon);  
    	
	    contentView.setTextViewText(R.id.notificationTitle, tickerText);
    	
    	//success
    	Intent successIntent = new Intent(ACTION_SUCCESSOK_UPLOAD_VIDEO);
    	successIntent.putExtra(EXTRAS_TASK_ID, taskId);
        PendingIntent successPendingIntent = PendingIntent.getBroadcast(mContext, 0, successIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        contentView.setOnClickPendingIntent(R.id.btnOk, successPendingIntent);
        
    	builder.setContent(contentView);
    	
        mNotificationManager.notify(getNotificationId(taskId), builder.build());
	}
	
	/**
	 * 自定义上传视频成功通知栏
	 */
	public void showVideoUploadSuccessNotification(int icon, String tickerText, int taskId){
		// 创建新的通知
    	NotificationCompat.Builder builder = new NotificationCompat.Builder(mContext);
        // 状态栏
    	builder.setSmallIcon(icon);
    	builder.setTicker(tickerText);
    	
    	// 自定义通知栏
    	RemoteViews contentView = new RemoteViews(mContext.getPackageName(), R.layout.notification_view_videoupload_success);
    	contentView.setImageViewResource(R.id.notificationImage, icon);  
    	
	    contentView.setTextViewText(R.id.notificationTitle, tickerText);
    	
    	//success
    	Intent successIntent = new Intent(ACTION_SUCCESSOK_UPLOAD_VIDEO);
    	successIntent.putExtra(EXTRAS_TASK_ID, taskId);
        PendingIntent successPendingIntent = PendingIntent.getBroadcast(mContext, 0, successIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        contentView.setOnClickPendingIntent(R.id.btnOk, successPendingIntent);
        
    	builder.setContent(contentView);
    	
        mNotificationManager.notify(getNotificationId(taskId), builder.build());
	}
	
	/**
	 * 根据TaskId获取NotificationId（规则：如果mTaskNotificationMap中有，直接取出，否则生成新的NotificationID）
	 * @param taskId
	 * @return
	 */
	private int getNotificationId(int taskId){
		int notificationId = 0;
		if(mTaskNotificationMap.containsKey(Integer.valueOf(taskId))){
			notificationId = mTaskNotificationMap.get(Integer.valueOf(taskId));
		}else{
			notificationId = getNewNotificationId();
			mTaskNotificationMap.put(Integer.valueOf(taskId), Integer.valueOf(notificationId));
		}
		return notificationId;
	}
	
	/**
	 * 生成新的NotificationId
	 */
	private int getNewNotificationId(){
		int notificationId = APP_VIDEO_UPLOAD_NOTIFICATION_BASEID;
		if(mCurrentMaxNotificationId < APP_VIDEO_UPLOAD_NOTIFICATION_BASEID + NOTIFICATION_MAX_COUNT){
			notificationId = ++mCurrentMaxNotificationId;
		}else{
			for(int i= APP_VIDEO_UPLOAD_NOTIFICATION_BASEID; i<mCurrentMaxNotificationId; i++){
				if(!mUsingNotificationMap.containsKey(Integer.valueOf(i))){
					notificationId = i;
				}
			}
		}
		mUsingNotificationMap.put(Integer.valueOf(notificationId), true);
		return notificationId;
	}
	
	/**
	 * 取消系统通知
	 * @param taskId
	 */
	public void cancelNotification(int taskId){
		if(mTaskNotificationMap.containsKey(Integer.valueOf(taskId))){
			int notificationId = mTaskNotificationMap.get(Integer.valueOf(taskId));
			mTaskNotificationMap.remove(Integer.valueOf(taskId));
			mUsingNotificationMap.remove(Integer.valueOf(notificationId));
			try {
				mNotificationManager.cancel(notificationId);
			} catch (Exception e) {
				// TODO: handle exception
			}
		}
	}
	
	/**
	 * 重置
	 */
	public void onDestroy(){
		mCurrentMaxNotificationId = APP_VIDEO_UPLOAD_NOTIFICATION_BASEID;
		mTaskNotificationMap.clear();
		mUsingNotificationMap.clear();
		try {
			mNotificationManager.cancelAll();
		} catch (Exception e) {
			// TODO: handle exception
		}
	}
}
