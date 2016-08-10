package com.qpidnetwork.livechat;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import android.annotation.SuppressLint;

import com.qpidnetwork.framework.util.Log;
import com.qpidnetwork.framework.util.StringUtil;
import com.qpidnetwork.livechat.LCMessageItem.MessageType;
import com.qpidnetwork.livechat.LCMessageItem.SendType;
import com.qpidnetwork.request.OnLCGetPhotoListCallback;
import com.qpidnetwork.request.RequestJni;
import com.qpidnetwork.request.RequestJniLivechat;
import com.qpidnetwork.request.RequestJniLivechat.PhotoModeType;
import com.qpidnetwork.request.RequestJniLivechat.PhotoSizeType;
import com.qpidnetwork.request.item.LCPhotoListAlbumItem;
import com.qpidnetwork.request.item.LCPhotoListPhotoItem;
import com.qpidnetwork.tool.Arithmetic;

/**
 * 图片管理类
 * @author Samson Fan
 *
 */
public class LCPhotoManager 
{
	/**
	 * Listener定义 
	 */
	public interface LCPhotoManagerListener {
		public void OnGetPhotoList(boolean isSuccess, String errno, String errmsg, LCPhotoListAlbumItem[] albums, LCPhotoListPhotoItem[] photos);
	}
	
	private LCPhotoManagerListener mCallback;
	/**
	 * msgId与item的待发送map表(msgId, photoItem)（记录未发送成功的item，发送成功则移除）
	 */
	private HashMap<Integer, LCMessageItem> mMsgIdMap;
	/**
	 * RequestId与item的待发送map表(RequestId, photoItem)（记录上传未成功的item，上传成功则移除）
	 */
	private HashMap<Long, LCMessageItem> mRequestMap;
	/**
	 * itme与RequestId的待发送map表(photoItem, RequestId)
	 */
	private HashMap<LCMessageItem, Long> mPhotoRequestMap;
	/**
	 * 自己图片下载请求map表(RequestId, 图片item)
	 */
	private HashMap<Long, LCPhotoItem> mSelfPhotoRequestMap;
	/**
	 * 图片map表(photoId, LCPhotoItem)
	 */
	private HashMap<String, LCPhotoItem> mSelfPhotoMap;
	/**
	 * 检测请求map表(RequestId, LCPhotoCheckItem)
	 */
	private HashMap<Long, LCPhotoCheckItem> mCheckPhotoMap;
	/**
	 * 本地缓存文件目录
	 */
	private String mDirPath;
	/**
	 * 获取图片列表RequestId
	 */
	private long mGetPhotoListRequestId;
	/**
	 * 图片列表相册数组
	 */
	private LCPhotoListAlbumItem[] mAlbums;
	/**
	 * 图片列表图片数组
	 */
	private LCPhotoListPhotoItem[] mPhotos;
	
