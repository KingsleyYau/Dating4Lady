package com.qpidnetwork.livechat;

import com.qpidnetwork.livechat.jni.LiveChatClient.UserStatusType;
import com.qpidnetwork.livechat.jni.LiveChatTalkUserListItem;

/**
 * LiveChat用户本人信息
 * @author Samson Fan
 */
public class LCSelfInfo
{
	/**
	 * 用户ID
	 */
	public String mUserId;
	/**
	 * 设置唯一标识
	 */
	public String mDeviceId;
	/**
	 * 是否风控
	 */
	public boolean mRiskControl;
	/**
	 * 是否接收视频消息
	 */
	public boolean mIsRecvVideoMsg;
	/**
	 * 用户名称
	 */
	public String mUserName;
	/**
	 * 登录的sid
	 */
	public String mSid;
	/**
	 * 用户头像URL
	 */
	public String mImgUrl;
	/**
	 * 用户在线状态
	 */
	public UserStatusType mStatus;
	/**
	 * 是否需要翻译
	 */
	public boolean mNeedTrans;
	/**
	 * 翻译ID
	 */
	public String mTransUserId;
	/**
	 * 翻译名称
	 */
	public String mTransUserName;
	/**
	 * 是否绑定翻译
	 */
	public boolean mTransBind;
	/**
	 * 翻译在线状态
	 */
	public UserStatusType mTransStatus;

	public LCSelfInfo() 
	{
		mUserId = "";
		mDeviceId = "";
		mRiskControl = false;
		mIsRecvVideoMsg = true;
		mSid = "";
		mUserName = "";
		mImgUrl = "";
		mStatus = UserStatusType.USTATUS_UNKNOW;
		// --- 以下是翻译信息(仅女士端) ---
		mNeedTrans = false;
		mTransUserId = "";
		mTransUserName = "";
		mTransBind = false;
		mTransStatus = UserStatusType.USTATUS_UNKNOW;
	}
	
	public void UpdateInfo(LiveChatTalkUserListItem item)
	{
		mUserName = item.userName;
		mImgUrl = item.imgUrl;
		// 翻译信息
		mNeedTrans = item.needTrans;
		mTransUserId = item.transUserId;
		mTransUserName = item.transUserName;
		mTransStatus = item.statusType;
		mTransBind = item.transBind;
	}
}
