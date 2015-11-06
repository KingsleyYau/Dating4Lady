/*
 * RequestLoginTask.cpp
 *
 *  Created on: 2015-9-10
 *      Author: Max
 */

#include "RequestLoginTask.h"
#include "RequestAuthorizationDefine.h"

RequestLoginTask::RequestLoginTask() {
	// TODO Auto-generated constructor stub
	mUrl = LOGIN_PATH;
	mUser = "";
	mPassword = "";
}

RequestLoginTask::~RequestLoginTask() {
	// TODO Auto-generated destructor stub
}

void RequestLoginTask::SetCallback(IRequestLoginCallback* pCallback) {
	mpCallback = pCallback;
}

bool RequestLoginTask::HandleCallback(const string& url, bool requestRet, const char* buf, int size) {
	FileLog("httprequest", "RequestLoginTask::HandleResult( "
			"url : %s,"
			"requestRet : %s "
			")",
			url.c_str(),
			requestRet?"true":"false"
			);

	if (size < MAX_LOG_BUFFER) {
		FileLog("httprequest", "RequestLoginTask::HandleResult( buf( %d ) : %s )", size, buf);
	}

	LoginItem item;
	string errnum = "";
	string errmsg = "";
	bool bFlag = false;

	if (requestRet) {
		// request success
		Json::Value dataJson;
		if( HandleResult(buf, size, errnum, errmsg, &dataJson) ) {
			bFlag = true;
			item.Parse(dataJson);
		}
	} else {
		// request fail
		errnum = LOCAL_ERROR_CODE_TIMEOUT;
		errmsg = LOCAL_ERROR_CODE_TIMEOUT_DESC;
	}

	if( mpCallback != NULL ) {
		mpCallback->OnLogin(bFlag, errnum, errmsg, item, this);
	}

	return bFlag;
}

/**
 * @param email				电子邮箱
 * @param password			密码
 * @param deviceId			设备唯一标识
 * @param versioncode		客户端内部版本号
 * @param model				移动设备型号
 * @param manufacturer		制造厂商
 */
void RequestLoginTask::SetParam(
		string email,
		string password,
		string deviceId,
		string model,
		string manufacturer
		) {

	char temp[16];
	mHttpEntiy.Reset();
	mHttpEntiy.SetSaveCookie(true);

	if( email.length() > 0 ) {
		mHttpEntiy.AddContent(AUTHORIZATION_ACCOUNT, email.c_str());
		mUser = email;
	}

	if( password.length() > 0 ) {
		mHttpEntiy.AddContent(AUTHORIZATION_PASSWORD, password.c_str());
		mPassword = password;
	}

	if( deviceId.length() > 0 ) {
		mHttpEntiy.AddContent(AUTHORIZATION_DEVICE_ID, deviceId.c_str());
	}

	if( model.length() > 0 ) {
		mHttpEntiy.AddContent(AUTHORIZATION_MODEL, model.c_str());
	}

	if( manufacturer.length() > 0 ) {
		mHttpEntiy.AddContent(AUTHORIZATION_MANUFACTURER, manufacturer.c_str());
	}

	FileLog("httprequest", "RequestLoginTask::SetParam( "
			"email : %s, "
			"password : %s, "
			"deviceId : %s, "
			"model : %s, "
			"manufacturer : %s "
			")",
			email.c_str(),
			password.c_str(),
			deviceId.c_str(),
			model.c_str(),
			manufacturer.c_str());
}

const string& RequestLoginTask::GetUser() {
	return mUser;
}

const string& RequestLoginTask::GetPassword() {
	return mPassword;
}
