package com.qpidnetwork.livechat;

import android.content.Context;

import com.qpidnetwork.tool.FileDownloader;

/**
 * 高级表情下载器
 * @author Samson Fan
 */
public class LCEmotionDownloader implements FileDownloader.FileDownloaderCallback 
{
	public interface LCEmotionDownloaderCallback {
		void onSuccess(EmotionFileType fileType, LCEmotionItem item);
		void onFail(EmotionFileType fileType, LCEmotionItem item);
	}
	
	private FileDownloader mFileDownloader;
	private LCEmotionItem mEmotionItem;
	private EmotionFileType mFileType;
	private LCEmotionDownloaderCallback mCallback;
	private String mFilePath;
	private String mUrl;
	/**
	 * 高级表情待下载的文件类型 
	 */
	public enum EmotionFileType {
		funknow,
		fimage,
		fplaybigimage,
		fplaymidimage,
		fplaysmallimage,
//		f3gp
	}
	
	public LCEmotionDownloader(Context context) {
		mFileType = EmotionFileType.funknow;
		mFileDownloader = new FileDownloader(context);
		mEmotionItem = null;
		mCallback = null;
		mFilePath = "";
		mUrl = "";
	}
	
	/**
	 * 开始下载
	 * @param url		文件下载URL
	 * @param filePath	文件本地路径
	 * @param fileType	文件类型
	 * @param item		高级表情item
	 * @param callback	回调
	 */
	public boolean Start(String url, String filePath, EmotionFileType fileType, LCEmotionItem item, LCEmotionDownloaderCallback callback) {
		boolean result = false;
		if (!url.isEmpty() 
			&& !filePath.isEmpty()
			&& fileType != EmotionFileType.funknow
			&& item != null
			&& callback != null)
		{
			mEmotionItem = item;
			mCallback = callback;
			mFilePath = filePath;
			mUrl = url;
			mFileType = fileType;
			mFileDownloader.StartDownload(mUrl, mFilePath, this);
			
			result = true;
		}
		return result;
	}
	
	/**
	 * 停止下载
	 */
	public void Stop() {
		mFileDownloader.Stop();
	}

	// ------------- FileDownloader.FileDownloaderCallback ------------- 
	@Override
	public void onSuccess(FileDownloader loader) {
		if (mEmotionItem != null) {
//			if (mFileType == EmotionFileType.f3gp) {
//				mEmotionItem.f3gpPath = mFilePath;
//			}
//			else 
			if (mFileType == EmotionFileType.fimage) {
				mEmotionItem.imagePath = mFilePath;
			}
			else if (mFileType == EmotionFileType.fplaybigimage) {
				mEmotionItem.playBigPath = mFilePath;
			}
			else if (mFileType == EmotionFileType.fplaymidimage) {
//				mEmotionItem.playMidPath = mFilePath;
			}
			else if (mFileType == EmotionFileType.fplaysmallimage) {
//				mEmotionItem.playSmallPath = mFilePath;
			}
		}
		
		if (mCallback != null) {
			mCallback.onSuccess(mFileType, mEmotionItem);
		}
		
		mCallback = null;
		mFileDownloader = null;
		mEmotionItem = null;
	}
	
	@Override
	public void onFail(FileDownloader loader) {
		if (mCallback != null) {
			mCallback.onFail(mFileType, mEmotionItem);
		}
		
		mCallback = null;
		mFileDownloader = null;
		mEmotionItem = null;
	}
	
	@Override
	public void onUpdate(FileDownloader loader, int progress) {
		
	}
}
