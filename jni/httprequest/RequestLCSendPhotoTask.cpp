/*
 * RequestLCSendPhotoTask.cpp
 *
 *  Created on: 2015-10-12
 *      Author: Samson
 * Description: 发送私密照片
 */

#include "RequestLCSendPhotoTask.h"

RequestLCSendPhotoTask::RequestLCSendPhotoTask()
{
	// TODO Auto-generated constructor stub
	mUrl = LC_LADYSENDPHOTO_PATH;
	mSiteType = WebSite;
	mHttpEntiy.SetGetMethod(true);
}

RequestLCSendPhotoTask::~RequestLCSendPhotoTask()
{
	// TODO Auto-generated destructor stub
}

void RequestLCSendPhotoTask::SetCallback(IRequestLCSendPhotoCallback* pCallback)
{
	mpCallback = pCallback;
}

void RequestLCSendPhotoTask::SetParam(const string& targetId, const string& inviteId, const string& photoId, const string& sid, const string& userId)
{
	mHttpEntiy.Reset();

	if( targetId.length() > 0 ) {
		mHttpEntiy.AddContent(LC_LADYSENDPHOTO_TARGETID, targetId.c_str());
	}

	if( inviteId.length() > 0 ) {
		mHttpEntiy.AddContent(LC_LADYSENDPHOTO_INVITEID, inviteId.c_str());
	}

	if( photoId.length() > 0 ) {
		mHttpEntiy.AddContent(LC_LADYSENDPHOTO_PHOTOID, photoId.c_str());
	}

	if (!sid.empty()) {
		mHttpEntiy.AddContent(LC_USER_SID, sid.c_str());
	}

	if (!userId.empty()) {
		mHttpEntiy.AddContent(LC_USER_ID, userId.c_str());
	}

	FileLog("httprequest", "RequestLCSendPhotoTask::SetParam( "
			"targetId: %s, "
			"inviteId: %s, "
			"photoId: %s, "
			"sid:%s, "
			"userId:%s"
			")",
			targetId.c_str(),
			inviteId.c_str(),
			photoId.c_str(),
			sid.c_str(),
			userId.c_str()
			);
}

bool RequestLCSendPhotoTask::HandleCallback(const string& url, bool requestRet, const char* buf, int size)
{
	FileLog("httprequest", "RequestLCSendPhotoTask::HandleResult( "
			"url : %s,"
			"requestRet : %s "
			")",
			url.c_str(),
			requestRet?"true":"false"
			);

	if (size < MAX_LOG_BUFFER) {
		FileLog("httprequest", "RequestLCSendPhotoTask::HandleResult( buf( %d ) : %s )", size, buf);
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

			TiXmlNode *rootNode = doc.FirstChild(COMMON_ROOT);
			if (NULL != rootNode) {
				TiXmlNode *infoNode = rootNode->FirstChild(COMMON_INFO);
				if (NULL != infoNode) {
					TiXmlNode *sendIdNode = infoNode->FirstChild(LC_LADYSENDPHOTO_SENDID);
					if (NULL != sendIdNode) {
						TiXmlElement* itemElement = sendIdNode->ToElement();
						if (NULL != itemElement) {
							const char* value = itemElement->GetText();
							if (NULL != value) {
								sendId = value;
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
		mpCallback->OnSendPhoto(bFlag, errnum, errmsg, sendId, this);
	}
	return bFlag;
}

