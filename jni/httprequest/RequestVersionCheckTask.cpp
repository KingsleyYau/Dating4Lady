/*
 * RequestVersionCheckTask.cpp
 *
 *  Created on: 2015-10-23
 *      Author: Hunter
 */
#include "RequestVersionCheckTask.h"

RequestVersionCheckTask::RequestVersionCheckTask(){
	mUrl = CHECK_VERSION_PATH;
	mpCallback = NULL;
}

RequestVersionCheckTask::~RequestVersionCheckTask(){

}

void RequestVersionCheckTask::setCallback(IRequestVersionCheckTaskCallback* pCallback){
	mpCallback = pCallback;
}


bool RequestVersionCheckTask::HandleCallback(const string& url, bool requestRet, const char* buf, int size){
	FileLog("httprequest", "RequestVersionCheckTask::HandleResult( "
			"url : %s,"
			"requestRet : %s "
			")",
			url.c_str(),
			requestRet?"true":"false"
			);

	if (size < MAX_LOG_BUFFER) {
		FileLog("httprequest", "RequestVersionCheckTask::HandleResult( buf( %d ) : %s )", size, buf);
	}

	bool isSuccess = false;
	string errnum;
	string errmsg;
	VersionCheckItem versionCheckItem;
	bool bContinue = true;
	if(requestRet){
		Json::Value data;
		if(HandleResult(buf, size, errnum, errmsg, &data, NULL, &bContinue)){
			if(versionCheckItem.Parsing(data)){
				isSuccess = true;
			}
		}else{
			errnum = LOCAL_ERROR_CODE_TIMEOUT;
			errmsg = LOCAL_ERROR_CODE_TIMEOUT_DESC;
		}
	}else{
		errnum = LOCAL_ERROR_CODE_TIMEOUT;
		errmsg = LOCAL_ERROR_CODE_TIMEOUT_DESC;
	}

	if( bContinue && mpCallback != NULL){
		mpCallback->onVersionCheckCallback(isSuccess, errnum, errmsg, versionCheckItem, this);
	}

	return isSuccess;
}
