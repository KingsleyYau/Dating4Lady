/*
 * RequestLCSendVoiceTask.cpp
 *
 *  Created on: 2015-10-12
 *      Author: Samson
 * Description: 上传语音文件
 */

#include "RequestLCSendVoiceTask.h"

RequestLCSendVoiceTask::RequestLCSendVoiceTask()
{
	// TODO Auto-generated constructor stub
	mUrl = "";
	mSiteType = ChatVoiceSite;
}

RequestLCSendVoiceTask::~RequestLCSendVoiceTask()
{
	// TODO Auto-generated destructor stub
}

void RequestLCSendVoiceTask::SetCallback(IRequestLCSendVoiceCallback* pCallback)
{
	mpCallback = pCallback;
}

void RequestLCSendVoiceTask::SetParam(const string& voiceCode
				, const string& inviteId
				, const string& manId
				, const string& womanId
				, const string& fileType
				, int voiceLen
				, const string& siteId
				, const string& filePath)
{
	char temp[2048] = {0};
	mHttpEntiy.Reset();

	// inviteId
	if( !inviteId.empty() ) {
		mHttpEntiy.AddContent(LC_UPLOADVOICE_INVITEID, inviteId.c_str());
	}

	// sex(女士)
	mHttpEntiy.AddContent(LC_UPLOADVOICE_SEX, LC_UPLOADVOICE_SEX_WOMAN);

	// manId
	if( !manId.empty() ) {
		mHttpEntiy.AddContent(LC_UPLOADVOICE_MANID, manId.c_str());
	}

	// womanId
	if( !womanId.empty() ) {
		mHttpEntiy.AddContent(LC_UPLOADVOICE_WOMANID, womanId.c_str());
	}

	// flieType
	if( !fileType.empty() ) {
		mHttpEntiy.AddContent(LC_UPLOADVOICE_FILETYPE, fileType.c_str());
	}

	// voiceLen
	snprintf(temp, sizeof(temp), "%d", voiceLen);
	string strVoiceLen("");
	strVoiceLen = temp;
	mHttpEntiy.AddContent(LC_UPLOADVOICE_VOICELENGTH, strVoiceLen.c_str());

	// siteId
	if ( !siteId.empty() ) {
		mHttpEntiy.AddContent(LC_UPLOADVOICE_SITEID, siteId.c_str());
	}

	// filePath
	mHttpEntiy.AddFile(LC_UPLOADVOICE_VOICEFILE, filePath.c_str());

	// url
	snprintf(temp, sizeof(temp), LC_UPLOADVOICE_PATH, voiceCode.c_str());
	mUrl = temp;

	FileLog("httprequest", "RequestLCSendVoiceTask::SetParam( "
			"url: %s, "
			"voiceCode: %s, "
			"inviteId: %s, "
			"manId: %s, "
			"womanId: %s, "
			"fileType: %s, "
			"voiceLen: %s, "
			"siteId: %s, "
			"filePath: %s"
			")",
			mUrl.c_str(),
			voiceCode.c_str(),
			inviteId.c_str(),
			manId.c_str(),
			womanId.c_str(),
			fileType.c_str(),
			strVoiceLen.c_str(),
			siteId.c_str(),
			filePath.c_str()
			);
}

bool RequestLCSendVoiceTask::HandleCallback(const string& url, bool requestRet, const char* buf, int size)
{
	FileLog("httprequest", "RequestLCSendVoiceTask::HandleResult( "
			"url : %s,"
			"requestRet : %s "
			")",
			url.c_str(),
			requestRet?"true":"false"
			);

	if (size < MAX_LOG_BUFFER) {
		FileLog("httprequest", "RequestLCSendVoiceTask::HandleResult( buf( %d ) : %s )", size, buf);
	}

	string errnum = "";
	string errmsg = "";
	bool bFlag = false;
	string voiceId = "";
	if (requestRet) {
		// request success
		if (size < 512) {
			voiceId.append(buf, size);
			bFlag = true;
		}
		else {
			errnum = LOCAL_ERROR_CODE_PARSEFAIL;
			errmsg = LOCAL_ERROR_CODE_PARSEFAIL_DESC;
		}
	} else {
		// request fail
		errnum = LOCAL_ERROR_CODE_TIMEOUT;
		errmsg = LOCAL_ERROR_CODE_TIMEOUT_DESC;
	}

	if( mpCallback != NULL ) {
		mpCallback->OnSendVoice(bFlag, errnum, errmsg, voiceId, this);
	}

	return bFlag;
}

