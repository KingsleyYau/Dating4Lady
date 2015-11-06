package com.qpidnetwork.manager;

import android.app.Notification;
import android.app.NotificationManager;
import android.content.Context;
import android.support.v4.app.NotificationCompat;
import android.widget.RemoteViews;

import com.qpidnetwork.ladydating.R;

public class CustomNotificationManager {
	
	private static int  APP_UPGRADE_NOTIFICATION_ID = 10000;
	
	private Context mContext;
	private NotificationManager mNotificationManager;
	
	public CustomNotificationManager(Context context){
		mContext = context;
		mNotificationManager = (NotificationManager)mContext.getSystemService(Context.NOTIFICATION_SERVICE);
	}
	
	/**
	 * App 升级通知栏
	 * @param icon
	 * @param tickerText
	 */
	public void showAppUpgradeNotification(int icon, String tickerText, int progress){
		
		// 创建新的通知
    	NotificationCompat.Builder builder = new NotificationCompat.Builder(mContext);
    	
    	// 声音
    	builder.setDefaults(Notification.DEFAULT_SOUND);
    	
        // 状态栏
    	builder.setSmallIcon(icon);
    	builder.setTicker(tickerText);
    	
    	// 自定义通知栏
    	RemoteViews contentView = new RemoteViews(mContext.getPackageName(), R.layout.notification_view_apkdownload);
    	contentView.setImageViewResource(R.id.notificationImage, icon);  
    	if(progress <= 100){
	        contentView.setTextViewText(R.id.notificationTitle, mContext.getResources().getString(R.string.apk_update_downloading, mContext.getResources().getString(R.string.app_name)));
	        contentView.setProgressBar(R.id.notificationProgress, 100, progress, false);
    	}else {
			contentView.setTextViewText(R.id.notificationTitle,
					mContext.getResources().getString(R.string.apk_update_downloaded, mContext.getResources().getString(R.string.app_name)));
			contentView.setProgressBar(R.id.notificationProgress, 100, 100, false);
		}
    	
    	builder.setContent(contentView);
    	
        mNotificationManager.notify(APP_UPGRADE_NOTIFICATION_ID, builder.build());
	}
	
	/**
	 * 取消apk安装进度提示
	 */
	public void cancelUpgradeNotification(){
		mNotificationManager.cancel(APP_UPGRADE_NOTIFICATION_ID);
	}
}
