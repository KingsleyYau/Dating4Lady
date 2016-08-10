/*
 * LiveChatVideoListItem.h
 *
 *  Created on: 2016-01-06
 *      Author: Max
 * Description: 微视频item
 */

#ifndef LIVECHATVIDEOLISTITEM_H_
#define LIVECHATVIDEOLISTITEM_H_

#include <string>
#include <list>
using namespace std;

#include <xml/tinyxml.h>

#include "../RequestEnumDefine.h"
#include "../RequestLCDefine.h"

class LiveChatVideoListItem
{
public:
	LiveChatVideoListItem()
	{
		videoId = "";
		groupId = "";		title = "";
	}

	virtual ~LiveChatVideoListItem()
	{

	}

public:
	bool Parse(TiXmlNode* photoNode)
	{
		bool result = false;
		if (NULL != photoNode)
		{
			TiXmlNode* videoIdNode = photoNode->FirstChild(LC_LADYGETVIDEOLIST_VIDEOID);
			if (NULL != videoIdNode) {
				TiXmlElement* videoIdElement = videoIdNode->ToElement();
				if (NULL != videoIdElement) {
					const char* pVideoId = videoIdElement->GetText();
					if (NULL != pVideoId) {
						videoId = pVideoId;
					}
				}
			}

			TiXmlNode* groupIdNode = photoNode->FirstChild(LC_LADYGETVIDEOLIST_GROUPID);
			if (NULL != groupIdNode) {
				TiXmlElement* groupIdElement = groupIdNode->ToElement();
				if (NULL != groupIdElement) {
					const char* pGroupId = groupIdElement->GetText();
					if (NULL != pGroupId) {
						groupId = pGroupId;
					}
				}
			}

			TiXmlNode* titleNode = photoNode->FirstChild(LC_LADYGETVIDEOLIST_TITLE);
			if (NULL != titleNode) {
				TiXmlElement* titleElement = titleNode->ToElement();
				if (NULL != titleElement) {
					const char* pTitle = titleElement->GetText();
					if (NULL != pTitle) {
						title = pTitle;
					}
				}
			}

			if (!groupId.empty() && !videoId.empty()) {
				result = true;
			}
		}

		return result;
	}

	string toString() {
		string result = "";
		result += "{groupId=";
		result += groupId;
		result += ", ";
		result += "videoId=";
		result += videoId;
		result += ", ";
		result += "title=";
		result += title;
		result += "}";
		return result;
	}

public:
	string videoId;
	string groupId;
	string title;
};

typedef list<LiveChatVideoListItem> LiveChatVideoList;

#endif /* LIVECHATVIDEOLISTITEM_H_ */
