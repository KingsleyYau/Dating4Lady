/*
 * RequestLCCheckSendPhotoTask.h
 *
 *  Created on: 2015-10-12
 *      Author: Samson
 * Description: 检测女士是否可发私密照
 */

#ifndef REQUESTLCCHECKSENDPHOTOTASK_H_
#define REQUESTLCCHECKSENDPHOTOTASK_H_

#include "RequestBaseTask.h"
#include "RequestLCDefine.h"

class RequestLCCheckSendPhotoTask;

class IRequestLCCheckSendPhotoCallback {
public:
	virtual ~IRequestLCCheckSendPhotoCallback(){};
	virtual void OnCheckSendPhoto(bool success
								, const string& errnum
								, const string& errmsg
								, RequestLCCheckSendPhotoTask* task) = 0;
};

class RequestLCCheckSendPhotoTask : public RequestBaseTask {
public:
	RequestLCCheckSendPhotoTask();
	virtual ~RequestLCCheckSendPhotoTask();

	// set request param
	void SetParam(const string& targetId, const string& inviteId, const string& photoId);

	// Implement RequestBaseTask
	bool HandleCallback(const string& url, bool requestRet, const char* buf, int size);

	void SetCallback(IRequestLCCheckSendPhotoCallback* pCallback);

protected:
	IRequestLCCheckSendPhotoCallback* mpCallback;
};

#endif /* REQUESTLCCHECKSENDPHOTOTASK_H_ */
