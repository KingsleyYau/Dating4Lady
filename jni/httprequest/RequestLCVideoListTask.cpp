/*
 * RequestLCVideoListTask.cpp
 *
 *  Created on: 2016-01-06
 *      Author: Max
 * Description: 5.13.	获取微视频列表
 */

#include "RequestLCVideoListTask.h"

RequestLCVideoListTask::RequestLCVideoListTask()
{
	// TODO Auto-generated constructor stub
	mUrl = LC_LADYGETVIDEOLIST_PATH;
	mSiteType = WebSite;
}

RequestLCVideoListTask::~RequestLCVideoListTask()
{
	// TODO Auto-generated destructor stub
}

// set request param
void RequestLCVideoListTask::SetParam(const string& sid, const string& userId)
{
	mHttpEntiy.Reset();
	mHttpEntiy.SetGetMethod(true);

	if (!sid.empty()) {
		mHttpEntiy.AddContent(LC_USER_SID, sid.c_str());
	}

	if (!userId.empty()) {
		mHttpEntiy.AddContent(LC_USER_ID, userId.c_str());
	}
}

void RequestLCVideoListTask::SetCallback(IRequestLCVideoListCallback* pCallback)
{
	mpCallback = pCallback;
}

bool RequestLCVideoListTask::HandleCallback(const string& url, bool requestRet, const char* buf, int size)
{
	FileLog("httprequest", "RequestLCVideoListTask::HandleResult( "
			"url : %s,"
			"requestRet : %s "
			")",
			url.c_str(),
			requestRet?"true":"false"
			);

	if (size < MAX_LOG_BUFFER) {
		FileLog("httprequest", "RequestLCVideoListTask::HandleResult( buf( %d ) : %s )", size, buf);
	}

	string errnum = "";
	string errmsg = "";
	bool bFlag = false;
	LiveChatVideoGroupList groupList;
	LiveChatVideoList videoList;
	bool bContinue = true;
	if (requestRet) {
		// request success
		TiXmlDocument doc;
		if( HandleResult(buf, size, errnum, errmsg, doc, &bContinue) ) {
			bFlag = true;

			TiXmlNode *rootNode = doc.FirstChild(COMMON_ROOT);
			if (NULL != rootNode)
			{
				// group list
				TiXmlNode *albumNode = rootNode->FirstChild(LC_LADYGETVIDEOLIST_GROUP);
				if (NULL != albumNode) {
					TiXmlNode *listNode = albumNode->FirstChild(LC_LADYGETVIDEOLIST_LIST);
					while( listNode != NULL ) {
						LiveChatVideoGroupItem item;
						if ( item.Parse(listNode) ) {
							FileLog("httprequest",
									"RequestLCVideoListTask::HandleResult( "
									"LiveChatVideoGroupItem : %s "
									")",
									item.toString().c_str()
									);

							groupList.push_back(item);
						}

						listNode = listNode->NextSibling();
					}
				}

				// video list
				TiXmlNode *photoNode = rootNode->FirstChild(LC_LADYGETVIDEOLIST_VIDEO);
				if (NULL != photoNode) {
					TiXmlNode *listNode = photoNode->FirstChild(LC_LADYGETVIDEOLIST_LIST);
					while( listNode != NULL ) {
						LiveChatVideoListItem item;
						if ( item.Parse(listNode) ) {
							FileLog("httprequest",
									"RequestLCVideoListTask::HandleResult( "
									"LiveChatVideoListItem : %s "
									")",
									item.toString().c_str()
									);

							videoList.push_back(item);
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
		mpCallback->OnVideoList(bFlag, errnum, errmsg, groupList, videoList, this);
	}

	return bFlag;
}

