/*
 * RequestVersionCheckTask.h
 *
 *  Created on: 2015-10-23
 *      Author: Hunter
 */
#ifndef REQUESTVERSIONCHECKTASK_H
#define REQUESTVERSIONCHECKTASK_H

using namespace std;
#include "RequestBaseTask.h"
#include "RequestOtherDefine.h"
#include "item/VersionCheckItem.h"

class RequestVersionCheckTask;

class IRequestVersionCheckTaskCallback{
public:
	virtual ~IRequestVersionCheckTaskCallback(){};
	virtual void onVersionCheckCallback(bool isSuccess, const string& errnum, const string& errmsg, const VersionCheckItem& item, RequestVersionCheckTask* task)=0;
};
class RequestVersionCheckTask : public RequestBaseTask{
public:
	RequestVersionCheckTask();
	~RequestVersionCheckTask();

	//Implement
	bool HandleCallback(const string& url, bool requestRet, const char* buf, int size);

	void setCallback(IRequestVersionCheckTaskCallback* pCallback);

protected:
	IRequestVersionCheckTaskCallback* mpCallback;
};

#endif/*REQUESTVERSIONCHECKTASK_H*/
