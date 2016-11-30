package com.qpidnetwork.livechat;

import android.content.Context;
import android.os.Handler;
import android.os.Message;

import com.qpidnetwork.ladydating.bean.RequestBaseResponse;
import com.qpidnetwork.ladydating.db.ChatHistoryDB;
import com.qpidnetwork.request.OnLCGetChatListCallback;
import com.qpidnetwork.request.RequestJniLivechat;
import com.qpidnetwork.request.item.LCChatListItem;

public class LCGetContactChatHistory {
	
	private static final int GET_CONTACTLIST_UPDATE = 1;
	private static final int GET_CHAT_HISTORY_CALLBACK =2;
	
	private Handler mHandler;
	private String[] mContactIds;
	private int mCurrentPosition = 0;
	private ChatHistoryDB mChatHistoryDB;
	
	public LCGetContactChatHistory(Context context){
		mChatHistoryDB = ChatHistoryDB.getInstance(context);
		mHandler = new Handler(){
			@Override
			public void handleMessage(Message msg) {
				// TODO Auto-generated method stub
				super.handleMessage(msg);
				switch (msg.what) {
				case GET_CONTACTLIST_UPDATE:{
					mContactIds = (String[])msg.obj;
					if(mContactIds != null
							&& mContactIds.length > 0){
						GetChatHistory(mContactIds[mCurrentPosition]);
					}
				}break;
				case GET_CHAT_HISTORY_CALLBACK:{
					mCurrentPosition++;
					if(mCurrentPosition < mContactIds.length){
						GetChatHistory(mContactIds[mCurrentPosition]);
					}
					RequestBaseResponse response = (RequestBaseResponse)msg.obj;
					if(response.isSuccess){
//						mChatHistoryDB
					}
				}break;
				default:
					break;
				}
			}
		};
	}
	
	/**
	 * LiveChat 获取联系人列表完成调用
	 * @param userIds
	 */
	public void OnGetContactListUpdate(String[] userIds){
		if(mContactIds == null
				|| mContactIds.length <= 0){
			Message msg = Message.obtain();
			msg.what = GET_CONTACTLIST_UPDATE;
			msg.obj = userIds;
			mHandler.sendMessage(msg);
		}
	}
	
	public void GetChatHistory(String userId){
		RequestJniLivechat.GetChatList(userId, new OnLCGetChatListCallback() {
			
			@Override
			public void OnLCGetChatList(boolean isSuccess, String errno, String errmsg,
					LCChatListItem[] list) {
				Message msg = Message.obtain();
				msg.what = GET_CHAT_HISTORY_CALLBACK;
				RequestBaseResponse response = new RequestBaseResponse(isSuccess, errno, errmsg, list);
				msg.obj = response;
				mHandler.sendMessage(msg);
			}
		});
	}
	
}
