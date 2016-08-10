package com.qpidnetwork.livechat;

import java.util.HashMap;

import android.annotation.SuppressLint;

/**
 * 获取聊天记录管理器
 * @author Samson Fan
 *
 */
public class LCGetHistoryManager {
	/**
	 * 获取聊天记录列表(inviteId, requestId)
	 */
	private HashMap<String, Long> mInviteMap = null;
	
	@SuppressLint("UseSparseArrays")
	public LCGetHistoryManager() 
	{
		mInviteMap = new HashMap<String, Long>();
	}
	
	/**
	 * 是否正在获取指定用户的聊天记录
	 * @param inviteId	邀请ID
	 * @return
	 */
	public boolean IsGetingHistoryMsgWithInviteId(String inviteId)
	{
		boolean result = false;
		synchronized(mInviteMap)
		{
			result = (null != mInviteMap.get(inviteId));
		}
		return result;
	}
	
	/**
	 * 设置正在获取指定用户的聊天记录
	 * @param inviteId	邀请ID
	 * @param requestId	请求ID
	 * @return
	 */
	public boolean SetGetingHistoryMsg(String inviteId, Long requestId)
	{
		boolean result = false;
		synchronized(mInviteMap)
		{
			if (null == mInviteMap.get(inviteId))
			{
				mInviteMap.put(inviteId, requestId);
				result = true;
			}
		}
		return result;
	}
	
	/**
	 * 设置获取聊天记录完成
	 * @param requestId	请求ID
	 * @return
	 */
	public boolean SetGetHistoryMsgFinish(String inviteId)
	{
		boolean result = false;
		synchronized(mInviteMap)
		{
			Long requestId = mInviteMap.get(inviteId);
			if (requestId != null) {
				
				mInviteMap.remove(inviteId);
				result = true;
			}
		}
		return result;		
	}
}
