/*
 * RequestLCGetVideoPhotoTask.cpp
 *
 *  Created on: 2016-01-06
 *      Author: Max
 * Description: 5.14.	获取微视频图片
 */

#include "RequestLCGetVideoPhotoTask.h"

RequestLCGetVideoPhotoTask::RequestLCGetVideoPhotoTask()
{
	// TODO Auto-generated constructor stub
	mUrl = LC_GETVIDEOPHOTO_PATH;
	mSiteType = WebSite;
	m_videoId = "";
	m_filePath = "";
	m_sizeType = GETVIDEOPHOTO_SIZE_TYPE_FUZZY;
}

RequestLCGetVideoPhotoTask::~RequestLCGetVideoPhotoTask()
{
	// TODO Auto-generated destructor stub
}

void RequestLCGetVideoPhotoTask::SetCallback(IRequestLCGetVideoPhotoCallback* pCallback)
{
	mpCallback = pCallback;
}

void RequestLCGetVideoPhotoTask::SetParam(
				const string& targetId,
				const string& videoId,
				GETVIDEOPHOTO_SIZE_TYPE sizeType,
				const string& sid,
				const string& userId,
				const string& filePath
				)
{
	mHttpEntiy.Reset();
	mHttpEntiy.SetGetMethod(true);
	char temp[32] = {0};

	// targetId
	if( targetId.length() > 0 ) {
		mHttpEntiy.AddContent(LC_GETVIDEOPHOTO_TARGETID, targetId.c_str());
	}

	// sid
	if( sid.length() > 0 ) {
		mHttpEntiy.AddContent(LC_USER_SID, sid.c_str());
	}

	// userId
	if( userId.length() > 0 ) {
		mHttpEntiy.AddContent(LC_USER_ID, userId.c_str());
	}

	// videoId
	m_videoId = videoId;
	if( videoId.length() > 0 ) {
		mHttpEntiy.AddContent(LC_GETVIDEOPHOTO_VIDEOID, videoId.c_str());
	}

	// sizeType
	m_sizeType = sizeType;
	string strSizeType("");
	if (GETVIDEOPHOTO_SIZE_TYPE_FUZZY <= m_sizeType && m_sizeType <= GETVIDEOPHOTO_SIZE_TYPE_CLEAR) {
		snprintf(temp, sizeof(temp), "%d", m_sizeType);
		strSizeType = temp;
		mHttpEntiy.AddContent(LC_GETVIDEOPHOTO_SIZE, strSizeType.c_str());
	}

	m_filePath = filePath;

	// 删除文件
	remove(m_filePath.c_str());

	FileLog("httprequest", "RequestLCGetVideoPhotoTask::SetParam( "
			"targetId: %s, "
			"sid: %s, "
			"userId: %s, "
			"videoId: %s, "
			"sizeType: %s, "
			"filePath: %s"
			")",
			targetId.c_str(),
			sid.c_str(),
			userId.c_str(),
			videoId.c_str(),
			strSizeType.c_str(),
			m_filePath.c_str()
			);
}

bool RequestLCGetVideoPhotoTask::HandleCallback(const string& url, bool requestRet, const char* buf, int size)
{
	FileLog("httprequest", "RequestLCGetVideoPhotoTask::HandleResult( "
			"url : %s,"
			"requestRet : %s "
			")",
			url.c_str(),
			requestRet?"true":"false"
			);

	// 返回文件数据不打印
//	if (size < MAX_LOG_BUFFER) {
//		FileLog("httprequest", "RequestLCGetVideoPhotoTask::HandleResult( buf( %d ) : %s )", size, buf);
//	}

	string errnum = "";
	string errmsg = "";
	bool bFlag = false;
	bool bContinue = true;
	if (requestRet) {
		// request success
		string contentType = GetContentType();
		if (string::npos != contentType.find("image")) {
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
				else {
					FileLog("httprequest", "RequestLCGetVideoPhotoTask::HandleResult"
								"( url:%s, tempPath:%s, totalSize:%d, fileSize:%d ) file size error",
								url.c_str(), tempPath.c_str(), total, fileSize);
				}
			}
			else {
				FileLog("httprequest", "RequestLCGetVideoPhotoTask::HandleResult"
							"( url:%s, tempPath:%s ) open file fail",
							url.c_str(), tempPath.c_str());
			}
		}
		else {
			FileLog("httprequest", "RequestLCGetVideoPhotoTask::HandleResult"
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

	if( bContinue && mpCallback != NULL ) {
		mpCallback->OnGetVideoPhoto(bFlag, errnum, errmsg, m_videoId, m_sizeType, m_filePath, this);
	}
	return bFlag;
}

void RequestLCGetVideoPhotoTask::onReceiveBody(long requestId, string path, const char* buf, int size)
{
	string tempPath = GetTempFilePath();
	FILE* pFile = fopen(tempPath.c_str(), "a+b");
	if (NULL != pFile) {
		fwrite(buf, 1, size, pFile);
		fclose(pFile);

		FileLog("httprequest", "RequestLCGetVideoPhotoTask::onReceiveBody"
					"( write file requestId:%ld, path:%s, filePath:%s, tempPath:%s )",
					requestId, path.c_str(), m_filePath.c_str(), tempPath.c_str());
	}
	else {
		FileLog("httprequest", "RequestLCGetVideoPhotoTask::onReceiveBody"
					"( open file fail, requestId:%ld, path:%s, filePath:%s, tempPath:%s )",
					requestId, path.c_str(), m_filePath.c_str(), tempPath.c_str());
	}
}

string RequestLCGetVideoPhotoTask::GetTempFilePath()
{
	return m_filePath + ".tmp";
}
