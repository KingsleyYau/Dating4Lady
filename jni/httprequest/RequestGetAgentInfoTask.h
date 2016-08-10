/*
 * RequestGetAgentInfoTask.h
 *
 *  Created on: 2015-11-20
 *      Author: Samson
 */
#ifndef REQUESTGETAGENTINFOTASK_H
#define REQUESTGETAGENTINFOTASK_H

using namespace std;
#include "RequestBaseTask.h"
#include "RequestOtherDefine.h"
#include "item/AgentInfoItem.h"

class RequestGetAgentInfoTask;

class IRequestGetAgentInfoTaskCallback{
public:
	virtual ~IRequestGetAgentInfoTaskCallback(){};
	virtual void onGetAgentInfoCallback(bool isSuccess, const string& errnum, const string& errmsg, const AgentInfoItem& item, RequestGetAgentInfoTask* task) = 0;
};

class RequestGetAgentInfoTask : public RequestBaseTask{
public:
	RequestGetAgentInfoTask();
	~RequestGetAgentInfoTask();

	//Implements
	bool HandleCallback(const string& url, bool requestRet, const char* buf, int size);

	void setCallback(IRequestGetAgentInfoTaskCallback* pCallback);
protected:
	IRequestGetAgentInfoTaskCallback* mpCallback;
};

#endif/*REQUESTGETAGENTINFOTASK_H*/
