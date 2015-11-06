/*
 * LiveChatPhotoListItem.h
 *
 *  Created on: 2015-10-12
 *      Author: Samson
 * Description: 私密照列表照片item
 */

#ifndef LIVECHATPHOTOLISTITEM_H_
#define LIVECHATPHOTOLISTITEM_H_

#include <string>
#include <list>
using namespace std;

#include <xml/tinyxml.h>

#include "../RequestEnumDefine.h"
#include "../RequestLCDefine.h"

class LiveChatPhotoListItem
{
public:
	LiveChatPhotoListItem()
	{
		photoId = "";
		albumId = "";		title = "";
	}

	virtual ~LiveChatPhotoListItem()
	{

	}

public:
	bool Parse(TiXmlNode* photoNode)
	{
		bool result = false;
		if (NULL != photoNode)
		{
			TiXmlNode* photoIdNode = photoNode->FirstChild(LC_LADYGETPHOTOLIST_PHOTOID);
			if (NULL != photoIdNode) {
				TiXmlElement* photoIdElement = photoIdNode->ToElement();
				if (NULL != photoIdElement) {
					const char* pPhotoId = photoIdElement->GetText();
					if (NULL != pPhotoId) {
						photoId = pPhotoId;
					}
				}
			}

			TiXmlNode* albumIdNode = photoNode->FirstChild(LC_LADYGETPHOTOLIST_ALBUMID);
			if (NULL != albumIdNode) {
				TiXmlElement* albumIdElement = albumIdNode->ToElement();
				if (NULL != albumIdElement) {
					const char* pAlbumId = albumIdElement->GetText();
					if (NULL != pAlbumId) {
						albumId = pAlbumId;
					}
				}
			}

			TiXmlNode* titleNode = photoNode->FirstChild(LC_LADYGETPHOTOLIST_TITLE);
			if (NULL != titleNode) {
				TiXmlElement* titleElement = titleNode->ToElement();
				if (NULL != titleElement) {
					const char* pTitle = titleElement->GetText();
					if (NULL != pTitle) {
						title = pTitle;
					}
				}
			}

			if (!albumId.empty() && !photoId.empty()) {
				result = true;
			}
		}

		return result;
	}

public:
	string photoId;
	string albumId;
	string title;
};

typedef list<LiveChatPhotoListItem> LiveChatPhotoList;

#endif /* LIVECHATPHOTOLISTITEM_H_ */
