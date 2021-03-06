package com.qpidnetwork.livechat;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import android.annotation.SuppressLint;

import com.qpidnetwork.framework.util.Log;
import com.qpidnetwork.livechat.LCMessageItem.MessageType;
import com.qpidnetwork.request.RequestJni;

/**
 * 语音管理类
 * @author Samson Fan
 *
 */
@SuppressLint("UseSparseArrays")
public class LCVoiceManager {
	/**
	 * msgId与item的待发送map表(msgId, item)（记录未发送成功item，发送成功则移除）
	 */
	private HashMap<Integer, LCMessageItem> mSendingMap;
	/**
	 * 正在获取验证码队列
	 */
	private ArrayList<LCMessageItem> mGetingCodeList;
	/**
	 * requestId与item的待请求map表(requestId, item)
	 */
	private HashMap<Long, LCMessageItem> mRequestIdMap;
	/**
	 * item与requestId的待请求map表(item, requestId)
	 */
	private HashMap<LCMessageItem, Long> mVoiceRequestMap;
	/**
	 * id与itemmap表(voiceId, item)（记录发送成功或收到的item）
	 */
//	private HashMap<String, LCMessageItem> mVoiceIdMap;
	private String mDirPath;
	
	public LCVoiceManager() {
		mSendingMap = new HashMap<Integer, LCMessageItem>();
		mGetingCodeList = new ArrayList<LCMessageItem>();
		mRequestIdMap = new HashMap<Long, LCMessageItem>();
		mVoiceRequestMap = new HashMap<LCMessageItem, Long>();
//		mVoiceIdMap = new HashMap<String, LCMessageItem>();
	}
	
	/**
	 * 初始化
	 * @param dirPath	语音文件存放目录
	 * @return
	 */
	public boolean init(String dirPath) {
		if (!dirPath.isEmpty()) {
			mDirPath = dirPath;
			if (!mDirPath.regionMatches(mDirPath.length()-1, "/", 0, 1)) {
				mDirPath += "/";
			}
		}
		return !mDirPath.isEmpty();
	}
	
	/**
	 * 获取语音本地缓存文件路径
	 * @param item		语音item
	 * @return
	 */
	public String getVoicePath(LCMessageItem item) {
		String path = "";
		if (item.msgType == MessageType.Voice
				&& !item.getVoiceItem().voiceId.isEmpty() 
				&& !mDirPath.isEmpty()) 
		{
			path = getVoicePath(item.getVoiceItem().voiceId, item.getVoiceItem().fileType);
		}
		return path;
	}
	
	/**
	 * 获取语音本地缓存文件路径
	 * @param voiceId	语音ID
	 * @param fileType	文件类型
	 * @return
	 */
	public String getVoicePath(String voiceId, String fileType) {
		String path = "";
		if (!voiceId.isEmpty() && !fileType.isEmpty()) 
		{
			path = mDirPath + voiceId;
			path += "." + fileType;
		}
		return path;
	}
	
