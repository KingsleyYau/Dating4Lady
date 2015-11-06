/*
 * RequestModifyPasswordTask.h
 *
 *  Created on: 2015-10-23
 *      Author: Hunter
 */
#ifndef REQUESTMODIFYPASSWORDTASK_H
#define REQUESTMODIFYPASSWORDTASK_H

using namespace std;
#include "RequestBaseTask.h"
#include "RequestOtherDefine.h"

class RequestModifyPasswordTask;

class IRequestModifyPasswordTaskCallback{
public:
		virtual ~IRequestModifyPasswordTaskCallback(){};
		virtual void onModifyPasswordCallback(bool isSuccess, const string& errnum, const string& errmsg, RequestModifyPasswordTask* task) = 0;
};

class RequestModifyPasswordTask : public RequestBaseTask{
public:
	RequestModifyPasswordTask();
	~RequestModifyPasswordTask();

	//Implement RequestBaseTask
	bool HandleCallback(const string& url, bool requestRet, const char* buf, int size);

	void setCallback(IRequestModifyPasswordTaskCallback* pCallback);

	/**
	 * 6.1 修改密码
	 * @param oldPassword 旧密码
	 * @param newPassword 修改后新密码
	 */
	void setParam(const string& oldPassword,
			const string& newPassword);
protected:
	IRequestModifyPasswordTaskCallback* mpCallback;

};

#endif/*REQUESTMODIFYPASSWORDTASK_H*/
