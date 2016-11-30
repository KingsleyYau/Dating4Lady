/*
 * RequestLCAddInviteTemplateTask.h
 *
 *  Created on: 2015-10-12
 *      Author: Samson
 * Description: 新建邀请模板
 */

#ifndef REQUESTLCADDINVITETEMPLATETASK_H_
#define REQUESTLCADDINVITETEMPLATETASK_H_

#include "RequestBaseTask.h"
#include "RequestLCDefine.h"

class RequestLCAddInviteTemplateTask;

class IRequestLCAddInviteTemplateCallback {
public:
	virtual ~IRequestLCAddInviteTemplateCallback(){};
	virtual void OnAddInviteTemplate(bool success
									, const string& errnum
									, const string& errmsg
									, RequestLCAddInviteTemplateTask* task) = 0;
};

class RequestLCAddInviteTemplateTask : public RequestBaseTask {
public:
	RequestLCAddInviteTemplateTask();
	virtual ~RequestLCAddInviteTemplateTask();

	// set request param
	void SetParam(const string& tempContent, bool isInviteAssistant);

	// Implement RequestBaseTask
	bool HandleCallback(const string& url, bool requestRet, const char* buf, int size);

	void SetCallback(IRequestLCAddInviteTemplateCallback* pCallback);

protected:
	IRequestLCAddInviteTemplateCallback* mpCallback;
};

#endif /* REQUESTLCADDINVITETEMPLATETASK_H_ */
