/*
 * RequestLCDelCustomTemplateTask.cpp
 *
 *  Created on: 2015-11-03
 *      Author: Hunter
 */
#include "RequestLCDelCustomTemplateTask.h"
#include "RequestLCDefine.h"

RequestLCDelCustomTemplateTask::RequestLCDelCustomTemplateTask(){
	mUrl = LC_DELCUSTOMTEMPLATE_PATH;
	mpCallback = NULL;
}

RequestLCDelCustomTemplateTask::~RequestLCDelCustomTemplateTask(){

}

void RequestLCDelCustomTemplateTask::setParams(const string& tempId){
	mHttpEntiy.Reset();
	if(tempId.length() > 0){
		mHttpEntiy.AddContent(LC_DELCUSTOMTEMPLAT_TEMPIDS, tempId.c_str());
	}

	FileLog("httprequest", "RequestLCDelCustomTemplateTask::SetParam( "
				"tempIds : %s "
				")",
				tempId.c_str()
				);
}

void RequestLCDelCustomTemplateTask :: setCallback(IRequestLCDelCustomTemplateTaskCallback * pCallback){
	mpCallback = pCallback;
}

bool RequestLCDelCustomTemplateTask::HandleCallback(const string& url, bool requestRet, const char* buf, int size){
	FileLog("httprequest", "RequestLCDelCustomTemplateTask::HandleResult( "
				"url : %s,"
				"requestRet : %s "
				")",
				url.c_str(),
				requestRet?"true":"false"
				);

	if (size < MAX_LOG_BUFFER) {
		FileLog("httprequest", "RequestLCDelCustomTemplateTask::HandleResult( buf( %d ) : %s )", size, buf);
	}

	string errnum = "";
	string errmsg = "";
	bool bFlag = false;
	bool bContinue = true;

	if (requestRet) {
		// request success
		bFlag = HandleResult(buf, size, errnum, errmsg, NULL, NULL, &bContinue);
	} else {
		// request fail
		errnum = LOCAL_ERROR_CODE_TIMEOUT;
		errmsg = LOCAL_ERROR_CODE_TIMEOUT_DESC;
	}

	if( bContinue && mpCallback != NULL ) {
		mpCallback->onDelCustomTemplate(bFlag, errnum, errmsg, this);
	}

	return bFlag;
}
