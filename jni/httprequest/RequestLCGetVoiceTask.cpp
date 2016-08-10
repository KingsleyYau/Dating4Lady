/*
 * RequestLCGetVoiceTask.cpp
 *
 *  Created on: 2015-10-12
 *      Author: Samson
 * Description: 获取语音文件
 */

#include "RequestLCGetVoiceTask.h"
#include <common/CommonFunc.h>

RequestLCGetVoiceTask::RequestLCGetVoiceTask()
{
	// TODO Auto-generated constructor stub
	mUrl = "";
	m_filePath = "";
	mSiteType = ChatVoiceSite;
}

RequestLCGetVoiceTask::~RequestLCGetVoiceTask()
{
	// TODO Auto-generated destructor stub
}

void RequestLCGetVoiceTask::SetCallback(IRequestLCGetVoiceCallback* pCallback)
{
	mpCallback = pCallback;
}

void RequestLCGetVoiceTask::SetParam(const string& voiceId, const string& siteId, const string& filePath)
{
	mHttpEntiy.Reset();
	char temp[2048] = {0};

	// 设置为http get
	mHttpEntiy.SetGetMethod(true);

	// 生成url
	snprintf(temp, sizeof(temp), LC_PLAYVOICE_PATH, voiceId.c_str(), siteId.c_str());
	mUrl = temp;

	// 设置文件路径
	m_filePath = filePath;
	// 删除文件
	RemoveFile(m_filePath);
	// 删除临时文件
	RemoveFile(GetTempFilePath());

	FileLog("httprequest", "RequestLCGetVoiceTask::SetParam( "
			"url: %s, "
			"voiceId: %s, "
			"siteId: %s, "
			"filePath: %s"
			")",
			mUrl.c_str(),
			voiceId.c_str(),
			siteId.c_str(),
			m_filePath.c_str()
			);
}

bool RequestLCGetVoiceTask::HandleCallback(const string& url, bool requestRet, const char* buf, int size)
{
	FileLog("httprequest", "RequestLCGetVoiceTask::HandleResult( "
			"url : %s,"
			"requestRet : %s "
			")",
			url.c_str(),
			requestRet?"true":"false"
			);

	string errnum = "";
	string errmsg = "";
	bool bFlag = false;

	if (requestRet) {
		// request success
		// 判断是否音频类型
		string contentType = GetContentType();
		if ( string::npos != contentType.find("mpeg")
			|| string::npos != contentType.find("aac")
			|| string::npos != contentType.find("audio"))
		{
			string tempPath = GetTempFilePath();
			FILE* file = fopen(tempPath.c_str(), "rb");
			if (NULL != file) {
				fseek(file, 0, SEEK_END);
				size_t fileSize = ftell(file);
				fclose(file);
				file = NULL;

				int recv = 0;
				int total = 0;
				GetRecvDataCount(total, recv);
				if (total <= 0
					|| fileSize == (size_t)total)
				{
					bFlag = true;
					rename(tempPath.c_str(), m_filePath.c_str());
				}
				else
				{
					FileLog("httprequest", "RequestLCGetVoiceTask::HandleResult() fileSize:%d, total:%d", fileSize, total);
				}
			}
			else {
				FileLog("httprequest", "RequestLCGetVoiceTask::HandleResult() open file fail, filePath:%s", tempPath.c_str());
			}
		}
		else {
			FileLog("httprequest", "RequestLCGetVoiceTask::HandleResult() content type error, contentType:%s", contentType.c_str());
		}

		if (!bFlag) {
			errnum = LOCAL_ERROR_CODE_FILEOPTFAIL;
			errmsg = LOCAL_ERROR_CODE_FILEOPTFAIL_DESC;
			remove(m_filePath.c_str());
		}
	} else {
		// request fail
		errnum = LOCAL_ERROR_CODE_TIMEOUT;
		errmsg = LOCAL_ERROR_CODE_TIMEOUT_DESC;
	}

	if( mpCallback != NULL ) {
		mpCallback->OnGetVoice(bFlag, errnum, errmsg, m_filePath, this);
	}

	return bFlag;
}

void RequestLCGetVoiceTask::onReceiveBody(long requestId, string path, const char* buf, int size)
{
	string tempPath = GetTempFilePath();
	FILE* pFile = fopen(tempPath.c_str(), "a+b");
	if (NULL != pFile) {
		fwrite(buf, 1, size, pFile);
		fclose(pFile);

		FileLog("httprequest", "RequestLCGetVoiceTask::onReceiveBody"
					"( write file requestId:%ld, path:%s, filePath:%s, tempPath:%s )",
					requestId, path.c_str(), m_filePath.c_str(), tempPath.c_str());
	}
	else {
		FileLog("httprequest", "RequestLCGetVoiceTask::onReceiveBody"
					"( open file fail, requestId:%ld, path:%s, filePath:%s, tempPath:%s )",
					requestId, path.c_str(), m_filePath.c_str(), tempPath.c_str());
	}
}

string RequestLCGetVoiceTask::GetTempFilePath()
{
	return m_filePath + ".tmp";
}

