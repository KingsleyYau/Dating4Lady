/*
 * RequestCommonTranslateTextTask.h
 *
 *  Created on: 2015-11-18
 *      Author: Samson
 * Description: 翻译文本
 */

#ifndef REQUESTCOMMONTRANSLATETEXTTASK_H_
#define REQUESTCOMMONTRANSLATETEXTTASK_H_

#include "RequestBaseTask.h"
#include "RequestCommonDefine.h"

class RequestCommonTranslateTextTask;

class IRequestCommonTranslateTextCallback
{
public:
	IRequestCommonTranslateTextCallback() {}
	virtual ~IRequestCommonTranslateTextCallback(){};
	virtual void OnTranslateText(bool success, const string& text, RequestCommonTranslateTextTask* task) = 0;
};

class RequestCommonTranslateTextTask : public RequestBaseTask
{
public:
	RequestCommonTranslateTextTask();
	virtual ~RequestCommonTranslateTextTask();

	// set request param
	void SetParam(const string& appId, const string& from, const string& to, const string& text);

	// Implement RequestBaseTask
	bool HandleCallback(const string& url, bool requestRet, const char* buf, int size);

	void SetCallback(IRequestCommonTranslateTextCallback* pCallback);

protected:
	IRequestCommonTranslateTextCallback* mpCallback;
};

#endif /* REQUESTCOMMONTRANSLATETEXTTASK_H_ */
