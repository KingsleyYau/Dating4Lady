/*
 * RequestManRecentViewListTask.cpp
 *
 *  Created on: 2015-9-16
 *      Author: Max
 */

#include "RequestManRecentViewListTask.h"

RequestManRecentViewListTask::RequestManRecentViewListTask() {
	// TODO Auto-generated constructor stub
	mUrl = MAN_RECENT_VIEW_LIST_PATH;
}

RequestManRecentViewListTask::~RequestManRecentViewListTask() {
	// TODO Auto-generated destructor stub
}

void RequestManRecentViewListTask::SetCallback(IRequestManRecentViewListCallback* pCallback) {
	mpCallback = pCallback;
}

bool RequestManRecentViewListTask::HandleCallback(const string& url, bool requestRet, const char* buf, int size) {
	FileLog("httprequest", "RequestManRecentViewListTask::HandleResult( "
			"url : %s,"
			"requestRet : %s "
			")",
			url.c_str(),
			requestRet?"true":"false"
			);

	if (size < MAX_LOG_BUFFER) {
		FileLog("httprequest", "RequestManRecentViewListTask::HandleResult( buf( %d ) : %s )", size, buf);
	}

	list<ManRecentViewListItem> itemList;
	int totalCount = 0;
	string errnum = "";
	string errmsg = "";
	bool bFlag = false;
	bool bContinue = true;
	if (requestRet) {
		// request success
		Json::Value dataJson;
		if( HandleResult(buf, size, errnum, errmsg, &dataJson, NULL, &bContinue) ) {
			if( dataJson[COMMON_DATA_LIST].isArray() ) {
				for(int i = 0; i < dataJson[COMMON_DATA_LIST].size(); i++ ) {
					ManRecentViewListItem item;
					item.Parse(dataJson[COMMON_DATA_LIST].get(i, Json::Value::null));
					itemList.push_back(item);
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
		mpCallback->OnQueryRecentViewList(bFlag, errnum, errmsg, itemList, this);
	}

	return bFlag;
}
