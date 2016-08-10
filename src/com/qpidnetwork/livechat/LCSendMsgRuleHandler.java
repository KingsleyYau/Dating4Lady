package com.qpidnetwork.livechat;

import java.io.Serializable;
import java.util.ArrayList;

import com.qpidnetwork.livechat.LCMessageItem.SendType;


/**
 * 发送消息规则处理器（每个User都有自己的）
 * @author Samson Fan
 *
 */
public class LCSendMsgRuleHandler implements Serializable
{
	private static final long serialVersionUID = -6766867578349984024L;
	// 用户item
	private LCUserItem mUserItem = null;
	
	// 文本邀请消息列表
	private ArrayList<LCMessageItem> mInviteTextMsgList = new ArrayList<LCMessageItem>();
	// 文本邀请消息最大数量
	private static final int TEXTMSG_LIMIT = 3;
	// 文本邀请消息最大时间间隔(秒)
	private static final int TEXTMSG_TIME = 90;
	
	// 高级表情消息列表
	private ArrayList<LCMessageItem> mEmotionMsgList = new ArrayList<LCMessageItem>();
	// 高级表情消息最大数量
	private static final int EMOTIONMSG_LIMIT = 1;
	// 高级表情消息最大时间间隔(秒)
	private static final int EMOTIONMSG_TIME = 60;
	
	// 语音消息列表
	private ArrayList<LCMessageItem> mVoiceMsgList = new ArrayList<LCMessageItem>();
	// 语音消息最大数量
	private static final int VOICEMSG_LIMIT = 1;
	// 语音消息最大时间间隔(秒)
	private static final int VOICEMSG_TIME = 60;
	
	// 图片(私密照)消息列表
	private ArrayList<LCMessageItem> mPhotoMsgList = new ArrayList<LCMessageItem>();
	// 图片(私密照)消息最大数量
	private static final int PHOTOMSG_LIMIT = 1;
	// 图片(私密照)消息最大时间间隔(秒)
	private static final int PHOTOMSG_TIME = 60;
	
	// 视频(微视频)消息列表
	private ArrayList<LCMessageItem> mVideoMsgList = new ArrayList<LCMessageItem>();
	// 视频(微视频)消息最大数量
	private static final int VIDEOMSG_LIMIT = 1;
	// 视频(微视频)消息最大时间间隔(秒)
	private static final int VIDEOMSG_TIME = 60;
	
	public LCSendMsgRuleHandler(LCUserItem userItem) 
	{
		mUserItem = userItem;
	}
	
	/**
	 * 插入消息处理
	 * @param item	消息item	
	 */
	public void InsertMessage(LCMessageItem item)
	{
		switch (item.msgType)
		{
		case Text:
			TextMsgInsert(item);
			break;
		case Emotion:
			EmotionMsgInsert(item);
			break;
		case Voice:
			VoiceMsgInsert(item);
			break;
		case Photo:
			PhotoMsgInsert(item);
			break;
		case Video:
			VideoMsgInsert(item);
		default:
			break;
		}
	}
	
	/**
	 * 判断是否可立即发送消息
	 * @param msgType
	 * @return
	 */
	public boolean CanSendMessage(LCMessageItem.MessageType msgType)
	{
		boolean result = false;
		switch (msgType)
		{
		case Text:
			result = TextMsgCanSend();
			break;
		case Emotion:
			result = EmotionMsgCanSend();
			break;
		case Voice:
			result = VoiceMsgCanSend();
			break;
		case Photo:
			result = PhotoMsgCanSend();
			break;
		case Video:
			result = VideoMsgCanSend();
		default:
			break;
		}
		return result;
	}
	
	/**
	 * 聊天状态改变
	 * @param chatType	聊天状态
	 */
	public void ChangeChatType(LCUserItem.ChatType chatType)
	{
		TextMsgChangeChatTypeProc(chatType);
		EmotionMsgChangeChatTypeProc(chatType);
		VoiceMsgChangeChatTypeProc(chatType);
		PhotoMsgChangeChatTypeProc(chatType);
		VideoMsgChangeChatTypeProc(chatType);
	}
	
	/**
	 * 清空规则数据 (LiveChat断线时调用)
	 */
	public void Clear()
	{
		TextMsgClear();
		EmotionMsgClear();
		VoiceMsgClear();
		PhotoMsgClear();
		VideoMsgClear();
	}
	
	// ------------------- 文本消息 -------------------
	/**
	 * 插入文本消息处理
	 * @param item	消息item
	 */
	private void TextMsgInsert(LCMessageItem item)
	{
		if (null != item
			&& item.sendType == SendType.Send)
		{
			// 判断是邀请消息
			if (mUserItem.chatType == LCUserItem.ChatType.WomanInvite)
			{
				synchronized(mInviteTextMsgList)
				{
					// 添加到邀请消息列表
					mInviteTextMsgList.add(item);
					// 超过3条，把最前面的去掉
					if (mInviteTextMsgList.size() > TEXTMSG_LIMIT)
					{
						mInviteTextMsgList.remove(0);
					}
				}
			}
		}
	}
	
