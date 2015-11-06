/*
 * RequestManListTask.cpp
 *
 *  Created on: 2015-9-16
 *      Author: Max
 */

#include "RequestManListTask.h"

RequestManListTask::RequestManListTask() {
	// TODO Auto-generated constructor stub
	mUrl = MAN_LIST_PATH;
}

RequestManListTask::~RequestManListTask() {
	// TODO Auto-generated destructor stub
}

void RequestManListTask::SetCallback(IRequestManListCallback* pCallback) {
	mpCallback = pCallback;
}

/**
 * @param email				电子邮箱
 * @param password			密码
 * @param deviceId			设备唯一标识
 * @param versioncode		客户端内部版本号
 * @param model				移动设备型号
 * @param manufacturer		制造厂商
 */
void RequestManListTask::SetParam(
		int pageIndex,
		int pageSize,
		QUERYTYPE query_type,
		const string& man_id,
		int from_age,
		int to_age,
		int country,
		bool photo
		) {

	char temp[16];
	mHttpEntiy.Reset();

	sprintf(temp, "%d", pageIndex);
	mHttpEntiy.AddContent(COMMON_PAGE_INDEX, temp);

	sprintf(temp, "%d", pageSize);
	mHttpEntiy.AddContent(COMMON_PAGE_SIZE, temp);

	switch(query_type) {
	case DEFAULT:{
		sprintf(temp, "%d", 1);
	}break;
	case BYID:{
		sprintf(temp, "%d", 9);
	}break;
	default:break;
	}
	mHttpEntiy.AddContent(MAN_LIST_QUERY_TYPE, temp);

	if( man_id.length() > 0 ) {
		mHttpEntiy.AddContent(MAN_LIST_MAN_ID, man_id.c_str());
	}

	sprintf(temp, "%d", photo?1:0);
	mHttpEntiy.AddContent(MAN_LIST_PHOTO, temp);

	if( from_age >= 0 ) {
		sprintf(temp, "%d", from_age);
		mHttpEntiy.AddContent(MAN_LIST_FROM_AGE, temp);
	}

	if( to_age >= 0 ) {
		sprintf(temp, "%d", to_age);
		mHttpEntiy.AddContent(MAN_LIST_TO_AGE, temp);
	}

	if( country >= 0 && country < _countof(CountryArray) ) {
		mHttpEntiy.AddContent(MAN_LIST_COUNTRY, CountryArray[country]);
	}

	FileLog("httprequest", "RequestManListTask::SetParam( "
			"pageIndex : %d, "
			"pageSize : %d, "
			"query_type : %d, "
			"man_id : %s, "
			"from_age : %d, "
			"to_age : %d "
			"country : %d, "
			"photo : %s"
			")",
			pageIndex,
			pageSize,
			query_type,
			man_id.c_str(),
			from_age,
			to_age,
			country,
			photo?"true":"false"
			);
}

bool RequestManListTask::HandleCallback(const string& url, bool requestRet, const char* buf, int size) {
	FileLog("httprequest", "RequestManListTask::HandleCallback( "
			"url : %s,"
			"requestRet : %s "
			")",
			url.c_str(),
			requestRet?"true":"false"
			);

	if (size < MAX_LOG_BUFFER) {
		FileLog("httprequest", "RequestManListTask::HandleCallback( buf( %d ) : %s )", size, buf);
	}

	list<ManListItem> itemList;
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
					ManListItem item;
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
		mpCallback->OnQueryManList(bFlag, errnum, errmsg, itemList, totalCount, this);
	}

	return bFlag;
}
