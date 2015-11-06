/*
 * RequestManAddFavourTask.cpp
 *
 *  Created on: 2015-9-16
 *      Author: Max
 */

#include "RequestManAddFavourTask.h"

RequestManAddFavourTask::RequestManAddFavourTask() {
	// TODO Auto-generated constructor stub
	mUrl = MAN_ADD_FAVOUR_PATH;
}

RequestManAddFavourTask::~RequestManAddFavourTask() {
	// TODO Auto-generated destructor stub
}

void RequestManAddFavourTask::SetCallback(IRequestManAddFavourCallback* pCallback) {
	mpCallback = pCallback;
}

/**
 * @param man_id			男士id
 */
void RequestManAddFavourTask::SetParam(const string& man_id) {
	char temp[16];
	mHttpEntiy.Reset();

	if( man_id.length() > 0 ) {
		mHttpEntiy.AddContent(MAN_ADD_FAVOUR_MAN_ID, man_id.c_str());
	}

	FileLog("httprequest", "RequestManAddFavourTask::SetParam( "
			"man_id : %s "
			")",
			man_id.c_str()
			);
}

bool RequestManAddFavourTask::HandleCallback(const string& url, bool requestRet, const char* buf, int size) {
	FileLog("httprequest", "RequestManAddFavourTask::HandleResult( "
			"url : %s, "
			"requestRet : %s "
			")",
			url.c_str(),
			requestRet?"true":"false"
			);

	if (size < MAX_LOG_BUFFER) {
		FileLog("httprequest", "RequestManAddFavourTask::HandleResult( buf( %d ) : %s )", size, buf);
	}

	string errnum = "";
	string errmsg = "";
	bool bFlag = false;
	bool bContinue = true;
	if (requestRet) {
		// request success
		Json::Value dataJson;
		if( HandleResult(buf, size, errnum, errmsg, &dataJson, NULL, &bContinue) ) {
			bFlag = true;
		}
	} else {
		// request fail
		errnum = LOCAL_ERROR_CODE_TIMEOUT;
		errmsg = LOCAL_ERROR_CODE_TIMEOUT_DESC;
	}

	if( bContinue && mpCallback != NULL ) {
		mpCallback->OnAddFavourites(bFlag, errnum, errmsg, this);
	}

	return bFlag;
}
