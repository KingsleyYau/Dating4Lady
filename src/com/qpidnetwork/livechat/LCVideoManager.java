package com.qpidnetwork.livechat;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import android.annotation.SuppressLint;

import com.qpidnetwork.framework.util.Log;
import com.qpidnetwork.framework.util.StringUtil;
import com.qpidnetwork.livechat.LCMessageItem.MessageType;
import com.qpidnetwork.livechat.LCMessageItem.SendType;
import com.qpidnetwork.request.OnLCGetVideoListCallback;
import com.qpidnetwork.request.RequestJni;
import com.qpidnetwork.request.RequestJniLivechat;
import com.qpidnetwork.request.RequestJniLivechat.VideoPhotoType;
import com.qpidnetwork.request.item.LCVideoListGroupItem;
import com.qpidnetwork.request.item.LCVideoListVideoItem;

/**
 * 视频管理类
 * @author Samson Fan
 *
 */
@SuppressLint("UseSparseArrays")
public class LCVideoManager implements OnLCGetVideoListCallback
									, LCVideoPhotoDownloader.LCVideoPhotoDownloaderCallback
									, LCVideoDownloader.LCVideoDownloaderCallback
{
	/**
	 * Listener定义 
	 */
	public interface LCVideoManagerListener {
		public void OnGetVideoList(boolean isSuccess, String errno, String errmsg, LCVideoListGroupItem[] groups, LCVideoListVideoItem[] videos);
		public void OnDownloadVideoFinish(boolean isSuccess, String errno, String errmsg, LCMessageItem item);
		public void OnDownloadVideoPhotoFinish(boolean isSuccess, String errno, String errmsg, VideoPhotoType photoType, LCVideoItem videoItem);
	}
	
	private LCVideoManagerListener mCallback = null;
	/**
	 * msgId与item的待发送map表(msgId, MessageItem)（记录未发送成功的item，发送成功则移除）
	 */
	private HashMap<Integer, LCMessageItem> mMsgIdMap = new HashMap<Integer, LCMessageItem>();
	/**
	 * RequestId与item的待发送map表(RequestId, MessageItem)（记录上传未成功的item，上传成功则移除）
	 */
	private HashMap<Long, LCMessageItem> mVideoSendingRequestMap = new HashMap<Long, LCMessageItem>();
	/**
	 * itme与RequestId的待发送map表(MessageItem, RequestId)（记录未发送成功的item，发送成功则移除）
	 */
	private HashMap<LCMessageItem, Long> mMsgSendingRequestMap = new HashMap<LCMessageItem, Long>();
	/**
	 * 视频图片下载列表
	 */
	private ArrayList<LCVideoPhotoDownloader> mVideoPhotoDownloadList = new ArrayList<LCVideoPhotoDownloader>();
	/**
	 * 视频下载map表(videoId, 视频下载器)
	 */
	private ArrayList<LCVideoDownloader> mVideoDownloadList = new ArrayList<LCVideoDownloader>();
	/**
	 * 视频图片map表(videoId, LCVideoItem)
	 */
	private HashMap<String, LCVideoItem> mVideoMap = new HashMap<String, LCVideoItem>();
	/**
	 * 检测请求map表(RequestId, LCVideoCheckItem)
	 */
	private HashMap<Long, LCVideoCheckItem> mCheckVideoMap = new HashMap<Long, LCVideoCheckItem>();
	/**
	 * 本地缓存文件目录
	 */
	private String mDirPath = "";
	/**
	 * 获取视频列表RequestId
	 */
	private long mGetVideoListRequestId = RequestJni.InvalidRequestId;
	/**
	 * 视频列表组数组
	 */
	private LCVideoListGroupItem[] mGroups = null;
	/**
	 * 视频列表视频数组
	 */
	private LCVideoListVideoItem[] mVideos = null;
	
	@SuppressLint("UseSparseArrays")
	public LCVideoManager(LCVideoManagerListener callback) 
	{
		mCallback = callback;
	}
	
	/**
	 * 初始化
	 * @param dirPath	文件存放目录
	 * @return
	 */
	public boolean init(String dirPath) {
		mDirPath = dirPath;
		if (!mDirPath.isEmpty()) 
		{
			if (!mDirPath.regionMatches(mDirPath.length()-1, "/", 0, 1)) {
				mDirPath += "/";
			}
		}
		return !mDirPath.isEmpty();
	}
	
	/**
	 * 获取视频图片本地缓存文件路径
	 * @param videoId	视频Id
	 * @param photoType	视频图片类型
	 * @return
	 */
	private String getVideoPhotoPath(String videoId, VideoPhotoType photoType) 
	{
		String path = "";
		if (!StringUtil.isEmpty(videoId)) 
		{
			path = mDirPath + videoId + "_" + photoType.name();
		}
		return path;
	}
	
	/**
	 * 获取视频文件本地缓存文件路径
	 * @param videoId	视频Id
	 * @return
	 */
	private String getVideoPath(String videoId)
	{
		String path = "";
		if (!StringUtil.isEmpty(videoId))
		{
			path = mDirPath + videoId;
		}
		return path;
	}
	
	/**
	 * 清除所有男士的图片
	 */
	public void removeAllFile()
	{
		String path = mDirPath;
		if (!path.isEmpty())
		{
			String dirPath = path + "*";
			String cmd = "rm -f " + dirPath;
			try {
				Runtime.getRuntime().exec(cmd);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	/**
	 * 合并视频消息记录（把女士发出及男士已购买记录合并为一条聊天记录）
	 * @param msgList
	 */
	public void combineMessageItem(ArrayList<LCMessageItem> msgList)
	{
		if (null != msgList && msgList.size() > 0) 
		{
			synchronized (msgList) 
			{
				// 女士发送视频列表
				ArrayList<LCMessageItem> womanMsgList = new ArrayList<LCMessageItem>();
				// 男士发送视频列表
				ArrayList<LCMessageItem> manMsgList = new ArrayList<LCMessageItem>();
				// 找出所有男士和女士发出的视频消息
				for (LCMessageItem item : msgList)
				{
					if (item.msgType == MessageType.Video
						&& null != item.getVideoItem()
						&& null != item.getVideoItem().videoItem
						&& !item.getVideoItem().videoItem.videoId.isEmpty())
					{
						if (item.sendType == SendType.Send) {
							womanMsgList.add(item);
						}
						else if (item.sendType == SendType.Recv) {
							manMsgList.add(item);
						}
					}
				}
				
				// 合并男士购买的视频消息
				if (!manMsgList.isEmpty() && !womanMsgList.isEmpty())
				{
					for (LCMessageItem manItem : manMsgList) {
						for (LCMessageItem womanItem : womanMsgList) {
							LCVideoMsgItem manVideoItem = manItem.getVideoItem();
							LCVideoMsgItem womanVideoItem = womanItem.getVideoItem();
							if (manVideoItem.videoItem.videoId.compareTo(womanVideoItem.videoItem.videoId) == 0
								&& manVideoItem.sendId.compareTo(womanVideoItem.sendId) == 0) 
							{
								// 男士发出的视频ID与女士发出的视频ID一致，需要合并
								msgList.remove(manItem);
								womanVideoItem.charge = true;
							}
						}
					}
				}
			}
		}
	}
	
	// --------------------- sending（正在发送） --------------------------
	/**
	 * 获取指定票根的item并从待发送map表中移除
	 * @param msgId	消息ID 
	 * @return
	 */
	public LCMessageItem getAndRemoveSendingItem(int msgId) {
		LCMessageItem item = null;
		synchronized (mMsgIdMap)
		{
			item = mMsgIdMap.remove(msgId);
			if (null == item) { 
				Log.e("livechat", "LCVideoManager::getAndRemoveSendingItem() fail msgId:%d", msgId);
			}
		}
		return item;
	}
	
	/**
	 * 添加指定票根的item到待发送map表中
	 * @param msgId	票根
	 * @param item	消息item
	 * @return
	 */
	public boolean addSendingItem(LCMessageItem item) {
		boolean result = false;
		synchronized (mMsgIdMap)
		{
			if (item.msgType == MessageType.Video
				&& null != item.getVideoItem()
				&& null != item.getVideoItem().videoItem
				&& null == mMsgIdMap.get(item.msgId)) 
			{
				mMsgIdMap.put(item.msgId, item);
				result = true;
			}
			else {
				Log.e("livechat", "LCVideoManager::addSendingItem() fail msgId:%d", item.msgId);
			}
		}
		return result;
	}
	
	/**
	 * 清除所有待发送表map表的item
	 */
	public void clearAllSendingItems() {
		synchronized (mMsgIdMap)
		{
			mMsgIdMap.clear();
		}
	}
	
	// --------------------------- 发送请求 -------------------------
	/**
	 * 根据消息item获取发送的RequestId
	 * @param item
	 * @return
	 */
	public long getSendingRequestIdWithItem(LCMessageItem item) {
		long requestId = RequestJni.InvalidRequestId;
		synchronized(mMsgSendingRequestMap) {
			Long result = mMsgSendingRequestMap.get(item);
			if (null != result) {
				requestId = result;
			}
		}
		return requestId;
	}
	
	/**
	 * 根据发送RequestId获取并消息消息item
	 * @param requestId	请求ID
	 * @return
	 */
	public LCMessageItem getAndRemoveSendingRequestItem(long requestId) {
		LCMessageItem item = null;
		synchronized (mVideoSendingRequestMap)
		{
			synchronized(mMsgSendingRequestMap) 
			{
				item = mVideoSendingRequestMap.remove(requestId);
				if (null == item) {
					Log.e("livechat", String.format("%s::%s() fail requestId: %d", "LCVideoManager", "getRequestItem", requestId));
				}
				else {
						mMsgSendingRequestMap.remove(item);
				}
			}
		}
		return item;
	}
	
	/**
	 * 添加发送消息
	 * @param requestId	请求ID
	 * @param item		消息item
	 * @return
	 */
	public boolean addSendingRequestItem(long requestId, LCMessageItem item) {
		boolean result = false;
		synchronized (mVideoSendingRequestMap)
		{
			synchronized(mMsgSendingRequestMap) 
			{
				if (item.msgType == MessageType.Video
						&& null != item.getVideoItem()
						&& null != item.getVideoItem().videoItem
						&& requestId != RequestJni.InvalidRequestId) 
				{
					if (null == mVideoSendingRequestMap.get(requestId)) {
						mVideoSendingRequestMap.put(requestId, item);
					}
					
					if (null == mMsgSendingRequestMap.get(item)) {
						mMsgSendingRequestMap.put(item, requestId);
					}
					result = true;
				}
				else {
					Log.e("livechat", String.format("%s::%s() fail requestId:%d", "LCVideoManager", "addRequestItem", requestId));
				}
			}
		}
		return result;
	} 
	
	/**
	 * 清除所有正在请求发送的item
	 */
	public void clearAllSendingRequestItems() 
	{
		ArrayList<Long> list = null;
		
		// 清空map表
		synchronized (mVideoSendingRequestMap)
		{
			synchronized(mMsgSendingRequestMap) 
			{
				if (!mVideoSendingRequestMap.isEmpty()) {
					list = new ArrayList<Long>(mVideoSendingRequestMap.keySet());
				}
				mVideoSendingRequestMap.clear();
				mMsgSendingRequestMap.clear();
			}
		}
		
		// 停止所有请求
		if (null != list && !list.isEmpty()) 
		{
			for (Long requestId : list)
			{
				RequestJni.StopRequest(requestId);
			}
		}
	}
	
	// --------------------------- Download Video Photo（下载的视频图片 ） -------------------------
	public boolean DownloadVideoPhoto(String userId, String sId, LCVideoItem videoItem, VideoPhotoType photoType)
	{
		boolean result = false;
		
		if (null != videoItem
			&& !StringUtil.isEmpty(userId)
			&& !StringUtil.isEmpty(sId)) 
		{
			result = videoItem.IsDownloadingPhoto(photoType);
			
			if (!result) {
				// 未下载
				LCVideoPhotoDownloader downloader = new LCVideoPhotoDownloader();
				result = downloader.StartDownload(
							userId
							, sId
							, photoType
							, videoItem
							, getVideoPhotoPath(videoItem.videoId, photoType)
							, this);
				if (result) {
					// 成功启动下载，加到下载列表
					mVideoPhotoDownloadList.add(downloader);
				}
			}
		}
		
		return result;
	}
	
	@Override
	public void onDownloadVideoPhotoFinish(LCVideoPhotoDownloader downloader,
			boolean success, String errno, String errmsg,
			VideoPhotoType photoType, LCVideoItem item) 
	{
		// 下载完成
		synchronized (mVideoPhotoDownloadList) {
			mVideoPhotoDownloadList.remove(downloader);
		}

		// callback
		if (null != mCallback) {
			mCallback.OnDownloadVideoPhotoFinish(success, errno, errmsg, photoType, item);
		}
	}
	
	/**
	 * 停止所有视频图片下载
	 */
	private void StopAllDownloadVideoPhoto()
	{
		synchronized (mVideoPhotoDownloadList) {
			for (LCVideoPhotoDownloader downloader : mVideoPhotoDownloadList) 
			{
				downloader.Stop();
			}
		}
	}
	
	// --------------------------- Download Video（下载的视频 ） -------------------------
	public boolean DownloadVideo(String userId, String sId, LCMessageItem item)
	{
		boolean result = false;
		
		if (!StringUtil.isEmpty(userId)
			&& !StringUtil.isEmpty(sId) 
			&& null != item
			&& item.msgType == MessageType.Video
			&& null != item.getVideoItem()
			&& null != item.getVideoItem().videoItem)
		{
			LCVideoItem videoItem = item.getVideoItem().videoItem;
			result = videoItem.IsDownloadingVideo();
			
			if (!result) {
				// 未下载
				LCVideoDownloader downloader = new LCVideoDownloader();
				result = downloader.StartDownload(
							userId
							, sId
							, item
							, getVideoPath(videoItem.videoId)
							, this);
				if (result) {
					// 成功启动下载，加到下载列表
					mVideoDownloadList.add(downloader);
				}
			}
		}
		
		return result;
	}
	
	@Override
	public void onDownloadVideoFinish(LCVideoDownloader downloader, boolean success
			, String errno, String errmsg, LCMessageItem item)
	{
		// 下载完成
		synchronized (mVideoPhotoDownloadList) {
			mVideoPhotoDownloadList.remove(downloader);
		}

		// callback
		if (null != mCallback) {
			mCallback.OnDownloadVideoFinish(success, errno, errmsg, item);
		}
	}
	
	/**
	 * 停止所有视频下载
	 */
	private void StopAllDownloadVideo()
	{
		synchronized (mVideoDownloadList) {
			for (LCVideoDownloader downloader : mVideoDownloadList) 
			{
				downloader.Stop();
			}
		}
	}
		
	// --------------------------- 视频列表 -------------------------
	/**
	 * 获取视频item（必需已存在）
	 * @param videoId	视频ID
	 * @return
	 */
	public LCVideoItem GetVideoWithExist(String videoId)
	{
		LCVideoItem videoItem = null;
		if (!StringUtil.isEmpty(videoId))
		{
			synchronized (mVideoMap)
			{
				videoItem = mVideoMap.get(videoId);
			}
		}
		
		return videoItem; 
	}
	
	/**
	 * 获取视频item（没有则新建）
	 * @param videoId	视频ID
	 * @return
	 */
	public LCVideoItem GetVideo(String videoId)
	{
		LCVideoItem videoItem = null;
		if (!StringUtil.isEmpty(videoId))
		{
			synchronized (mVideoMap)
			{
				videoItem = mVideoMap.get(videoId);
				if (null == videoItem) {
					// 生成视频item
					videoItem = new LCVideoItem();
					videoItem.videoId = videoId;
					// 插入map表
					mVideoMap.put(videoId, videoItem);
				}
			}
		}
		
		return videoItem; 
	}
	
	/**
	 * 清除视频列表
	 */
	public void clearVideoList()
	{
		mGroups = null;
		mVideos = null;
	}
	
	/**
	 * 停止所有下载
	 */
	public void StopAllDownload()
	{
		StopAllDownloadVideoPhoto();
		StopAllDownloadVideo();
	}
	
	/**
	 * 获取视频列表
	 * @return
	 */
	public void GetVideoList(String userId, String sid)
	{
		if (null != mGroups && null != mVideos) 
		{
			// 已请求过，直接返回
			if (null != mCallback) {
				mCallback.OnGetVideoList(true, "", "", mGroups, mVideos);
			}
		}
		else if (mGetVideoListRequestId == RequestJni.InvalidRequestId)
		{
			// 马上请求
			mGetVideoListRequestId = RequestJniLivechat.GetVideoList(sid, userId, this);
			if (mGetVideoListRequestId == RequestJni.InvalidRequestId) {
				// 请求失败（返回网络错误）
				if (null != mCallback) {
					mCallback.OnGetVideoList(false, "", "", null, null);
				}
			}
		}
	}
	
	@Override
	public void OnLCGetVideoList(boolean isSuccess, String errno,
			String errmsg, LCVideoListGroupItem[] groups,
			LCVideoListVideoItem[] videos) 
	{
		mGetVideoListRequestId = RequestJni.InvalidRequestId;
		
		if (isSuccess) {
			mGroups = groups;
			mVideos = videos;
			
			if (null != mVideos) 
			{
				for (LCVideoListVideoItem video : videos) 
				{
					LCVideoItem videoItem = GetVideo(video.videoId);
					videoItem.init(
							video.videoId
							, video.title
							, getVideoPhotoPath(video.videoId, VideoPhotoType.Big)
							, getVideoPhotoPath(video.videoId, VideoPhotoType.Default)
							, getVideoPath(video.videoId));
				}
			}
		}
		
		if (null != mCallback) {
			mCallback.OnGetVideoList(isSuccess, errno, errmsg, mGroups, mVideos);
		}
	}
	
	// --------------------------- 检测图片是否可发送 -------------------------
	/**
	 * 添加到检测map表
	 * @param requestId	请求ID
	 * @param checkItem	检测item
	 * @return
	 */
	public boolean addCheckVideoRequest(long requestId, LCVideoCheckItem checkItem)
	{
		boolean result = false;
		synchronized (mCheckVideoMap)
		{
			if (null == mCheckVideoMap.get(requestId))
			{
				mCheckVideoMap.put(requestId, checkItem);
			}
		}
		return result;
	}
	
	/**
	 * 获取并移除检测item
	 * @param requestId	请求ID
	 * @return
	 */
	public LCVideoCheckItem getAndRemoveCheckVideoRequest(long requestId)
	{
		LCVideoCheckItem checkItem = null;
		synchronized (mCheckVideoMap)
		{
			checkItem = mCheckVideoMap.remove(requestId);
		}
		return checkItem;
	}
	
	/**
	 * 获取最后一次检测
	 * @return
	 */
	public long getLastCheckVideoRequest()
	{
		long requestId = RequestJni.InvalidRequestId;
		synchronized (mCheckVideoMap)
		{
			if (!mCheckVideoMap.isEmpty())
			{
				for (Long requestItem : mCheckVideoMap.keySet())
				{
					requestId = requestItem;
					break;
				}
			}
		}
		return requestId;
	}
}
