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
	mSiteType = WebSite;
	mHttpEntiy.SetGetMethod(true);
}

RequestLCCheckSendPhotoTask::~RequestLCCheckSendPhotoTask()
{
	// TODO Auto-generated destructor stub
}

void RequestLCCheckSendPhotoTask::SetCallback(IRequestLCCheckSendPhotoCallback* pCallback)
{
	mpCallback = pCallback;
}

void RequestLCCheckSendPhotoTask::SetParam(const string& targetId, const string& inviteId, const string& photoId, const string& sid, const string& userId)
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

	if (!sid.empty()) {
		mHttpEntiy.AddContent(LC_USER_SID, sid.c_str());
	}

	if (!userId.empty()) {
		mHttpEntiy.AddContent(LC_USER_ID, userId.c_str());
	}

	FileLog("httprequest", "RequestLCCheckSendPhotoTask::SetParam( "
			"targetId: %s, "
			"inviteId: %s, "
			"photoId: %s"
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
	LC_CHECKPHOTO_TYPE result = LCT_CANNOT_SEND;
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
								if (LCT_BEGIN <= status <= LCT_END)
								{
									result = (LC_CHECKPHOTO_TYPE)atoi(p);
									bFlag = (LCT_ALLOW_SEND == result);
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
		mpCallback->OnCheckSendPhoto(result, errnum, errmsg, this);
	}

	return bFlag;
}

