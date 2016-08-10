/*
 * RequestManFavourListTask.cpp
 *
 *  Created on: 2015-9-16
 *      Author: Max
 */

#include "RequestManFavourListTask.h"
#include <common/StringHandle.h>

RequestManFavourListTask::RequestManFavourListTask() {
	// TODO Auto-generated constructor stub
	mUrl = MAN_FAVOUR_LIST_PATH;
}

RequestManFavourListTask::~RequestManFavourListTask() {
	// TODO Auto-generated destructor stub
}

void RequestManFavourListTask::SetCallback(IRequestManFavourListCallback* pCallback) {
	mpCallback = pCallback;
}

bool RequestManFavourListTask::HandleCallback(const string& url, bool requestRet, const char* buf, int size) {
	FileLog("httprequest", "RequestManFavourListTask::HandleResult( "
			"url : %s,"
			"requestRet : %s "
			")",
			url.c_str(),
			requestRet?"true":"false"
			);

	if (size < MAX_LOG_BUFFER) {
		FileLog("httprequest", "RequestManFavourListTask::HandleResult( buf( %d ) : %s )", size, buf);
	}

	list<string> itemList;
	int totalCount = 0;
	string errnum = "";
	string errmsg = "";
	bool bFlag = false;
	bool bContinue = true;
	if (requestRet) {
		// request success
		Json::Value dataJson;
		if( HandleResult(buf, size, errnum, errmsg, &dataJson, NULL, &bContinue) ) {
			if( dataJson.isObject() && dataJson[MAN_FAVOUR_LIST_MAN_IDS].isString() ) {
				string manids = dataJson[MAN_FAVOUR_LIST_MAN_IDS].asString();
				if(!manids.empty()){
					itemList = StringHandle::split(manids, ",");
				}
				bFlag = true;
			} else {
				// parsing fail
				bFlag = false;
				errnum = LOCAL_ERROR_CODE_PARSEFAIL;
				errmsg = LOCAL_ERROR_CODE_PARSEFAIL_DESC;
			}
		}
	} else {
		// request fail
		errnum = LOCAL_ERROR_CODE_TIMEOUT;
		errmsg = LOCAL_ERROR_CODE_TIMEOUT_DESC;
	}

	if( bContinue && mpCallback != NULL ) {
		mpCallback->OnQueryFavourList(bFlag, errnum, errmsg, itemList, this);
	}

	return bFlag;
}
