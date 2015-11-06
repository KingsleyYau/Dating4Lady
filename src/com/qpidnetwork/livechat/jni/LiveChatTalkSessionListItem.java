package com.qpidnetwork.livechat.jni;

/**
 * 获取邀请/在聊用户列表的session item
 * @author Samson Fan
 *
 */
public class LiveChatTalkSessionListItem {
	public LiveChatTalkSessionListItem() {
		
	}
	
	public LiveChatTalkSessionListItem(
			String userId
			, String invitedId
			, boolean charget
			, int chatTime) 
	{
		this.userId = userId;
		this.invitedId = invitedId;
		this.charget = charget;
		this.chatTime = chatTime;
	}
	
	/**
	 * 用户ID 
	 */
	public String userId;
	
	/**
	 * 邀请ID
	 */
	public String invitedId;
	
	/**
	 * 是否已付费
	 */
	public boolean charget;
	
	/**
	 * 聊天时长
	 */
	public int chatTime;
}
