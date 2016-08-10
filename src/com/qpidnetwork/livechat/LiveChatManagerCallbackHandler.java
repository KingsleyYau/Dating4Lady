package com.qpidnetwork.livechat;

import java.util.ArrayList;
import java.util.Iterator;

import com.qpidnetwork.framework.util.Log;
import com.qpidnetwork.livechat.jni.LiveChatClientListener.KickOfflineType;
import com.qpidnetwork.livechat.jni.LiveChatClientListener.LiveChatErrType;
import com.qpidnetwork.livechat.jni.LiveChatClientListener.TalkEmfNoticeType;
import com.qpidnetwork.livechat.jni.LiveChatTalkUserListItem;
import com.qpidnetwork.request.OnLCCheckSendPhotoCallback;
import com.qpidnetwork.request.OnLCCheckSendVideoCallback;
import com.qpidnetwork.request.RequestJniLivechat.VideoPhotoType;
import com.qpidnetwork.request.item.EmotionConfigItem;
import com.qpidnetwork.request.item.LCPhotoListAlbumItem;
import com.qpidnetwork.request.item.LCPhotoListPhotoItem;
import com.qpidnetwork.request.item.LCVideoListGroupItem;
import com.qpidnetwork.request.item.LCVideoListVideoItem;

/**
 * LiveChatManager回调处理类
 * @author Samson Fan
 *
 */
