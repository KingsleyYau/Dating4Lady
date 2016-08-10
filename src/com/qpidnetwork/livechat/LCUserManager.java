package com.qpidnetwork.livechat;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map.Entry;

import com.qpidnetwork.framework.util.Log;
import com.qpidnetwork.framework.util.StringUtil;
import com.qpidnetwork.livechat.LCUserItem.ChatType;
import com.qpidnetwork.livechat.jni.LiveChatTalkUserListItem;

/**
 * 在聊用户管理类
 * @author Samson Fan
 *
 */
public class LCUserManager {
	/**
	 * userId与UserItem的map
	 */
	private HashMap<String, LCUserItem> mUserMap;
	
	public LCUserManager() {
		mUserMap = new HashMap<String, LCUserItem>();
	}
	
	/**
	 * 用户是否已存在
	 * @param userId	用户ID
	 * @return
	 */
	public boolean isUserExists(String userId)
	{
		boolean result = false;
		synchronized (mUserMap) {
			LCUserItem item = mUserMap.get(userId);
			if (null != item) {
				result = true;
			}
		}
		return result;
	}
	
	/**
	 * 获取用户item
	 * @param userId
	 */
	public LCUserItem getUserItem(String userId) {
		if (StringUtil.isEmpty(userId)) {
			Log.d("livechat", String.format("%s::%s() userId:%s is null", "LCUserManager", "getUserItem", userId));
			return null;
		}
		
		LCUserItem item = null;
		synchronized(mUserMap)
		{
			item = mUserMap.get(userId);
			if (null == item) {
				item = new LCUserItem();
				item.userId = userId;
				if (!addUserItem(item)) {
					item = null;
				}
			}
		}
		return item;
	}
	
	/**
	 * 添加用户
	 * @param item
	 * @return
	 */
	public boolean addUserItem(LCUserItem item) {
		boolean result = false;
		if (!item.userId.isEmpty()) 
		{
			synchronized(mUserMap) 
			{
				LCUserItem old = mUserMap.get(item.userId);
				if (null == old) {
					mUserMap.put(item.userId, item);
					result = true;
				}
				else {
					Log.d("livechat", String.format("%s::%s() userId:%s, is exist", "LCUserManager", "addUserItem", item.userId));
				}
			}
		}
		return result;
	}
	
	/**
	 * 添加在聊用户
	 * @param userId	用户ID
	 * @param inviteId	邀请ID
	 * @return
	 */
	public boolean addInChatUser(String userId, String inviteId)
	{
		boolean result = false;
		LCUserItem userItem = getUserItem(userId);
		if (null != userItem) {
			userItem.inviteId = inviteId;
			userItem.setChatType(ChatType.InChatCharge);
		}
		return result;
	}
	
	/**
	 * 添加邀请用户
	 * @param userId	用户ID
	 * @param inviteId	邀请ID
	 * @return
	 */
	public boolean addInviteUser(String userId, String inviteId)
	{
		boolean result = false;
		LCUserItem userItem = getUserItem(userId);
		if (null != userItem) {
			userItem.inviteId = inviteId;
			userItem.setChatType(ChatType.ManInvite);
		}
		return result;
	}
	
	/**
	 * 更新用户信息
	 * @param item
	 * @return
	 */
	public boolean updateUser(LiveChatTalkUserListItem item)
	{
		boolean result = false;
		LCUserItem userItem = getUserItem(item.userId);
		if (null != userItem) {
			result = userItem.UpdateWithLiveChatTalkUserListItem(item);
		}
		return result;
	}
	
	/**
	 * 移除用户
	 * @param userId
	 * @return
	 */
	public boolean removeUserItem(String userId) {
		synchronized(mUserMap) 
		{
			LCUserItem item = mUserMap.remove(userId);
			if (null == item) {
				Log.d("livechat", String.format("%s::%s() userId:%s, is not exist", "LCUserManager", "removeUserItem", userId));
			}
			else {
				item.clearMsgList();
			}
		}
		return true;
	}
	
	/**
	 * 移除所有用户
	 * @return
	 */
	public boolean removeAllUserItem() {
		synchronized(mUserMap) 
		{
			// 清除所有用户的聊天记录
//			for (Entry<String, LCUserItem> entry: mUserMap.entrySet()) {
//				LCUserItem item = entry.getValue();
//				item.clearMsgList();
//			}
			mUserMap.clear();
		}
		return true;
	}
	
	/**
	 * 查找指定邀请ID的userItem
	 * @param inviteId	邀请ID
	 * @return
	 */
	public LCUserItem getUserItemWithInviteId(String inviteId) {
		LCUserItem userItem = null; 
		synchronized(mUserMap) {
			for (Entry<String, LCUserItem> entry: mUserMap.entrySet()) {
				LCUserItem item = entry.getValue();
				if (null != item.inviteId && item.inviteId.equals(inviteId)) {
					userItem = item;
					break;
				}
			}
		}
		return userItem;
	}
	
	/**
	 * 获取女士邀请的用户item
	 * @return
	 */
	public ArrayList<LCUserItem> getWomanInviteUsers() 
	{
		ArrayList<LCUserItem> list = new ArrayList<LCUserItem>(); 
		synchronized(mUserMap) {
			for (Entry<String, LCUserItem> entry: mUserMap.entrySet()) {
				LCUserItem item = entry.getValue();
				if (item.chatType == ChatType.WomanInvite) {
					list.add(item);
				}
			}
			
			// 排序
			try{
				Collections.sort(list, getWomanInviteComparator());
			}catch(Exception e){
				
			}
		}
		
		return list;
	}
	
	/**
	 * 获取有待发消息的用户列表
	 * @return
	 */
	public ArrayList<LCUserItem> getToSendUsers()
	{
		ArrayList<LCUserItem> list = new ArrayList<LCUserItem>();
		synchronized(mUserMap) {
			for (Entry<String, LCUserItem> entry: mUserMap.entrySet()) {
				LCUserItem item = entry.getValue();
				if (item.sendMsgList.size() > 0) 
				{
					list.add(item);
				}
			}
		}
		return list;
	}
	
	/**
	 * 女士邀请比较器
	 */
	static private Comparator<LCUserItem> getWomanInviteComparator()
	{
		Comparator<LCUserItem> comparator = new Comparator<LCUserItem>() {
			@Override
			public int compare(LCUserItem lhs, LCUserItem rhs) {
				// TODO Auto-generated method stub
				int result = 0;
				if (lhs != rhs) 
				{
					// 按消息排序（最后一条消息时间倒序）
					if (result == 0) 
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
					
					// 按名字排序
					if (result == 0) 
					{
						if (!lhs.userName.isEmpty() & !rhs.userName.isEmpty()) {
							// 两个都不为空
							result = lhs.userName.compareToIgnoreCase(rhs.userName);
						}
						else if (lhs.userName.isEmpty() != rhs.userName.isEmpty()) {
							// 其中一个为空
							result = !lhs.userName.isEmpty() ? -1 : 1; 
						}
					}
					
					// 按ID排序
					if (result == 0) 
					{
						// ID不可能为空
						result = lhs.userId.compareToIgnoreCase(rhs.userId);
					}
				}
				return result;
			}
		};
		return comparator;
	}
}
