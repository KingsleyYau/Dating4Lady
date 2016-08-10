/*
 * RequestPhoneInfoTask.cpp
 *
 *  Created on: 2015-10-23
 *      Author: Hunter
 */

#include "RequestPhoneInfoTask.h"

RequestPhoneInfoTask::RequestPhoneInfoTask(){
	mUrl = PHONE_INFO_PATH;
	mpCallback = NULL;
}

RequestPhoneInfoTask :: ~RequestPhoneInfoTask(){

}

void RequestPhoneInfoTask::setCallback(IRequestPhoneInfoTaskCallback* pCallback){
	mpCallback = pCallback;
}

void RequestPhoneInfoTask::setParams(const string& model,
			const string& manufacturer,
			const string& os,
			const string& release,
			const string& sdk,
			const string& densityDpi,
			int width,
			int height,
			const string& data,
			const string& versionName,
			const string& language,
			const string& strCountry,
			int siteid,
			int action,
			const string& device_id){

	char temp[16];

	mHttpEntiy.Reset();

	if(model.length() > 0){
		mHttpEntiy.AddContent(OTHER_REQUEST_MODEL, model);
	}

	if(manufacturer.length() > 0){
		mHttpEntiy.AddContent(OTHER_REQUEST_MANUFACT, manufacturer);
	}

	if(os.length() > 0){
		mHttpEntiy.AddContent(OTHER_REQUEST_OS, os);
	}

	if(release.length() > 0){
		mHttpEntiy.AddContent(OTHER_REQUEST_RELEASE, release);
	}

	if(sdk.length() > 0){
		mHttpEntiy.AddContent(OTHER_REQUEST_SDK, sdk);
	}

	if(densityDpi.length() > 0){
		mHttpEntiy.AddContent(OTHER_REQUEST_DENSITYDPI, densityDpi);
	}

	sprintf(temp, "%d", width);
	mHttpEntiy.AddContent(OTHER_REQUEST_WIDTH, temp);

	sprintf(temp, "%d", height);
	mHttpEntiy.AddContent(OTHER_REQUEST_HEIGHT, temp);

	if(data.length() > 0){
		string strData = "";
		if(siteid == 0){
			//CL
			strData += "P2:";
		}else if(siteid == 1){
			//IDA
			strData += "P3:";
		}else if(siteid == 4){
			//CD
			strData += "P4:";
		}else if(siteid == 5){
			//LD
			strData += "P12:";
		}
		strData += data;
		mHttpEntiy.AddContent(OTHER_REQUEST_DATA, strData);
	}

	if(versionName.length() > 0){
		mHttpEntiy.AddContent(OTHER_REQUEST_VERNAME, versionName);
	}

	if(language.length() > 0){
		mHttpEntiy.AddContent(OTHER_REQUEST_LANGUAGE, language);
	}

	if(strCountry.length() > 0){
		mHttpEntiy.AddContent(OTHER_REQUEST_COUNTRY, strCountry);
	}

	sprintf(temp, "%d", siteid);
	mHttpEntiy.AddContent(OTHER_REQUEST_SITEID, temp);

	sprintf(temp, "%d", action);
	mHttpEntiy.AddContent(OTHER_REQUEST_ACTION, temp);

	if(device_id.length() > 0){
		mHttpEntiy.AddContent(OTHER_REQUEST_DEVICEID, device_id);
	}

	FileLog("httprequest", "RequestPhoneInfoTask::SetParam( "
				"model : %s, "
				"manufacturer : %s, "
				"os : %s, "
				"release : %s, "
				"sdk : %s, "
				"densityDpi : %s, "
				"width : %d, "
				"height : %d, "
				"data : %s, "
				"versionName : %s, "
				"language : %s, "
				"strCountry : %s, "
				"siteid : %d, "
				"action : %d, "
				"device_id : %s"
				")",
				model.c_str(),
				manufacturer.c_str(),
				os.c_str(),
				release.c_str(),
				sdk.c_str(),
				densityDpi.c_str(),
				width,
				height,
				data.c_str(),
				versionName.c_str(),
				language.c_str(),
				strCountry.c_str(),
				siteid,
				action,
				device_id.c_str()
				);

}

bool RequestPhoneInfoTask::HandleCallback(const string& url, bool requestRet, const char* buf, int size){
	FileLog("httprequest", "RequestPhoneInfoTask::HandleResult( "
			"url : %s,"
			"requestRet : %s "
			")",
			url.c_str(),
			requestRet?"true":"false"
			);

	if (size < MAX_LOG_BUFFER) {
		FileLog("httprequest", "RequestPhoneInfoTask::HandleResult( buf( %d ) : %s )", size, buf);
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
		mpCallback->onPhoneInfoCallback(isSuccess, errnum, errmsg, this);
	}

	return isSuccess;
}