	/**
	 * 复制文件至缓冲目录(用于发送语音消息)
	 * @param item
	 * @return
	 */
	public boolean copyVoiceFileToDir(LCMessageItem item)
	{
		boolean result = false;
		String desFilePath = getVoicePath(item);
		if (!desFilePath.isEmpty()) {
			String srcFilePath = item.getVoiceItem().filePath;
			File file = new File(srcFilePath);
			if (file.exists() && file.isFile()) {
				String cmd = "cp -f " + srcFilePath + " " + desFilePath;
				try {
					Runtime.getRuntime().exec(cmd);
					result = true;
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
		return result;
	}
	
	/**
	 * 清除所有语音文件
	 */
	public void removeAllVoiceFile()
	{
		if (!mDirPath.isEmpty())
		{
			String dirPath = mDirPath + "*";
			String cmd = "rm -f " + dirPath;
			try {
				Runtime.getRuntime().exec(cmd);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	// --------------------- sending（正在发送） --------------------------
	/**
	 * 获取从待发送map表中获取指定文件路径的item，并把item从照表中移除
	 * @param filePath	文件路径 
	 * @return
	 */
	public LCMessageItem getAndRemoveSendingItem(int msgId) {
		LCMessageItem item = null;
		synchronized (mSendingMap)
		{
			item = mSendingMap.remove(msgId);
		}
		
		if (null == item) { 
			Log.e("livechat", String.format("%s::%s() fail msgId:%d", "LCVoiceManager", "getAndRemoveSendingItem", msgId));
		}
		return item;
	}
	
	/**
	 * 添加到待发送map表中
	 * @param filePath	文件路径
	 * @param item		item
	 * @return
	 */
	public boolean addSendingItem(int msgId, LCMessageItem item) {
		boolean result = false;
		synchronized (mSendingMap)
		{
			if (item.msgType == MessageType.Voice
					&& null != item.getVoiceItem()
					&& null == mSendingMap.get(msgId)) 
			{
				mSendingMap.put(msgId, item);
				result = true;
			}
			else {
				Log.e("livechat", String.format("%s::%s() fail msgId:%d, item.msgType:%s", "LCVoiceManager", "addSendingItem", msgId, item.msgType.name()));
			}
		}
		return result;
	}
	
	/**
	 * 清除所有待发送表map表的item
	 */
	public void clearAllSendingItems() {
		synchronized (mSendingMap) 
		{
			mSendingMap.clear();
		}
	}
	
	// --------------------- GetingCode（正在获取验证码） --------------------------
	/**
	 * 获取待发送语音消息并移除该消息 
	 * @return
	 */
	public LCMessageItem getAndRemoveGetingCodeItem() {
		LCMessageItem item = null;
		synchronized (mGetingCodeList)
		{
			if (!mGetingCodeList.isEmpty()) {
				item = mGetingCodeList.remove(0);
			}
		}
		
		if (null == item) { 
			Log.e("livechat", "LCVoiceManager::getAndRemoveGetingCodeItem() mSendingList is empty");
		}
		return item;
	}
	
	/**
	 * 添加到获取验证码队列
	 * @param item		item
	 * @return
	 */
	public boolean addGetingCodeItem(LCMessageItem item) {
		boolean result = false;
		synchronized (mGetingCodeList)
		{
			if (item.msgType == MessageType.Voice
					&& null != item.getVoiceItem()) 
			{
				mGetingCodeList.add(item);
			}
			else {
				Log.e("livechat", "LCVoiceManager::addGetingCodeItem() fail item.msgType:%s", item.msgType.name());
			}
		}
		return result;
	}
	
	/**
	 * 清除获取验证码队列
	 */
	public void clearAllGetingCodeItems() {
		synchronized (mGetingCodeList) 
		{
			mGetingCodeList.clear();
		}
	}
	
	// ----------------------- Uploading/Downloading Voice（正在上传/下载） -------------------------
	/**
	 * 获取正在上传/下载的RequestId
	 * @param item
	 * @return
	 */
	public long getRequestIdWithItem(LCMessageItem item) {
		long requestId = RequestJni.InvalidRequestId;
		synchronized(mVoiceRequestMap) {
			Long result = mVoiceRequestMap.get(item);
			if (null != result) {
				requestId = result;
			}
		}
		return requestId;
	}
	
	/**
	 * 添加到正在上传/下载的map
	 * @param requestId
	 * @param item
	 * @return
	 */
	public boolean addRequestItem(long requestId, LCMessageItem item) {
		boolean result = false;
		synchronized (mRequestIdMap) 
		{
			if (item.msgType == MessageType.Voice
					&& null != item.getVoiceItem()
					&& null == mRequestIdMap.get(requestId))
			{
				mRequestIdMap.put(requestId, item);
				synchronized(mVoiceRequestMap) {
					mVoiceRequestMap.put(item, requestId);
				}
				result = true;
			}
			else {
				Log.e("livechat", String.format("%s::%s() fail requestId:%d, item.msgType:%s", "LCVoiceManager", "addRequestItem", requestId, item.msgType.name()));
			}
		}
		return result;
	}
	
	/**
	 * 获取并移除正在上传/下载的item
	 * @param requestId	请求ID
	 * @return
	 */
	public LCMessageItem getAndRemoveRquestItem(long requestId) {
		LCMessageItem item = null;
		synchronized (mRequestIdMap)
		{
			item = mRequestIdMap.remove(requestId);
			if (null == item) {
				Log.e("livechat", String.format("%s::%s() fail requestId:%d", "LCVoiceManager", "getAndRemoveRquestItem", requestId));
			}
			else {
				synchronized(mVoiceRequestMap) {
					mVoiceRequestMap.remove(item);
				}
			}
		}
		return item;
	}
	
	public void clearAllRequestItem() 
	{
		ArrayList<Long> list = null;
		
		// 清空map表
		synchronized (mRequestIdMap)
		{
			list = new ArrayList<Long>(mRequestIdMap.keySet());
			mRequestIdMap.clear();
			
			synchronized(mVoiceRequestMap) {
				mVoiceRequestMap.clear();
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
}
