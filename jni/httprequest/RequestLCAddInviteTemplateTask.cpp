
/*
 * RequestLCAddInviteTemplateTask.cpp
 *
 *  Created on: 2015-10-12
 *      Author: Samson
 * Description: 新建邀请模板
 */

#include "RequestLCAddInviteTemplateTask.h"

RequestLCAddInviteTemplateTask::RequestLCAddInviteTemplateTask()
{
	// TODO Auto-generated constructor stub
	mUrl = LC_ADDINVITETEMPLATE_PATH;
}

RequestLCAddInviteTemplateTask::~RequestLCAddInviteTemplateTask()
{
	// TODO Auto-generated destructor stub
}

void RequestLCAddInviteTemplateTask::SetCallback(IRequestLCAddInviteTemplateCallback* pCallback)
{
	mpCallback = pCallback;
}

void RequestLCAddInviteTemplateTask::SetParam(const string& tempContent
												, bool isInviteAssistant)
{
	mHttpEntiy.Reset();

	if( tempContent.length() > 0 ) {
		mHttpEntiy.AddContent(LC_ADDINVITETEMPLATE_CONTENT, tempContent.c_str());
	}

	char temp[16];
	sprintf(temp, "%d", isInviteAssistant?1:0);
	mHttpEntiy.AddContent(LC_ADDINVITETEMPLATE_AUTOINVITEFLAG, temp);

	FileLog("httprequest", "RequestLCAddInviteTemplateTask::SetParam( "
			"tempContent : %s ,"
			"isInviteAssistant : %s"
			")",
			tempContent.c_str(),
			isInviteAssistant?"true":"false"
			);
}

bool RequestLCAddInviteTemplateTask::HandleCallback(const string& url, bool requestRet, const char* buf, int size)
{
	FileLog("httprequest", "RequestLCAddInviteTemplateTask::HandleResult( "
			"url : %s,"
			"requestRet : %s "
			")",
			url.c_str(),
			requestRet?"true":"false"
			);

	if (size < MAX_LOG_BUFFER) {
		FileLog("httprequest", "RequestLCAddInviteTemplateTask::HandleResult( buf( %d ) : %s )", size, buf);
	}

	string errnum = "";
	string errmsg = "";
	bool bFlag = false;
	bool bContinue = true;

	if (requestRet) {
		// request success
		bFlag = HandleResult(buf, size, errnum, errmsg, NULL, NULL, &bContinue);
	} else {
		// request fail
		errnum = LOCAL_ERROR_CODE_TIMEOUT;
		errmsg = LOCAL_ERROR_CODE_TIMEOUT_DESC;
	}

	if( bContinue && mpCallback != NULL ) {
		mpCallback->OnAddInviteTemplate(bFlag, errnum, errmsg, this);
	}

	return bFlag;
}
