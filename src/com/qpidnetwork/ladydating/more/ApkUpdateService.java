package com.qpidnetwork.ladydating.more;

import java.io.File;

import com.qpidnetwork.framework.util.Log;
import com.qpidnetwork.framework.util.StringUtil;
import com.qpidnetwork.ladydating.R;
import com.qpidnetwork.manager.CustomNotificationManager;
import com.qpidnetwork.manager.FileCacheManager;
import com.qpidnetwork.request.item.VersionCheckItem;
import com.qpidnetwork.tool.FileDownloader;
import com.qpidnetwork.tool.FileDownloader.FileDownloaderCallback;

import android.app.Service;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.widget.Toast;

public class ApkUpdateService extends Service implements FileDownloaderCallback{
	
	public static final String APk_UPDATE_VERSION_ITEM = "versionItem";
	private static final int APK_UPDATE_PROGRESSING = 0;
	private static final int APK_UPDATE_DOWNLOAD_SUCCESS = 1;
	private static final int APK_UPDATE_DOWNLOAD_FAIL = 2;
	
	private FileDownloader mFileDownloader;
	private VersionCheckItem mVersionItem;
	private Handler mHandler;
	private CustomNotificationManager mNotificationManager;

	@Override
	public IBinder onBind(Intent intent) {
		// TODO Auto-generated method stub
		return null;
	}
	
	@Override
	public void onStart(Intent intent, int startId) {
		Bundle bundle = intent.getExtras();
		if(bundle != null && bundle.containsKey(APk_UPDATE_VERSION_ITEM)){
			mVersionItem = (VersionCheckItem)bundle.getSerializable(APk_UPDATE_VERSION_ITEM);
		}
		mNotificationManager = new CustomNotificationManager(this);
		mHandler = new Handler(){
			@Override
			public void handleMessage(Message msg) {
				// TODO Auto-generated method stub
				super.handleMessage(msg);
				int progress = 0;
				switch (msg.what) {
				case APK_UPDATE_PROGRESSING:{
					progress = mFileDownloader.getCurrentProgress();
					Log.i("hunter", "Current progress is : " + progress);
					if(mFileDownloader.IsDownloading()){
						mHandler.sendEmptyMessageDelayed(APK_UPDATE_PROGRESSING, 100);
					}
					mNotificationManager.showAppUpgradeNotification(R.drawable.ic_launcher, "qpidnetwork_lady", progress);
				}break;
				case APK_UPDATE_DOWNLOAD_SUCCESS:{
					mHandler.removeMessages(APK_UPDATE_PROGRESSING);
					progress = msg.arg1;
					Log.i("hunter", "Current progress is : " + progress);
					mNotificationManager.cancelUpgradeNotification();
					reSetupApk();
				}break;
				case APK_UPDATE_DOWNLOAD_FAIL:{
					mHandler.removeMessages(APK_UPDATE_PROGRESSING);
					Toast.makeText(ApkUpdateService.this, getString(R.string.apk_download_fail), Toast.LENGTH_LONG).show();
					stopSelf();//下载失败，停止自己
				}break;
				default:
					break;
				}
				
			}
		};
		if(mVersionItem != null){
			if(mFileDownloader == null){
				mFileDownloader = new FileDownloader();
			}
			if(!mFileDownloader.IsDownloading()){
				String localPath = FileCacheManager.getInstance().CacheUpgradeApkPath(mVersionItem.verCode);
				File file = new File(localPath);
				if(file!=null && file.exists()){
					//本地已存在，先删除
					file.delete();
				}
				
				mFileDownloader.SetBigFile(true);
				mFileDownloader.SetUseCache(false);
				mFileDownloader.StartDownload(mVersionItem.apkUrl, localPath, this);
				mHandler.sendEmptyMessageDelayed(APK_UPDATE_PROGRESSING, 100);
			}
		}
	}

	@Override
	public void onSuccess(FileDownloader loader) {
		Message msg = Message.obtain();
		msg.what = APK_UPDATE_DOWNLOAD_SUCCESS;
		msg.arg1 = 101;
		mHandler.sendMessage(msg);
	}

	@Override
	public void onFail(FileDownloader loader) {
		Message msg = Message.obtain();
		msg.what = APK_UPDATE_DOWNLOAD_FAIL;
		mHandler.sendMessage(msg);		
	}

	@Override
	public void onUpdate(FileDownloader loader, int progress) {
		// TODO Auto-generated method stub
		
	}
	
	
	/* 在手机上打开文件的method */
	private void reSetupApk() {
		Intent intent = new Intent();
		intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		intent.setAction(android.content.Intent.ACTION_VIEW);

		String localPath = FileCacheManager.getInstance().CacheUpgradeApkPath(mVersionItem.verCode);
		File file = new File(StringUtil.mergeMultiString(localPath));

		/* 调用getMIMEType()来取得MimeType */
		String type = getMIMEType();
		/* 设置intent的file与MimeType */
		intent.setDataAndType(Uri.fromFile(file), type);
		startActivity(intent);
		stopSelf(); // 停止服务
	}
	
	/* 判断文件MimeType的method */
	private String getMIMEType() {
		String mimeType = "application/vnd.android.package-archive";

		return mimeType;
	}

}
