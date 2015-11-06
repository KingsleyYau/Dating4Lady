package com.qpidnetwork.livechat;

import java.util.ArrayList;

import android.annotation.SuppressLint;

import com.qpidnetwork.framework.util.Log;
import com.qpidnetwork.livechat.jni.LiveChatClientListener.KickOfflineType;
import com.qpidnetwork.livechat.jni.LiveChatClientListener.LiveChatErrType;
import com.qpidnetwork.livechat.jni.LiveChatClientListener.TalkEmfNoticeType;
import com.qpidnetwork.livechat.jni.LiveChatTalkUserListItem;
import com.qpidnetwork.livechat.jni.LiveChatUserStatus;
import com.qpidnetwork.request.item.EmotionConfigItem;

/**
 * LiveChat管理器测试类
 * @author Samson Fan
 *
 */
@SuppressLint("HandlerLeak")
public class LiveChatManagerTest 
			implements LiveChatManagerOtherListener
					, LiveChatManagerMessageListener
					, LiveChatManagerEmotionListener
{
	private LiveChatManager	mMgr = null;
	private int mRecvMsgCount = 0;
	private EmotionConfigItem mEmotionConfig = null; 
	
	public LiveChatManagerTest()
	{
	}
	
	public void Init()
	{
		Log.d("LiveChatManagerTest", "Init()");
		mMgr = LiveChatManager.getInstance();
	}

	@Override
	public void OnLogin(LiveChatErrType errType, String errmsg,
			boolean isAutoLogin) {
		// TODO Auto-generated method stub
		Log.d("LiveChatManagerTest", "OnLogin() errType:%s, errmsg:%s, isAutoLogin:%b"
				, errType.name(), errmsg, isAutoLogin);
		if (LiveChatErrType.Success == errType) {
			Log.d("LiveChatManagerTest", "OnLogin() SearchOnlineMan()");
			mMgr.SearchOnlineMan(0, 100);
			
//			Log.d("LiveChatManagerTest", "OnLogin() GetUsersInfo()");
//			String[] userIds = {"CM28171208", "CM42137154", "CM35734204"};
//			mMgr.GetUsersInfo(userIds);
		}
	}

	@Override
	public void OnLogout(LiveChatErrType errType, String errmsg,
			boolean isAutoLogin) {
		// TODO Auto-generated method stub
		Log.d("LiveChatManagerTest", "OnLogout() errType:%s, errmsg:%s, isAutoLogin:%b"
				, errType.name(), errmsg, isAutoLogin);
	}

	@Override
	public void OnSearchOnlineMan(LiveChatErrType errType, String errmsg,
			String[] userIds) {
		// TODO Auto-generated method stub
		String userIdsContext = "";
		if (errType == LiveChatErrType.Success) {
			for (int i = 0; i < userIds.length; i++) {
				if (!userIdsContext.isEmpty()) {
					userIdsContext += ",";
				}
				userIdsContext += userIds[i];
			}
		}
		Log.d("LiveChatManagerTest", "OnSearchOnlineMan() errType:%s, errmsg:%s, userIds:%s"
				, errType.name(), errmsg, userIdsContext);
		
		mMgr.GetUsersInfo(userIds);
		
		// say hi
//		for (int i = 0; i < userIds.length; i++) {
//			mMgr.SendMessage(userIds[i], "hi");
//		}
	}

	@Override
	public void OnGetUsersInfo(LiveChatErrType errType, String errmsg,
			int seq, LiveChatTalkUserListItem[] list) {
		// TODO Auto-generated method stub
		String userIdsContext = "";
		if (errType == LiveChatErrType.Success) {
			for (int i = 0; i < list.length; i++) {
				if (!userIdsContext.isEmpty()) {
					userIdsContext += ",";
				}
				userIdsContext += list[i].userName;
				userIdsContext += "(" + list[i].userId + ")";
			}
		}
		
		Log.d("LiveChatManagerTest", "OnGetUsersInfo() errType:%s, errmsg:%s, list:%s"
				, errType.name(), errmsg, userIdsContext);
	}

	@Override
	public void OnGetHistoryMessage(boolean success, String errno,
			String errmsg, LCUserItem userItem) {
		// TODO Auto-generated method stub
		String context = "";
		if (success) 
		{
			for (LCMessageItem item : userItem.msgList) {
				if (!context.isEmpty()) {
					context += "\n";
				}
				context += item.fromId;
				context += "(" + item.createTime + ")";
				context += ":";
				switch(item.msgType) {
				case Text: {
					context += item.getTextItem().message;
				}break;
				case Emotion: {
					context += item.getEmotionItem().emotionId;
				}break;
				case Photo: {
					context += item.getPhotoItem().photoId;
				}break;
				case System: {
					context += item.getSystemItem().message;
				}break;
				case Video: {
//					context += item.get
				}break;
				case Voice: {
					context += item.getVoiceItem().voiceId;
				}break;
				case Warning: {
					context += item.getWarningItem().message;
				}break;
				default: {
					// 不处理
				}
				}
			}
		}
		
		Log.d("LiveChatManagerTest", "OnGetHistoryMessage() success:%b, errmsg:%s, context:%s"
				, success, errmsg, context);
	}

	@Override
	public void OnSetStatus(LiveChatErrType errType, String errmsg) {
		// TODO Auto-generated method stub
		Log.d("LiveChatManagerTest", "OnSetStatus() errType:%s, errmsg:%s"
				, errType, errmsg);
	}

	@Override
	public void OnGetUserStatus(LiveChatErrType errType, String errmsg,
			LiveChatUserStatus[] userList) {
		// TODO Auto-generated method stub
		String context = "";
		for (int i = 0; i < userList.length; i++) {
			if (!context.isEmpty()) {
				context += "\n";
			}
			
			context += userList[i].userId;
			context += ":";
			context += userList[i].statusType.name();
		}
		
		Log.d("LiveChatManagerTest", "OnGetUserStatus() errType:%s, errmsg:%s, context:%s"
				, errType, errmsg, context);
	}

	@Override
	public void OnUpdateStatus(LCUserItem userItem) {
		// TODO Auto-generated method stub
		Log.d("LiveChatManagerTest", "OnUpdateStatus() userId:%s, userName:%s, status:%s"
				, userItem.userId, userItem.userName, userItem.statusType.name());
	}

	@Override
	public void OnChangeOnlineStatus(LCUserItem userItem) {
		// TODO Auto-generated method stub
		Log.d("LiveChatManagerTest", "OnChangeOnlineStatus() userId:%s, userName:%s, status:%s"
				, userItem.userId, userItem.userName, userItem.statusType.name());		
	}

	@Override
	public void OnRecvKickOffline(KickOfflineType kickType) {
		// TODO Auto-generated method stub
		Log.d("LiveChatManagerTest", "OnRecvKickOffline() kickType:%s"
				, kickType.name());	
	}

	@Override
	public void OnRecvTalkEvent(LCUserItem item) {
		// TODO Auto-generated method stub
		Log.d("LiveChatManagerTest", "OnRecvTalkEvent() userId:%s, userName:%s, chatType:%s"
				, item.userId, item.userName, item.chatType.name());
	}

	@Override
	public void OnRecvEMFNotice(String fromId, TalkEmfNoticeType noticeType) {
		// TODO Auto-generated method stub
		Log.d("LiveChatManagerTest", "OnRecvTalkEvent() fromId:%s, noticeType:%s"
				, fromId, noticeType.name());
	}
	
	// --------------------- text manager ---------------------
	@Override
	public void OnSendMessage(LiveChatErrType errType, String errmsg,
			LCMessageItem item) {
		// TODO Auto-generated method stub
		Log.d("LiveChatManagerTest", "OnSendMessage() errType:%s, errmsg:%s, userId:%s, illegal:%b, message:%s"
				, errType.name(), errmsg, item.toId, item.getTextItem().illegal, item.getTextItem().message);
	}

	@Override
	public void OnRecvMessage(LCMessageItem item) {
		// TODO Auto-generated method stub
		Log.d("LiveChatManagerTest", "OnRecvMessage() userId:%s, illegal:%b, message:%s"
				, item.toId, item.getTextItem().illegal, item.getTextItem().message);
		
//		String[] userIds = {item.getUserItem().userId};
//		mMgr.GetUsersInfo(userIds);
		String[] userIds1 = {"CM28171208", "CM42137154", "CM35734204"};
		mMgr.GetUsersInfo(userIds1);
		
		String[] userIds2 = {item.fromId};
		mMgr.GetUsersInfo(userIds2);
		
		mMgr.SendMessage(item.fromId, item.getTextItem().message);
		
		mRecvMsgCount++;
	}

	@Override
	public void OnRecvWarning(LCMessageItem item) {
		// TODO Auto-generated method stub
		Log.d("LiveChatManagerTest", "OnRecvWarning() userId:%s, message:%s"
				, item.toId, item.getWarningItem().message);		
	}

	@Override
	public void OnRecvEditMsg(String fromId) {
		// TODO Auto-generated method stub
		Log.d("LiveChatManagerTest", "OnRecvEditMsg() fromId:%s"
				, fromId);
	}

	@Override
	public void OnRecvSystemMsg(LCMessageItem item) {
		// TODO Auto-generated method stub
		Log.d("LiveChatManagerTest", "OnRecvSystemMsg() message:%s"
				, item.getSystemItem().message);
	}

	@Override
	public void OnSendMessageListFail(LiveChatErrType errType,
			ArrayList<LCMessageItem> msgList) {
		// TODO Auto-generated method stub
		Log.d("LiveChatManagerTest", "OnSendMessageListFail() errType:%s msgList.size:%d"
				, errType.name(), msgList.size());		
	}

	@Override
	public void OnReplyIdentifyCode(LiveChatErrType errType, String errmsg) 
	{
		// TODO Auto-generated method stub
		Log.d("LiveChatManagerTest", "OnReplyIdentifyCode() errType:%s errmsg:%s"
				, errType.name(), errmsg);	
	}

	@Override
	public void OnRecvIdentifyCode(String filePath) 
	{
		// TODO Auto-generated method stub
		Log.d("LiveChatManagerTest", "OnRecvIdentifyCode() filePath:%s"
				, filePath);
	}

	@Override
	public void OnContactStatusChange() 
	{
		// TODO Auto-generated method stub
		String log = "";
		ArrayList<LCUserItem> list = mMgr.GetContactUsers();
		for (LCUserItem userItem : list)
		{
			if (!log.isEmpty()) {
				log += ",";
			}
			log += userItem.userName;
			log += "(" + userItem.userId + ")";
			log += ":";
			log += userItem.statusType.name();
		}
		Log.d("LiveChatManagerTest", "OnContactStatusChange() list:%s", log);
	}
	
	// --------------------------- emotion ---------------------------

	@Override
	public void OnGetEmotionConfig(boolean success, String errno,
			String errmsg, EmotionConfigItem item) 
	{
		Log.d("LiveChatManagerTest", "OnGetEmotionConfig() success:%b, errno:%s, errmsg:%s"
				, success, errno, errmsg);
		
		mEmotionConfig = item;
		
		int emotion = mRecvMsgCount % mEmotionConfig.manEmotionList.length;
		mMgr.SendEmotion("cm28171208", mEmotionConfig.manEmotionList[emotion].fileName);
	}

	@Override
	public void OnSendEmotion(LiveChatErrType errType, String errmsg,
			LCMessageItem item) 
	{
		Log.d("LiveChatManagerTest", "OnSendEmotion() errType:%s, errmsg:%s, userId:%s, emotionId:%s"
				, errType.name(), errmsg, item.toId, item.getEmotionItem().emotionId);
		
		// 获取缩略图
		mMgr.GetEmotionImage(item.getEmotionItem().emotionId);
		// 获取播放图
		mMgr.GetEmotionPlayImage(item.getEmotionItem().emotionId);
	}

	@Override
	public void OnRecvEmotion(LCMessageItem item) 
	{
		// TODO Auto-generated method stub
		Log.d("LiveChatManagerTest", "OnRecvEmotion() userId:%s, emotionId:%s"
				, item.fromId, item.getEmotionItem().emotionId);
		
		mRecvMsgCount++;
		
		// 获取缩略图
		mMgr.GetEmotionImage(item.getEmotionItem().emotionId);
		// 获取播放图
		mMgr.GetEmotionPlayImage(item.getEmotionItem().emotionId);
		
		// 发送高级表情
		int emotion = mRecvMsgCount % mEmotionConfig.manEmotionList.length;
		mMgr.SendEmotion(item.fromId, mEmotionConfig.manEmotionList[emotion].fileName);
	}

	@Override
	public void OnGetEmotionImage(boolean success, LCEmotionItem emotionItem) 
	{
		// TODO Auto-generated method stub
		Log.d("LiveChatManagerTest", "OnGetEmotionImage() success:%b, emotionId:%s, imagePath:%s"
				, success, emotionItem.emotionId, emotionItem.imagePath);
	}

	@Override
	public void OnGetEmotionPlayImage(boolean success, LCEmotionItem emotionItem) 
	{
		// TODO Auto-generated method stub
		String path = "";
		for (String subPath : emotionItem.playBigImages) {
			if (!path.isEmpty()) {
				path += "\n";
			}
			path += subPath;
		}
		
		Log.d("LiveChatManagerTest", "OnGetEmotionPlayImage() success:%b, emotionId:%s, playBigPath:%s\nsubPath:%s"
				, success, emotionItem.emotionId, emotionItem.playBigPath, path);
	}

//	@Override
//	public void OnGetFeeRecentContactList(LiveChatErrType errType,
//			String errmsg, String[] userIds) {
//		// TODO Auto-generated method stub
//		String userIdsContext = "";
//		if (errType == LiveChatErrType.Success) {
//			for (int i = 0; i < userIds.length; i++) {
//				if (!userIdsContext.isEmpty()) {
//					userIdsContext += ",";
//				}
//				userIdsContext += userIds[i];
//			}
//		}
//		
//		Log.d("LiveChatManagerTest", "OnGetFeeRecentContactList() errType:%s, errmsg:%s, userIds: %s"
//				, errType.name(), errmsg, userIdsContext);
//	}
//
//	@Override
//	public void OnGetLadyChatInfo(LiveChatErrType errType, String errmsg,
//			String[] chattingUserIds, String[] chattingInviteIds,
//			String[] missingUserIds, String[] missingInviteIds) {
//		// TODO Auto-generated method stub
//		String chattingUser = "";
//		String chattingInvite = "";
//		String missingUser = "";
//		String missingInvite = "";
//		if (errType == LiveChatErrType.Success) {
//			// chattingUserIds
//			for (int i = 0; i < chattingUserIds.length; i++) {
//				if (!chattingUser.isEmpty()) {
//					chattingUser += ",";
//				}
//				chattingUser += chattingUserIds[i];
//			}
//			
//			// chattingInviteIds
//			for (int i = 0; i < chattingInviteIds.length; i++) {
//				if (!chattingInvite.isEmpty()) {
//					chattingInvite += ",";
//				}
//				chattingInvite += chattingInviteIds[i];
//			}
//			
//			// missingUserIds
//			for (int i = 0; i < missingUserIds.length; i++) {
//				if (!missingUser.isEmpty()) {
//					missingUser += ",";
//				}
//				missingUser += missingUserIds[i];
//			}
//			
//			// missingInviteIds
//			for (int i = 0; i < missingInviteIds.length; i++) {
//				if (!missingInvite.isEmpty()) {
//					missingInvite += ",";
//				}
//				missingInvite += missingInviteIds[i];
//			}
//		}
//		
//		Log.d("LiveChatManagerTest", "OnGetLadyChatInfo() errType:%s, errmsg:%s, chattingUserIds:%s, chattingInviteIds:%s, missingUserIds:%s, missingInviteIds:%s"
//				, errType.name(), errmsg, chattingUser, chattingInvite, missingUser, missingInvite);
//	}
	
	
}