public class LiveChatManagerCallbackHandler implements LiveChatManagerOtherListener
													 , LiveChatManagerMessageListener
													 , LiveChatManagerEmotionListener
													 , LiveChatManagerPhotoListener
													 , LiveChatManagerVideoListener
													 , LiveChatManagerVoiceListener
{
	/**
	 * 回调OtherListener的object列表
	 */
	private ArrayList<LiveChatManagerOtherListener> mOtherListeners;
	/**
	 * 回调MessageListener的object列表
	 */
	private ArrayList<LiveChatManagerMessageListener> mMessageListeners;
	/**
	 * 回调EmotionListener的object列表
	 */
	private ArrayList<LiveChatManagerEmotionListener> mEmotionListeners;
	/**
	 * 回调PhotoListener的object列表
	 */
	private ArrayList<LiveChatManagerPhotoListener> mPhotoListeners;
	/**
	 * 回调VideoListener的object列表
	 */
	private ArrayList<LiveChatManagerVideoListener> mVideoListeners;
	/**
	 * 回调VoiceListener的object列表
	 */
	private ArrayList<LiveChatManagerVoiceListener> mVoiceListeners;
	
	
	public LiveChatManagerCallbackHandler() {
		mOtherListeners = new ArrayList<LiveChatManagerOtherListener>();
		mMessageListeners = new ArrayList<LiveChatManagerMessageListener>();
		mEmotionListeners = new ArrayList<LiveChatManagerEmotionListener>();
		mPhotoListeners = new ArrayList<LiveChatManagerPhotoListener>();
		mVideoListeners = new ArrayList<LiveChatManagerVideoListener>();
		mVoiceListeners = new ArrayList<LiveChatManagerVoiceListener>();
	}
	
	// ----------------------- 注册/注销回调 -----------------------
	/**
	 * 注册Other回调
	 * @param listener
	 * @return
	 */
	public boolean RegisterOtherListener(LiveChatManagerOtherListener listener) 
	{
		boolean result = false;
		synchronized(mOtherListeners) 
		{
			if (null != listener) {
				boolean isExist = false;
				
				for (Iterator<LiveChatManagerOtherListener> iter = mOtherListeners.iterator(); iter.hasNext(); ) {
					LiveChatManagerOtherListener theListener = iter.next();
					if (theListener == listener) {
						isExist = true;
						break;
					}
				}
				
				if (!isExist) {
					result = mOtherListeners.add(listener);
				}
				else {
					Log.d("livechat", String.format("%s::%s() fail, listener:%s is exist", "LiveChatManagerCallbackHandler", "RegisterListener", listener.getClass().getSimpleName()));
				}
			}
			else {
				Log.e("livechat", String.format("%s::%s() fail, listener is null", "LiveChatManagerCallbackHandler", "RegisterOtherListener"));
			}
		}
		return result;
	}
	
	/**
	 * 注销Other回调
	 * @param listener
	 * @return
	 */
	public boolean UnregisterOtherListener(LiveChatManagerOtherListener listener) 
	{
		boolean result = false;
		synchronized(mOtherListeners)
		{
			result = mOtherListeners.remove(listener);
		}

		if (!result) {
			Log.e("livechat", String.format("%s::%s() fail, listener:%s", "LiveChatManagerCallbackHandler", "UnregisterOtherListener", listener.getClass().getSimpleName()));
		}
		return result;
	}
	
	/**
	 * 注册文本消息(Message)回调
	 * @param listener
	 * @return
	 */
	public boolean RegisterMessageListener(LiveChatManagerMessageListener listener) 
	{
		boolean result = false;
		synchronized(mMessageListeners) 
		{
			if (null != listener) {
				boolean isExist = false;
				
				for (Iterator<LiveChatManagerMessageListener> iter = mMessageListeners.iterator(); iter.hasNext(); ) {
					LiveChatManagerMessageListener theListener = iter.next();
					if (theListener == listener) {
						isExist = true;
						break;
					}
				}
				
				if (!isExist) {
					result = mMessageListeners.add(listener);
				}
				else {
					Log.d("livechat", String.format("%s::%s() fail, listener:%s is exist", "LiveChatManagerCallbackHandler", "RegisterMessageListener", listener.getClass().getSimpleName()));
				}
			}
			else {
				Log.e("livechat", String.format("%s::%s() fail, listener is null", "LiveChatManagerCallbackHandler", "RegisterListener"));
			}
		}
		return result;
	}
	
	/**
	 * 注销文本消息(Message)回调
	 * @param listener
	 * @return
	 */
	public boolean UnregisterMessageListener(LiveChatManagerMessageListener listener) 
	{
		boolean result = false;
		synchronized(mMessageListeners)
		{
			result = mMessageListeners.remove(listener);
		}

		if (!result) {
			Log.e("livechat", String.format("%s::%s() fail, listener:%s", "LiveChatManagerCallbackHandler", "UnregisterMessageListener", listener.getClass().getSimpleName()));
		}
		return result;
	}
	
	/**
	 * 注册高级表情(Emotion)回调
	 * @param listener
	 * @return
	 */
	public boolean RegisterEmotionListener(LiveChatManagerEmotionListener listener) 
	{
		boolean result = false;
		synchronized(mEmotionListeners) 
		{
			if (null != listener) {
				boolean isExist = false;
				
				for (Iterator<LiveChatManagerEmotionListener> iter = mEmotionListeners.iterator(); iter.hasNext(); ) {
					LiveChatManagerEmotionListener theListener = iter.next();
					if (theListener == listener) {
						isExist = true;
						break;
					}
				}
				
				if (!isExist) {
					result = mEmotionListeners.add(listener);
				}
				else {
					Log.d("livechat", String.format("%s::%s() fail, listener:%s is exist", "LiveChatManagerCallbackHandler", "RegisterEmotionListener", listener.getClass().getSimpleName()));
				}
			}
			else {
				Log.e("livechat", String.format("%s::%s() fail, listener is null", "LiveChatManagerCallbackHandler", "RegisterListener"));
			}
		}
		return result;
	}
	
	/**
	 * 注销高级表情(Emotion)回调
	 * @param listener
	 * @return
	 */
	public boolean UnregisterEmotionListener(LiveChatManagerEmotionListener listener) 
	{
		boolean result = false;
		synchronized(mEmotionListeners)
		{
			result = mEmotionListeners.remove(listener);
		}

		if (!result) {
			Log.e("livechat", String.format("%s::%s() fail, listener:%s", "LiveChatManagerCallbackHandler", "UnregisterEmotionListener", listener.getClass().getSimpleName()));
		}
		return result;
	}
	
	/**
	 * 注册私密照(Photo)回调
	 * @param listener
	 * @return
	 */
	public boolean RegisterPhotoListener(LiveChatManagerPhotoListener listener) 
	{
		boolean result = false;
		synchronized(mPhotoListeners) 
		{
			if (null != listener) {
				boolean isExist = false;
				
				for (Iterator<LiveChatManagerPhotoListener> iter = mPhotoListeners.iterator(); iter.hasNext(); ) {
					LiveChatManagerPhotoListener theListener = iter.next();
					if (theListener == listener) {
						isExist = true;
						break;
					}
				}
				
				if (!isExist) {
					result = mPhotoListeners.add(listener);
				}
				else {
					Log.d("livechat", String.format("%s::%s() fail, listener:%s is exist", "LiveChatManagerCallbackHandler", "RegisterPhotoListener", listener.getClass().getSimpleName()));
				}
			}
			else {
				Log.e("livechat", String.format("%s::%s() fail, listener is null", "LiveChatManagerCallbackHandler", "RegisterListener"));
			}
		}
		return result;
	}
	
	/**
	 * 注销私密照(Photo)回调
	 * @param listener
	 * @return
	 */
	public boolean UnregisterPhotoListener(LiveChatManagerPhotoListener listener) 
	{
		boolean result = false;
		synchronized(mPhotoListeners)
		{
			result = mPhotoListeners.remove(listener);
		}

		if (!result) {
			Log.e("livechat", String.format("%s::%s() fail, listener:%s", "LiveChatManagerCallbackHandler", "UnregisterPhotoListener", listener.getClass().getSimpleName()));
		}
		return result;
	}
	
	/**
	 * 注册微视频(Video)回调
	 * @param listener
	 * @return
	 */
	public boolean RegisterVideoListener(LiveChatManagerVideoListener listener) 
	{
		boolean result = false;
		synchronized(mVideoListeners) 
		{
			if (null != listener) {
				boolean isExist = false;
				
				for (Iterator<LiveChatManagerVideoListener> iter = mVideoListeners.iterator(); iter.hasNext(); ) {
					LiveChatManagerVideoListener theListener = iter.next();
					if (theListener == listener) {
						isExist = true;
						break;
					}
				}
				
				if (!isExist) {
					result = mVideoListeners.add(listener);
				}
				else {
					Log.d("livechat", String.format("%s::%s() fail, listener:%s is exist", "LiveChatManagerCallbackHandler", "RegisterVideoListener", listener.getClass().getSimpleName()));
				}
			}
			else {
				Log.e("livechat", String.format("%s::%s() fail, listener is null", "LiveChatManagerCallbackHandler", "RegisterListener"));
			}
		}
		return result;
	}
	
	/**
	 * 注销视频(Video)回调
	 * @param listener
	 * @return
	 */
	public boolean UnregisterVideoListener(LiveChatManagerVideoListener listener) 
	{
		boolean result = false;
		synchronized(mVideoListeners)
		{
			result = mVideoListeners.remove(listener);
		}

		if (!result) {
			Log.e("livechat", String.format("%s::%s() fail, listener:%s", "LiveChatManagerCallbackHandler", "UnregisterVideoListener", listener.getClass().getSimpleName()));
		}
		return result;
	}
	
	/**
	 * 注册语音(Voice)回调
	 * @param listener
	 * @return
	 */
	public boolean RegisterVoiceListener(LiveChatManagerVoiceListener listener) 
	{
		boolean result = false;
		synchronized(mVoiceListeners) 
		{
			if (null != listener) {
				boolean isExist = false;
				
				for (Iterator<LiveChatManagerVoiceListener> iter = mVoiceListeners.iterator(); iter.hasNext(); ) {
					LiveChatManagerVoiceListener theListener = iter.next();
					if (theListener == listener) {
						isExist = true;
						break;
					}
				}
				
				if (!isExist) {
					result = mVoiceListeners.add(listener);
				}
				else {
					Log.d("livechat", String.format("%s::%s() fail, listener:%s is exist", "LiveChatManagerCallbackHandler", "RegisterVoiceListener", listener.getClass().getSimpleName()));
				}
			}
			else {
				Log.e("livechat", String.format("%s::%s() fail, listener is null", "LiveChatManagerCallbackHandler", "RegisterListener"));
			}
		}
		return result;
	}
	
	/**
	 * 注销语音(Voice)回调
	 * @param listener
	 * @return
	 */
	public boolean UnregisterVoiceListener(LiveChatManagerVoiceListener listener) 
	{
		boolean result = false;
		synchronized(mVoiceListeners)
		{
			result = mVoiceListeners.remove(listener);
		}

		if (!result) {
			Log.e("livechat", String.format("%s::%s() fail, listener:%s", "LiveChatManagerCallbackHandler", "UnregisterVoiceListener", listener.getClass().getSimpleName()));
		}
		return result;
	}
	
	// ---------------------------- Other ----------------------------
	/**
	 * 登录回调
	 * @param errType	错误类型
	 * @param errmsg	错误代码
	 * @param isAutoLogin	自动重登录
	 */
	public void OnLogin(LiveChatErrType errType, String errmsg, boolean isAutoLogin)
	{
		synchronized(mOtherListeners) 
		{
			for (Iterator<LiveChatManagerOtherListener> iter = mOtherListeners.iterator(); iter.hasNext(); ) {
				LiveChatManagerOtherListener listener = iter.next();
				listener.OnLogin(errType, errmsg, isAutoLogin);
			}
		}
	}
	
	/**
	 * 注销/断线回调
	 * @param errType	错误类型
	 * @param errmsg	错误代码
	 * @param isAutoLogin	是否自动重新登录
	 */
	public void OnLogout(LiveChatErrType errType, String errmsg, boolean isAutoLogin)
	{
		synchronized(mOtherListeners) 
		{
			for (Iterator<LiveChatManagerOtherListener> iter = mOtherListeners.iterator(); iter.hasNext(); ) {
				LiveChatManagerOtherListener listener = iter.next();
				listener.OnLogout(errType, errmsg, isAutoLogin);
			}
		}		
	}
	
	/**
	 * 搜索在线男士回调
	 * @param errType		错误类型
	 * @param errmsg		错误描述
	 * @param userIds		在线男士ID数组
	 */
	public void OnSearchOnlineMan(LiveChatErrType errType, String errmsg, String[] userIds)
	{
		synchronized(mOtherListeners)
		{
			for (Iterator<LiveChatManagerOtherListener> iter = mOtherListeners.iterator(); iter.hasNext(); ) {
				LiveChatManagerOtherListener listener = iter.next();
				listener.OnSearchOnlineMan(errType, errmsg, userIds);
			}
		}
	}
	
	/**
	 * 获取多个用户信息回调
	 * @param errType		错误类型
	 * @param errmsg		错误描述
	 * @param list			用户信息数组
	 */
	public void OnGetUsersInfo(LiveChatErrType errType, String errmsg, int seq, LiveChatTalkUserListItem[] list)
	{
		synchronized(mOtherListeners)
		{
			for (Iterator<LiveChatManagerOtherListener> iter = mOtherListeners.iterator(); iter.hasNext(); ) {
				LiveChatManagerOtherListener listener = iter.next();
				listener.OnGetUsersInfo(errType, errmsg, seq, list);
			}
		}
	}
	
//	/**
//	 * 获取最近扣费联系人回调
//	 * @param errType		错误类型
//	 * @param errmsg		错误描述
//	 * @param userIds		用户ID数组
//	 */
//	public void OnGetFeeRecentContactList(LiveChatErrType errType, String errmsg, String[] userIds)
//	{
//		synchronized(mOtherListeners)
//		{
//			for (Iterator<LiveChatManagerOtherListener> iter = mOtherListeners.iterator(); iter.hasNext(); ) {
//				LiveChatManagerOtherListener listener = iter.next();
//				listener.OnGetFeeRecentContactList(errType, errmsg, userIds);
//			}
//		}
//	}
//	
//	/**
//	 * 获取女士聊天信息回调
//	 * @param errType			错误类型
//	 * @param errmsg			错误描述
//	 * @param chattingUserIds	在聊用户ID数组
//	 * @param chattingInviteIds	在聊邀请ID数组
//	 * @param missingUserIds	未回用户ID数组
//	 * @param missingInviteIds	未回邀请ID数组
//	 */
//	public void OnGetLadyChatInfo(LiveChatErrType errType, String errmsg
//			, String[] chattingUserIds, String[] chattingInviteIds, String[] missingUserIds, String[] missingInviteIds)
//	{
//		synchronized(mOtherListeners)
//		{
//			for (Iterator<LiveChatManagerOtherListener> iter = mOtherListeners.iterator(); iter.hasNext(); ) {
//				LiveChatManagerOtherListener listener = iter.next();
//				listener.OnGetLadyChatInfo(errType, errmsg, chattingUserIds, chattingInviteIds, missingUserIds, missingInviteIds);
//			}
//		}
//	}
	
	/**
	 * 获取单个用户历史聊天记录（包括文本、高级表情、语音、图片）
	 * @param success	是否成功
	 * @param errno		错误代码
	 * @param errmsg	错误描述
	 * @param userItem	用户item
	 */
	public void OnGetHistoryMessage(boolean success, String errno, String errmsg, LCUserItem userItem)
	{
		synchronized(mOtherListeners) 
		{
			for (Iterator<LiveChatManagerOtherListener> iter = mOtherListeners.iterator(); iter.hasNext(); ) {
				LiveChatManagerOtherListener listener = iter.next();
				listener.OnGetHistoryMessage(success, errno, errmsg, userItem);
			}
		}
	}
	
	/**
	 * 接收邮件更新消息回调
	 * @param fromId		发送者ID
	 * @param noticeType	邮件类型
	 */
	public void OnRecvEMFNotice(String fromId, TalkEmfNoticeType noticeType)
	{
		synchronized(mOtherListeners) 
		{
			for (Iterator<LiveChatManagerOtherListener> iter = mOtherListeners.iterator(); iter.hasNext(); ) {
				LiveChatManagerOtherListener listener = iter.next();
				listener.OnRecvEMFNotice(fromId, noticeType);
			}
		}
	}
//	
//	/**
//	 * 获取多个用户历史聊天记录（包括文本、高级表情、语音、图片）
//	 * @param success 	是否成功
//	 * @param errno		错误代码
//	 * @param errmsg	错误描述
//	 * @param userItem	用户item
//	 */
//	public void OnGetUsersHistoryMessage(boolean success, String errno, String errmsg, LCUserItem[] userItems)
//	{
//		synchronized(mOtherListeners) 
//		{
//			for (Iterator<LiveChatManagerOtherListener> iter = mOtherListeners.iterator(); iter.hasNext(); ) {
//				LiveChatManagerOtherListener listener = iter.next();
//				listener.OnGetUsersHistoryMessage(success, errno, errmsg, userItems);
//			}
//		}
//	}
//	
	// ---------------- 在线状态相关回调函数(online status) ----------------
	/**
	 * 翻译状态改变通知(可调用GetUserInfo()获取用户本人信息及翻译信息)
	 */
	public void OnTransStatusChange()
	{
		synchronized(mOtherListeners) 
		{
			for (Iterator<LiveChatManagerOtherListener> iter = mOtherListeners.iterator(); iter.hasNext(); ) {
				LiveChatManagerOtherListener listener = iter.next();
				listener.OnTransStatusChange();
			}
		}
	}
	
	/**
	 * 设置在线状态回调
	 * @param errType	处理结果类型
	 * @param errmsg	处理结果描述
	 */
	public void OnSetStatus(LiveChatErrType errType, String errmsg)
	{
		synchronized(mOtherListeners) 
		{
			for (Iterator<LiveChatManagerOtherListener> iter = mOtherListeners.iterator(); iter.hasNext(); ) {
				LiveChatManagerOtherListener listener = iter.next();
				listener.OnSetStatus(errType, errmsg);
			}
		}
	}
	
	/**
	 * 接收他人在线状态更新消息回调
	 * @param userItem	用户item
	 */
	public void OnUpdateStatus(LCUserItem userItem)
	{
		synchronized(mOtherListeners) 
		{
			for (Iterator<LiveChatManagerOtherListener> iter = mOtherListeners.iterator(); iter.hasNext(); ) {
				LiveChatManagerOtherListener listener = iter.next();
				listener.OnUpdateStatus(userItem);
			}
		}
	}
	
	/**
	 * 他人在线状态更新回调
	 * @param userItem	用户item
	 */
	public void OnChangeOnlineStatus(LCUserItem userItem)
	{
		synchronized(mOtherListeners) 
		{
			for (Iterator<LiveChatManagerOtherListener> iter = mOtherListeners.iterator(); iter.hasNext(); ) {
				LiveChatManagerOtherListener listener = iter.next();
				listener.OnChangeOnlineStatus(userItem);
			}
		}
	}
	
	/**
	 * 接收被踢下线消息回调
	 * @param kickType	被踢下线原因
	 */
	public void OnRecvKickOffline(KickOfflineType kickType)
	{
		synchronized(mOtherListeners) 
		{
			for (Iterator<LiveChatManagerOtherListener> iter = mOtherListeners.iterator(); iter.hasNext(); ) {
				LiveChatManagerOtherListener listener = iter.next();
				listener.OnRecvKickOffline(kickType);
			}
		}
	}
	
	/**
	 * 回复验证码
	 * @param errType	错误类型
	 * @param errmsg	错误描述
	 */
	public void OnReplyIdentifyCode(LiveChatErrType errType, String errmsg)
	{
		synchronized(mOtherListeners) 
		{
			for (Iterator<LiveChatManagerOtherListener> iter = mOtherListeners.iterator(); iter.hasNext(); ) {
				LiveChatManagerOtherListener listener = iter.next();
				listener.OnReplyIdentifyCode(errType, errmsg);
			}
		}
	}
	
	/**
	 * 接收验证码
	 * @param data	验证码图片数据
	 */
	public void OnRecvIdentifyCode(byte[] data)
	{
		synchronized(mOtherListeners)
		{
			for (Iterator<LiveChatManagerOtherListener> iter = mOtherListeners.iterator(); iter.hasNext(); ) {
				LiveChatManagerOtherListener listener = iter.next();
				listener.OnRecvIdentifyCode(data);
			}
		}
	}
	
	// ---------------- 联系人状态改变 ----------------
	/**
	 * 联系人状态改变回调
	 */
	public void OnContactListChange()
	{
		synchronized(mOtherListeners) 
		{
			for (Iterator<LiveChatManagerOtherListener> iter = mOtherListeners.iterator(); iter.hasNext(); ) {
				LiveChatManagerOtherListener listener = iter.next();
				listener.OnContactListChange();
			}
		}
	}

	// ---------------- 对话状态改变或对话操作回调函数 ----------------
	
	/**
	 * 接收聊天事件消息回调
	 * @param item	用户item
	 */
	public void OnRecvTalkEvent(LCUserItem item)
	{
		synchronized(mOtherListeners) 
		{
			for (Iterator<LiveChatManagerOtherListener> iter = mOtherListeners.iterator(); iter.hasNext(); ) {
				LiveChatManagerOtherListener listener = iter.next();
				listener.OnRecvTalkEvent(item);
			}
		}
	}
	
	// ---------------- 文字聊天回调函数(message) ----------------
	/**
	 * 发送文本聊天消息回调
	 * @param errType	错误代码
	 * @param errmsg	错误描述
	 * @param item		消息item
	 * @return
	 */
	public void OnSendMessage(LiveChatErrType errType, String errmsg, LCMessageItem item)
	{
		synchronized(mMessageListeners) 
		{
			for (Iterator<LiveChatManagerMessageListener> iter = mMessageListeners.iterator(); iter.hasNext(); ) {
				LiveChatManagerMessageListener listener = iter.next();
				listener.OnSendMessage(errType, errmsg, item);
			}
		}
	}
	
	/**
	 * 接收聊天文本消息回调
	 * @param item		消息item
	 */
	public void OnRecvMessage(LCMessageItem item)
	{
		synchronized(mMessageListeners) 
		{
			for (Iterator<LiveChatManagerMessageListener> iter = mMessageListeners.iterator(); iter.hasNext(); ) {
				LiveChatManagerMessageListener listener = iter.next();
				listener.OnRecvMessage(item);
			}
		}
	}
	
	/**
	 * 接收警告消息回调
	 * @param item		消息item
	 */
	public void OnRecvWarning(LCMessageItem item)
	{
		synchronized(mMessageListeners) 
		{
			for (Iterator<LiveChatManagerMessageListener> iter = mMessageListeners.iterator(); iter.hasNext(); ) {
				LiveChatManagerMessageListener listener = iter.next();
				listener.OnRecvWarning(item);
			}
		}
	}
	
	/**
	 * 接收用户正在编辑消息回调 
	 * @param fromId	用户ID
	 */
	public void OnRecvEditMsg(String fromId)
	{
		synchronized(mMessageListeners) 
		{
			for (Iterator<LiveChatManagerMessageListener> iter = mMessageListeners.iterator(); iter.hasNext();) {
				LiveChatManagerMessageListener listener = iter.next();
				listener.OnRecvEditMsg(fromId);
			}
		}
	}
	
	/**
	 * 接收系统消息回调
	 * @param item		消息item
	 */
	public void OnRecvSystemMsg(LCMessageItem item) 
	{
		synchronized(mMessageListeners) 
		{
			for (Iterator<LiveChatManagerMessageListener> iter = mMessageListeners.iterator(); iter.hasNext();) {
				LiveChatManagerMessageListener listener = iter.next();
				listener.OnRecvSystemMsg(item);
			}
		}
	}
	
	/**
	 * 接收发送待发列表不成功消息
	 * @param errType	不成功原因
	 * @param msgList	待发列表
	 */
	public void OnSendMessageListFail(LiveChatErrType errType, ArrayList<LCMessageItem> msgList)
	{
		synchronized(mMessageListeners) 
		{
			for (Iterator<LiveChatManagerMessageListener> iter = mMessageListeners.iterator(); iter.hasNext();) {
				LiveChatManagerMessageListener listener = iter.next();
				listener.OnSendMessageListFail(errType, msgList);
			}
		}
	}
	
	// ---------------- 高级表情回调函数(Emotion) ----------------
	/**
	 * 获取高级表情配置回调
	 * @param success	是否成功
	 * @param errType	处理结果错误代码
	 * @param errmsg	处理结果描述
	 */
	public void OnGetEmotionConfig(boolean success, String errno, String errmsg, EmotionConfigItem item)
	{
		synchronized(mEmotionListeners) 
		{
			for (Iterator<LiveChatManagerEmotionListener> iter = mEmotionListeners.iterator(); iter.hasNext(); ) {
				LiveChatManagerEmotionListener listener = iter.next();
				listener.OnGetEmotionConfig(success,errno, errmsg, item);
			}
		}
	}
	
	
	/**
	 * 发送高级表情回调
	 * @param errType	处理结果错误代码
	 * @param errmsg	处理结果描述
	 * @param item		消息item
	 * @return
	 */
	public void OnSendEmotion(LiveChatErrType errType, String errmsg, LCMessageItem item)
	{
		synchronized(mEmotionListeners) 
		{
			for (Iterator<LiveChatManagerEmotionListener> iter = mEmotionListeners.iterator(); iter.hasNext(); ) {
				LiveChatManagerEmotionListener listener = iter.next();
				listener.OnSendEmotion(errType, errmsg, item);
			}
		}
	}
	
	/**
	 * 接收高级表情消息回调
	 * @param item		消息item
	 */
	public void OnRecvEmotion(LCMessageItem item)
	{
		synchronized(mEmotionListeners) 
		{
			for (Iterator<LiveChatManagerEmotionListener> iter = mEmotionListeners.iterator(); iter.hasNext(); ) {
				LiveChatManagerEmotionListener listener = iter.next();
				listener.OnRecvEmotion(item);
			}
		}
	}
	
	/**
	 * 下载高级表情图片成功回调
	 * @param emotionId	高级表情ID
	 * @param filePath	文件路径
	 */
	public void OnGetEmotionImage(boolean success, LCEmotionItem emotionItem)
	{
		synchronized(mEmotionListeners) 
		{
			for (Iterator<LiveChatManagerEmotionListener> iter = mEmotionListeners.iterator(); iter.hasNext(); ) {
				LiveChatManagerEmotionListener listener = iter.next();
				listener.OnGetEmotionImage(success, emotionItem);
			}
		}
	}
	
	/**
	 * 下载高级表情播放图片成功回调
	 * @param emotionId	高级表情ID
	 * @param filePath	文件路径
	 */
	public void OnGetEmotionPlayImage(boolean success, LCEmotionItem emotionItem)
	{
		synchronized(mEmotionListeners) 
		{
			for (Iterator<LiveChatManagerEmotionListener> iter = mEmotionListeners.iterator(); iter.hasNext(); ) {
				LiveChatManagerEmotionListener listener = iter.next();
				listener.OnGetEmotionPlayImage(success, emotionItem);
			}
		}
	}
	
	// ---------------- 图片回调函数(Private Album) ----------------
	/**
	 * 检测图片是否可发送
	 * @param errType	处理结果错误代码
	 * @param result	返回是否可发送结果
	 * @param errno		php返回错误代码
	 * @param errmsg	php返回错误描述
	 * @param userItem	用户item
	 * @param photoItem	图片item
	 */
	public void OnCheckSendPhoto(LiveChatErrType errType, OnLCCheckSendPhotoCallback.ResultType result, String errno, String errmsg, LCUserItem userItem, LCPhotoItem photoItem)
	{
		synchronized(mPhotoListeners) 
		{
			for (Iterator<LiveChatManagerPhotoListener> iter = mPhotoListeners.iterator(); iter.hasNext(); ) {
				LiveChatManagerPhotoListener listener = iter.next();
				listener.OnCheckSendPhoto(errType, result, errno, errmsg, userItem, photoItem);
			}
		}
	}
	
	/**
	 * 发送图片（包括上传图片文件(php)、发送图片(livechat)）回调
	 * @param errType	处理结果错误代码
	 * @param errmsg	处理结果描述
	 * @param item		消息item
	 * @return
	 */
	public void OnSendPhoto(LiveChatErrType errType, String errno, String errmsg, LCMessageItem item)
	{
		synchronized(mPhotoListeners) 
		{
			for (Iterator<LiveChatManagerPhotoListener> iter = mPhotoListeners.iterator(); iter.hasNext(); ) {
				LiveChatManagerPhotoListener listener = iter.next();
				listener.OnSendPhoto(errType, errno, errmsg, item);
			}
		}
	}
	
	/**
	 * 获取图片回调
	 * @param errType	处理结果错误代码
	 * @param errno		购买/下载请求失败的错误代码
	 * @param errmsg	处理结果描述
	 * @param item		消息item
	 * @return
	 */
	public void OnGetPhoto(LiveChatErrType errType, String errno, String errmsg, LCMessageItem item)
	{
		synchronized(mPhotoListeners) 
		{
			for (Iterator<LiveChatManagerPhotoListener> iter = mPhotoListeners.iterator(); iter.hasNext(); ) {
				LiveChatManagerPhotoListener listener = iter.next();
				listener.OnGetPhoto(errType, errno, errmsg, item);
			}
		}
	}
	
	/**
	 * 获取自己的图片回调
	 * @param errType	处理结果错误代码
	 * @param errno		购买/下载请求失败的错误代码
	 * @param errmsg	处理结果描述
	 * @param photoItem	图片item
	 */
	public void OnGetSelfPhoto(LiveChatErrType errType, String errno, String errmsg, LCPhotoItem photoItem)
	{
		synchronized(mPhotoListeners) 
		{
			for (Iterator<LiveChatManagerPhotoListener> iter = mPhotoListeners.iterator(); iter.hasNext(); ) {
				LiveChatManagerPhotoListener listener = iter.next();
				listener.OnGetSelfPhoto(errType, errno, errmsg, photoItem);
			}
		}
	}
	
	/**
	 * 接收图片消息回调
	 * @param item		消息item
	 */
	public void OnRecvPhoto(LCMessageItem item)
	{
		synchronized(mPhotoListeners) 
		{
			for (Iterator<LiveChatManagerPhotoListener> iter = mPhotoListeners.iterator(); iter.hasNext(); ) {
				LiveChatManagerPhotoListener listener = iter.next();
				listener.OnRecvPhoto(item);
			}
		}
	}
	
	/**
	 * 接收图片被查看回调
	 * @param userItem		用户item
	 * @param photoId		图片ID
	 * @param photoDesc		图片描述
	 */
	public void OnRecvShowPhoto(LCUserItem userItem, String photoId, String photoDesc)
	{
		synchronized(mPhotoListeners) 
		{
			for (Iterator<LiveChatManagerPhotoListener> iter = mPhotoListeners.iterator(); iter.hasNext(); ) {
				LiveChatManagerPhotoListener listener = iter.next();
				listener.OnRecvShowPhoto(userItem, photoId, photoDesc);
			}
		}
	}
	
	/**
	 * 获取私密照列表回调
	 * @param isSuccess	是否成功
	 * @param errno		错误代码
	 * @param errmsg	错误描述
	 * @param albums	相册数组
	 * @param photos	私密照数组
	 */
	public void OnGetPhotoList(boolean isSuccess, String errno, String errmsg, LCPhotoListAlbumItem[] albums, LCPhotoListPhotoItem[] photos)
	{
		synchronized(mPhotoListeners) 
		{
			for (Iterator<LiveChatManagerPhotoListener> iter = mPhotoListeners.iterator(); iter.hasNext(); ) {
				LiveChatManagerPhotoListener listener = iter.next();
				listener.OnGetPhotoList(isSuccess, errno, errmsg, albums, photos);
			}
		}
	}
	
	// ---------------- 语音回调函数(Voice) ----------------
	/**
	 * 发送语音（包括获取语音验证码(livechat)、上传语音文件(livechat)、发送语音(livechat)）回调
	 * @param errType	处理结果错误代码
	 * @param errmsg	处理结果描述
	 * @param item		消息item
	 * @return
	 */
	public void OnSendVoice(LiveChatErrType errType, String errno, String errmsg, LCMessageItem item)
	{
		synchronized(mVoiceListeners) 
		{
			for (Iterator<LiveChatManagerVoiceListener> iter = mVoiceListeners.iterator(); iter.hasNext(); ) {
				LiveChatManagerVoiceListener listener = iter.next();
				listener.OnSendVoice(errType, errno, errmsg, item);
			}
		}
	}
	
	/**
	 * 获取语音（包括下载语音(livechat)）回调
	 * @param errType	处理结果错误代码
	 * @param errmsg	处理结果描述
	 * @param item		消息item
	 * @return
	 */
	public void OnGetVoice(LiveChatErrType errType, String errmsg, LCMessageItem item)
	{
		synchronized(mVoiceListeners) 
		{
			for (Iterator<LiveChatManagerVoiceListener> iter = mVoiceListeners.iterator(); iter.hasNext(); ) {
				LiveChatManagerVoiceListener listener = iter.next();
				listener.OnGetVoice(errType, errmsg, item);
			}
		}
	}
	
	/**
	 * 接收语音消息回调
	 * @param item		消息item
	 */
	public void OnRecvVoice(LCMessageItem item)
	{
		synchronized(mVoiceListeners) 
		{
			for (Iterator<LiveChatManagerVoiceListener> iter = mVoiceListeners.iterator(); iter.hasNext(); ) {
				LiveChatManagerVoiceListener listener = iter.next();
				listener.OnRecvVoice(item);
			}
		}
	}

	// ---------------- 微视频回调函数(Video) ----------------
	@Override
	public void OnCheckSendVideo(LiveChatErrType errType, OnLCCheckSendVideoCallback.ResultType result, 
			String errno, String errmsg, LCUserItem userItem, LCVideoItem videoItem) 
	{
		synchronized(mVideoListeners) 
		{
			for (Iterator<LiveChatManagerVideoListener> iter = mVideoListeners.iterator(); iter.hasNext();) 
			{
				LiveChatManagerVideoListener listener = iter.next();
				listener.OnCheckSendVideo(errType, result, errno, errmsg, userItem, videoItem);
			}
		}
	}

	@Override
	public void OnSendVideo(LiveChatErrType errType, String errno,
			String errmsg, LCMessageItem item) 
	{
		synchronized(mVideoListeners) 
		{
			for (Iterator<LiveChatManagerVideoListener> iter = mVideoListeners.iterator(); iter.hasNext();) 
			{
				LiveChatManagerVideoListener listener = iter.next();
				listener.OnSendVideo(errType, errno, errmsg, item);
			}
		}
	}

	@Override
	public void OnGetVideoPhoto(LiveChatErrType errType, String errno,
			String errmsg, VideoPhotoType photoType, LCVideoItem item) 
	{
		synchronized(mVideoListeners) 
		{
			for (Iterator<LiveChatManagerVideoListener> iter = mVideoListeners.iterator(); iter.hasNext();) 
			{
				LiveChatManagerVideoListener listener = iter.next();
				listener.OnGetVideoPhoto(errType, errno, errmsg, photoType, item);
			}
		}
	}

	@Override
	public void OnGetVideo(LiveChatErrType errType, String errno,
			String errmsg, LCMessageItem item) 
	{
		synchronized(mVideoListeners) 
		{
			for (Iterator<LiveChatManagerVideoListener> iter = mVideoListeners.iterator(); iter.hasNext();) 
			{
				LiveChatManagerVideoListener listener = iter.next();
				listener.OnGetVideo(errType, errno, errmsg, item);
			}
		}
	}

	@Override
	public void OnGetVideoList(boolean isSuccess, String errno, String errmsg,
			LCVideoListGroupItem[] groups, LCVideoListVideoItem[] videos) 
	{
		synchronized(mVideoListeners) 
		{
			for (Iterator<LiveChatManagerVideoListener> iter = mVideoListeners.iterator(); iter.hasNext();) 
			{
				LiveChatManagerVideoListener listener = iter.next();
				listener.OnGetVideoList(isSuccess, errno, errmsg, groups, videos);
			}
		}
	}
	
	/**
	 * 接收视频被查看回调
	 * @param userItem		用户item
	 * @param videoId		视频ID
	 * @param videoDesc		视频描述
	 */
	public void OnRecvShowVideo(LCUserItem userItem, String videoId, String videoDesc)
	{
		synchronized(mPhotoListeners) 
		{
			for (Iterator<LiveChatManagerVideoListener> iter = mVideoListeners.iterator(); iter.hasNext(); ) {
				LiveChatManagerVideoListener listener = iter.next();
				listener.OnRecvShowVideo(userItem, videoId, videoDesc);
			}
		}
	}
}
