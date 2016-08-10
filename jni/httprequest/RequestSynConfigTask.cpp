/*
 * RequestSynConfigTask.cpp
 *
 *  Created on: 2015-10-23
 *      Author: Hunter
 */
#include "RequestSynConfigTask.h"

RequestSynConfigTask::RequestSynConfigTask(){
	mUrl = SYN_CONFIG_PATH;
	mpCallback = NULL;
}

RequestSynConfigTask::~RequestSynConfigTask(){

}


void RequestSynConfigTask::setCallback(IRequestSynConfigTaskCallback* pCallback){
	mpCallback = pCallback;
}

bool RequestSynConfigTask::HandleCallback(const string& url, bool requestRet, const char* buf, int size){
	FileLog("httprequest", "RequestSynConfigTask::HandleResult( "
			"url : %s,"
			"requestRet : %s "
			")",
			url.c_str(),
			requestRet?"true":"false"
			);

	if (size < MAX_LOG_BUFFER) {
		FileLog("httprequest", "RequestSynConfigTask::HandleResult( buf( %d ) : %s )", size, buf);
	}

	bool isSuccess = false;
	string errnum;
	string errmsg;
	SynConfigItem synConfigItem;
	bool bContinue = true;
	if(requestRet){
		Json::Value data;
		if(HandleResult(buf, size, errnum, errmsg, &data, NULL, &bContinue)){
			isSuccess = synConfigItem.Parsing(data);
		}else{
			errnum = LOCAL_ERROR_CODE_TIMEOUT;
			errmsg = LOCAL_ERROR_CODE_TIMEOUT_DESC;
		}
	}else{
		errnum = LOCAL_ERROR_CODE_TIMEOUT;
		errmsg = LOCAL_ERROR_CODE_TIMEOUT_DESC;
	}

	if( bContinue && mpCallback != NULL){
		mpCallback->onSynConfigCallback(isSuccess, errnum, errmsg, synConfigItem, this);
	}

	return isSuccess;
}
