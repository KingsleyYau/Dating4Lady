/*
 * RequestLoginTask.h
 *
 *  Created on: 2015-9-10
 *      Author: Max
 */

#ifndef REQUESTLOGINTASK_H_
#define REQUESTLOGINTASK_H_

#include "RequestBaseTask.h"
#include "item/LoginItem.h"

class RequestLoginTask;

class IRequestLoginCallback {
public:
	virtual ~IRequestLoginCallback(){};
	virtual void OnLogin(bool success, const string& errnum, const string& errmsg, const LoginItem& item, RequestLoginTask* task) = 0;
};

class RequestLoginTask : public RequestBaseTask {
public:
	RequestLoginTask();
	virtual ~RequestLoginTask();

	// Implement RequestBaseTask
	bool HandleCallback(const string& url, bool requestRet, const char* buf, int size);

	void SetCallback(IRequestLoginCallback* pCallback);

    /**
     * 2.1.登录
     * @param email				电子邮箱
     * @param password			密码
     * @param deviceId			设备唯一标识
     * @param model				移动设备型号
     * @param manufacturer		制造厂商
     */
	void SetParam(
			string email,
			string password,
			string deviceId,
			string model,
			string manufacturer
			);

	/**
	 * 获取用户名
	 */
	const string& GetUser();

	/**
	 * 获取密码
	 */
	const string& GetPassword();

protected:
	IRequestLoginCallback* mpCallback;
	string mUser;
	string mPassword;
};

#endif /* REQUESTLOGINTASK_H_ */
