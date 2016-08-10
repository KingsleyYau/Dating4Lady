package com.qpidnetwork.livechat;

import com.qpidnetwork.livechat.jni.LiveChatClientListener.KickOfflineType;
import com.qpidnetwork.livechat.jni.LiveChatClientListener.LiveChatErrType;
import com.qpidnetwork.livechat.jni.LiveChatClientListener.TalkEmfNoticeType;
import com.qpidnetwork.livechat.jni.LiveChatTalkUserListItem;

/**
 * LiveChat管理回调接口类
 * @author Samson Fan
 *
 */
public interface LiveChatManagerOtherListener {
	/**
	 * 登录回调
	 * @param errType	错误类型
	 * @param errmsg	错误描述
	 * @param isAutoLogin	自动重登录
	 */
	public void OnLogin(LiveChatErrType errType, String errmsg, boolean isAutoLogin);
	
	/**
	 * 注销/断线回调
	 * @param errType	错误类型
	 * @param errmsg	错误描述
	 * @param isAutoLogin	是否自动登录
	 */
	public void OnLogout(LiveChatErrType errType, String errmsg, boolean isAutoLogin);
	
	/**
	 * 获取单个用户历史聊天记录（包括文本、高级表情、语音、图片）
	 * @param errno		错误代码
	 * @param errmsg	错误描述
	 * @param userItem	用户item
	 */
	public void OnGetHistoryMessage(boolean success, String errno, String errmsg, LCUserItem userItem);
//	
//	/**
//	 * 获取多个用户历史聊天记录（包括文本、高级表情、语音、图片）
//	 * @param errno		错误代码
//	 * @param errmsg	错误描述
//	 * @param userItem	用户item
//	 */
//	public void OnGetUsersHistoryMessage(boolean success, String errno, String errmsg, LCUserItem[] userItems);
	
	// ---------------- 在线状态相关回调函数(online status) ----------------
	/**
	 * 翻译状态改变通知(可调用GetUserInfo()获取用户本人信息及翻译信息)
	 */
	public void OnTransStatusChange();
	
	/**
	 * 设置在线状态回调
	 * @param errType	处理结果类型
	 * @param errmsg	处理结果描述
	 */
	public void OnSetStatus(LiveChatErrType errType, String errmsg);
	
	/**
	 * 接收他人在线状态更新消息回调
	 * @param userItem	用户item
	 */
	public void OnUpdateStatus(LCUserItem userItem);
	
	/**
	 * 他人在线状态更新回调
	 * @param userItem	用户item
	 */
	public void OnChangeOnlineStatus(LCUserItem userItem);
	
	/**
	 * 接收被踢下线消息回调
	 * @param kickType	被踢下线原因
	 */
	public void OnRecvKickOffline(KickOfflineType kickType);
	
	/**
	 * 回复验证码
	 * @param errType	错误类型
	 * @param errmsg	错误描述
	 */
	public void OnReplyIdentifyCode(LiveChatErrType errType, String errmsg);
	
	/**
	 * 接收验证码
	 * @param data	验证码图片数据
	 */
	public void OnRecvIdentifyCode(byte[] data);
	
	// ---------------- 会话状态改变 ----------------
	/**
	 * 接收聊天事件消息回调
	 * @param item		用户item
	 */
	public void OnRecvTalkEvent(LCUserItem item);
	
	// ---------------- 联系人状态改变 ----------------
	/**
	 * 联系人状态改变回调
	 */
	public void OnContactListChange();

	// ---------------- Push消息 ----------------
	/**
	 * 接收邮件更新消息回调
	 * @param fromId		发送者ID
	 * @param noticeType	邮件类型
	 */
	public void OnRecvEMFNotice(String fromId, TalkEmfNoticeType noticeType);
	
	// ---------------- 其它请求操作 ----------------
	/**
	 * 搜索在线男士回调
	 * @param errType		错误类型
	 * @param errmsg		错误描述
	 * @param userIds		在线男士ID数组
	 */
	public void OnSearchOnlineMan(LiveChatErrType errType, String errmsg, String[] userIds);
	
	/**
	 * 获取多个用户信息回调
	 * @param errType		错误类型
	 * @param errmsg		错误描述
	 * @param list			用户信息数组
	 */
	public void OnGetUsersInfo(LiveChatErrType errType, String errmsg, int seq, LiveChatTalkUserListItem[] list);
	
//	/**
//	 * 获取最近扣费联系人回调
//	 * @param errType		错误类型
//	 * @param errmsg		错误描述
//	 * @param userIds		用户ID数组
//	 */
//	public void OnGetFeeRecentContactList(LiveChatErrType errType, String errmsg, String[] userIds);
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
//			, String[] chattingUserIds, String[] chattingInviteIds, String[] missingUserIds, String[] missingInviteIds);
}
