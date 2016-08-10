package com.qpidnetwork.livechat;

import com.qpidnetwork.framework.util.StringUtil;
import com.qpidnetwork.request.OnLCGetVideoCallback;
import com.qpidnetwork.request.RequestJni;
import com.qpidnetwork.request.RequestJniLivechat;
import com.qpidnetwork.request.RequestJniLivechat.VideoToFlagType;
import com.qpidnetwork.tool.FileDownloader;
import com.qpidnetwork.tool.FileDownloader.FileDownloaderCallback;

/**
 * 视频下载器
 * @author Samson Fan
 */
public class LCVideoDownloader implements OnLCGetVideoCallback
										, FileDownloaderCallback
{
	public interface LCVideoDownloaderCallback {
		void onDownloadVideoFinish(LCVideoDownloader downloader, boolean success, String errno, String errmsg, LCMessageItem item);
	}
	
	private LCMessageItem mMsgItem = null;
	private LCVideoDownloaderCallback mListener = null;
	private long mRequestId = RequestJni.InvalidRequestId;
	private String mFilePath = "";
	private FileDownloader downloader = null;
	
	public LCVideoDownloader() {
		
	}
	
	/**
	 * 开始下载视频
	 * @param videoItem	视频item
	 * @return
	 */
	public boolean StartDownload(String userId, String sId
			, LCMessageItem msgItem, String filePath, LCVideoDownloaderCallback listener)
	{
		boolean result = false;
		
		if (!StringUtil.isEmpty(userId)
			&& !StringUtil.isEmpty(sId)
			&& null != msgItem
			&& null != msgItem.getVideoItem()
			&& null != msgItem.getVideoItem().videoItem
			&& !StringUtil.isEmpty(msgItem.getVideoItem().videoItem.videoId)
			&& !msgItem.getVideoItem().videoItem.IsDownloadingVideo()
			&& !StringUtil.isEmpty(filePath)
			&& null != listener)
		{
			LCVideoMsgItem videoMsgItem = msgItem.getVideoItem();
			LCVideoItem videoItem = msgItem.getVideoItem().videoItem;
			long requestId = RequestJniLivechat.GetVideo(
								msgItem.toId
								, videoItem.videoId
								, msgItem.inviteId
								, VideoToFlagType.Woman
								, videoMsgItem.sendId
								, sId
								, userId
								, this);
			
			result = (requestId != RequestJni.InvalidRequestId);
			if (result) {
				// 设置参数
				mRequestId = requestId;
				mMsgItem = msgItem;
				mFilePath = filePath;
				mListener = listener;
				
				// 添加下载状态
				mMsgItem.getVideoItem().videoItem.AddDownloadingVideo();
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
	public void OnLCGetVideo(long requestId, boolean isSuccess, String errno,
			String errmsg, String videoUrl) 
	{
		if (isSuccess) {
			// 获取url成功，立即下载
			downloader = new FileDownloader();
			downloader.StartDownload(videoUrl, mFilePath, this);
		}
		else {
			// 获取url不成功
			mMsgItem.getVideoItem().videoItem.RemoveDownloadingVideo();
			if (null != mListener) {
				mListener.onDownloadVideoFinish(this, isSuccess, errno, errmsg, mMsgItem);
			}
		}
	}

	@Override
	public void onSuccess(FileDownloader loader) 
	{
		boolean result = false;
		
		// 文件下载成功
		mMsgItem.getVideoItem().videoItem.RemoveDownloadingVideo();
		result = mMsgItem.getVideoItem().videoItem.SetVideoPath(mFilePath);
		if (null != mListener) {
			if (result) {
				mListener.onDownloadVideoFinish(this, true, "", "", mMsgItem);
			}
			else {
				mListener.onDownloadVideoFinish(this, false, "", "", mMsgItem);
			}
		}
	}

	@Override
	public void onFail(FileDownloader loader) 
	{
		// 文件下载失败
		mMsgItem.getVideoItem().videoItem.RemoveDownloadingVideo();
		if (null != mListener) {
			mListener.onDownloadVideoFinish(this, false, "", "", mMsgItem);
		}
	}

	@Override
	public void onUpdate(FileDownloader loader, int progress) 
	{
		
	}
}
