/*
 * RequestEmotionConfigTask.h
 *
 *  Created on: 2015-10-23
 *      Author: Hunter
 */
#ifndef REQUESTEMOTIONCONFIGTASK_H
#define REQUESTEMOTIONCONFIGTASK_H

using namespace std;

#include "RequestBaseTask.h"
#include "RequestOtherDefine.h"
#include "item/EmotionConfigItem.h"

class RequestEmotionConfigTask;

class IRequestEmotionConfigCallback{
public:
	virtual ~IRequestEmotionConfigCallback(){};
	virtual void onEmotionConfigCallback(bool isSuccess, const string& errnum, const string& errmsg, const EmotionConfigItem& emotionConfigItem, RequestEmotionConfigTask* task) = 0;
};

class RequestEmotionConfigTask : public RequestBaseTask{
public:
	RequestEmotionConfigTask();
	~RequestEmotionConfigTask();

	//Implements
	bool HandleCallback(const string& url, bool requestRet, const char* buf, int size);

	void setCallback(IRequestEmotionConfigCallback* callback);
protected:
	IRequestEmotionConfigCallback* mpCallback;
};

#endif/*REQUESTEMOTIONCONFIGTASK_H*/
