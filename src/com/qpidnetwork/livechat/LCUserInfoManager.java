package com.qpidnetwork.livechat;

import java.util.HashMap;

import com.qpidnetwork.livechat.jni.LiveChatTalkUserListItem;

public class LCUserInfoManager {
	
	/**
	 * 存放男士资料
	 */
	private HashMap<String, LiveChatTalkUserListItem> mWomanInfoMap;
		
	public LCUserInfoManager(){
		mWomanInfoMap = new HashMap<String, LiveChatTalkUserListItem>();
	}
	
	/**
	 * 临时存储男士资料
	 * @param item
	 */
	private void saveLadyInfo(LiveChatTalkUserListItem item){
		synchronized (mWomanInfoMap) {
			mWomanInfoMap.put(item.userId, item);
		}
	}
	
	/**
	 * 临时存储男士资料列表
	 * @param item
	 */
	private void saveLadyInfos(LiveChatTalkUserListItem[] itemArray){
		synchronized (mWomanInfoMap) {
			for(LiveChatTalkUserListItem item : itemArray){
				mWomanInfoMap.put(item.userId, item);
			}
		}
	}
	
	/**
	 * 删除男士本地缓存
	 * @param userId
	 */
	private void removeLadyInfo(String userId){
		synchronized (mWomanInfoMap) {
			mWomanInfoMap.remove(userId);
		}
	}
	
	/**
	 * 获取男士信息
	 * @param userId
	 * @return
	 */
	public LiveChatTalkUserListItem getLadyInfo(String userId){
		LiveChatTalkUserListItem item = null;
		synchronized (mWomanInfoMap) {
			if(mWomanInfoMap.containsKey(userId)){
				item = mWomanInfoMap.get(userId);
			}
		}
		return item;
	}
	
	/**
	 * GetUserInfo 回调刷新
	 * @param item
	 */
	public void OnGetUserInfoUpdate(LiveChatTalkUserListItem item){
			saveLadyInfo(item);
	}
	
	/**
	 * GetUserInfo 回调刷新
	 * @param itemList
	 */
	public void OnGetUersInfoUpdate(LiveChatTalkUserListItem[] itemList){
		saveLadyInfos(itemList);
	}
	
	/**
	 * 上下线换端刷新
	 * @param userId
	 */
	public void OnUpdateStatus(String userId){
		removeLadyInfo(userId);
	}

}
