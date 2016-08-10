/*
 * RequestMyProfileTask.cpp
 *
 *  Created on: 2015-11-20
 *      Author: Samson
 */
#include "RequestMyProfileTask.h"

RequestMyProfileTask::RequestMyProfileTask()
{
	mUrl = OTHER_MYPROFILE_PATH;
	mpCallback = NULL;
}

RequestMyProfileTask::~RequestMyProfileTask()
{

}


void RequestMyProfileTask::setCallback(IRequestMyProfileTaskCallback* pCallback)
{
	mpCallback = pCallback;
}

bool RequestMyProfileTask::HandleCallback(const string& url, bool requestRet, const char* buf, int size)
{
	FileLog("httprequest", "RequestMyProfileTask::HandleResult( "
			"url : %s,"
			"requestRet : %s "
			")",
			url.c_str(),
			requestRet?"true":"false"
			);

	if (size < MAX_LOG_BUFFER) {
		FileLog("httprequest", "RequestMyProfileTask::HandleResult( buf( %d ) : %s )", size, buf);
	}

	bool isSuccess = false;
	string errnum;
	string errmsg;
	MyProfileItem item;
	if(requestRet)
	{
		Json::Value data;
		if(HandleResult(buf, size, errnum, errmsg, &data, NULL)) {
			isSuccess = item.Parsing(data);
		}else{
			errnum = LOCAL_ERROR_CODE_PARSEFAIL;
			errmsg = LOCAL_ERROR_CODE_PARSEFAIL_DESC;
		}
	}
	else {
		errnum = LOCAL_ERROR_CODE_TIMEOUT;
		errmsg = LOCAL_ERROR_CODE_TIMEOUT_DESC;
	}

	if(mpCallback != NULL) {
		mpCallback->onMyProfileCallback(isSuccess, errnum, errmsg, item, this);
	}

	return isSuccess;
}
