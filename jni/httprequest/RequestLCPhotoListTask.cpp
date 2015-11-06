/*
 * RequestLCPhotoListTask.cpp
 *
 *  Created on: 2015-10-12
 *      Author: Samson
 * Description: 查询男士聊天历史
 */

#include "RequestLCPhotoListTask.h"

RequestLCPhotoListTask::RequestLCPhotoListTask()
{
	// TODO Auto-generated constructor stub
	mUrl = LC_LADYGETPHOTOLIST_PATH;
}

RequestLCPhotoListTask::~RequestLCPhotoListTask()
{
	// TODO Auto-generated destructor stub
}

void RequestLCPhotoListTask::SetCallback(IRequestLCPhotoListCallback* pCallback)
{
	mpCallback = pCallback;
}

bool RequestLCPhotoListTask::HandleCallback(const string& url, bool requestRet, const char* buf, int size)
{
	FileLog("httprequest", "RequestLCPhotoListTask::HandleResult( "
			"url : %s,"
			"requestRet : %s "
			")",
			url.c_str(),
			requestRet?"true":"false"
			);

	if (size < MAX_LOG_BUFFER) {
		FileLog("httprequest", "RequestLCPhotoListTask::HandleResult( buf( %d ) : %s )", size, buf);
	}

	string errnum = "";
	string errmsg = "";
	bool bFlag = false;
	LiveChatAlbumList albumList;
	LiveChatPhotoList photoList;
	bool bContinue = true;
	if (requestRet) {
		// request success
		TiXmlDocument doc;
		if( HandleResult(buf, size, errnum, errmsg, doc, &bContinue) ) {
			bFlag = true;

			TiXmlNode *rootNode = doc.FirstChild(COMMON_ROOT);
			if (NULL != rootNode) {
				// album list
				TiXmlNode *albumNode = rootNode->FirstChild(LC_LADYGETPHOTOLIST_ALBUM);
				if (NULL != albumNode) {
					TiXmlNode *listNode = rootNode->FirstChild(LC_LADYGETPHOTOLIST_LIST);
					while( listNode != NULL ) {
						LiveChatAlbumListItem item;
						if ( item.Parse(listNode) ) {
							albumList.push_back(item);
						}

						listNode = listNode->NextSibling();
					}
				}

				// photo list
				TiXmlNode *photoNode = rootNode->FirstChild(LC_LADYGETPHOTOLIST_PHOTO);
				if (NULL != photoNode) {
					TiXmlNode *listNode = rootNode->FirstChild(LC_LADYGETPHOTOLIST_LIST);
					while( listNode != NULL ) {
						LiveChatPhotoListItem item;
						if ( item.Parse(listNode) ) {
							photoList.push_back(item);
						}

						listNode = listNode->NextSibling();
					}
				}
			}
		}
	} else {
		// request fail
		errnum = LOCAL_ERROR_CODE_TIMEOUT;
		errmsg = LOCAL_ERROR_CODE_TIMEOUT_DESC;
	}

	if( bContinue && mpCallback != NULL ) {
		mpCallback->OnPhotoList(bFlag, errnum, errmsg, albumList, photoList, this);
	}

	return bFlag;
}

