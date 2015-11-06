/*
 * RequestLCInviteTemplateTask.h
 *
 *  Created on: 2015-10-12
 *      Author: Samson
 * Description: 查询个人邀请模板列表
 */

#ifndef REQUESTLCINVITETEMPLATETASK_H_
#define REQUESTLCINVITETEMPLATETASK_H_

#include "RequestBaseTask.h"
#include "RequestLCDefine.h"
#include "item/LiveChatInviteTemplateListItem.h"

class RequestLCInviteTemplateTask;

class IRequestLCInviteTemplateCallback {
public:
	virtual ~IRequestLCInviteTemplateCallback(){};
	virtual void OnInviteTemplate(bool success
								, const string& errnum
								, const string& errmsg
								, const LiveChatInviteTemplateList& theList
								, RequestLCInviteTemplateTask* task) = 0;
};

class RequestLCInviteTemplateTask : public RequestBaseTask {
public:
	RequestLCInviteTemplateTask();
	virtual ~RequestLCInviteTemplateTask();

	// Implement RequestBaseTask
	bool HandleCallback(const string& url, bool requestRet, const char* buf, int size);

	void SetCallback(IRequestLCInviteTemplateCallback* pCallback);

protected:
	IRequestLCInviteTemplateCallback* mpCallback;
};

#endif /* REQUESTLCINVITETEMPLATETASK_H_ */
