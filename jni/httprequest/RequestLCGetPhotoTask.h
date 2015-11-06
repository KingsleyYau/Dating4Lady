/*
 * RequestLCGetPhotoTask.h
 *
 *  Created on: 2015-10-12
 *      Author: Samson
 * Description: 获取对方私密照片
 */

#ifndef REQUESTLCGETPHOTOTASK_H_
#define REQUESTLCGETPHOTOTASK_H_

#include "RequestBaseTask.h"
#include "RequestLCDefine.h"

class RequestLCGetPhotoTask;

class IRequestLCGetPhotoCallback {
public:
	virtual ~IRequestLCGetPhotoCallback(){};
	virtual void OnGetPhoto(bool success
							, const string& errnum
							, const string& errmsg
							, const string& filePath
							, RequestLCGetPhotoTask* task) = 0;
};

class RequestLCGetPhotoTask : public RequestBaseTask {
public:
	RequestLCGetPhotoTask();
	virtual ~RequestLCGetPhotoTask();

	// set request param
	void SetParam(GETPHOTO_TOFLAG_TYPE toFlag
				, const string& targetId
				, const string& sid
				, const string& userId
				, const string& photoId
				, GETPHOTO_SIZE_TYPE sizeType
				, GETPHOTO_MODE_TYPE modeType
				, const string& filePath);

	// Implement RequestBaseTask
	bool HandleCallback(const string& url, bool requestRet, const char* buf, int size);

	void SetCallback(IRequestLCGetPhotoCallback* pCallback);

protected:
	IRequestLCGetPhotoCallback* mpCallback;
	string m_filePath;
};

#endif /* REQUESTLCGETPHOTOTASK_H_ */
