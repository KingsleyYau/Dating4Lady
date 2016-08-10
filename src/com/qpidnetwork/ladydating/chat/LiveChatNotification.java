package com.qpidnetwork.ladydating.chat;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.text.format.Time;

import com.qpidnetwork.ladydating.home.HomeActivity;

public class LiveChatNotification {


	private static final int NOTIFICATION_BASE_ID = 20000;
	private static final int NOTIFICATION_MAX_COUNT = 10;
	private static int mCurNotificationId = NOTIFICATION_BASE_ID;

	private static LiveChatNotification gAdvertNotification;
	private Context mContext = null;
	private NotificationManager mNotification;
	
	public static LiveChatNotification newInstance(Context context) {
		if( gAdvertNotification == null ) {
			gAdvertNotification = new LiveChatNotification(context);
		} 
		return gAdvertNotification;
	}
	
	public static LiveChatNotification getInstance() {
		return gAdvertNotification;
	}
	
	public LiveChatNotification(Context context)  {
		mContext = context;
		mNotification = (NotificationManager)mContext.getSystemService(Context.NOTIFICATION_SERVICE);
		mCurNotificationId = NOTIFICATION_BASE_ID;
	}
	
	/**
	 * 通知栏显示通知
	 * @param icon
	 * @param tickerText
	 * @param isVibrate
	 * @param isSound
	 */
	public void ShowNotification(
			int icon, 
			String tickerText, 
    		boolean isSound 
    		) {
		
		// 去除旧的通知栏消息
		mCurNotificationId = NOTIFICATION_BASE_ID + ++mCurNotificationId % NOTIFICATION_MAX_COUNT;
		mNotification.cancel(mCurNotificationId);
		
    	NotificationCompat.Builder builder = new NotificationCompat.Builder(mContext);
    	
    	// 声音
    	if( isSound ) {
    		builder.setDefaults(Notification.DEFAULT_SOUND);
    	}
    	
        // 状态栏
    	builder.setSmallIcon(icon);
    	builder.setTicker(tickerText);
        builder.setContentTitle(tickerText);
    	
    	// 自定义通知栏
        
        Time time = new Time();
		time.setToNow();
        builder.setWhen(time.toMillis(false));
        
        // 点击事件
    	Intent intent = new Intent();
    	intent.setClass(mContext, HomeActivity.class);
    	intent.putExtra(HomeActivity.START_LIVECHAT_LIST, true);
    	intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
    	
    	PendingIntent.getActivity(mContext, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
    	
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(mContext);
        stackBuilder.addParentStack(HomeActivity.class);
        stackBuilder.addNextIntent(intent);
        PendingIntent resultPendingIntent =
                stackBuilder.getPendingIntent(
                    0,
                    PendingIntent.FLAG_UPDATE_CURRENT
                );
        builder.setContentIntent(resultPendingIntent);
		
    	mNotification.notify(mCurNotificationId, builder.build());
    }
	
	public void Cancel() {
		try{
			mNotification.cancelAll();
		}catch(Exception e){
			
		}
	}

}
