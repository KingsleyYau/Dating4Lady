/*
 * RequestLCCheckFunctionsTask.cpp
 *
 *  Created on: 2016-7-12
 *      Author: Hunter
 * Description: 检测功能是否开通
 */

#include "RequestLCCheckFunctionsTask.h"

RequestLCCheckFunctionsTask::RequestLCCheckFunctionsTask()
{
	// TODO Auto-generated constructor stub
	mUrl = LC_CHECKFUNCTIONS_PATH;
	mSiteType = WebSite;
}

RequestLCCheckFunctionsTask::~RequestLCCheckFunctionsTask()
{
	// TODO Auto-generated destructor stub
}

void RequestLCCheckFunctionsTask::SetCallback(IRequestLCCheckFunctionsCallback* pCallback)
{
	mpCallback = pCallback;
}

// set request param
void RequestLCCheckFunctionsTask::SetParam(
		string functionIds,
		TDEVICE_TYPE deviceType,
		string versionCode,
		const string& sid,
		const string& userId
		) {
	mHttpEntiy.Reset();

	char temp[32] = {0};

	if ( !functionIds.empty() ) {
		mHttpEntiy.AddContent(LC_CHECKFUNCTIONS_FUNCTIONIDS, functionIds.c_str());
	}

	sprintf(temp, "%d", deviceType);
	mHttpEntiy.AddContent(LC_CHECKFUNCTIONS_DEVICETYPE, temp);

	if ( !versionCode.empty() ) {
		mHttpEntiy.AddContent(LC_CHECKFUNCTIONS_VERSIONCODE, versionCode.c_str());
	}

	if ( !sid.empty() ) {
		mHttpEntiy.AddContent(LC_USER_SID, sid.c_str());
	}

	if ( !userId.empty() ) {
		mHttpEntiy.AddContent(LC_USER_ID, userId.c_str());
	}

	FileLog("httprequest", "RequestLCCheckFunctionsTask::SetParam( "
			"functionIds : %s "
			"deviceType : %d "
			"versionCode : %s "
			"sid : %s "
			"userId : %s "
			")",
			functionIds.c_str(),
			deviceType,
			versionCode.c_str(),
			sid.c_str(),
			userId.c_str()
			);

}

bool RequestLCCheckFunctionsTask::HandleCallback(const string& url, bool requestRet, const char* buf, int size)
{
	FileLog("httprequest", "RequestLCCheckFunctionsTask::HandleResult( "
			"url : %s,"
			"requestRet : %s "
			")",
			url.c_str(),
			requestRet?"true":"false"
			);

	if (size < MAX_LOG_BUFFER) {
		FileLog("httprequest", "RequestLCCheckFunctionsTask::HandleResult( buf( %d ) : %s )", size, buf);
	}

	string errnum = "";
	string errmsg = "";
	bool bFlag = false;
	list<string> flagList;
	bool bContinue = true;
	if (requestRet) {
		// request success
		TiXmlDocument doc;
		const string split = ",";
		if(HandleResult(buf, size, errnum, errmsg, doc, &bContinue)){
			if( bContinue && !doc.Error() ) {
				TiXmlNode *rootNode = doc.FirstChild(COMMON_ROOT);
				TiXmlElement* dataElement = rootNode->FirstChildElement(LC_CHECKFUNCTIONS_DATA);
				if(NULL != dataElement){
					string flags = dataElement->GetText();
					size_t pos = 0;
					do {
						size_t cur = flags.find(split, pos);
						if (cur != string::npos) {
							string temp = flags.substr(pos, cur - pos);
							if (!temp.empty()) {
								flagList.push_back(temp);
							}
							pos = cur + 1;
						}
						else {
							string temp = flags.substr(pos);
							if (!temp.empty()) {
								flagList.push_back(temp);
							}
							break;
						}
					} while(true);
					if(flagList.size()>0){
						bFlag = true;
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
		mpCallback->OnCheckFunctions(bFlag, errnum, errmsg, flagList, this);
	}
	return bFlag;
}

