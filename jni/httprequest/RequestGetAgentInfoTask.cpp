/*
 * RequestGetAgentInfoTask.cpp
 *
 *  Created on: 2015-11-20
 *      Author: Samson
 */
#include "RequestGetAgentInfoTask.h"

RequestGetAgentInfoTask::RequestGetAgentInfoTask()
{
	mUrl = GET_AGENTINFO_PATH;
	mpCallback = NULL;
}

RequestGetAgentInfoTask::~RequestGetAgentInfoTask()
{

}


void RequestGetAgentInfoTask::setCallback(IRequestGetAgentInfoTaskCallback* pCallback)
{
	mpCallback = pCallback;
}

bool RequestGetAgentInfoTask::HandleCallback(const string& url, bool requestRet, const char* buf, int size)
{
	FileLog("httprequest", "RequestGetAgentInfoTask::HandleResult( "
			"url : %s,"
			"requestRet : %s "
			")",
			url.c_str(),
			requestRet?"true":"false"
			);

	if (size < MAX_LOG_BUFFER) {
		FileLog("httprequest", "RequestGetAgentInfoTask::HandleResult( buf( %d ) : %s )", size, buf);
	}

	bool isSuccess = false;
	string errnum;
	string errmsg;
	AgentInfoItem item;
	if(requestRet)
	{
		Json::Value data;
		if(HandleResult(buf, size, errnum, errmsg, &data, NULL)) {
			isSuccess = item.Parsing(data);
		}else{
			errnum = LOCAL_ERROR_CODE_PARSEFAIL;
			errmsg = LOCAL_ERROR_CODE_PARSEFAIL_DESC;
		}
	}
	else {
		errnum = LOCAL_ERROR_CODE_TIMEOUT;
		errmsg = LOCAL_ERROR_CODE_TIMEOUT_DESC;
	}

	if(mpCallback != NULL) {
		mpCallback->onGetAgentInfoCallback(isSuccess, errnum, errmsg, item, this);
	}

	return isSuccess;
}
