/*
 * RequestLCGetVoiceTask.cpp
 *
 *  Created on: 2015-10-12
 *      Author: Samson
 * Description: 获取语音文件
 */

#include "RequestLCGetVoiceTask.h"

RequestLCGetVoiceTask::RequestLCGetVoiceTask()
{
	// TODO Auto-generated constructor stub
	mUrl = "";
	m_filePath = "";
}

RequestLCGetVoiceTask::~RequestLCGetVoiceTask()
{
	// TODO Auto-generated destructor stub
}

void RequestLCGetVoiceTask::SetCallback(IRequestLCGetVoiceCallback* pCallback)
{
	mpCallback = pCallback;
}

void RequestLCGetVoiceTask::SetParam(const string& voiceId, OTHER_SITE_TYPE siteId, const string& filePath)
{
	mHttpEntiy.Reset();
	char temp[2048] = {0};

	// 生成url
	snprintf(temp, sizeof(temp), LC_PLAYVOICE_PATH, voiceId.c_str(), GetSiteId(siteId));
	mUrl = temp;

	m_filePath = filePath;

	// 删除文件
	remove(m_filePath.c_str());

	FileLog("httprequest", "RequestLCGetVoiceTask::SetParam( "
			"url: %s, "
			"filePath: %s"
			")",
			mUrl.c_str(),
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

	// 返回文件数据不打印
//	if (size < MAX_LOG_BUFFER) {
//		FileLog("httprequest", "RequestLCGetVoiceTask::HandleResult( buf( %d ) : %s )", size, buf);
//	}

	string errnum = "";
	string errmsg = "";
	bool bFlag = false;

	if (requestRet) {
		// request success
		string contentType = GetContentType();
		if (string::npos != contentType.find("image")) {
			string tempPath = m_filePath + ".tmp";
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
				else {
					FileLog("httprequest", "RequestLCGetVoiceTask::HandleResult"
								"( url:%s, tempPath:%s, totalSize:%d, fileSize:%d ) file size error",
								url.c_str(), tempPath.c_str(), total, fileSize);
				}
			}
			else {
				FileLog("httprequest", "RequestLCGetVoiceTask::HandleResult"
							"( url:%s, tempPath:%s ) open file fail",
							url.c_str(), tempPath.c_str());
			}
		}
		else {
			FileLog("httprequest", "RequestLCGetVoiceTask::HandleResult"
						"( url:%s, contentType:%s ) file type error",
						url.c_str(), contentType.c_str());
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
		mpCallback->OnGetPhoto(bFlag, errnum, errmsg, m_filePath, this);
	}

	return bFlag;
}

