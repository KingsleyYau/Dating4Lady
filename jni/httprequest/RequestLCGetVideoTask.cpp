/*
 * RequestLCGetVideoTask.cpp
 *
 *  Created on: 2016-01-06
 *      Author: Max
 * Description: 5.13.	获取微视频列表
 */

#include "RequestLCGetVideoTask.h"

RequestLCGetVideoTask::RequestLCGetVideoTask()
{
	// TODO Auto-generated constructor stub
	mUrl = LC_GETVIDEO_PATH;
	mSiteType = WebSite;
}

RequestLCGetVideoTask::~RequestLCGetVideoTask()
{
	// TODO Auto-generated destructor stub
}

// set request param
void RequestLCGetVideoTask::SetParam(
		string targetId,
		string videoId,
		string inviteId,
		GETVIDEO_TO_FLAG_TYPE toflag,
		string sendId,
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

	if( toflag >= GETVIDEO_TO_FLAG_TYPE_WOMAN && toflag <= GETVIDEO_TO_FLAG_TYPE_MAN ) {
		snprintf(temp, sizeof(temp), "%d", toflag);
		mHttpEntiy.AddContent(LC_GETVIDEO_TOFLAG, temp);
	}

	if ( !sendId.empty() ) {
		mHttpEntiy.AddContent(LC_GETVIDEO_SENDID, sendId.c_str());
	}

	if ( !sid.empty() ) {
		mHttpEntiy.AddContent(LC_USER_SID, sid.c_str());
	}

	if ( !userId.empty() ) {
		mHttpEntiy.AddContent(LC_USER_ID, userId.c_str());
	}

}

void RequestLCGetVideoTask::SetCallback(IRequestLCGetVideoCallback* pCallback)
{
	mpCallback = pCallback;
}

bool RequestLCGetVideoTask::HandleCallback(const string& url, bool requestRet, const char* buf, int size)
{
	FileLog("httprequest", "RequestLCGetVideoTask::HandleResult( "
			"url : %s,"
			"requestRet : %s "
			")",
			url.c_str(),
			requestRet?"true":"false"
			);

	if (size < MAX_LOG_BUFFER) {
		FileLog("httprequest", "RequestLCGetVideoTask::HandleResult( buf( %d ) : %s )", size, buf);
	}

	string errnum = "";
	string errmsg = "";
	string videoUrl = "";
	bool bFlag = false;
	bool bContinue = true;
	if (requestRet) {
		// request success
		TiXmlDocument doc;
		if( HandleResult(buf, size, errnum, errmsg, doc, &bContinue) ) {
			bFlag = true;

			TiXmlNode *rootNode = doc.FirstChild(COMMON_ROOT);
			if (NULL != rootNode) {
				// group list
				TiXmlNode *videoNode = rootNode->FirstChild(LC_GETVIDEO_VIDEO_URL);
				if (NULL != videoNode) {
					TiXmlElement* videoUrlElement  = videoNode->ToElement();
					if (NULL != videoUrlElement) {
						videoUrl = videoUrlElement->GetText();
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
		mpCallback->OnGetVideo(bFlag, errnum, errmsg, videoUrl, this);
	}

	return bFlag;
}

