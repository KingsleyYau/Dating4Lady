/*
 * RequestManRemoveFavourTask.cpp
 *
 *  Created on: 2015-9-16
 *      Author: Max
 */

#include "RequestManRemoveFavourTask.h"

RequestManRemoveFavourTask::RequestManRemoveFavourTask() {
	// TODO Auto-generated constructor stub
	mUrl = MAN_REMOVE_FAVOUR_PATH;
}

RequestManRemoveFavourTask::~RequestManRemoveFavourTask() {
	// TODO Auto-generated destructor stub
}

void RequestManRemoveFavourTask::SetCallback(IRequestManRemoveFavourCallback* pCallback) {
	mpCallback = pCallback;
}

/**
 * @param man_id			男士id
 */
/**
 * 3.5.删除已收藏男士（http post）
 * @param man_ids			男士ids
 */
void RequestManRemoveFavourTask::SetParam(list<string> man_ids) {
	char temp[16];
	mHttpEntiy.Reset();

	string man_ids_value = "";
	if( man_ids.size() > 0 ) {
		for(list<string>::iterator itr = man_ids.begin(); itr != man_ids.end(); itr++) {
			man_ids_value += *itr;
			man_ids_value += ",";
		}

		if( man_ids_value.length() > 0 ) {
			man_ids_value = man_ids_value.substr(0, man_ids_value.length() - 1);
		}

		mHttpEntiy.AddContent(MAN_REMOVE_FAVOUR_MAN_IDS, man_ids_value);
	}

	FileLog("httprequest", "RequestManRemoveFavourTask::SetParam( "
			"man_ids : %s, "
			")",
			man_ids_value.c_str()
			);
}

bool RequestManRemoveFavourTask::HandleCallback(const string& url, bool requestRet, const char* buf, int size) {
	FileLog("httprequest", "RequestManRemoveFavourTask::HandleResult( "
			"url : %s,"
			"requestRet : %s "
			")",
			url.c_str(),
			requestRet?"true":"false"
			);

	if (size < MAX_LOG_BUFFER) {
		FileLog("httprequest", "RequestManRemoveFavourTask::HandleResult( buf( %d ) : %s )", size, buf);
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
		mpCallback->OnRemoveFavourites(bFlag, errnum, errmsg, this);
	}

	return bFlag;
}
