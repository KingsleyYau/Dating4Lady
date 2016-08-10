package com.qpidnetwork.livechat;

import java.util.ArrayList;

import com.qpidnetwork.livechat.jni.LiveChatTalkUserListItem;

/**
 * 黑名单管理器
 * @author Samson Fan
 *
 */
public class LCBlockManager {
	/**
	 * 黑名单列表
	 */
	private ArrayList<LiveChatTalkUserListItem> mBlockList;
	
	public LCBlockManager() 
	{
		mBlockList = new ArrayList<LiveChatTalkUserListItem>();
	}
	
	/**
	 * 更新黑名单列表
	 * @param array	黑名单列表
	 */
	public synchronized void UpdateWithBlockList(LiveChatTalkUserListItem[] array)
	{
		mBlockList.clear();
		for (int i = 0; i < array.length; i++)
		{
			mBlockList.add(array[i]);
		}
	}
	
	/**
	 * 用户是否存在于黑名单
	 * @param userId	用户ID
	 * @return
	 */
	public synchronized boolean IsExist(String userId)
	{
		boolean result = false;
		// 判断黑名单
		for (LiveChatTalkUserListItem item : mBlockList)
		{
			if (item.userId.compareTo(userId) == 0) {
				result = true;
				break;
			}
		}

		return result;
	}
	
	/**
	 * 清空黑名单
	 */
	public synchronized void Clear()
	{
		mBlockList.clear();
	}
}
