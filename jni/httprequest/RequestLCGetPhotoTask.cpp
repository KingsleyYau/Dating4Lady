/*
 * RequestLCGetPhotoTask.cpp
 *
 *  Created on: 2015-10-12
 *      Author: Samson
 * Description: 获取对方私密照片
 */

#include "RequestLCGetPhotoTask.h"

RequestLCGetPhotoTask::RequestLCGetPhotoTask()
{
	// TODO Auto-generated constructor stub
	mUrl = LC_GETPHOTO_PATH;
	mSiteType = WebSite;
	m_photoId = "";
	m_filePath = "";
	m_sizeType = GETPHOTO_SIZETYPE_LARGE;
	m_modeType = GETPHOTO_MODETYPE_FUZZY;
}

RequestLCGetPhotoTask::~RequestLCGetPhotoTask()
{
	// TODO Auto-generated destructor stub
}

void RequestLCGetPhotoTask::SetCallback(IRequestLCGetPhotoCallback* pCallback)
{
	mpCallback = pCallback;
}

void RequestLCGetPhotoTask::SetParam(
				GETPHOTO_TOFLAG_TYPE toFlag
				, const string& targetId
				, const string& sid
				, const string& userId
				, const string& photoId
				, GETPHOTO_SIZE_TYPE sizeType
				, GETPHOTO_MODE_TYPE modeType
				, const string& filePath)
{
	mHttpEntiy.Reset();
	char temp[32] = {0};

	// toflag
	string strToFlag("");
	snprintf(temp, sizeof(temp), "%d", toFlag);
	strToFlag = temp;
	mHttpEntiy.AddContent(LC_GETPHOTO_TOFLAG, strToFlag.c_str());

	// targetId
	if( targetId.length() > 0 ) {
		mHttpEntiy.AddContent(LC_GETPHOTO_TARGETID, targetId.c_str());
	}

	// sid
	if( sid.length() > 0 ) {
		mHttpEntiy.AddContent(LC_GETPHOTO_USERSID, sid.c_str());
	}

	// userId
	if( userId.length() > 0 ) {
		mHttpEntiy.AddContent(LC_GETPHOTO_USERID, userId.c_str());
	}

	// photoId
	m_photoId = photoId;
	if( photoId.length() > 0 ) {
		mHttpEntiy.AddContent(LC_GETPHOTO_PHOTOID, photoId.c_str());
	}

	// sizeType
	m_sizeType = sizeType;
	string strSizeType("");
	if (GETPHOTO_SIZETYPE_BEGIN <= m_sizeType && m_sizeType <= GETPHOTO_SIZETYPE_END) {
		strSizeType = GETPHOTO_PHOTOSIZE_PROTOCOL[m_sizeType];
		mHttpEntiy.AddContent(LC_GETPHOTO_SIZE, strSizeType.c_str());
	}

	// modeType
	m_modeType = modeType;
	string strModeType("");
	snprintf(temp, sizeof(temp), "%d", m_modeType);
	strModeType = temp;
	mHttpEntiy.AddContent(LC_GETPHOTO_MODE, strModeType.c_str());

	m_filePath = filePath;

	// 删除文件
	remove(m_filePath.c_str());

	FileLog("httprequest", "RequestLCGetPhotoTask::SetParam( "
			"toFlag: %s, "
			"targetId: %s, "
			"sid: %s, "
			"userId: %s, "
			"photoId: %s, "
			"sizeType: %s, "
			"modeType: %s, "
			"filePath: %s"
			")",
			strToFlag.c_str(),
			targetId.c_str(),
			sid.c_str(),
			userId.c_str(),
			photoId.c_str(),
			strSizeType.c_str(),
			strModeType.c_str(),
			m_filePath.c_str()
			);
}

bool RequestLCGetPhotoTask::HandleCallback(const string& url, bool requestRet, const char* buf, int size)
{
	FileLog("httprequest", "RequestLCGetPhotoTask::HandleResult( "
			"url : %s,"
			"requestRet : %s "
			")",
			url.c_str(),
			requestRet?"true":"false"
			);

	// 返回文件数据不打印
//	if (size < MAX_LOG_BUFFER) {
//		FileLog("httprequest", "RequestLCGetPhotoTask::HandleResult( buf( %d ) : %s )", size, buf);
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
					FileLog("httprequest", "RequestLCGetPhotoTask::HandleResult"
								"( url:%s, tempPath:%s, totalSize:%d, fileSize:%d ) file size error",
								url.c_str(), tempPath.c_str(), total, fileSize);
				}
			}
			else {
				FileLog("httprequest", "RequestLCGetPhotoTask::HandleResult"
							"( url:%s, tempPath:%s ) open file fail",
							url.c_str(), tempPath.c_str());
			}
		}
		else {
			FileLog("httprequest", "RequestLCGetPhotoTask::HandleResult"
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
		mpCallback->OnGetPhoto(bFlag, errnum, errmsg, m_photoId, m_sizeType, m_modeType, m_filePath, this);
	}
	return bFlag;
}

void RequestLCGetPhotoTask::onReceiveBody(long requestId, string path, const char* buf, int size)
{
	string tempPath = GetTempFilePath();
	FILE* pFile = fopen(tempPath.c_str(), "a+b");
	if (NULL != pFile) {
		fwrite(buf, 1, size, pFile);
		fclose(pFile);

		FileLog("httprequest", "RequestLCGetPhotoTask::onReceiveBody"
					"( write file requestId:%ld, path:%s, filePath:%s, tempPath:%s )",
					requestId, path.c_str(), m_filePath.c_str(), tempPath.c_str());
	}
	else {
		FileLog("httprequest", "RequestLCGetPhotoTask::onReceiveBody"
					"( open file fail, requestId:%ld, path:%s, filePath:%s, tempPath:%s )",
					requestId, path.c_str(), m_filePath.c_str(), tempPath.c_str());
	}
}

string RequestLCGetPhotoTask::GetTempFilePath()
{
	return m_filePath + ".tmp";
}

