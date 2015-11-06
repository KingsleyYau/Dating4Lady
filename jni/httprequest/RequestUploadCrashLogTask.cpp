/*
 * RequestUploadCrashLogTask.cpp
 *
 *  Created on: 2015-10-23
 *      Author: Hunter
 */
#include "RequestUploadCrashLogTask.h"

RequestUploadCrashLogTask::RequestUploadCrashLogTask(){
	mUrl = UPLOAD_CRASHLOG_PATH;
	mpCallback = NULL;
}

RequestUploadCrashLogTask::~RequestUploadCrashLogTask(){

}


void RequestUploadCrashLogTask::setCallback(IRequestUploadCrashLogTaskCallback* pCallback){
	mpCallback = pCallback;
}

void RequestUploadCrashLogTask::setParam(const string& deviceId, const string& filePath){
	mHttpEntiy.Reset();
	if(deviceId.length() > 0){
		mHttpEntiy.AddContent(UPLOAD_CRASHLOG_DEVICEID, deviceId);
	}
	if(filePath.length() > 0){
		mHttpEntiy.AddFile(UPLOAD_CRASHLOG_CRASHFILE, filePath, "application/x-zip-compressed");
	}

	FileLog("httprequest", "RequestUploadCrashLogTask::SetParam( "
					"deviceId : %s, "
					"filePath : %s "
					")",
					deviceId.c_str(),
					filePath.c_str()
					);
}

bool RequestUploadCrashLogTask::HandleCallback(const string& url, bool requestRet, const char* buf, int size){
	FileLog("httprequest", "RequestUploadCrashLogTask::HandleResult( "
				"url : %s,"
				"requestRet : %s "
				")",
				url.c_str(),
				requestRet?"true":"false"
				);

		if (size < MAX_LOG_BUFFER) {
			FileLog("httprequest", "RequestUploadCrashLogTask::HandleResult( buf( %d ) : %s )", size, buf);
		}
		bool isSuccess = false;
		string errnum;
		string errmsg;
		bool bContinue = true;
		if(requestRet){
			Json::Value dataJson;
			if(HandleResult(buf, size, errnum, errmsg, &dataJson, NULL, &bContinue)){
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
			mpCallback->onUploadCrashLogCallback(isSuccess, errnum, errmsg, this);
		}

		return isSuccess;
}

