/*
 * LiveChatRecordListItem.h
 *
 *  Created on: 2015-10-12
 *      Author: Samson
 * Description: 聊天消息item
 */

#ifndef LIVECHATRECORDLISTITEM_H_
#define LIVECHATRECORDLISTITEM_H_

#include <string>
#include <list>
using namespace std;

#include <common/CommonFunc.h>
#include <common/Arithmetic.h>

#include <json/json/json.h>

#include "../RequestEnumDefine.h"
#include "../RequestLCDefine.h"

class LiveChatRecordListItem
{
public:
	bool Parse(const Json::Value& root)
	{
		bool result = false;
		if( root.isObject() ) {
			if( root[LC_LADYINVITEMSG_TOFLAG].isString() ) {
				toflag = atoi(root[LC_LADYINVITEMSG_TOFLAG].asString().c_str());
			}

			if( root[LC_LADYINVITEMSG_ADDTIME].isInt() ) {
				addtime = root[LC_LADYINVITEMSG_ADDTIME].asInt();
			}

			string message("");
			if (root[LC_LADYINVITEMSG_MSG].isString()) {
				message = root[LC_LADYINVITEMSG_MSG].asString();
			}

			if( root[LC_LADYINVITEMSG_MSGTYPE].isString() ) {
				int msgType = atoi(root[LC_LADYINVITEMSG_MSGTYPE].asString().c_str());
				switch(msgType) {
				case LIPM_TEXT:
				case LIPM_COUPON:
				case LIPM_AUTO_INVITE:{
					textMsg = message;
					messageType = LIM_TEXT;
				}break;
				case LIPM_INVITE:{
					inviteMsg = message;
					messageType = LIM_INVITE;
				}break;
				case LIPM_WARNING:{
					warningMsg = message;
					messageType = LIM_WARNING;
				}break;
				case LIPM_EMOTION:{
					emotionId = message;
					messageType = LIM_EMOTION;
				}break;
				case LIPM_VOICE:{
					voiceId = message;
					ParsingVoiceMsg(voiceId, voiceType, voiceTime);
					messageType = LIM_VOICE;
				}break;
				case LIPM_PHOTO:{
					ParsingPhotoMsg(message, photoId, photoSendId, photoDesc, photoCharge);
					messageType = LIM_PHOTO;
				}break;
				case LIPM_VIDEO:{
					ParsingVideoMsg(message, videoId, videoSendId, videoDesc, videoCharge);
					messageType = LIM_VIDEO;
				}break;
				case LIPM_MAGIC_ICON:{
					magicIconId = message;
					messageType = LIM_MAGICICON;
				}break;
				default:{
					messageType = LIM_UNKNOW;
				}break;
				}
			}

			result = true;
		}

		return result;
	}

	LiveChatRecordListItem()
	{
		toflag = 0;
		addtime = 0;
		messageType = LIM_UNKNOW;
		textMsg = "";
		inviteMsg = "";
		warningMsg = "";
		emotionId = "";
		photoId = "";
		photoSendId = "";
		photoDesc = "";
		photoCharge = false;
		voiceId = "";
		voiceType = "";
		voiceTime = 0;
		videoId = "";
		videoSendId = "";
		videoDesc = "";
		videoCharge = false;
		magicIconId = "";
	}

	virtual ~LiveChatRecordListItem()
	{

	}

private:
	// 解析语音ID
	inline void ParsingVoiceMsg(const string& voiceId, string& fileType, int& timeLen)
	{
		char buffer[512] = {0};
		if (sizeof(buffer) > voiceId.length())
		{
			strcpy(buffer, voiceId.c_str());
			char* pIdItem = strtok(buffer, LIVECHAT_VOICEID_DELIMIT);
			int i = 0;
			while (NULL != pIdItem)
			{
				if (i == 4) {
					fileType = pIdItem;
				}
				else if (i == 5) {
					timeLen = atoi(pIdItem);
				}
				pIdItem = strtok(NULL, LIVECHAT_VOICEID_DELIMIT);
				i++;
			}
		}
	}

