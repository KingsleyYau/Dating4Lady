/*
 * RequestLCCheckSendVideoTask.h
 *
 *  Created on: 2016-01-06
 *      Author: Max
 * Description: 5.16.	检测女士是否可发微视频
 */

#ifndef REQUESTLCCHECKSENDVIDEOTASK_H_
#define REQUESTLCCHECKSENDVIDEOTASK_H_

#include "RequestBaseTask.h"
#include "RequestLCDefine.h"

class RequestLCCheckSendVideoTask;

class IRequestLCCheckSendVideoCallback {
public:
	virtual ~IRequestLCCheckSendVideoCallback(){};
	virtual void OnCheckSendVideo(
			LC_CHECKVIDEO_TYPE result,
			const string& errnum,
			const string& errmsg,
			RequestLCCheckSendVideoTask* task) = 0;
};

class RequestLCCheckSendVideoTask : public RequestBaseTask {
public:
	RequestLCCheckSendVideoTask();
	virtual ~RequestLCCheckSendVideoTask();

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

	void SetCallback(IRequestLCCheckSendVideoCallback* pCallback);

protected:
	IRequestLCCheckSendVideoCallback* mpCallback;
};

#endif /* REQUESTLCCHECKSENDVIDEOTASK_H_ */
