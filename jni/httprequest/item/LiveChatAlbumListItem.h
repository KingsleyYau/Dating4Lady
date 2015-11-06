/*
 * LiveChatAlbumListItem.h
 *
 *  Created on: 2015-10-12
 *      Author: Samson
 * Description: 私密照列表相册item
 */

#ifndef LIVECHATALBUMLISTITEM_H_
#define LIVECHATALBUMLISTITEM_H_

#include <string>
#include <list>
using namespace std;

#include <xml/tinyxml.h>

#include "../RequestEnumDefine.h"
#include "../RequestLCDefine.h"

class LiveChatAlbumListItem
{
public:
	LiveChatAlbumListItem()
	{		albumId = "";
		title = "";
	}

	virtual ~LiveChatAlbumListItem()
	{

	}

public:
	bool Parse(TiXmlNode* albumNode)
	{
		bool result = false;
		if (NULL != albumNode)
		{
			TiXmlNode* albumIdNode = albumNode->FirstChild(LC_LADYGETPHOTOLIST_ALBUMID);
			if (NULL != albumIdNode) {
				TiXmlElement* albumIdElement = albumIdNode->ToElement();
				if (NULL != albumIdElement) {
					const char* pAlbumId = albumIdElement->GetText();
					if (NULL != pAlbumId) {
						albumId = pAlbumId;
					}
				}
			}

			TiXmlNode* titleNode = albumNode->FirstChild(LC_LADYGETPHOTOLIST_TITLE);
			if (NULL != titleNode) {
				TiXmlElement* titleElement = titleNode->ToElement();
				if (NULL != titleElement) {
					const char* pTitle = titleElement->GetText();
					if (NULL != pTitle) {
						title = pTitle;
					}
				}
			}

			if (!albumId.empty()) {
				result = true;
			}
		}

		return result;
	}

public:
	string albumId;
	string title;
};

typedef list<LiveChatAlbumListItem> LiveChatAlbumList;

#endif /* LIVECHATALBUMLISTITEM_H_ */
