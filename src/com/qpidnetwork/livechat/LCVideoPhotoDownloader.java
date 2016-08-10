package com.qpidnetwork.livechat;

import com.qpidnetwork.framework.util.StringUtil;
import com.qpidnetwork.request.OnLCGetVideoPhotoCallback;
import com.qpidnetwork.request.RequestJni;
import com.qpidnetwork.request.RequestJniLivechat;
import com.qpidnetwork.request.RequestJniLivechat.VideoPhotoType;

/**
 * 视频图片下载器
 * @author Samson Fan
 */
public class LCVideoPhotoDownloader implements OnLCGetVideoPhotoCallback
{
	public interface LCVideoPhotoDownloaderCallback {
		void onDownloadVideoPhotoFinish(LCVideoPhotoDownloader downloader, boolean success, String errno, String errmsg, VideoPhotoType photoType, LCVideoItem item);
	}
	
	private VideoPhotoType mPhotoType = VideoPhotoType.Default;
	private LCVideoItem mVideoItem = null;
	private LCVideoPhotoDownloaderCallback mListener = null;
	private long mRequestId = RequestJni.InvalidRequestId;
	private String mFilePath = "";
	
	public LCVideoPhotoDownloader() {
		
	}
	
	/**
	 * 开始下载
	 * @param videoItem	视频item
	 * @param callback	回调
	 */
	public boolean StartDownload(String userId, String sId
			, VideoPhotoType photoType, LCVideoItem videoItem
			, String filePath, LCVideoPhotoDownloaderCallback listener) 
	{
		boolean result = false;
		
		if (!StringUtil.isEmpty(userId)
			&& !StringUtil.isEmpty(sId)
			&& null != videoItem
			&& !StringUtil.isEmpty(videoItem.videoId)
			&& !videoItem.IsDownloadingPhoto(photoType)
			&& !StringUtil.isEmpty(filePath)
			&& null != listener)
		{
			long requestId = RequestJniLivechat.GetVideoPhoto(userId, videoItem.videoId, photoType, sId, userId, filePath, this);
			
			result = (requestId != RequestJni.InvalidRequestId);
			if (result) {
				// 设置参数
				mRequestId = requestId;
				mVideoItem = videoItem;
				mPhotoType = photoType;
				mFilePath = filePath;
				mListener = listener;
				
				// 添加下载状态
				mVideoItem.AddDownloadingPhoto(photoType);
			}
		}
		
		return result;
	}
	
	/**
	 * 停止下载
	 */
	public void Stop() {
		if (mRequestId != RequestJni.InvalidRequestId) {
			RequestJni.StopRequest(mRequestId);
		}
	}

	// ------------- Callback ------------- 
	@Override
	public void OnLCGetVideoPhoto(long requestId, boolean isSuccess,
			String errno, String errmsg, String videoId, String filePath) 
	{
		boolean success = false;
		
//		Log.d("LiveChatManager", "LCVideoPhotoDownloader::OnLCGetVideoPhoto() " +
//				"success:%b, errno:%s, errmsg:%s, videoId:%s, filePath:%s"
//				, isSuccess, errno, errmsg, videoId, filePath);
		
		if (isSuccess) {
			success = mVideoItem.SetVideoPhotoPath(mFilePath, mPhotoType);
		}
		mVideoItem.RemoveDownloadingPhoto(mPhotoType);
			
		if (null != mListener) {
			mListener.onDownloadVideoPhotoFinish(this, success, errno, errmsg, mPhotoType, mVideoItem);
		}
	}
}
