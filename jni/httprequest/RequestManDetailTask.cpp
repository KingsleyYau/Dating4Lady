/*
 * RequestManDetailTask.cpp
 *
 *  Created on: 2015-9-16
 *      Author: Max
 */

#include "RequestManDetailTask.h"

RequestManDetailTask::RequestManDetailTask() {
	// TODO Auto-generated constructor stub
	mUrl = MAN_DETAIL_PATH;
}

RequestManDetailTask::~RequestManDetailTask() {
	// TODO Auto-generated destructor stub
}

void RequestManDetailTask::SetCallback(IRequestManDetailCallback* pCallback) {
	mpCallback = pCallback;
}

/**
 * @param man_id			男士id
 */
void RequestManDetailTask::SetParam(const string& man_id) {
	char temp[16];
	mHttpEntiy.Reset();

	if( man_id.length() > 0 ) {
		mHttpEntiy.AddContent(MAN_LIST_MAN_ID, man_id.c_str());
	}

	FileLog("httprequest", "RequestManDetailTask::SetParam( "
			"man_id : %s "
			")",
			man_id.c_str()
			);
}

bool RequestManDetailTask::HandleCallback(const string& url, bool requestRet, const char* buf, int size) {
	FileLog("httprequest", "RequestManDetailTask::HandleResult( "
			"url : %s,"
			"requestRet : %s "
			")",
			url.c_str(),
			requestRet?"true":"false"
			);

	if (size < MAX_LOG_BUFFER) {
		FileLog("httprequest", "RequestManDetailTask::HandleResult( buf( %d ) : %s )", size, buf);
	}

	ManDetailItem item;
	int totalCount = 0;
	string errnum = "";
	string errmsg = "";
	bool bFlag = false;
	bool bContinue = true;
	if (requestRet) {
		// request success
		Json::Value dataJson;
		if( HandleResult(buf, size, errnum, errmsg, &dataJson, NULL, &bContinue) ) {
			item.Parse(dataJson);
			bFlag = true;
		}
	} else {
		// request fail
		errnum = LOCAL_ERROR_CODE_TIMEOUT;
		errmsg = LOCAL_ERROR_CODE_TIMEOUT_DESC;
	}

	if( bContinue && mpCallback != NULL ) {
		mpCallback->OnQueryManDetail(bFlag, errnum, errmsg, item, this);
	}

	return bFlag;
}
