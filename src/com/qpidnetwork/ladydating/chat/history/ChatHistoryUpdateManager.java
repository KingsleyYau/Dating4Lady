package com.qpidnetwork.ladydating.chat.history;

import java.util.HashMap;

import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;

import com.qpidnetwork.ladydating.db.ChatHistoryDB;
import com.qpidnetwork.livechat.LCUserItem;
import com.qpidnetwork.request.OnLCGetChatListCallback;
import com.qpidnetwork.request.RequestJniLivechat;
import com.qpidnetwork.request.item.LCChatListItem;

public class ChatHistoryUpdateManager {
	
	public static final String ACTION_UPDATE_CHATHISTORY_FINISH = "action.update.chathistory";
	private static final int UPDATE_CHAT_HISTORY_NOTIFY = 1;

	private Context mContext;
	private ChatHistoryDB mChatHistoryDB;
	private static ChatHistoryUpdateManager mChatHistoryUpdateManager;
	
	//防止断线重连重复刷新
	private boolean isInited = false;//是否已更新过；
	private HashMap<String, Boolean> mWomanList; //已更新过女士列表；
	
	//存储联系人列表
	private String[] mContactList;
	private int mCurrentPostion = 0;
	
	//定时器
	private Handler mHandler;
	private HandlerThread mHandlerThread;
	
	private int synServerTimeStamp = 0;
	
	private ChatHistoryUpdateManager(Context context){
		mContext = context;
		mChatHistoryDB = ChatHistoryDB.getInstance(mContext);
		mWomanList = new HashMap<String, Boolean>();
	}
	
	public static ChatHistoryUpdateManager getInstance(Context context){
		if(mChatHistoryUpdateManager == null){
			mChatHistoryUpdateManager = new ChatHistoryUpdateManager(context);
		}
		return mChatHistoryUpdateManager;
	}
	
	/**
	 * 获取联系人结束，启动逐个获取联系人历史消息
	 * @param usrIds
	 */
	public void startUpdateContactHistory(String womanId, String[] usrIds){
		if(!isInited && !mWomanList.containsKey(womanId)){
			isInited = true;
			mWomanList.put(womanId, true);
			mContactList = usrIds;
			mCurrentPostion = 0;
			if(mContactList != null && mContactList.length > 0){
				startTimerUpdate();
			}
		}
	}
	
	/**
	 * 启动定时更新联系人历史消息
	 */
	private void startTimerUpdate(){
		mHandlerThread = new HandlerThread("UpdateChatHistory");
		mHandlerThread.start();
		mHandler = new Handler(mHandlerThread.getLooper()){
			@Override
			public void handleMessage(Message msg) {
				// TODO Auto-generated method stub
				super.handleMessage(msg);
				switch (msg.what) {
				case UPDATE_CHAT_HISTORY_NOTIFY:{
					if(mContactList != null && mCurrentPostion < mContactList.length){
						String userID = mContactList[mCurrentPostion];
						mCurrentPostion++;
						GetChatHistoryById(userID, new OnLCGetChatListCallback() {
							
							@Override
							public void OnLCGetChatList(boolean isSuccess, String errno, String errmsg,
									LCChatListItem[] list) {
								mHandler.sendEmptyMessage(UPDATE_CHAT_HISTORY_NOTIFY);
							}
						});
					}else{
						//获取结束，停止定时器线程
						mHandlerThread.getLooper().quit();
						if(mContactList != null){
							Intent intent = new Intent();
							intent.setAction(ACTION_UPDATE_CHATHISTORY_FINISH);
							mContext.sendBroadcast(intent);
						}
					}
				}break;

				default:
					break;
				}
			}
		};
		mHandler.sendEmptyMessage(UPDATE_CHAT_HISTORY_NOTIFY);
	}
	
	/**
	 * 获取聊天列表
	 * @param manId
	 * @param callback
	 */
	public void GetChatHistoryById(String manId, final OnLCGetChatListCallback callback){
		RequestJniLivechat.GetChatList(manId, new OnLCGetChatListCallback() {
			
			@Override
			public void OnLCGetChatList(boolean isSuccess, String errno, String errmsg,
					LCChatListItem[] list) {
				if(isSuccess && list != null){
					for(LCChatListItem item : list){
						mChatHistoryDB.InsertChatHistory(item);
					}
				}
				callback.OnLCGetChatList(isSuccess, errno, errmsg, list);
			}
		});
	}
	
	/**
	 * 清除超过1个月的历史记录（仅保存最近一个月有效记录）
	 * @param dbTime
	 */
	public void clearInvalidRecord(int dbTime){
		mChatHistoryDB.clearInvalidRecord(dbTime);
		if(dbTime > 0){
			synServerTimeStamp = dbTime - (int)(System.currentTimeMillis()/1000);
		}
		Intent intent = new Intent();
		intent.setAction(ACTION_UPDATE_CHATHISTORY_FINISH);
		mContext.sendBroadcast(intent);
	}
	
	/**
	 * 添加本地聊天记录到历史，并标记为已读
	 * @param userItem
	 * @param inviteId
	 */
	public void addLocalChatToHistory(LCUserItem userItem, String inviteId){
		int startime = (int)(System.currentTimeMillis()/1000) + synServerTimeStamp;
		mChatHistoryDB.AddLocalToChatHistory(userItem, inviteId, startime);
	}
	
	/**
	 * 手动断开链接重置状态
	 */
	public void ResetStatus(){
		isInited = false;
		if(mWomanList != null){
			mWomanList.clear();
		}
		mContactList = null;
		mCurrentPostion = 0;
	}
}
