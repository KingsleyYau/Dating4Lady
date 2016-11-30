/*
 * RequestLCMagicIconConfigTask.h
 *
 *  Created on: 2016-10-9
 *      Author: Hunter
 * Description: 获取小高表配置
 */

#include "RequestLCMagicIconConfigTask.h"

RequestLCMagicIconConfigTask::RequestLCMagicIconConfigTask()
{
	// TODO Auto-generated constructor stub
	mUrl = LC_GETMAGICICONCONFIG_PATH;
	mSiteType = WebSite;
}

RequestLCMagicIconConfigTask::~RequestLCMagicIconConfigTask()
{
	// TODO Auto-generated destructor stub
}

void RequestLCMagicIconConfigTask::SetCallback(IRequestLCMagicIconConfigCallback* pCallback)
{
	mpCallback = pCallback;
}

bool RequestLCMagicIconConfigTask::HandleCallback(const string& url, bool requestRet, const char* buf, int size)
{
	FileLog("httprequest", "RequestLCMagicIconConfigTask::HandleResult( "
			"url : %s,"
			"requestRet : %s "
			")",
			url.c_str(),
			requestRet?"true":"false"
			);

	if (size < MAX_LOG_BUFFER) {
		FileLog("httprequest", "RequestLCMagicIconConfigTask::HandleResult( buf( %d ) : %s )", size, buf);
	}

	string errnum = "";
	string errmsg = "";
	bool bFlag = false;
	MagicIconConfig magicIconConfig;
	bool bContinue = true;

	if(requestRet){
		TiXmlDocument doc;
		HandleResult(buf, size, errnum, errmsg, doc, &bContinue);
		if( bContinue && !doc.Error() ) {
			TiXmlNode *rootNode = doc.FirstChild(COMMON_ROOT);
			if (NULL != rootNode) {
				if(magicIconConfig.parsing(rootNode)){
					bFlag = true;
				}
			}
		}
	}else{
		errnum = LOCAL_ERROR_CODE_TIMEOUT;
		errmsg = LOCAL_ERROR_CODE_TIMEOUT_DESC;
	}

	if( bContinue && mpCallback != NULL ) {
		mpCallback->OnGetMagicIconConfig(bFlag, errnum, errmsg, magicIconConfig, this);
	}
	return bFlag;
}

