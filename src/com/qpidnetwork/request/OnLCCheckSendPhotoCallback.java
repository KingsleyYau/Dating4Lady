package com.qpidnetwork.request;

public abstract class OnLCCheckSendPhotoCallback 
{
	/**
	 *	返回结果 
	 */
	public enum ResultType {
		CannotSend,		// 不能发送
		AllowSend,		// 允许发送
		PhotoNotExist,	// 相片不存在或未审核
		NotAllow,		// 权限不允许
		OverUnread,		// 超过未读相片数
		Sent,			// 本次会话已发过
		SentAndRead,	// 已发送且已读
	};
	public void OnLCCheckSendPhoto(long requestId, ResultType result, String errno, String errmsg) {};
	public void OnLCCheckSendPhoto(long requestId, int result, String errno, String errmsg) {
		OnLCCheckSendPhoto(requestId, ResultType.values()[result], errno, errmsg);
	}
}
