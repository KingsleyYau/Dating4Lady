/*
 * RequestLCSendVideoTask.cpp
 *
 *  Created on: 2016-01-06
 *      Author: Max
 * Description: 5.13.	获取微视频列表
 */

#include "RequestLCSendVideoTask.h"

RequestLCSendVideoTask::RequestLCSendVideoTask()
{
	// TODO Auto-generated constructor stub
	mUrl = LC_SENDVIDEO_PATH;
	mSiteType = WebSite;
}

RequestLCSendVideoTask::~RequestLCSendVideoTask()
{
	// TODO Auto-generated destructor stub
}

// set request param
void RequestLCSendVideoTask::SetParam(
		string targetId,
		string videoId,
		string inviteId,
		const string& sid,
		const string& userId
		) {
	mHttpEntiy.Reset();
	mHttpEntiy.SetGetMethod(true);
	char temp[32] = {0};

	if ( !targetId.empty() ) {
		mHttpEntiy.AddContent(LC_GETVIDEO_TARGETID, targetId.c_str());
	}

	if ( !videoId.empty() ) {
		mHttpEntiy.AddContent(LC_GETVIDEO_VIDEOID, videoId.c_str());
	}

	if ( !inviteId.empty() ) {
		mHttpEntiy.AddContent(LC_GETVIDEO_INVITEID, inviteId.c_str());
	}

	if ( !sid.empty() ) {
		mHttpEntiy.AddContent(LC_USER_SID, sid.c_str());
	}

	if ( !userId.empty() ) {
		mHttpEntiy.AddContent(LC_USER_ID, userId.c_str());
	}

}

void RequestLCSendVideoTask::SetCallback(IRequestLCSendVideoCallback* pCallback)
{
	mpCallback = pCallback;
}

bool RequestLCSendVideoTask::HandleCallback(const string& url, bool requestRet, const char* buf, int size)
{
	FileLog("httprequest", "RequestLCSendVideoTask::HandleResult( "
			"url : %s,"
			"requestRet : %s "
			")",
			url.c_str(),
			requestRet?"true":"false"
			);

	if (size < MAX_LOG_BUFFER) {
		FileLog("httprequest", "RequestLCSendVideoTask::HandleResult( buf( %d ) : %s )", size, buf);
	}

	string errnum = "";
	string errmsg = "";
	string sendId = "";
	bool bFlag = false;
	bool bContinue = true;
	if (requestRet) {
		// request success
		TiXmlDocument doc;
		if( HandleResult(buf, size, errnum, errmsg, doc, &bContinue) ) {
			bFlag = true;

			TiXmlNode *rootNode = doc.FirstChild(COMMON_ROOT);
			if (NULL != rootNode) {
				// info
				TiXmlNode *infoIdNode = rootNode->FirstChild(COMMON_INFO);
				if (NULL != infoIdNode) {
					// sendId
					TiXmlNode *sendIdNode = infoIdNode->FirstChild(LC_SENDVIDEO_SENDID);
					if (NULL != sendIdNode) {
						TiXmlElement* sendIdElement = sendIdNode->ToElement();
						if (NULL != sendIdElement) {
							sendId = sendIdElement->GetText();
						}
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
		mpCallback->OnSendVideo(bFlag, errnum, errmsg, sendId, this);
	}

	return bFlag;
}

