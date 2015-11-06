/*
 * RequestManDetailTask.h
 *
 *  Created on: 2015-9-16
 *      Author: Max
 */

#ifndef REQUESTMANDETAILTASK_H_
#define REQUESTMANDETAILTASK_H_

using namespace std;

#include "RequestBaseTask.h"
#include "RequestManDefine.h"
#include "item/ManDetailItem.h"

class RequestManDetailTask;

class IRequestManDetailCallback {
public:
	virtual ~IRequestManDetailCallback(){};
	virtual void OnQueryManDetail(bool success, const string& errnum, const string& errmsg, const ManDetailItem& item, RequestManDetailTask* task) = 0;
};

class RequestManDetailTask : public RequestBaseTask {
public:
	RequestManDetailTask();
	virtual ~RequestManDetailTask();

	// Implement RequestBaseTask
	bool HandleCallback(const string& url, bool requestRet, const char* buf, int size);

	void SetCallback(IRequestManDetailCallback* pCallback);

	/**
	 * 3.2.查询男士详情（http post）
	 * @param man_id			男士id
	 */
	void SetParam(const string& man_id);

protected:
	IRequestManDetailCallback* mpCallback;
};

#endif /* REQUESTMANDETAILTASK_H_ */
