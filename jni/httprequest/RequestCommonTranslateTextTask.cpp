/*
 * RequestCommonTranslateTextTask.cpp
 *
 *  Created on: 2015-10-12
 *      Author: Samson
 * Description: 发送私密照片
 */

#include "RequestCommonTranslateTextTask.h"
#include <common/CommonFunc.h>

RequestCommonTranslateTextTask::RequestCommonTranslateTextTask()
{
	// TODO Auto-generated constructor stub
	mUrl = TRANSLATETEXT_PATH;
	mSiteType = TransSite;
}

RequestCommonTranslateTextTask::~RequestCommonTranslateTextTask()
{
	// TODO Auto-generated destructor stub
}

void RequestCommonTranslateTextTask::SetCallback(IRequestCommonTranslateTextCallback* pCallback)
{
	mpCallback = pCallback;
}

void RequestCommonTranslateTextTask::SetParam(const string& appId, const string& from, const string& to, const string& text)
{
	mHttpEntiy.Reset();
	mHttpEntiy.SetGetMethod(true);

	mUrl += "?";
	mUrl += TRANSLATETEXT_APPID;
	mUrl += "=";
	mUrl += appId;

	mUrl += "&";
	mUrl += TRANSLATETEXT_FROM;
	mUrl += "=";
	mUrl += from;

	mUrl += "&";
	mUrl += TRANSLATETEXT_TO;
	mUrl += "=";
	mUrl += to;

	mUrl += "&";
	mUrl += TRANSLATETEXT_TEXT;
	mUrl += "=";
	mUrl += text;

	mUrl += "&";
	mUrl += TRANSLATETEXT_CONTENTTYPE;
	mUrl += "=";
	mUrl += TRANSLATETEXT_CONTENTTYPE_VALUE;

//	if( !appId.empty() ) {
//		mHttpEntiy.AddContent(TRANSLATETEXT_APPID, appId.c_str());
//	}

//	if( !from.empty() ) {
//		mHttpEntiy.AddContent(TRANSLATETEXT_FROM, from.c_str());
//	}
//
//	if( !to.empty() ) {
//		mHttpEntiy.AddContent(TRANSLATETEXT_TO, to.c_str());
//	}

//	if (!text.empty()) {
//		mHttpEntiy.AddContent(TRANSLATETEXT_TEXT, text.c_str());
//	}

//	mHttpEntiy.AddContent(TRANSLATETEXT_CONTENTTYPE, TRANSLATETEXT_CONTENTTYPE_VALUE);

	FileLog("httprequest", "RequestCommonTranslateTextTask::SetParam( "
			"appId: %s, "
			"from: %s, "
			"to: %s, "
			"text:%s"
			")",
			appId.c_str(),
			from.c_str(),
			to.c_str(),
			text.c_str()
			);
}

bool RequestCommonTranslateTextTask::HandleCallback(const string& url, bool requestRet, const char* buf, int size)
{
	FileLog("httprequest", "RequestCommonTranslateTextTask::HandleResult( "
			"url : %s,"
			"requestRet : %s "
			")",
			url.c_str(),
			requestRet?"true":"false"
			);

	if (size < MAX_LOG_BUFFER) {
		FileLog("httprequest", "RequestCommonTranslateTextTask::HandleResult( buf( %d ) : %s )", size, buf);
	}

	bool bFlag = false;
	string text = "";
	if (requestRet) {
		// request success
		TiXmlDocument doc;
		bool XMLParse = doc.Parse(buf);
		if( !doc.Error() )
		{
			TiXmlNode *stringNode = doc.FirstChild(TRANSLATETEXT_STRING);
			if (NULL != stringNode) {
				TiXmlElement* stringElement = stringNode->ToElement();
				if (NULL != stringElement) {
					const char* value = stringElement->GetText();
					if (NULL != value) {
						text = value;

						bFlag = true;
					}
				}
			}
		}
	}

	if( NULL != mpCallback ) {
		mpCallback->OnTranslateText(bFlag, text, this);
	}
	return bFlag;
}

