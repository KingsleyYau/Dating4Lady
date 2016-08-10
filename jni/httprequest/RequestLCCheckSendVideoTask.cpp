/*
 * RequestLCCheckSendVideoTask.cpp
 *
 *  Created on: 2016-01-06
 *      Author: Max
 * Description: 5.13.	获取微视频列表
 */

#include "RequestLCCheckSendVideoTask.h"

RequestLCCheckSendVideoTask::RequestLCCheckSendVideoTask()
{
	// TODO Auto-generated constructor stub
	mUrl = LC_CHECKVIDEO_PATH;
	mSiteType = WebSite;
}

RequestLCCheckSendVideoTask::~RequestLCCheckSendVideoTask()
{
	// TODO Auto-generated destructor stub
}

// set request param
void RequestLCCheckSendVideoTask::SetParam(
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

void RequestLCCheckSendVideoTask::SetCallback(IRequestLCCheckSendVideoCallback* pCallback)
{
	mpCallback = pCallback;
}

bool RequestLCCheckSendVideoTask::HandleCallback(const string& url, bool requestRet, const char* buf, int size)
{
	FileLog("httprequest", "RequestLCCheckSendVideoTask::HandleResult( "
			"url : %s,"
			"requestRet : %s "
			")",
			url.c_str(),
			requestRet?"true":"false"
			);

	if (size < MAX_LOG_BUFFER) {
		FileLog("httprequest", "RequestLCCheckSendVideoTask::HandleResult( buf( %d ) : %s )", size, buf);
	}

	string errnum = "";
	string errmsg = "";
	LC_CHECKVIDEO_TYPE result = LCTV_CANNOT_SEND;
	bool bFlag = false;
	bool bContinue = true;
	if (requestRet) {
		// request success
		TiXmlDocument doc;
		HandleResult(buf, size, errnum, errmsg, doc, &bContinue);
		if( bContinue && !doc.Error() ) {
			bFlag = true;

			TiXmlNode *rootNode = doc.FirstChild(COMMON_ROOT);
			if (NULL != rootNode) {
				TiXmlNode *resultNode = rootNode->FirstChild(COMMON_RESULT);
				if( resultNode != NULL ) {
					// status
					TiXmlNode *statusNode = resultNode->FirstChild(COMMON_STATUS);
					if( statusNode != NULL ) {
						TiXmlElement* itemElement = statusNode->ToElement();
						if ( itemElement != NULL ) {
							const char* p = itemElement->GetText();
							if( p != NULL )
							{
								int status = atoi(p);
								if (LCTV_BEGIN <= status <= LCTV_END)
								{
									result = (LC_CHECKVIDEO_TYPE)atoi(p);
									bFlag = (LCTV_ALLOW_SEND == result);
								}
							}
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
		mpCallback->OnCheckSendVideo(result, errnum, errmsg, this);
	}

	return bFlag;
}

