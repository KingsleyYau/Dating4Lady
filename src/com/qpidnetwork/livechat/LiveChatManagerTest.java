package com.qpidnetwork.livechat;

import java.util.ArrayList;

import android.annotation.SuppressLint;

import com.qpidnetwork.framework.util.Log;
import com.qpidnetwork.livechat.jni.LiveChatClientListener.KickOfflineType;
import com.qpidnetwork.livechat.jni.LiveChatClientListener.LiveChatErrType;
import com.qpidnetwork.livechat.jni.LiveChatClientListener.TalkEmfNoticeType;
import com.qpidnetwork.livechat.jni.LiveChatTalkUserListItem;
import com.qpidnetwork.request.OnLCCheckSendPhotoCallback;
import com.qpidnetwork.request.OnLCCheckSendVideoCallback;
import com.qpidnetwork.request.OnQueryMyProfileCallback;
import com.qpidnetwork.request.OnTranslateTextCallback;
import com.qpidnetwork.request.RequestJniLivechat.PhotoSizeType;
import com.qpidnetwork.request.RequestJniLivechat.VideoPhotoType;
import com.qpidnetwork.request.RequestJniOther;
import com.qpidnetwork.request.item.EmotionConfigItem;
import com.qpidnetwork.request.item.LCPhotoListAlbumItem;
import com.qpidnetwork.request.item.LCPhotoListPhotoItem;
import com.qpidnetwork.request.item.LCVideoListGroupItem;
import com.qpidnetwork.request.item.LCVideoListVideoItem;
import com.qpidnetwork.request.item.MyProfileItem;

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
					, LiveChatManagerPhotoListener
					, LiveChatManagerVoiceListener
					, LiveChatManagerVideoListener
					, OnTranslateTextCallback
{
	private LiveChatManager	mMgr = null;
	private int mRecvMsgCount = 0;
	private EmotionConfigItem mEmotionConfig = null; 
	private int mPhotoIndex = 0; 
	private LCPhotoListPhotoItem[] mPhotos = null;
	private LCVideoListVideoItem[] mVideos = null;
	
	public LiveChatManagerTest()
	{
	}
	
	public void Init()
	{
		Log.d("LiveChatManagerTest", "Init()");
		mMgr = LiveChatManager.getInstance();
		
		mMgr.RegisterOtherListener(this);
		mMgr.RegisterMessageListener(this);
		mMgr.RegisterEmotionListener(this);
		mMgr.RegisterVoiceListener(this);
		mMgr.RegisterPhotoListener(this);
		mMgr.RegisterVideoListener(this);
	}

	@Override
	public void OnLogin(LiveChatErrType errType, String errmsg,
			boolean isAutoLogin) {
		
		Log.d("LiveChatManagerTest", "OnLogin() errType:%s, errmsg:%s, isAutoLogin:%b"
				, errType.name(), errmsg, isAutoLogin);
		if (LiveChatErrType.Success == errType) {
			Log.d("LiveChatManagerTest", "OnLogin() SearchOnlineMan()");
//			mMgr.SearchOnlineMan(0, 100);
			
//			Log.d("LiveChatManagerTest", "OnLogin() GetUsersInfo()");
//			String[] userIds = {"CM28171208", "CM42137154", "CM35734204"};
//			mMgr.GetUsersInfo(userIds);
			
			// 请求验证码
			mMgr.RefreshIdentifyCode();
			
			RequestJniOther.QueryMyProfile(new OnQueryMyProfileCallback() {
				
				@Override
				public void OnQueryMyProfileDetail(long requestId, boolean isSuccess,
						String errno, String errmsg, MyProfileItem item) {
					
					Log.d("LiveChatManagerTest", "OnQueryMyProfileDetail() isSuccess:%b, errmsg:%s"
							, isSuccess, errmsg);
				}
			});
		}
	}

	@Override
	public void OnLogout(LiveChatErrType errType, String errmsg,
			boolean isAutoLogin) {
		
		Log.d("LiveChatManagerTest", "OnLogout() errType:%s, errmsg:%s, isAutoLogin:%b"
				, errType.name(), errmsg, isAutoLogin);
	}

	@Override
	public void OnSearchOnlineMan(LiveChatErrType errType, String errmsg,
			String[] userIds) {
		
		String userIdsContext = "";
		if (errType == LiveChatErrType.Success) {
			for (int i = 0; i < userIds.length; i++) {
				if (!userIdsContext.isEmpty()) {
					userIdsContext += "\n";
				}
				userIdsContext += userIds[i];
			}
		}
		Log.d("LiveChatManagerTest", "OnSearchOnlineMan() errType:%s, errmsg:%s, userIds:%s"
				, errType.name(), errmsg, userIdsContext);
		
		mMgr.GetUsersInfo(userIds);
		
		// say hi to CM28171208
		for (int i = 0; i < userIds.length; i++) {
			if (userIds[i].compareTo("CM28171208") == 0) {
				mMgr.SendMessage(userIds[i], "hi");
				break;
			}
		}
	}

	@Override
	public void OnGetUsersInfo(LiveChatErrType errType, String errmsg,
			int seq, LiveChatTalkUserListItem[] list) {
		
		String userIdsContext = "";
		if (errType == LiveChatErrType.Success) {
			for (int i = 0; i < list.length; i++) {
				if (!userIdsContext.isEmpty()) {
					userIdsContext += "\n";
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
		
		String context = "";
		if (success) 
		{
			for (LCMessageItem item : userItem.msgList) {
				if (!context.isEmpty()) {
					context += "\n";
				}
				context += item.fromId;
				context += "*";
				context += item.sendType.name();
				context += "*";
				context += item.toId;
				context += "(" + item.createTime + ")";
				context += "--";
				context += item.msgType.name();
				context += ": ";
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
					context += item.getVideoItem().videoItem.videoId;
					if (item.getVideoItem().charge) {
						context += "(charge)";
					}
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
		
		Log.d("LiveChatManagerTest", "OnGetHistoryMessage() success:%b, errmsg:%s\n%s"
				, success, errmsg, context);
	}

	@Override
	public void OnSetStatus(LiveChatErrType errType, String errmsg) {
		
		Log.d("LiveChatManagerTest", "OnSetStatus() errType:%s, errmsg:%s"
				, errType, errmsg);
	}

	@Override
	public void OnUpdateStatus(LCUserItem userItem) {
		
		Log.d("LiveChatManagerTest", "OnUpdateStatus() userId:%s, userName:%s, status:%s"
				, userItem.userId, userItem.userName, userItem.statusType.name());
	}

	@Override
	public void OnChangeOnlineStatus(LCUserItem userItem) {
		
		Log.d("LiveChatManagerTest", "OnChangeOnlineStatus() userId:%s, userName:%s, status:%s"
				, userItem.userId, userItem.userName, userItem.statusType.name());		
	}

	@Override
	public void OnRecvKickOffline(KickOfflineType kickType) {
		
		Log.d("LiveChatManagerTest", "OnRecvKickOffline() kickType:%s"
				, kickType.name());	
	}

	@Override
	public void OnRecvTalkEvent(LCUserItem item) {
		
		Log.d("LiveChatManagerTest", "OnRecvTalkEvent() userId:%s, userName:%s, chatType:%s"
				, item.userId, item.userName, item.chatType.name());
	}

	@Override
	public void OnRecvEMFNotice(String fromId, TalkEmfNoticeType noticeType) {
		
		Log.d("LiveChatManagerTest", "OnRecvTalkEvent() fromId:%s, noticeType:%s"
				, fromId, noticeType.name());
	}
	
	// --------------------- text manager ---------------------
	@Override
	public void OnSendMessage(LiveChatErrType errType, String errmsg,
			LCMessageItem item) {
		
		Log.d("LiveChatManagerTest", "OnSendMessage() errType:%s, errmsg:%s, userId:%s, illegal:%b, message:%s"
				, errType.name(), errmsg, item.toId, item.getTextItem().illegal, item.getTextItem().message);
		
		String context = "";
		ArrayList<LCUserItem> userList = mMgr.GetWomanInviteUsers();
		for (LCUserItem userItem : userList)
		{
			if (!context.isEmpty())
			{
				context += "\n";
			}
			context += userItem.userName;
			context += "(" + userItem.userId + ")";
		}
		Log.d("LiveChatManagerTest", "OnSendMessage() GetWomanInviteUsers()\n%s", context);
		
		if (errType == LiveChatErrType.Success) 
		{
			if (mRecvMsgCount % 3 == 0
				&& null != mVideos)
			{
				int index = (mRecvMsgCount / 3) % mVideos.length;
				boolean result = mMgr.CheckSendVideo(item.toId, mVideos[index].videoId);
				
				Log.d("LiveChatManagerTest", "OnSendMessage() CheckSendVideoMessage() result:%b", result);
			}
		}
	}

	@Override
	public void OnRecvMessage(LCMessageItem item) {
		
		Log.d("LiveChatManagerTest", "OnRecvMessage() userId:%s, illegal:%b, message:%s"
				, item.fromId, item.getTextItem().illegal, item.getTextItem().message);
		
		mRecvMsgCount++;
		
		mMgr.SendMessage(item.fromId, item.getTextItem().message);
	}

	@Override
	public void OnRecvWarning(LCMessageItem item) {
		
		Log.d("LiveChatManagerTest", "OnRecvWarning() userId:%s, message:%s"
				, item.toId, item.getWarningItem().message);		
	}

	@Override
	public void OnRecvEditMsg(String fromId) {
		
		Log.d("LiveChatManagerTest", "OnRecvEditMsg() fromId:%s"
				, fromId);
	}

	@Override
	public void OnRecvSystemMsg(LCMessageItem item) {
		
		Log.d("LiveChatManagerTest", "OnRecvSystemMsg() message:%s"
				, item.getSystemItem().message);
	}

	@Override
	public void OnSendMessageListFail(LiveChatErrType errType,
			ArrayList<LCMessageItem> msgList) 
	{
		Log.d("LiveChatManagerTest", "OnSendMessageListFail() errType:%s msgList.size:%d"
				, errType.name(), msgList.size());
	}

	@Override
	public void OnReplyIdentifyCode(LiveChatErrType errType, String errmsg) 
	{
		
		Log.d("LiveChatManagerTest", "OnReplyIdentifyCode() errType:%s errmsg:%s"
				, errType.name(), errmsg);	
	}

	@Override
	public void OnRecvIdentifyCode(byte[] data) 
	{
		
		Log.d("LiveChatManagerTest", "OnRecvIdentifyCode() data.length:%d"
				, data.length);
		
		// 回复验证码测试
		mMgr.ReplyIdentifyCode("1234");
	}

	@Override
	public void OnContactListChange() 
	{
		
		String log = "";
		ArrayList<LCUserItem> list = mMgr.GetContactList();
		for (LCUserItem userItem : list)
		{
			if (!log.isEmpty()) {
				log += "\n";
			}
			log += userItem.userName;
			log += "(" + userItem.userId + ")";
			log += ":";
			log += userItem.statusType.name();
		}
		Log.d("LiveChatManagerTest", "OnContactListChange() list:%s", log);
	}
	
	// --------------------------- emotion ---------------------------

	@Override
	public void OnGetEmotionConfig(boolean success, String errno,
			String errmsg, EmotionConfigItem item) 
	{
		Log.d("LiveChatManagerTest", "OnGetEmotionConfig() success:%b, errno:%s, errmsg:%s"
				, success, errno, errmsg);
		
		mEmotionConfig = item;
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
		
		Log.d("LiveChatManagerTest", "OnRecvEmotion() userId:%s, emotionId:%s"
				, item.fromId, item.getEmotionItem().emotionId);
		
		mRecvMsgCount++;
		
		// 获取缩略图
		mMgr.GetEmotionImage(item.getEmotionItem().emotionId);
		// 获取播放图
		mMgr.GetEmotionPlayImage(item.getEmotionItem().emotionId);
		
		// 发送高级表情
		int emotion = mRecvMsgCount % mEmotionConfig.ladyEmotionList.length;
		mMgr.SendEmotion(item.fromId, mEmotionConfig.ladyEmotionList[emotion].fileName);
	}

	@Override
	public void OnGetEmotionImage(boolean success, LCEmotionItem emotionItem) 
	{
		
		Log.d("LiveChatManagerTest", "OnGetEmotionImage() success:%b, emotionId:%s, imagePath:%s"
				, success, emotionItem.emotionId, emotionItem.imagePath);
	}

	@Override
	public void OnGetEmotionPlayImage(boolean success, LCEmotionItem emotionItem) 
	{
		
		String path = "";
		for (String subPath : emotionItem.playBigImages) {
			if (!path.isEmpty()) {
				path += "\n";
			}
//			path += subPath;
			// 只打印最后一张图路径
			path = subPath;
		}
		
		Log.d("LiveChatManagerTest", "OnGetEmotionPlayImage() success:%b, emotionId:%s, playBigPath:%s\nsubPath:%s"
				, success, emotionItem.emotionId, emotionItem.playBigPath, path);
	}

	@Override
	public void OnSendVoice(LiveChatErrType errType, String errno,
			String errmsg, LCMessageItem item) {
		
		Log.d("LiveChatManagerTest", "OnSendVoice() errType:%s, errno:%s, errmsg:%s, path:%s"
				, errType.name(), errno, errmsg, item.getVoiceItem().filePath);
	}

	@Override
	public void OnGetVoice(LiveChatErrType errType, String errmsg,
			LCMessageItem item) {
		
		Log.d("LiveChatManagerTest", "OnGetVoice() errType:%s, errmsg:%s, path:%s"
				, errType.name(), errmsg, item.getVoiceItem().filePath);
		
		if (errType == LiveChatErrType.Success) {
			mMgr.SendVoice(item.fromId, item.getVoiceItem().filePath, item.getVoiceItem().fileType, item.getVoiceItem().timeLength);
		}
	}

	@Override
	public void OnRecvVoice(LCMessageItem item) {
		
		Log.d("LiveChatManagerTest", "OnRecvVoice() voiceId:%s"
				, item.getVoiceItem().voiceId);
		
		mMgr.GetVoice(item);
	}

	@Override
	public void OnSendPhoto(LiveChatErrType errType, String errno,
			String errmsg, LCMessageItem item) {
		
		Log.d("LiveChatManagerTest", "OnSendPhoto() errType:%s, errno:%s, errmsg:%s, photoId:%s"
				, errType.name(), errno, errmsg, item.getPhotoItem().photoId);
	}

	@Override
	public void OnGetPhoto(LiveChatErrType errType, String errno,
			String errmsg, LCMessageItem item) 
	{
		
		LCPhotoItem photoItem = item.getPhotoItem();
		Log.d("LiveChatManagerTest", "OnGetPhoto() errType:%s, errno:%s, errmsg:%s, photoId:%s, showPath%s, thumbPath:%s, srcPath:%s"
				, errType.name(), errno, errmsg, photoItem.photoId, photoItem.showSrcFilePath, photoItem.thumbSrcFilePath
				, photoItem.srcFilePath);
		
		// 发送图片
		if (errType == LiveChatErrType.Success
			&& mPhotos.length > 0) 
		{
			mMgr.CheckSendPhotoMessage(item.getUserItem().userId, mPhotos[mPhotoIndex].photoId);
			mPhotoIndex = (mPhotoIndex-1 >= 0 ? mPhotoIndex-1 : mPhotos.length-1);
		}
	}

	@Override
	public void OnRecvPhoto(LCMessageItem item) {
		
		LCPhotoItem photoItem = item.getPhotoItem();
		Log.d("LiveChatManagerTest", "OnRecvPhoto() photoId:%s, photoDesc:%s, showPath%s, thumbPath:%s, srcPath:%s"
				, photoItem.photoId, photoItem.photoDesc, photoItem.showSrcFilePath, photoItem.thumbSrcFilePath
				, photoItem.srcFilePath);
		
		// 获取图片
		mMgr.GetPhotoWithMessage(item.getUserItem().userId, item, PhotoSizeType.Large);
	}
	
	@Override
	public void OnRecvShowPhoto(LCUserItem userItem, String photoId, String photoDesc) {
		
		Log.d("LiveChatManagerTest", "OnRecvShowPhoto() userId:%s, userName:%s, photoId:%s, photoDesc:%s"
				, userItem.userId, userItem.userName, photoId, photoDesc);
	}
	
	@Override
	public void OnGetPhotoList(boolean isSuccess, String errno, String errmsg, LCPhotoListAlbumItem[] albums, LCPhotoListPhotoItem[] photos) {
		String strAlbum = "";
		if (null != albums) {
			for (int i = 0; i < albums.length; i++) 
			{
				if (!strAlbum.isEmpty()) {
					strAlbum += ", ";
				}
				strAlbum += albums[i].albumId;
			}
		}

		String strPhoto = "";
		if (null != photos) {
			for (int i = 0; i < photos.length; i++) 
			{
				if (!strPhoto.isEmpty()) {
					strPhoto += ", ";
				}
				strPhoto += photos[i].photoId;
				
				mMgr.GetSelfPhoto(photos[i].photoId, PhotoSizeType.Large);
				mMgr.GetSelfPhoto(photos[i].photoId, PhotoSizeType.Large);
			}
		}
		
		if (null != photos
			&& photos.length > 0) 
		{
			mPhotos = photos;
			mPhotoIndex = mPhotos.length - 1;
		}
		
		Log.d("LiveChatManagerTest", "OnGetPhotoList() isSuccess:%b, errno:%s, errmsg:%s, albums:%s, photos:%s"
				, isSuccess, errno, errmsg, strAlbum, strPhoto);
	}

	@Override
	public void OnGetSelfPhoto(LiveChatErrType errType, String errno,
			String errmsg, LCPhotoItem photoItem) 
	{
		
		Log.d("LiveChatManagerTest", "OnGetSelfPhoto() errType:%s, errno:%s, errmsg:%s, showFilePath:%s, srcFilePath:%s"
				, errType.name(), errno, errmsg, photoItem.showSrcFilePath, photoItem.srcFilePath);
	}

	@Override
	public void OnCheckSendPhoto(LiveChatErrType errType, OnLCCheckSendPhotoCallback.ResultType result, String errno,
			String errmsg, LCUserItem userItem, LCPhotoItem photoItem) 
	{
		Log.d("LiveChatManagerTest", "OnCheckSendPhoto() errType:%s, result:%s, errno:%s, errmsg:%s, userId:%s, userName:%s, photoId:%s"
				, errType.name(), result.name(), errno, errmsg, userItem.userId, userItem.userName, photoItem.photoId);
		
		if (errType == LiveChatErrType.Success) {
			// 发送图片
			mMgr.SendPhoto(userItem.userId, photoItem.photoId);
		}
	}

	@Override
	public void OnTranslateText(long requestId, boolean isSuccess, String text) {
		
		Log.d("LiveChatManagerTest", "OnTranslateText() requestId:%d, isSuccess:%b, text:%s"
				, requestId, isSuccess, text);
	}

	@Override
	public void OnTransStatusChange() {
		
		LCSelfInfo info = mMgr.GetSelfInfo();
		Log.d("LiveChatManagerTest", "OnTransStatusChange() needTrans:%b, transUserId:%s, transUserName:%s, bind:%b, status:%s"
				, info.mNeedTrans, info.mTransUserId, info.mTransUserName, info.mTransBind, info.mTransStatus.name());
	}

	@Override
	public void OnCheckSendVideo(LiveChatErrType errType, OnLCCheckSendVideoCallback.ResultType result, 
			String errno, String errmsg, LCUserItem userItem, LCVideoItem videoItem) 
	{
		Log.d("LiveChatManagerTest", "OnCheckSendVideo() errType:%s, result:%s, errno:%s, errmsg:%s, userId:%s, videoId:%s, videoDesc:%s"
				, errType.name(), result.name(), errno, errmsg, userItem.userId, videoItem.videoId, videoItem.videoDesc);
		
		if (errType == LiveChatErrType.Success)
		{
			LCMessageItem item = mMgr.SendVideo(userItem.userId, videoItem.videoId);
			
			Log.d("LiveChatManagerTest", "OnCheckSendVideo() SendVideo() " +
					", userId:%s, sendType:%s" +
					", videoId:%s, videoDesc:%s, videoPath:%s" +
					", sendId:%s, charge:%b"
					, item.toId, item.sendType.name()
					, item.getVideoItem().videoItem.videoId, item.getVideoItem().videoItem.videoDesc, item.getVideoItem().videoItem.videoPath
					, item.getVideoItem().sendId, item.getVideoItem().charge);
		}
	}

	@Override
	public void OnSendVideo(LiveChatErrType errType, String errno,
			String errmsg, LCMessageItem item) 
	{
		LCVideoMsgItem videoMsgItem = item.getVideoItem();
		LCVideoItem videoItem = item.getVideoItem().videoItem;
		Log.d("LiveChatManagerTest", "OnSendVideo() errType:%s, errno:%s, errmsg:%s" +
				", userId:%s, sendType:%s" +
				", videoId:%s, videoDesc:%s, videoPath:%s" +
				", sendId:%s, charge:%b"
				, errType.name(), errno, errmsg
				, item.toId, item.sendType.name()
				, videoItem.videoId, videoItem.videoDesc, videoItem.videoPath
				, videoMsgItem.sendId, videoMsgItem.charge);
		
		Log.d("LiveChatManagerTest", "OnSendVideo() GetVideo() result:%b"
				, mMgr.GetVideo(item));
		
		Log.d("LiveChatManagerTest", "OnSendVideo() GetVideoPhoto() Big result:%b"
				, mMgr.GetVideoPhoto(item.getVideoItem().videoItem, VideoPhotoType.Big));
		
		Log.d("LiveChatManagerTest", "OnSendVideo() GetVideoPhoto() Default result:%b"
				, mMgr.GetVideoPhoto(item.getVideoItem().videoItem, VideoPhotoType.Default));
	}

	@Override
	public void OnGetVideoPhoto(LiveChatErrType errType, String errno,
			String errmsg, VideoPhotoType photoType, LCVideoItem item) 
	{
		Log.d("LiveChatManagerTest", "OnGetVideoPhoto() errType:%s, errno:%s, errmsg:%s, photoType:%s" +
				", videoId:%s, videoDesc:%s, bigPhotoPath:%s, smallPhotoPath:%s"
				, errType.name(), errno, errmsg, photoType.name()
				, item.videoId, item.videoDesc, item.bigPhotoPath, item.smallPhotoPath);
	}

	@Override
	public void OnGetVideo(LiveChatErrType errType, String errno,
			String errmsg, LCMessageItem item) 
	{
		LCVideoMsgItem videoMsgItem = item.getVideoItem();
		LCVideoItem videoItem = item.getVideoItem().videoItem;
		Log.d("LiveChatManagerTest", "OnGetVideo() errType:%s, errno:%s, errmsg:%s" +
				", userId:%s, sendType:%s" +
				", videoId:%s, videoDesc:%s, videoPath:%s" +
				", sendId:%s, charge:%b"
				, errType.name(), errno, errmsg
				, item.toId, item.sendType.name()
				, videoItem.videoId, videoItem.videoDesc, videoItem.videoPath
				, videoMsgItem.sendId, videoMsgItem.charge);
	}

	@Override
	public void OnGetVideoList(boolean isSuccess, String errno, String errmsg,
			LCVideoListGroupItem[] groups, LCVideoListVideoItem[] videos) 
	{
		String groupsLog = "";
		for (LCVideoListGroupItem groupItem : groups)
		{
			if (!groupsLog.isEmpty()) {
				groupsLog += ",";
			}
			groupsLog += groupItem.groupId;
			groupsLog += ":";
			groupsLog += groupItem.groupTitle;
		}
		
		String videosLog = "";
		for (LCVideoListVideoItem videoItem : videos)
		{
			if (!videosLog.isEmpty()) {
				videosLog += ",";
			}
			videosLog += videoItem.videoId;
			videosLog += "(";
			videosLog += videoItem.groupId;
			videosLog += ")";
			videosLog += videoItem.title;
		}
		
		Log.d("LiveChatManagerTest", "OnGetVideoList() isSuccess:%b, errno:%s, errmsg:%s, groups:%s, videos:%s"
				, isSuccess, errno, errmsg, groupsLog, videosLog);
		
		if (isSuccess) {
			mVideos = videos;
		}
	}

	@Override
	public void OnRecvShowVideo(LCUserItem userItem, String videoId,
			String videoDesc) 
	{
		Log.d("LiveChatManagerTest", "OnRecvShowVideo() userId:%s, videoId:%s, videoDesc:%s"
				, userItem.userId, videoId, videoDesc);
	}
}
