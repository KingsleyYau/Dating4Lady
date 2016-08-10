/*
 * RequestMyProfileTask.h
 *
 *  Created on: 2015-11-23
 *      Author: Samson
 */
#ifndef REQUESTMYPROFILETASK_H
#define REQUESTMYPROFILETASK_H

using namespace std;
#include "RequestBaseTask.h"
#include "RequestOtherDefine.h"
#include "item/MyProfileItem.h"

class RequestMyProfileTask;

class IRequestMyProfileTaskCallback{
public:
	virtual ~IRequestMyProfileTaskCallback(){};
	virtual void onMyProfileCallback(bool isSuccess, const string& errnum, const string& errmsg, const MyProfileItem& item, RequestMyProfileTask* task) = 0;
};

class RequestMyProfileTask : public RequestBaseTask{
public:
	RequestMyProfileTask();
	~RequestMyProfileTask();

	//Implements
	bool HandleCallback(const string& url, bool requestRet, const char* buf, int size);

	void setCallback(IRequestMyProfileTaskCallback* pCallback);
protected:
	IRequestMyProfileTaskCallback* mpCallback;
};

#endif/*REQUESTMYPROFILETASK_H*/
