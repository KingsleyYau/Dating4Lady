/*
 * RequestEmotionConfigTask.cpp
 *
 *  Created on: 2015-10-23
 *      Author: Hunter
 */
#include "RequestEmotionConfigTask.h"

RequestEmotionConfigTask::RequestEmotionConfigTask(){
	mUrl = EMOTION_CONFIG_PATH;
	mpCallback = NULL;
}

RequestEmotionConfigTask::~RequestEmotionConfigTask(){

}

void RequestEmotionConfigTask::setCallback(IRequestEmotionConfigCallback* callback){
	mpCallback = callback;
}

bool RequestEmotionConfigTask::HandleCallback(const string& url, bool requestRet, const char* buf, int size){
	FileLog("httprequest", "RequestEmotionConfigTask::HandleResult( "
					"url : %s,"
					"requestRet : %s "
					")",
					url.c_str(),
					requestRet?"true":"false"
					);

	if (size < MAX_LOG_BUFFER) {
		FileLog("httprequest", "RequestEmotionConfigTask::HandleResult( buf( %d ) : %s )", size, buf);
	}
	bool bFlag = false;
	string errnum;
	string errmsg;
	EmotionConfigItem  emotionConfigItem;
	bool bContinue = true;

	if(requestRet){
		Json::Value dataJson;
		if(HandleResult(buf, size, errnum, errmsg, &dataJson, NULL, &bContinue)){
			if(emotionConfigItem.Parsing(dataJson)){
				bFlag = true;
			}else{
				errnum = LOCAL_ERROR_CODE_TIMEOUT;
				errmsg = LOCAL_ERROR_CODE_TIMEOUT_DESC;
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
		mpCallback->onEmotionConfigCallback(bFlag, errnum, errmsg, emotionConfigItem, this);
	}

	return bFlag;
}
