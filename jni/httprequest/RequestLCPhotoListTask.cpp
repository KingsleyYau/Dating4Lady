/*
 * RequestLCPhotoListTask.cpp
 *
 *  Created on: 2015-10-12
 *      Author: Samson
 * Description: 查询图片（私密照）列表
 */

#include "RequestLCPhotoListTask.h"

RequestLCPhotoListTask::RequestLCPhotoListTask()
{
	// TODO Auto-generated constructor stub
	mUrl = LC_LADYGETPHOTOLIST_PATH;
	mSiteType = WebSite;
}

RequestLCPhotoListTask::~RequestLCPhotoListTask()
{
	// TODO Auto-generated destructor stub
}

// set request param
void RequestLCPhotoListTask::SetParam(const string& sid, const string& userId)
{
	mHttpEntiy.Reset();
	mHttpEntiy.SetGetMethod(true);

	if (!sid.empty()) {
		mHttpEntiy.AddContent(LC_USER_SID, sid.c_str());
	}

	if (!userId.empty()) {
		mHttpEntiy.AddContent(LC_USER_ID, userId.c_str());
		mHttpEntiy.AddContent(LC_LADYGETPHOTOLIST_WOMANID, userId.c_str());
	}
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
			if (NULL != rootNode)
			{
				// album list
				TiXmlNode *albumNode = rootNode->FirstChild(LC_LADYGETPHOTOLIST_ALBUM);
				if (NULL != albumNode) {
					TiXmlNode *listNode = albumNode->FirstChild(LC_LADYGETPHOTOLIST_LIST);
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
					TiXmlNode *listNode = photoNode->FirstChild(LC_LADYGETPHOTOLIST_LIST);
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

