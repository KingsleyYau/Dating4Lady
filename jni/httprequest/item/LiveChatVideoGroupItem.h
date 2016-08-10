/*
 * LiveChatVideoGroupItem.h
 *
 *  Created on: 2016-01-06
 *      Author: Max
 * Description: 视频组item
 */

#ifndef LIVECHATVIDEOGROUPITEM_H_
#define LIVECHATVIDEOGROUPITEM_H_

#include <string>
#include <list>
using namespace std;

#include <xml/tinyxml.h>

#include "../RequestEnumDefine.h"
#include "../RequestLCDefine.h"

class LiveChatVideoGroupItem
{
public:
	LiveChatVideoGroupItem()
	{		groupId = "";
		groupTitle = "";
	}

	virtual ~LiveChatVideoGroupItem()
	{

	}

public:
	bool Parse(TiXmlNode* groupNode)
	{
		bool result = false;
		if (NULL != groupNode)
		{
			TiXmlNode* groupIdNode = groupNode->FirstChild(LC_LADYGETVIDEOLIST_GROUPID);
			if (NULL != groupIdNode) {
				TiXmlElement* groupIdElement = groupIdNode->ToElement();
				if (NULL != groupIdElement) {
					const char* pGroupId = groupIdElement->GetText();
					if (NULL != pGroupId) {
						groupId = pGroupId;
					}
				}
			}

			TiXmlNode* titleNode = groupNode->FirstChild(LC_LADYGETVIDEOLIST_GROUPTITLE);
			if (NULL != titleNode) {
				TiXmlElement* titleElement = titleNode->ToElement();
				if (NULL != titleElement) {
					const char* pTitle = titleElement->GetText();
					if (NULL != pTitle) {
						groupTitle = pTitle;
					}
				}
			}

			if (!groupId.empty()) {
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
		result += "groupTitle=";
		result += groupTitle;
		result += "}";
		return result;
	}

public:
	string groupId;
	string groupTitle;
};

typedef list<LiveChatVideoGroupItem> LiveChatVideoGroupList;

#endif /* LIVECHATVIDEOGROUPITEM_H_ */