	/**
	 * 判断是否可立即发送文本消息
	 * @return
	 */
	private boolean TextMsgCanSend()
	{
		boolean result = false;
		synchronized(mInviteTextMsgList)
		{
			if (mInviteTextMsgList.size() < TEXTMSG_LIMIT) {
				result = true;
			}
			else {
				// 取邀请列表最前面的消息
				LCMessageItem item = mInviteTextMsgList.get(0);
				if (null != item)
				{
					// 判断是否大于时间间隔
					int createTime = LCMessageItem.GetCreateTime();
					result = createTime > (item.createTime + TEXTMSG_TIME);
				}
			}
		}
		return result;
	}
	
	/**
	 * 聊天状态改变(文本消息处理)
	 * @param chatType	聊天状态
	 */
	private void TextMsgChangeChatTypeProc(LCUserItem.ChatType chatType)
	{
		if (chatType != LCUserItem.ChatType.WomanInvite) 
		{
			synchronized(mInviteTextMsgList)
			{
				// 清空数据
				mInviteTextMsgList.clear();
			}
		}
	}
	
	/**
	 * 清空文本消息规则数据
	 */
	private void TextMsgClear()
	{
		synchronized(mInviteTextMsgList)
		{
			mInviteTextMsgList.clear();
		}
	}
	
	// ------------------- 高级表情消息 -------------------
	/**
	 * 插入高级表情消息处理
	 * @param item	消息item
	 */
	private void EmotionMsgInsert(LCMessageItem item)
	{
		if (null != item 
			&& item.sendType == LCMessageItem.SendType.Send)
		{
			synchronized(mEmotionMsgList)
			{
				// 添加到高级表情消息列表
				mEmotionMsgList.add(item);
				// 超过1条，把最前面的去掉
				if (mEmotionMsgList.size() > EMOTIONMSG_LIMIT)
				{
					mEmotionMsgList.remove(0);
				}
			}
		}
	}
	
	/**
	 * 判断是否可立即发送高级表情消息
	 * @return
	 */
	private boolean EmotionMsgCanSend()
	{
		boolean result = false;
		// 女士可以发高级表情消息作为邀请
		synchronized(mEmotionMsgList)
		{
			if (mEmotionMsgList.size() < EMOTIONMSG_LIMIT) {
				result = true;
			}
			else {
				// 取列表最前面的消息
				LCMessageItem item = mEmotionMsgList.get(0);
				if (null != item)
				{
					// 判断是否大于最大时间间隔
					int createTime = LCMessageItem.GetCreateTime();
					result = createTime > (item.createTime + EMOTIONMSG_TIME);
				}
			}
		}
		return result;
	}
	
	/**
	 * 聊天状态改变(高级表情处理)
	 * @param chatType	聊天状态
	 */
	private void EmotionMsgChangeChatTypeProc(LCUserItem.ChatType chatType)
	{
		if (chatType == LCUserItem.ChatType.WomanInvite
			|| chatType == LCUserItem.ChatType.Other) 
		{
			synchronized(mEmotionMsgList)
			{
				// 清空数据
				mEmotionMsgList.clear();
			}
		}
	}
	
	/**
	 * 清空高级表情消息规则数据
	 */
	private void EmotionMsgClear()
	{
		synchronized(mEmotionMsgList)
		{
			mEmotionMsgList.clear();
		}
	}
	
	// ------------------- 语音消息 -------------------
	/**
	 * 插入语音消息处理
	 * @param item	消息item
	 */
	private void VoiceMsgInsert(LCMessageItem item)
	{
		if (null != item)
		{
			if (item.sendType == LCMessageItem.SendType.Send)
			{
				// 插入消息到列表
				synchronized(mVoiceMsgList)
				{
					// 添加到语音消息列表
					mVoiceMsgList.add(item);
					// 超过1条，把最前面的去掉
					if (mVoiceMsgList.size() > VOICEMSG_LIMIT)
					{
						mVoiceMsgList.remove(0);
					}
				}
			}
			else 
			{
				// 清除列表消息（收到语音消息就可立即发送）
				synchronized(mVoiceMsgList)
				{
					mVoiceMsgList.clear();
				}
			}
		}
	}
	
	/**
	 * 判断是否可立即发送语音消息
	 * @return
	 */
	private boolean VoiceMsgCanSend()
	{
		boolean result = false;
		// 女士邀请状态不能发语音消息
		if (mUserItem.chatType != LCUserItem.ChatType.WomanInvite)
		{
			synchronized(mVoiceMsgList)
			{
				if (mVoiceMsgList.size() < VOICEMSG_LIMIT) {
					result = true;
				}
				else {
					// 取列表最前面的消息
					LCMessageItem item = mVoiceMsgList.get(0);
					if (null != item)
					{
						// 判断是否大于最大时间间隔
						int createTime = LCMessageItem.GetCreateTime();
						result = createTime > (item.createTime + VOICEMSG_TIME);
					}
				}
			}
		}
		return result;
	}
	
