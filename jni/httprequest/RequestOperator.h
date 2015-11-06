/*
 * RequestOperator.h
 *
 *  Created on: 2015-10-28
 *      Author: Max
 */

#ifndef REQUESTOPERATOR_H_
#define REQUESTOPERATOR_H_

#include "RequestBaseTask.h"
#include "LoginManager.h"

#include <common/KThread.h>
#include <common/KCond.h>

typedef list<const ITaskCallback*> TaskCallbackList;
class RequestOperatorRunnable;
class RequestOperator : public BaseTask, ITaskCallback, LoginManagerCallback, ErrcodeHandler {
public:
	RequestOperator();
	virtual ~RequestOperator();

	void SetTask(RequestBaseTask* pTask);

	/**
	 * Implement from LoginManagerCallback
	 */
	void OnLogin(const LoginManager* pLoginManager, bool success, const string& errnum, const string& errmsg, const LoginItem& item);
	void OnLogout(const LoginManager* pLoginManager, LogoutType type);

	// Implement from BaseTask
	bool Start();
	void Stop();
	bool IsFinishOK();
	const char* GetErrCode();

	// Implement from ITaskCallback
	void OnTaskFinish(ITask* pTask);

	// Implement from ErrcodeHandler
	bool ErrcodeHandle(const RequestBaseTask* request, const string &errnum);

	void HandleRunnable();

private:
	RequestOperatorRunnable* mpRunnable;
	KThread mKThread;
	KCond mKCond;

	RequestBaseTask* mpTask;
	ITaskCallback* mpOldITaskCallback;

	/**
	 * 是否已经处理
	 */
	bool mbAlreadyHandle;

	/**
	 * 是否符合处理逻辑
	 */
	bool mbCanBeHandle;
};

#endif /* REQUESTOPERATOR_H_ */
