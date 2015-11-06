/*
 * RequestLCSystemInviteTemplateTask.h
 *
 *  Created on: 2015-10-12
 *      Author: Samson
 * Description: 查询系统邀请模板列表
 */

#ifndef REQUESTLCSYSTEMINVITETEMPLATETASK_H_
#define REQUESTLCSYSTEMINVITETEMPLATETASK_H_

#include "RequestBaseTask.h"
#include "RequestLCDefine.h"
#include <list>

using namespace std;

class RequestLCSystemInviteTemplateTask;

class IRequestLCSystemInviteTemplateCallback {
public:
	virtual ~IRequestLCSystemInviteTemplateCallback(){};
	virtual void OnSystemInviteTemplate(bool success
										, const string& errnum
										, const string& errmsg
										, const list<string>& theList
										, RequestLCSystemInviteTemplateTask* task) = 0;
};

class RequestLCSystemInviteTemplateTask : public RequestBaseTask {
public:
	RequestLCSystemInviteTemplateTask();
	virtual ~RequestLCSystemInviteTemplateTask();

	// Implement RequestBaseTask
	bool HandleCallback(const string& url, bool requestRet, const char* buf, int size);

	void SetCallback(IRequestLCSystemInviteTemplateCallback* pCallback);

protected:
	IRequestLCSystemInviteTemplateCallback* mpCallback;
};

#endif /* REQUESTLCSYSTEMINVITETEMPLATETASK_H_ */
