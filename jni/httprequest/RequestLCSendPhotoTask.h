/*
 * RequestLCSendPhotoTask.h
 *
 *  Created on: 2015-10-12
 *      Author: Samson
 * Description: 发送私密照片
 */

#ifndef REQUESTLCSENDPHOTOTASK_H_
#define REQUESTLCSENDPHOTOTASK_H_

#include "RequestBaseTask.h"
#include "RequestLCDefine.h"

class RequestLCSendPhotoTask;

class IRequestLCSendPhotoCallback {
public:
	virtual ~IRequestLCSendPhotoCallback(){};
	virtual void OnSendPhoto(bool success
							, const string& errnum
							, const string& errmsg
							, const string& sendId
							, RequestLCSendPhotoTask* task) = 0;
};

class RequestLCSendPhotoTask : public RequestBaseTask {
public:
	RequestLCSendPhotoTask();
	virtual ~RequestLCSendPhotoTask();

	// set request param
	void SetParam(const string& targetId, const string& inviteId, const string& photoId, const string& sid, const string& userId);

	// Implement RequestBaseTask
	bool HandleCallback(const string& url, bool requestRet, const char* buf, int size);

	void SetCallback(IRequestLCSendPhotoCallback* pCallback);

protected:
	IRequestLCSendPhotoCallback* mpCallback;
};

#endif /* REQUESTLCSENDPHOTOTASK_H_ */
