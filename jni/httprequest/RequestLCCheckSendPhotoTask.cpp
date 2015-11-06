/*
 * RequestLCCheckSendPhotoTask.cpp
 *
 *  Created on: 2015-10-12
 *      Author: Samson
 * Description: 检测女士是否可发私密照
 */

#include "RequestLCCheckSendPhotoTask.h"

RequestLCCheckSendPhotoTask::RequestLCCheckSendPhotoTask()
{
	// TODO Auto-generated constructor stub
	mUrl = LC_LADYCHECKPHOTO_PATH;
}

RequestLCCheckSendPhotoTask::~RequestLCCheckSendPhotoTask()
{
	// TODO Auto-generated destructor stub
}

void RequestLCCheckSendPhotoTask::SetCallback(IRequestLCCheckSendPhotoCallback* pCallback)
{
	mpCallback = pCallback;
}

void RequestLCCheckSendPhotoTask::SetParam(const string& targetId, const string& inviteId, const string& photoId)
{
	mHttpEntiy.Reset();

	if( targetId.length() > 0 ) {
		mHttpEntiy.AddContent(LC_LADYCHECKPHOTO_TARGETID, targetId.c_str());
	}

	if( inviteId.length() > 0 ) {
		mHttpEntiy.AddContent(LC_LADYCHECKPHOTO_INVITEID, inviteId.c_str());
	}

	if( photoId.length() > 0 ) {
		mHttpEntiy.AddContent(LC_LADYCHECKPHOTO_PHOTOID, photoId.c_str());
	}

	FileLog("httprequest", "RequestLCCheckSendPhotoTask::SetParam( "
			"targetId: %s, "
			"inviteId: %s, "
			"photoId: %s"
			")",
			targetId.c_str(),
			inviteId.c_str(),
			photoId.c_str()
			);
}

bool RequestLCCheckSendPhotoTask::HandleCallback(const string& url, bool requestRet, const char* buf, int size)
{
	FileLog("httprequest", "RequestLCCheckSendPhotoTask::HandleResult( "
			"url : %s,"
			"requestRet : %s "
			")",
			url.c_str(),
			requestRet?"true":"false"
			);

	if (size < MAX_LOG_BUFFER) {
		FileLog("httprequest", "RequestLCCheckSendPhotoTask::HandleResult( buf( %d ) : %s )", size, buf);
	}

	string errnum = "";
	string errmsg = "";
	bool bFlag = false;
	string sendId = "";
	bool bContinue = true;
	if (requestRet) {
		// request success
		TiXmlDocument doc;
		if( HandleResult(buf, size, errnum, errmsg, doc, &bContinue) ) {
			bFlag = true;
		}
	} else {
		// request fail
		errnum = LOCAL_ERROR_CODE_TIMEOUT;
		errmsg = LOCAL_ERROR_CODE_TIMEOUT_DESC;
	}

	if( mpCallback != NULL ) {
		mpCallback->OnCheckSendPhoto(bFlag, errnum, errmsg, this);
	}

	return bFlag;
}

