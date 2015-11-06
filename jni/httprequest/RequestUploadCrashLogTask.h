/*
 * RequestUploadCrashLogTask.h
 *
 *  Created on: 2015-10-23
 *      Author: Hunter
 */
#ifndef REQUESTUPLOADCRASHLOGTASK_H
#define REQUESTUPLOADCRASHLOGTASK_H

using namespace std;
#include "RequestBaseTask.h"
#include "RequestOtherDefine.h"

class RequestUploadCrashLogTask;

class IRequestUploadCrashLogTaskCallback{
public:
	virtual ~IRequestUploadCrashLogTaskCallback(){};
	virtual void onUploadCrashLogCallback(bool isSuccess, const string& errnum, const string& errmsg, RequestUploadCrashLogTask* task)=0;
};

class RequestUploadCrashLogTask : public RequestBaseTask{
public:
	RequestUploadCrashLogTask();
	~RequestUploadCrashLogTask();

	//Implement
	bool HandleCallback(const string& url, bool requestRet, const char* buf, int size);

	void setParam(const string& deviceId, const string& filePath);

	void setCallback(IRequestUploadCrashLogTaskCallback* pCallback);
protected:
	IRequestUploadCrashLogTaskCallback* mpCallback;
};

#endif/*REQUESTUPLOADCRASHLOGTASK_H*/
