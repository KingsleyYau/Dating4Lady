/*
 * RequestLCCheckFunctionsTask.h
 *
 *  Created on: 2016-07-12
 *      Author: Hunter
 * Description: 7.12.	检测功能是否开通
 */

#ifndef REQUESTLCCHECKFUNCTIONSTASK_H_
#define REQUESTLCCHECKFUNCTIONSTASK_H_

#include "RequestBaseTask.h"
#include "RequestLCDefine.h"
#include <ProtocolCommon/DeviceTypeDef.h>

class RequestLCCheckFunctionsTask;

class IRequestLCCheckFunctionsCallback {
public:
	virtual ~IRequestLCCheckFunctionsCallback(){};
	virtual void OnCheckFunctions(bool success, const string& errnum, const string& errmsg, const list<string>& flagList, RequestLCCheckFunctionsTask* task) = 0;
};

class RequestLCCheckFunctionsTask : public RequestBaseTask {
public:
	RequestLCCheckFunctionsTask();
	virtual ~RequestLCCheckFunctionsTask();

	// set request param
	void SetParam(
			string functionIds,
			TDEVICE_TYPE deviceType,
			string versionCode,
			const string& sid,
			const string& userId
			);

	// Implement RequestBaseTask
	bool HandleCallback(const string& url, bool requestRet, const char* buf, int size);

	void SetCallback(IRequestLCCheckFunctionsCallback* pCallback);

protected:
	IRequestLCCheckFunctionsCallback* mpCallback;
};

#endif /* REQUESTLCCHECKFUNCTIONSTASK_H_ */
