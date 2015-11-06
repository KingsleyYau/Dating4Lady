/*
 * RequestModifyPasswordTask.cpp
 *
 *  Created on: 2015-10-23
 *      Author: Hunter
 */
#include "RequestModifyPasswordTask.h"

RequestModifyPasswordTask::RequestModifyPasswordTask(){
	mUrl = MODIFY_PASSWORD_PATH;
	mpCallback = NULL;
}

RequestModifyPasswordTask::~RequestModifyPasswordTask(){

}


void RequestModifyPasswordTask::setCallback(IRequestModifyPasswordTaskCallback* pCallback){
	this->mpCallback = pCallback;
}

bool RequestModifyPasswordTask::HandleCallback(const string& url, bool requestRet, const char* buf, int size){

	FileLog("httprequest", "RequestModifyPasswordTask::HandleResult( "
				"url : %s,"
				"requestRet : %s "
				")",
				url.c_str(),
				requestRet?"true":"false"
				);

	if (size < MAX_LOG_BUFFER) {
		FileLog("httprequest", "RequestModifyPasswordTask::HandleResult( buf( %d ) : %s )", size, buf);
	}

	bool isSuccess = false;
	string errnum;
	string errmsg;
	bool bContinue = true;
	if(requestRet){
		Json::Value data;
		if(HandleResult(buf, size, errnum, errmsg, &data, NULL, &bContinue)){
			isSuccess = true;
		}else{
			errnum = LOCAL_ERROR_CODE_TIMEOUT;
			errmsg = LOCAL_ERROR_CODE_TIMEOUT_DESC;
		}
	}else{
		errnum = LOCAL_ERROR_CODE_TIMEOUT;
		errmsg = LOCAL_ERROR_CODE_TIMEOUT_DESC;
	}

	if( bContinue && mpCallback != NULL){
		mpCallback->onModifyPasswordCallback(isSuccess, errmsg, errnum, this);
	}

	return isSuccess;
}

void RequestModifyPasswordTask::setParam(const string& oldPassword, const string& newPassword){

	mHttpEntiy.Reset();

	if(oldPassword.length() > 0){
		mHttpEntiy.AddContent(MODIFY_PASSWORD_OLD_PASSWORD, oldPassword);
	}
	if(newPassword.length() > 0){
		mHttpEntiy.AddContent(MODIFY_PASSWORD_NEW_PASSWORD, newPassword);
	}

	FileLog("httprequest", "RequestModifyPasswordTask: setParam("
			"old_password : %s,"
			"new_password : %s"
			")",
			oldPassword.c_str(),
			newPassword.c_str());
}


