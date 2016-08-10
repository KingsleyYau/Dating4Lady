/*
 * RequestLCDelCustomTemplateTask.h
 *
 *  Created on: 2015-11-03
 *      Author: Hunter
 * Description: 新建邀请模板
 */
#ifndef REQUESTLCDELCUSTOMTEMPLATETASK_H
#define REQUESTLCDELCUSTOMTEMPLATETASK_H

using namespace std;

#include "RequestBaseTask.h"


class RequestLCDelCustomTemplateTask;

class IRequestLCDelCustomTemplateTaskCallback{
public:
	virtual ~IRequestLCDelCustomTemplateTaskCallback(){};
	virtual void onDelCustomTemplate(bool success, const string& errnum, const string& errmsg, RequestLCDelCustomTemplateTask* task) = 0;
};

class RequestLCDelCustomTemplateTask : public RequestBaseTask{
public:
	RequestLCDelCustomTemplateTask();
	virtual ~RequestLCDelCustomTemplateTask();
public:
	//implements
	bool HandleCallback(const string& url, bool requestRet, const char* buf, int size);

	void setCallback(IRequestLCDelCustomTemplateTaskCallback* pCallback);

	/**
	 * 5.4.删除用于自定义模板
	 * tempId 模板Id
	 */
	void setParams(const string& tempId);

protected:
	IRequestLCDelCustomTemplateTaskCallback* mpCallback;
};

#endif/*REQUESTLCDELCUSTOMTEMPLATETASK_H*/
