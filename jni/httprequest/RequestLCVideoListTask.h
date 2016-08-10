/*
 * RequestLCVideoListTask.h
 *
 *  Created on: 2016-01-06
 *      Author: Max
 * Description: 5.13.	获取微视频列表
 */

#ifndef REQUESTLCVIDEOLISTTASK_H_
#define REQUESTLCVIDEOLISTTASK_H_

#include "RequestBaseTask.h"
#include "RequestLCDefine.h"
#include "item/LiveChatVideoGroupItem.h"
#include "item/LiveChatVideoListItem.h"

class RequestLCVideoListTask;

class IRequestLCVideoListCallback {
public:
	virtual ~IRequestLCVideoListCallback(){};
	virtual void OnVideoList(bool success,
			const string& errnum,
			const string& errmsg,
			const LiveChatVideoGroupList& groupList,
			const LiveChatVideoList& videoList,
			RequestLCVideoListTask* task
			) = 0;
};

class RequestLCVideoListTask : public RequestBaseTask {
public:
	RequestLCVideoListTask();
	virtual ~RequestLCVideoListTask();

	// set request param
	void SetParam(const string& sid, const string& userId);

	// Implement RequestBaseTask
	bool HandleCallback(const string& url, bool requestRet, const char* buf, int size);

	void SetCallback(IRequestLCVideoListCallback* pCallback);

protected:
	IRequestLCVideoListCallback* mpCallback;
};

#endif /* REQUESTLCVIDEOLISTTASK_H_ */
