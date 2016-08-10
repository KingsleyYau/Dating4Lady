package com.qpidnetwork.livechat;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import com.qpidnetwork.framework.util.StringUtil;
import com.qpidnetwork.livechat.LCUserItem.ChatType;
import com.qpidnetwork.livechat.jni.LiveChatClient.UserStatusType;

/**
 * 已扣费联系人管理器
 * @author Samson Fan
 *
 */
public class LCContactManager {
	/**
	 * 已扣费联系人列表
	 */
	private ArrayList<String> mContactList = null;
	/**
	 * 用户管理器
	 */
	private LCUserManager mUserMgr = null;
	
	public LCContactManager(LCUserManager userMgr)
	{
		mContactList = new ArrayList<String>();
		mUserMgr = userMgr;
	}
	
	/**
	 * 更新联系人列表
	 * @param array	联系人列表
	 */
	public void UpdateWithContactList(String[] array)
	{
		synchronized(mContactList) 
		{
			mContactList.clear();
			for (int i = 0; i < array.length; i++)
			{
				mContactList.add(array[i]);
			}
		}
	}
	
	/**
	 * 根据用户状态添加联系人
	 * @param item		用户item
	 */
	public boolean AddContactWithUserItem(LCUserItem item)
	{
		boolean result = false;
		// 不存在 且 可添加
		if (!IsExist(item.userId) && CanAddContact(item.chatType)) 
		{
			AddContact(item.userId);
			result = true;
		}
		return result;
	}
	
	/**
	 * 添加联系人
	 * @param userId	用户ID
	 */
	public void AddContact(String userId)
	{
		synchronized(mContactList)
		{
			if (!IsExistProc(userId))
			{
				mContactList.add(userId);
			}
		}
	}
	
	/**
	 * 清除联系人列表
	 */
	public void Clear()
	{
		synchronized (mContactList)
		{
			mContactList.clear();
		}
	}
	
	/**
	 * 用户是否存在于联系人列表
	 * @param userId	用户ID
	 * @return
	 */
	public boolean IsExist(String userId)
	{
		boolean result = false;
		synchronized(mContactList)
		{
			result = IsExistProc(userId);
		}
		return result;
	}
	
	/**
	 * 用户是否存在于联系人列表处理函数
	 * @param userId	用户ID
	 * @return
	 */
	private boolean IsExistProc(String userId)
	{
		boolean result = false;
		for (String id : mContactList)
		{
			if (userId.compareTo(id) == 0) {
				result = true;
				break;
			}
		}
		return result;
	}
	
	/**
	 * 获取联系人列表
	 * @return
	 */
	public ArrayList<LCUserItem> GetContactUserList()
	{
		ArrayList<LCUserItem> userList = new ArrayList<LCUserItem>();
		if (null != mUserMgr) 
		{
			// 找出所有用户item并添加到列表
			synchronized(mContactList)
			{
				for (String userId : mContactList)
				{
					LCUserItem userItem = mUserMgr.getUserItem(userId);
					if (null != userItem) 
					{
						userList.add(userItem);
					}
				}
			}
			
			// 排序
			try{
				Collections.sort(userList, getComparator());
			}catch(Exception e){

			}
		}
		return userList;
	}
	
	/**
	 * 根据聊天状态判断是否能添加联系人
	 * @param chatType
	 * @return
	 */
	private static boolean CanAddContact(ChatType chatType)
	{
		boolean result = false;
		result = IsInChatChatType(chatType);
		result = result || chatType == ChatType.ManInvite;
		return result;
	}
	
	/**
	 * 判断是否在聊状态
	 * @param chatType
	 * @return
	 */
	private static boolean IsInChatChatType(ChatType chatType)
	{
		return chatType == ChatType.InChatCharge || chatType == ChatType.InChatUseTryTicket;
	}
	
	/**
	 * 在线状态比较
	 * @param s1
	 * @param s2
	 * @return
	 */
	private static int CompareOnlineType(UserStatusType s1, UserStatusType s2)
	{
		int result = 0;
		if (s1 != s2)
		{
			if (s1 == UserStatusType.USTATUS_ONLINE || s2 == UserStatusType.USTATUS_ONLINE)
			{
				result = (s1 == UserStatusType.USTATUS_ONLINE ? -1 : 1);
			}
		}
		return result;
	}

	/**
	 * 聊天状态比较
	 * @param c1
	 * @param c2
	 * @return
	 */
	private static int CompareChatType(ChatType c1, ChatType c2)
	{
		int result = 0;
		if (c1 != c2)
		{
			// 聊天状态不一样
			if (c1 == ChatType.ManInvite || c2 == ChatType.ManInvite)
			{
				// 其中一个为男士邀请状态
				result = (c1 == ChatType.ManInvite ? -1 : 1);
			}
			else if (IsInChatChatType(c1) != IsInChatChatType(c2))
			{
				// 其中一个为在聊状态
				result = (IsInChatChatType(c1) ? -1 : 1);
			}
		}
		return result;
	}
	
	/**
	 * 获取比较器
	 * @return
	 */
	static public Comparator<LCUserItem> getComparator() 
	{
		Comparator<LCUserItem> comparator = new Comparator<LCUserItem>() {
			@Override
			public int compare(LCUserItem lhs, LCUserItem rhs) {
				// TODO Auto-generated method stub
				int result = 0;
				if (lhs != rhs) 
				{
					// 比较在线状态
					result = CompareOnlineType(lhs.statusType, rhs.statusType);
					
					// 按聊天状态（inchat或男士邀请排前）
					if (result == 0) 
					{
						if (lhs.chatType != rhs.chatType) 
						{
							boolean lhsChatType = (lhs.chatType == ChatType.InChatCharge
													|| lhs.chatType == ChatType.InChatUseTryTicket
													|| lhs.chatType == ChatType.ManInvite);
							boolean rhsChatType = (rhs.chatType == ChatType.InChatCharge
													|| rhs.chatType == ChatType.InChatUseTryTicket
													|| rhs.chatType == ChatType.ManInvite);
							if (lhsChatType != rhsChatType) {
								result = lhsChatType ? -1 : 1;
							}
						}
					}
					
					// 比较消息
					if (0 == result)
					{
						// 获取最后一条聊天消息
						LCMessageItem lMsgItem = lhs.GetLastTalkMsg();
						LCMessageItem rMsgItem = rhs.GetLastTalkMsg();
								
						if (null != lMsgItem && null != rMsgItem)
						{
							// 两个都不为空
							if (lMsgItem.createTime != rMsgItem.createTime)
							{
								result = (lMsgItem.createTime > rMsgItem.createTime ? -1 : 1);
							}
						}
						else if (null != lMsgItem || null != rMsgItem)
						{
							// 其中一个为空
							result = (null != lMsgItem ? -1 : 1);
						}
					}
					
					// 比较在聊状态
					if (0 == result) {
						result = CompareChatType(lhs.chatType, rhs.chatType);
					}
					
					// 比较名字
					if (0 == result) {
						if (!StringUtil.isEmpty(lhs.userName) && !StringUtil.isEmpty(rhs.userName))
						{
							// 两个名字都不为空
							result = lhs.userName.compareToIgnoreCase(rhs.userName);
						}
						else if (!StringUtil.isEmpty(lhs.userName) || !StringUtil.isEmpty(rhs.userName)) 
						{
							// 其中一个不为空
							result = (!StringUtil.isEmpty(lhs.userName) ? 1 : -1);
						}
					}
					
					// 比较ID
					if (0 == result) {
						result = lhs.userId.compareToIgnoreCase(rhs.userId);
					}
				}
				return result;
			}
		};
		return comparator;
	}
}
