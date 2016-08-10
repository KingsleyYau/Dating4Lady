package com.qpidnetwork.ladydating.authorization;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.text.format.Time;

import com.qpidnetwork.ladydating.R;
import com.qpidnetwork.ladydating.home.HomeActivity;

public class KickOffNotification {



	private static int mCurNotificationId = 30000;
	
	private static KickOffNotification gKickoffNotification;
	private Context mContext = null;
	private NotificationManager mNotification;
	
	public static KickOffNotification newInstance(Context context) {
		if( gKickoffNotification == null ) {
			gKickoffNotification = new KickOffNotification(context);
		} 
		return gKickoffNotification;
	}
	
	public static KickOffNotification getInstance() {
		return gKickoffNotification;
	}
	
	public KickOffNotification(Context context)  {
		mContext = context;
		mNotification = (NotificationManager)mContext.getSystemService(Context.NOTIFICATION_SERVICE);
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
		mNotification.cancel(mCurNotificationId);
		
		// 创建新的通知
    	NotificationCompat.Builder builder = new NotificationCompat.Builder(mContext);
    	
    	// 声音
    	if( isSound ) {
    		builder.setDefaults(Notification.DEFAULT_SOUND);
    	}
    	
        // 状态栏
    	builder.setSmallIcon(icon);
    	builder.setTicker(tickerText);
        
        
        Time time = new Time();
		time.setToNow();
        builder.setWhen(time.toMillis(false));
        
        builder.setContentTitle(mContext.getString(R.string.app_name));
        builder.setContentText(tickerText);
        
        // 点击事件
    	Intent intent = new Intent();
    	intent.setClass(mContext, HomeActivity.class);
    	intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP  | Intent.FLAG_ACTIVITY_NEW_TASK);
    	
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
