/*
 * RequestLCGetVideoTask.h
 *
 *  Created on: 2016-01-06
 *      Author: Max
 * Description: 5.15.	获取微视频文件URL
 */

#ifndef REQUESTLCGETVIDEOTASK_H_
#define REQUESTLCGETVIDEOTASK_H_

#include "RequestBaseTask.h"
#include "RequestLCDefine.h"

class RequestLCGetVideoTask;

class IRequestLCGetVideoCallback {
public:
	virtual ~IRequestLCGetVideoCallback(){};
	virtual void OnGetVideo(
			bool success,
			const string& errnum,
			const string& errmsg,
			const string& videoUrl,
			RequestLCGetVideoTask* task) = 0;
};

class RequestLCGetVideoTask : public RequestBaseTask {
public:
	RequestLCGetVideoTask();
	virtual ~RequestLCGetVideoTask();

	// set request param
	void SetParam(
			string targetId,
			string videoId,
			string inviteId,
			GETVIDEO_TO_FLAG_TYPE toflag,
			string sendId,
			const string& sid,
			const string& userId
			);

	// Implement RequestBaseTask
	bool HandleCallback(const string& url, bool requestRet, const char* buf, int size);

	void SetCallback(IRequestLCGetVideoCallback* pCallback);

protected:
	IRequestLCGetVideoCallback* mpCallback;
};

#endif /* REQUESTLCGETVIDEOTASK_H_ */
