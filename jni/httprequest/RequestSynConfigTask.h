/*
 * RequestSynConfigTask.h
 *
 *  Created on: 2015-10-23
 *      Author: Hunter
 */
#ifndef REQUESTSYNCONFIGTASK_H
#define REQUESTSYNCONFIGTASK_H

using namespace std;
#include "RequestBaseTask.h"
#include "RequestOtherDefine.h"
#include "item/SynConfigItem.h"

class RequestSynConfigTask;

class IRequestSynConfigTaskCallback{
public:
	virtual ~IRequestSynConfigTaskCallback(){};
	virtual void onSynConfigCallback(bool isSuccess, const string& errnum, const string& errmsg, const SynConfigItem& item, RequestSynConfigTask* task)=0;
};

class RequestSynConfigTask : public RequestBaseTask{
public:
	RequestSynConfigTask();
	~RequestSynConfigTask();

	//Implements
	bool HandleCallback(const string& url, bool requestRet, const char* buf, int size);

	void setCallback(IRequestSynConfigTaskCallback* pCallback);
protected:
	IRequestSynConfigTaskCallback* mpCallback;
};

#endif/*REQUESTSYNCONFIGTASK_H*/
