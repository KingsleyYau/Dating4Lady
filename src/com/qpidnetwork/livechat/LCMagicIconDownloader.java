package com.qpidnetwork.livechat;

import android.content.Context;

import com.qpidnetwork.tool.FileDownloader;

public class LCMagicIconDownloader implements FileDownloader.FileDownloaderCallback{
	
	private FileDownloader mFileDownloader;
	private LCMagicIconItem mMagicIconItem;
	private MagicIconDownloadType mDownloadType;
	private LCMagicIconDownloaderCallback mCallback;
	
	public LCMagicIconDownloader(Context context){
		mFileDownloader = new FileDownloader(context);
		mMagicIconItem = null;
		mCallback = null;
		mDownloadType = MagicIconDownloadType.DEFAULT;
	} 
	
	/**
	 * 开始下载
	 * @param url		文件下载URL
	 * @param filePath	文件本地路径
	 * @param fileType	文件类型
	 * @param item		高级表情item
	 * @param callback	回调
	 */
	public boolean Start(String url, String filePath, MagicIconDownloadType downloadType, LCMagicIconItem item, LCMagicIconDownloaderCallback callback) {
		boolean result = false;
		if (!url.isEmpty() 
			&& !filePath.isEmpty()
			&& downloadType != MagicIconDownloadType.DEFAULT
			&& item != null
			&& callback != null)
		{
			mMagicIconItem = item;
			mCallback = callback;
			mDownloadType = downloadType;
			mFileDownloader.StartDownload(url, filePath, this);
			result = true;
		}
		return result;
	}
	
	/**
	 * 停止下载
	 */
	public void Stop() {
		if(mFileDownloader != null){
			mFileDownloader.Stop();
		}
	}

	@Override
	public void onSuccess(FileDownloader loader) {
		// TODO Auto-generated method stub
		if (mCallback != null) {
			mCallback.onSuccess(mDownloadType, mMagicIconItem);
		}
		
		mCallback = null;
		mFileDownloader = null;
		mMagicIconItem = null;
	}

	@Override
	public void onFail(FileDownloader loader) {
		if (mCallback != null) {
			mCallback.onFail(mDownloadType, mMagicIconItem);
		}
		
		mCallback = null;
		mFileDownloader = null;
		mMagicIconItem = null;		
	}

	@Override
	public void onUpdate(FileDownloader loader, int progress) {
		// TODO Auto-generated method stub
		
	}
	
	public enum MagicIconDownloadType{
		DEFAULT,
		THUMB,
		SOURCE
	}
	
	public interface LCMagicIconDownloaderCallback {
		void onSuccess(MagicIconDownloadType downloadType, LCMagicIconItem item);
		void onFail(MagicIconDownloadType downloadType, LCMagicIconItem item);
	}
}