	/**
	 * 聊天状态改变(语音消息处理)
	 * @param chatType	聊天状态
	 */
	private void VoiceMsgChangeChatTypeProc(LCUserItem.ChatType chatType)
	{
		if (chatType == LCUserItem.ChatType.WomanInvite
			|| chatType == LCUserItem.ChatType.Other) 
		{
			synchronized(mVoiceMsgList)
			{
				// 清空数据
				mVoiceMsgList.clear();
			}
		}
	}
	
	/**
	 * 清空语音消息规则数据
	 */
	private void VoiceMsgClear()
	{
		synchronized(mVoiceMsgList)
		{
			mVoiceMsgList.clear();
		}
	}
	
	// ------------------- 图片(私密照)消息 -------------------
	/**
	 * 插入图片消息处理
	 * @param item	消息item
	 */
	private void PhotoMsgInsert(LCMessageItem item)
	{
		if (null != item)
		{
			if (item.sendType == LCMessageItem.SendType.Send)
			{
				// 插入消息到列表
				synchronized(mPhotoMsgList)
				{
					// 添加到图片消息列表
					mPhotoMsgList.add(item);
					// 超过1条，把最前面的去掉
					if (mPhotoMsgList.size() > PHOTOMSG_LIMIT)
					{
						mPhotoMsgList.remove(0);
					}
				}
			}
		}
	}
	
	/**
	 * 判断是否可立即发送图片消息
	 * @return
	 */
	private boolean PhotoMsgCanSend()
	{
		boolean result = false;
		// 女士邀请状态不能发图片消息
		if (mUserItem.chatType != LCUserItem.ChatType.WomanInvite)
		{
			synchronized(mPhotoMsgList)
			{
				if (mPhotoMsgList.size() < PHOTOMSG_LIMIT) {
					result = true;
				}
				else {
					// 取列表最前面的消息
					LCMessageItem item = mPhotoMsgList.get(0);
					if (null != item)
					{
						// 判断是否大于最大时间间隔
						int createTime = LCMessageItem.GetCreateTime();
						result = createTime > (item.createTime + PHOTOMSG_TIME);
					}
				}
			}
		}
		return result;
	}
	
	/**
	 * 聊天状态改变(图片消息处理)
	 * @param chatType	聊天状态
	 */
	private void PhotoMsgChangeChatTypeProc(LCUserItem.ChatType chatType)
	{
		if (chatType == LCUserItem.ChatType.WomanInvite
			|| chatType == LCUserItem.ChatType.Other) 
		{
			synchronized(mPhotoMsgList)
			{
				// 清空数据
				mPhotoMsgList.clear();
			}
		}
	}
	
	/**
	 * 清空图片消息规则数据
	 */
	private void PhotoMsgClear()
	{
		synchronized(mPhotoMsgList)
		{
			mPhotoMsgList.clear();
		}
	}
	
	// ------------------- 视频(微视频)消息 -------------------
	/**
	 * 插入视频消息处理
	 * @param item	消息item
	 */
	private void VideoMsgInsert(LCMessageItem item)
	{
		if (null != item)
		{
			if (item.sendType == LCMessageItem.SendType.Send)
			{
				// 插入消息到列表
				synchronized(mVideoMsgList)
				{
					// 添加到视频消息列表
					mVideoMsgList.add(item);
					// 超过1条，把最前面的去掉
					if (mVideoMsgList.size() > VIDEOMSG_LIMIT)
					{
						mVideoMsgList.remove(0);
					}
				}
			}
		}
	}
	
	/**
	 * 判断是否可立即发送视频消息
	 * @return
	 */
	private boolean VideoMsgCanSend()
	{
		boolean result = false;
		// 女士邀请状态不能发视频消息
		if (mUserItem.chatType != LCUserItem.ChatType.WomanInvite)
		{
			synchronized(mVideoMsgList)
			{
				if (mVideoMsgList.size() < VIDEOMSG_LIMIT) {
					result = true;
				}
				else {
					// 取列表最前面的消息
					LCMessageItem item = mVideoMsgList.get(0);
					if (null != item)
					{
						// 判断是否大于最大时间间隔
						int createTime = LCMessageItem.GetCreateTime();
						result = createTime > (item.createTime + VIDEOMSG_TIME);
					}
				}
			}
		}
		return result;
	}
	
	/**
	 * 聊天状态改变(视频消息处理)
	 * @param chatType	聊天状态
	 */
	private void VideoMsgChangeChatTypeProc(LCUserItem.ChatType chatType)
	{
		if (chatType == LCUserItem.ChatType.WomanInvite
			|| chatType == LCUserItem.ChatType.Other) 
		{
			synchronized(mVideoMsgList)
			{
				// 清空数据
				mVideoMsgList.clear();
			}
		}
	}
	
	/**
	 * 清空视频消息规则数据
	 */
	private void VideoMsgClear()
	{
		synchronized(mVideoMsgList)
		{
			mVideoMsgList.clear();
		}
	}
}