	@SuppressLint("UseSparseArrays")
	public LCPhotoManager(LCPhotoManagerListener callback) 
	{
		mCallback = callback;
		mMsgIdMap = new HashMap<Integer, LCMessageItem>();
		mRequestMap = new HashMap<Long, LCMessageItem>();
		mPhotoRequestMap = new HashMap<LCMessageItem, Long>();
		mSelfPhotoRequestMap = new HashMap<Long, LCPhotoItem>();
		mSelfPhotoMap = new HashMap<String, LCPhotoItem>();
		mCheckPhotoMap = new HashMap<Long, LCPhotoCheckItem>();
		mDirPath = "";
		mGetPhotoListRequestId = RequestJni.InvalidRequestId;
		mAlbums = null;
		mPhotos = null;
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
			
			String ladyPath = getLadyPhotoPath();
			File ladyFile = new File(ladyPath);
			if (null != ladyFile
				&& (!ladyFile.exists() || !ladyFile.isDirectory()))
			{
				ladyFile.mkdirs();
			}
			
			String manPath = getManPhotoPath();
			File manFile = new File(manPath);
			if (null != manFile
				&& (!manFile.exists() || !manFile.isDirectory()))
			{
				manFile.mkdirs();
			}
		}
		return !mDirPath.isEmpty();
	}
	
	/**
	 * 获取女士图片目录
	 * @return
	 */
	private String getLadyPhotoPath()
	{
		return mDirPath + "lady/";
	}
	
	/**
	 * 获取男士图片目录
	 * @return
	 */
	private String getManPhotoPath()
	{
		return mDirPath + "man/";
	}
	
	/**
	 * 获取图片本地缓存文件路径
	 * @param item		消息item
	 * @param modeType	图片类型
	 * @param sizeType	图片尺寸
	 * @return
	 */
	public String getPhotoPath(LCMessageItem item, PhotoModeType modeType, PhotoSizeType sizeType) 
	{
		String path = "";
		if (item.msgType == MessageType.Photo && null != item.getPhotoItem()
				&& !item.getPhotoItem().photoId.isEmpty() && !mDirPath.isEmpty()) 
		{
			boolean isMine = (item.sendType == SendType.Send);
			path = getPhotoPath(item.getPhotoItem().photoId, modeType, sizeType, isMine);
		}
		return path;
	}
	
	/**
	 * 获取图片本地缓存文件路径(全路径)
	 * @param photoId	图片ID
	 * @param modeType	照片类型
	 * @param sizeType	照片尺寸
	 * @param isMine	是否自己的
	 * @return
	 */
	public String getPhotoPath(String photoId, PhotoModeType modeType, PhotoSizeType sizeType, boolean isMine) 
	{
		String path = "";
		if (!photoId.isEmpty()) {
			path =  getPhotoPathWithMode(photoId, modeType, isMine)
					+ "_"
					+ sizeType.name();
		}
		return path;
	}
	
	/**
	 * 获取图片指定类型路径(非全路径)
	 * @param photoId	照片ID
	 * @param modeType	照片类型
	 * @param isMine	是否自己的
	 * @return
	 */
	private String getPhotoPathWithMode(String photoId, PhotoModeType modeType, boolean isMine)
	{
		String path = "";
		if (!photoId.isEmpty()) {
			if (isMine) {
				path = getLadyPhotoPath();
			}
			else {
				path = getManPhotoPath();
			}
			path += Arithmetic.MD5(photoId.getBytes(), photoId.getBytes().length);
			path += "_";
			path += modeType.name();
		}
		return path;
	}
	
	/**
	 * 获取图片临时文件路径
	 * @param photoId	图片ID
	 * @param modeType	图片类型
	 * @param sizeType	图片尺寸
	 * @param isMine	是否自己的
	 * @return
	 */
	public String getTempPhotoPath(String photoId, PhotoModeType modeType, PhotoSizeType sizeType, boolean isMine) 
	{
		String path = "";
		if (!StringUtil.isEmpty(photoId)  
			&& !mDirPath.isEmpty()) 
		{
			path = getPhotoPath(photoId, modeType, sizeType, isMine) + "_temp";
		}
		return path;
	}
	
	/**
	 * 下载完成的临时文件转换成图片文件
	 * @param item		消息item
	 * @param tempPath	临时文件路径
	 * @param modeType	图片类型
	 * @param sizeType	图片尺寸
	 * @param isMine	是否自己的
	 * @return
	 */
	public boolean tempToPhoto(LCPhotoItem photoItem, String tempPath, PhotoModeType modeType, PhotoSizeType sizeType, boolean isMine) 
	{
		boolean result = false;
		if (null != photoItem
			&& !StringUtil.isEmpty(tempPath))
		{
			String path = getPhotoPath(photoItem.photoId, modeType, sizeType, isMine);
			if (!path.isEmpty()) {
				boolean renameResult = false; 
				File tempFile = new File(tempPath);
				File newFile = new File(path);
				if (tempFile.exists() 
					&& tempFile.isFile()
					&& tempFile.renameTo(newFile)) 
				{
					renameResult = true;
				}
				
				if (renameResult) {
					LCPhotoItem.DownloadStatus status = LCPhotoItem.GetDownloadStatus(modeType, sizeType);
					switch (status)
					{
					case DownloadShowFuzzyPhoto:
						photoItem.showFuzzyFilePath = path;
						break;
					case DownloadThumbFuzzyPhoto:
						photoItem.thumbFuzzyFilePath = path;
						break;
					case DownloadShowSrcPhoto:
						photoItem.showSrcFilePath = path;
						break;
					case DownloadThumbSrcPhoto:
						photoItem.thumbSrcFilePath = path;
						break;
					case DownloadSrcPhoto:
						photoItem.srcFilePath = path;
						break;
					default:
						break;
					}
				}
			}
		}
		return result;
	}
	
	/**
	 * 清除所有男士的图片
	 */
	public void removeAllManPhotoFile()
	{
		String path = getManPhotoPath();
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
	 * 合并图片消息记录（把女士发出及男士已购买的图片记录合并为一条聊天记录）
	 * @param msgList
	 */
	public void combineMessageItem(ArrayList<LCMessageItem> msgList)
	{
		if (null != msgList && msgList.size() > 0) 
		{
			synchronized (msgList) 
			{
				// 女士发送图片列表
				ArrayList<LCMessageItem> womanPhotoList = new ArrayList<LCMessageItem>();
				// 男士发送图片列表
				ArrayList<LCMessageItem> manPhotoList = new ArrayList<LCMessageItem>();
				// 找出所有男士和女士发出的图片消息
				for (LCMessageItem item : msgList)
				{
					if (item.msgType == MessageType.Photo
						&& null != item.getPhotoItem()
						&& !item.getPhotoItem().photoId.isEmpty())
					{
						if (item.sendType == SendType.Send) {
							womanPhotoList.add(item);
						}
						else if (item.sendType == SendType.Recv) {
							manPhotoList.add(item);
						}
					}
				}
				
				// 合并男士购买的图片消息
				if (manPhotoList.size() > 0 && womanPhotoList.size() > 0)
				{
					for (LCMessageItem manItem : manPhotoList) {
						for (LCMessageItem womanItem : womanPhotoList) {
							LCPhotoItem manPhotoItem = manItem.getPhotoItem();
							LCPhotoItem womanPhotoItem = womanItem.getPhotoItem();
							if (manPhotoItem.photoId.compareTo(womanPhotoItem.photoId) == 0
								&& manPhotoItem.sendId.compareTo(womanPhotoItem.sendId) == 0) 
							{
								// 男士发出的图片ID与女士发出的图片ID一致，需要合并
								msgList.remove(manItem);
								womanPhotoItem.charge = true;
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
				Log.e("livechat", String.format("%s::%s() fail msgId: %d", "LCPhotoManager", "getAndRemoveSendingItem", msgId));
			}
		}
		return item;
	}
	
	/**
	 * 添加指定票根的item到待发送map表中
	 * @param msgId	票根
	 * @param item	图片item
	 * @return
	 */
	public boolean addSendingItem(LCMessageItem item) {
		boolean result = false;
		synchronized (mMsgIdMap)
		{
			if (item.msgType == MessageType.Photo
					&& null != item.getPhotoItem()
					&& null == mMsgIdMap.get(item.msgId)) {
				mMsgIdMap.put(item.msgId, item);
				result = true;
			}
			else {
				Log.e("livechat", String.format("%s::%s() fail msgId: %d", "LCPhotoManager", "addSendingItem", item.msgId));
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
	
	// --------------------------- Uploading/Download Photo（正在上传/下载 ） -------------------------
	/**
	 * 获取正在上传/下载的RequestId
	 * @param item
	 * @return
	 */
	public long getRequestIdWithItem(LCMessageItem item) {
		long requestId = RequestJni.InvalidRequestId;
		synchronized(mPhotoRequestMap) {
			Long result = mPhotoRequestMap.get(item);
			if (null != result) {
				requestId = result;
			}
		}
		return requestId;
	}
	
	/**
	 * 获取并移除正在上传/下载的item
	 * @param requestId	请求ID
	 * @return
	 */
	public LCMessageItem getAndRemoveRequestItem(long requestId) {
		LCMessageItem item = null;
		synchronized (mRequestMap)
		{
			item = mRequestMap.remove(requestId);
			if (null == item) {
//				Log.e("livechat", String.format("%s::%s() fail requestId: %d", "LCPhotoManager", "getRequestItem", requestId));
			}
			else {
				synchronized(mPhotoRequestMap) {
					mPhotoRequestMap.remove(item);
				}
			}
		}
		return item;
	}
	
	/**
	 * 添加正在上传/下载的item
	 * @param requestId	请求ID
	 * @param item		消息item
	 * @return
	 */
	public boolean addRequestItem(long requestId, LCMessageItem item) {
		boolean result = false;
		synchronized (mRequestMap)
		{
			if (item.msgType == MessageType.Photo
					&& null != item.getPhotoItem()
					&& requestId != RequestJni.InvalidRequestId
					&& null == mRequestMap.get(requestId)) 
			{
				mRequestMap.put(requestId, item);
				synchronized(mPhotoRequestMap) {
					mPhotoRequestMap.put(item, requestId);
				}
				result = true;
			}
			else {
				Log.e("livechat", String.format("%s::%s() fail requestId:%d", "LCPhotoManager", "addRequestItem", requestId));
			}
		}
		return result;
	} 
	
	/**
	 * 清除所有正在上传/下载的item
	 */
	public void clearAllRequestItems() 
	{
		ArrayList<Long> list = null;
		
		// 清空map表
		synchronized (mRequestMap)
		{
			if (mRequestMap.size() > 0) {
				list = new ArrayList<Long>(mRequestMap.keySet());
			}
			mRequestMap.clear();
			
			synchronized(mPhotoRequestMap) {
				mPhotoRequestMap.clear();
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
	
	// --------------------------- 图片列表（包括获取列表及下载） -------------------------
	/**
	 * 获取自己的图片item(不新建)
	 * @param photoId	图片ID
	 * @return
	 */
	public LCPhotoItem GetSelfPhoto(String photoId)
	{
		LCPhotoItem photoItem = null;
		if (!StringUtil.isEmpty(photoId))
		{
			synchronized (mSelfPhotoMap)
			{
				photoItem = mSelfPhotoMap.get(photoId);
			}
		}
		
		return photoItem; 
	}
	
	/**
	 * 获取自己的图片，若不存在则新建item
	 * @param photoId	图片ID
	 * @return
	 */
	public LCPhotoItem GetOrNewSelfPhoto(String photoId)
	{
		LCPhotoItem photoItem = null;
		if (!StringUtil.isEmpty(photoId))
		{
			synchronized (mSelfPhotoMap)
			{
				photoItem = mSelfPhotoMap.get(photoId);
				if (null == photoItem) 
				{
					photoItem = new LCPhotoItem();
					photoItem.photoId = photoId;
					mSelfPhotoMap.put(photoId, photoItem);
				}
			}
		}
		
		return photoItem; 
	}
	
	/**
	 * 判断自己图片是否正在下载
	 * @param photoItem		图片item
	 * @return
	 */
	public boolean IsExistSelfPhotoRequestItem(LCPhotoItem photoItem)
	{
		boolean result = false;
		if (null != photoItem)
		{
			synchronized (mSelfPhotoRequestMap)
			{
				result = mSelfPhotoRequestMap.values().contains(photoItem);
			}
		}
		return result;
	}
	
	/**
	 * 添加自己图片下载item
	 * @param requestId		请求ID
	 * @param photoItem		图片item
	 * @return
	 */
	public boolean AddSelfPhotoRequestItem(long requestId, LCPhotoItem photoItem)
	{
		boolean result = false;
		if (requestId != RequestJni.InvalidRequestId
			&& null != photoItem)
		{
			synchronized (mSelfPhotoRequestMap)
			{
				if (null == mSelfPhotoRequestMap.get(requestId)) {
					mSelfPhotoRequestMap.put(requestId, photoItem);
				}
			}
//			Log.d("LiveChatManager", "AddSelfPhotoRequestItem() requestId:%d", requestId);
		}
		return result;
	}
	
	/**
	 * 自己图片下载item
	 * @param requestId		请求ID
	 * @return
	 */
	public LCPhotoItem GetAndRemoveSelfPhotoRequestItem(long requestId)
	{
		LCPhotoItem item = null;
		synchronized (mSelfPhotoRequestMap)
		{
			item = mSelfPhotoRequestMap.remove(requestId);
		}
		return item;
	}
	
	/**
	 * 清除所有自己图片下载item
	 * @return
	 */
	public void clearAllSelfPhotoRequestItems()
	{
		ArrayList<Long> list = null;
		
		// 清空map表
		synchronized (mSelfPhotoRequestMap)
		{
			if (!mSelfPhotoRequestMap.isEmpty()) {
				list = new ArrayList<Long>(mSelfPhotoRequestMap.keySet());
			}
			mSelfPhotoRequestMap.clear();
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
	
	/**
	 * 清除图片列表
	 */
	public synchronized void clearPhotoList()
	{
		mAlbums = null;
		mPhotos = null;
	}
	
	/**
	 * 获取图片列表
	 * @return
	 */
	public synchronized void GetPhotoList(String sid, String userId)
	{
		if (null != mAlbums && null != mPhotos) 
		{
			// 已请求过，直接返回
			if (null != mCallback) {
				mCallback.OnGetPhotoList(true, "", "", mAlbums, mPhotos);
			}
		}
		else if (mGetPhotoListRequestId == RequestJni.InvalidRequestId)
		{
			// 马上请求
			mGetPhotoListRequestId = RequestJniLivechat.GetPhotoList(sid, userId, new OnLCGetPhotoListCallback() {
			
				@Override
				public void OnLCGetPhotoList(boolean isSuccess, String errno,
						String errmsg, LCPhotoListAlbumItem[] albums,
						LCPhotoListPhotoItem[] photos) 
				{
					mGetPhotoListRequestId = RequestJni.InvalidRequestId;
					
					if (isSuccess) {
						mAlbums = albums;
						mPhotos = photos;
						
						if (null != photos) 
						{
							for (int i = 0; i < photos.length; i++) 
							{
								LCPhotoItem photoItem = GetOrNewSelfPhoto(photos[i].photoId);
								photoItem.init(
										photos[i].photoId
										, ""
										, photos[i].title
										, getPhotoPath(photos[i].photoId, PhotoModeType.Fuzzy, PhotoSizeType.Large, true)
										, getPhotoPath(photos[i].photoId, PhotoModeType.Fuzzy, PhotoSizeType.Middle, true)
										, getPhotoPath(photos[i].photoId, PhotoModeType.Clear, PhotoSizeType.Original, true)
										, getPhotoPath(photos[i].photoId, PhotoModeType.Clear, PhotoSizeType.Large, true)
										, getPhotoPath(photos[i].photoId, PhotoModeType.Clear, PhotoSizeType.Middle, true)
										, true);
							}
						}
					}
					
					if (null != mCallback) {
						mCallback.OnGetPhotoList(isSuccess, errno, errmsg, mAlbums, mPhotos);
					}
				}
			});
			
			if (mGetPhotoListRequestId == RequestJni.InvalidRequestId) {
				// 请求失败（返回网络错误）
				if (null != mCallback) {
					mCallback.OnGetPhotoList(false, "", "", null, null);
				}
			}
		}
	}
	
	// --------------------------- 检测图片是否可发送 -------------------------
	/**
	 * 添加到检测map表
	 * @param requestId	请求ID
	 * @param checkItem	检测item
	 * @return
	 */
	public boolean addCheckPhotoRequest(long requestId, LCPhotoCheckItem checkItem)
	{
		boolean result = false;
		synchronized (mCheckPhotoMap)
		{
			if (null == mCheckPhotoMap.get(requestId))
			{
				mCheckPhotoMap.put(requestId, checkItem);
			}
		}
		return result;
	}
	
	/**
	 * 获取并移除检测item
	 * @param requestId	请求ID
	 * @return
	 */
	public LCPhotoCheckItem getAndRemoveCheckPhotoRequest(long requestId)
	{
		LCPhotoCheckItem checkItem = null;
		synchronized (mCheckPhotoMap)
		{
			checkItem = mCheckPhotoMap.remove(requestId);
		}
		return checkItem;
	}
	
	/**
	 * 获取最后一次检测
	 * @return
	 */
	public long getLastCheckPhotoRequest()
	{
		long requestId = RequestJni.InvalidRequestId;
		synchronized (mCheckPhotoMap)
		{
			if (!mCheckPhotoMap.isEmpty())
			{
				for (Long requestItem : mCheckPhotoMap.keySet())
				{
					requestId = requestItem;
					break;
				}
			}
		}
		return requestId;
	}
}
