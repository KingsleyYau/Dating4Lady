/*
 * RequestLCSystemInviteTemplateTask.cpp
 *
 *  Created on: 2015-10-12
 *      Author: Samson
 */

#include "RequestLCSystemInviteTemplateTask.h"

RequestLCSystemInviteTemplateTask::RequestLCSystemInviteTemplateTask()
{
	// TODO Auto-generated constructor stub
	mUrl = LC_SYSTEMINVITETEMPLATE_PATH;
}

RequestLCSystemInviteTemplateTask::~RequestLCSystemInviteTemplateTask()
{
	// TODO Auto-generated destructor stub
}

void RequestLCSystemInviteTemplateTask::SetCallback(IRequestLCSystemInviteTemplateCallback* pCallback)
{
	mpCallback = pCallback;
}

bool RequestLCSystemInviteTemplateTask::HandleCallback(const string& url, bool requestRet, const char* buf, int size)
{
	FileLog("httprequest", "RequestLCSystemInviteTemplateTask::HandleResult( "
			"url : %s,"
			"requestRet : %s "
			")",
			url.c_str(),
			requestRet?"true":"false"
			);

	if (size < MAX_LOG_BUFFER) {
		FileLog("httprequest", "RequestLCSystemInviteTemplateTask::HandleResult( buf( %d ) : %s )", size, buf);
	}

	string errnum = "";
	string errmsg = "";
	bool bFlag = false;
	list<string> theList;
	bool bContinue = true;
	if (requestRet) {
		// request success
		Json::Value dataJson;
		if( HandleResult(buf, size, errnum, errmsg, &dataJson, NULL, &bContinue) ) {
			bFlag = true;

			if ( dataJson[COMMON_DATA_LIST].isArray() ) {
				for(int i = 0; i < dataJson[COMMON_DATA_LIST].size(); i++ ) {
					Json::Value item = dataJson[COMMON_DATA_LIST].get(i, Json::Value::null);
					if (item.isString()) {
						theList.push_back(item.asString());
					}
				}
			}
		}
	} else {
		// request fail
		errnum = LOCAL_ERROR_CODE_TIMEOUT;
		errmsg = LOCAL_ERROR_CODE_TIMEOUT_DESC;
	}

	if( bContinue && mpCallback != NULL ) {
		mpCallback->OnSystemInviteTemplate(bFlag, errnum, errmsg, theList, this);
	}

	return bFlag;
}
