/*
 * RequestLCMagicIconConfigTask.h
 *
 *  Created on: 2016-10-9
 *      Author: Hunter
 * Description: 获取小高表配置
 */
#ifndef REQUESTLCMAGICICONCONFIGTASK_H_
#define REQUESTLCMAGICICONCONFIGTASK_H_

#include "RequestBaseTask.h"
#include "RequestLCDefine.h"
#include "item/MagicIconConfig.h"

class RequestLCMagicIconConfigTask;

class IRequestLCMagicIconConfigCallback {
public:
	virtual ~IRequestLCMagicIconConfigCallback(){};
	virtual void OnGetMagicIconConfig(bool success
									, const string& errnum
									, const string& errmsg
									, const MagicIconConfig& magicIconConfig
									, RequestLCMagicIconConfigTask* task) = 0;
};

class RequestLCMagicIconConfigTask : public RequestBaseTask {
public:
	RequestLCMagicIconConfigTask();
	virtual ~RequestLCMagicIconConfigTask();

	// Implement RequestBaseTask
	bool HandleCallback(const string& url, bool requestRet, const char* buf, int size);

	void SetCallback(IRequestLCMagicIconConfigCallback* pCallback);

protected:
	IRequestLCMagicIconConfigCallback* mpCallback;
};

#endif/*REQUESTLCMAGICICONCONFIGTASK_H_*/