	// 解析img（图片信息）
	inline void ParsingPhotoMsg(const string& message, string& photoId, string& sendId, string& photoDesc, bool& charge)
	{
		size_t begin = 0;
		size_t end = 0;
		int i = 0;

		while (string::npos != (end = message.find_first_of(LIVECHAT_PHOTO_DELIMIT, begin)))
		{
			if (i == 0) {
				// photoId
				photoId = message.substr(begin, end - begin);
				begin = end + strlen(LIVECHAT_PHOTO_DELIMIT);
			}
			else if (i == 1) {
				// charget
				string strCharget = message.substr(begin, end - begin);
				charge = (strCharget==LIVECHAT_CHARGE_YES ? true : false);
				begin = end + strlen(LIVECHAT_PHOTO_DELIMIT);
			}
			else if (i == 2) {
				// photoDesc
				photoDesc = message.substr(begin, end - begin);
				const int bufferSize = 1024;
				char buffer[bufferSize] = {0};
				if (!photoDesc.empty() && photoDesc.length() < bufferSize) {
					Arithmetic::Base64Decode(photoDesc.c_str(), photoDesc.length(), buffer);
					photoDesc = buffer;
				}
				begin = end + strlen(LIVECHAT_PHOTO_DELIMIT);

				// sendId
				sendId = message.substr(begin);
				break;
			}
			i++;
		}
	}

	// 解析video（视频信息）
	inline void ParsingVideoMsg(const string& message, string& videoId, string& sendId, string& videoDesc, bool& charge)
	{
		size_t begin = 0;
		size_t end = 0;
		int i = 0;

		while (string::npos != (end = message.find_first_of(LIVECHAT_VIDEO_DELIMIT, begin)))
		{
			if (i == 0) {
				// videoId
				videoId = message.substr(begin, end - begin);
				begin = end + strlen(LIVECHAT_VIDEO_DELIMIT);
			}
			else if (i == 1) {
				// charget
				string strCharget = message.substr(begin, end - begin);
				charge = (strCharget==LIVECHAT_CHARGE_YES ? true : false);
				begin = end + strlen(LIVECHAT_VIDEO_DELIMIT);
			}
			else if (i == 2) {
				// videoDesc
				videoDesc = message.substr(begin, end - begin);
				const int bufferSize = 1024;
				char buffer[bufferSize] = {0};
				if (!videoDesc.empty() && videoDesc.length() < bufferSize) {
					Arithmetic::Base64Decode(videoDesc.c_str(), videoDesc.length(), buffer);
					videoDesc = buffer;
				}
				begin = end + strlen(LIVECHAT_VIDEO_DELIMIT);

				// sendId
				sendId = message.substr(begin);
				break;
			}
			i++;
		}
	}

public:
	/**
	 * 查询聊天消息列表
	 * @param toflag		发送类型
	 * @param addtime		消息生成时间
	 * @param messageType	消息类型
	 * @param textMsg		文本消息
	 * @param inviteMsg		邀请消息
	 * @param warningMsg	警告消息
	 * @param emotionId		高级表情ID
	 * @param photoId		图片ID
	 * @param photoSendId	图片发送ID
	 * @param photoDesc		图片描述
	 * @param photoCharge	图片是否已付费
	 * @param voiceId		语音ID
	 * @param voiceType		语音文件类型
	 * @param voiceTime		语音时长
	 * @param videoId		视频ID
	 * @param videoSendId	视频发送ID
	 * @param videoDesc		视频描述
	 * @param videoCharge	视频是否已付费
	 * @param magicIconId   小高表Id
	 */
	int toflag;
	long addtime;
	LIVECHAT_INVITEMSG_MSGTYPE messageType;
	string textMsg;
	string inviteMsg;
	string warningMsg;
	string emotionId;
	string photoId;
	string photoSendId;
	string photoDesc;
	bool photoCharge;
	string voiceId;
	string voiceType;
	int voiceTime;
	string videoId;
	string videoSendId;
	string videoDesc;
	bool videoCharge;
	string magicIconId;
};

typedef list<LiveChatRecordListItem> LiveChatRecordList;

#endif /* LIVECHATRECORDLISTITEM_H_ */
