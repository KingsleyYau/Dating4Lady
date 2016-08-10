/*
 * RequestLCSendVideoTask.h
 *
 *  Created on: 2016-01-06
 *      Author: Max
 * Description: 5.17.	发送微视频
 */

#ifndef REQUESTLCSENDVIDEOTASK_H_
#define REQUESTLCSENDVIDEOTASK_H_

#include "RequestBaseTask.h"
#include "RequestLCDefine.h"

class RequestLCSendVideoTask;

class IRequestLCSendVideoCallback {
public:
	virtual ~IRequestLCSendVideoCallback(){};
	virtual void OnSendVideo(
			bool success,
			const string& errnum,
			const string& errmsg,
			const string& sendId,
			RequestLCSendVideoTask* task) = 0;
};

class RequestLCSendVideoTask : public RequestBaseTask {
public:
	RequestLCSendVideoTask();
	virtual ~RequestLCSendVideoTask();

	// set request param
	void SetParam(
			string targetId,
			string videoId,
			string inviteId,
			const string& sid,
			const string& userId
			);

	// Implement RequestBaseTask
	bool HandleCallback(const string& url, bool requestRet, const char* buf, int size);

	void SetCallback(IRequestLCSendVideoCallback* pCallback);

protected:
	IRequestLCSendVideoCallback* mpCallback;
};

#endif /* REQUESTLCSENDVIDEOTASK_H_ */
