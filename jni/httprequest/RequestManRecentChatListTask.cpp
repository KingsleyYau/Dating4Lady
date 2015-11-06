/*
 * RequestManRecentChatListTask.cpp
 *
 *  Created on: 2015-9-16
 *      Author: Max
 */

#include "RequestManRecentChatListTask.h"

RequestManRecentChatListTask::RequestManRecentChatListTask() {
	// TODO Auto-generated constructor stub
	mUrl = MAN_RECENT_CHAT_LIST_PATH;
}

RequestManRecentChatListTask::~RequestManRecentChatListTask() {
	// TODO Auto-generated destructor stub
}

void RequestManRecentChatListTask::SetCallback(IRequestManRecentChatListCallback* pCallback) {
	mpCallback = pCallback;
}

/**
 * 3.6.获取最近聊天男士列表（http post）
 * @param pageIndex			当前页数
 * @param pageSize			每页行数
 * @param query_type		查询类型
 */
void RequestManRecentChatListTask::SetParam(
		int pageIndex,
		int pageSize,
		RECENT_CHAT_QUERYTYPE query_type
		) {

	char temp[16];
	mHttpEntiy.Reset();

	sprintf(temp, "%d", pageIndex);
	mHttpEntiy.AddContent(COMMON_PAGE_INDEX, temp);

	sprintf(temp, "%d", pageSize);
	mHttpEntiy.AddContent(COMMON_PAGE_SIZE, temp);

	sprintf(temp, "%d", query_type);
	mHttpEntiy.AddContent(MAN_RECENT_CHAT_LIST_QUERY_TYPE, temp);

	FileLog("httprequest", "RequestManRecentChatListTask::SetParam( "
			"pageIndex : %d, "
			"pageSize : %d, "
			"query_type : %d "
			")",
			pageIndex,
			pageSize,
			query_type
			);
}

bool RequestManRecentChatListTask::HandleCallback(const string& url, bool requestRet, const char* buf, int size) {
	FileLog("httprequest", "RequestManRecentChatListTask::HandleResult( "
			"url : %s,"
			"requestRet : %s "
			")",
			url.c_str(),
			requestRet?"true":"false"
			);

	if (size < MAX_LOG_BUFFER) {
		FileLog("httprequest", "RequestManRecentChatListTask::HandleResult( buf( %d ) : %s )", size, buf);
	}

	list<ManRecentChatListItem> itemList;
	int totalCount = 0;
	string errnum = "";
	string errmsg = "";
	bool bFlag = false;
	bool bContinue = true;
	if (requestRet) {
		// request success
		Json::Value dataJson;
		if( HandleResult(buf, size, errnum, errmsg, &dataJson, NULL, &bContinue) ) {
			if( dataJson[COMMON_DATA_COUNT].isInt() ) {
				totalCount = dataJson[COMMON_DATA_COUNT].asInt();
			}

			if( dataJson[COMMON_DATA_LIST].isArray() ) {
				for(int i = 0; i < dataJson[COMMON_DATA_LIST].size(); i++ ) {
					ManRecentChatListItem item;
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
		mpCallback->OnQueryManRecentChatList(bFlag, errnum, errmsg, itemList, totalCount, this);
	}

	return bFlag;
}
