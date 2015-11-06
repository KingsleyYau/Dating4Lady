/*
 * RequestLCInviteTemplateTask.cpp
 *
 *  Created on: 2015-10-12
 *      Author: Samson
 * Description: 查询系统邀请模板列表
 */

#include "RequestLCInviteTemplateTask.h"

RequestLCInviteTemplateTask::RequestLCInviteTemplateTask()
{
	// TODO Auto-generated constructor stub
	mUrl = LC_INVITETEMPLATE_PATH;
}

RequestLCInviteTemplateTask::~RequestLCInviteTemplateTask()
{
	// TODO Auto-generated destructor stub
}

void RequestLCInviteTemplateTask::SetCallback(IRequestLCInviteTemplateCallback* pCallback)
{
	mpCallback = pCallback;
}

bool RequestLCInviteTemplateTask::HandleCallback(const string& url, bool requestRet, const char* buf, int size)
{
	FileLog("httprequest", "RequestLCInviteTemplateTask::HandleResult( "
			"url : %s,"
			"requestRet : %s "
			")",
			url.c_str(),
			requestRet?"true":"false"
			);

	if (size < MAX_LOG_BUFFER) {
		FileLog("httprequest", "RequestLCInviteTemplateTask::HandleResult( buf( %d ) : %s )", size, buf);
	}

	string errnum = "";
	string errmsg = "";
	bool bFlag = false;
	LiveChatInviteTemplateList theList;
	bool bContinue = true;
	if (requestRet) {
		// request success
		Json::Value dataJson;
		if( HandleResult(buf, size, errnum, errmsg, &dataJson, NULL, &bContinue) ) {
			bFlag = true;

			if ( dataJson[COMMON_DATA_LIST].isArray() ) {
				for(int i = 0; i < dataJson[COMMON_DATA_LIST].size(); i++ ) {
					LiveChatInviteTemplateListItem item;
					if ( item.Parse(dataJson[COMMON_DATA_LIST].get(i, Json::Value::null)) )
					{
						theList.push_back(item);
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
		mpCallback->OnInviteTemplate(bFlag, errnum, errmsg, theList, this);
	}
	return bFlag;
}

