/*
 * LoginManager.h
 *
 *  Created on: 2015-10-27
 *      Author: Max
 */

#ifndef LOGINMANAGER_H_
#define LOGINMANAGER_H_

#include "RequestLoginTask.h"
#include <common/KMutex.h>

#include <list>
using namespace std;

/**
 * 登录状态
 * @param NONE			未登录
 * @param LOGINING		登录中
 * @param LOGINED		已登录
 */
typedef enum LoginStatus {
	LOGINSTATUS_NONE,
	LOGINSTATUS_LOGINING,
	LOGINSTATUS_LOGINED,
} LoginStatus;

/**
 * 登录状态
 * @param NORMAL			普通注销
 * @param SESSIONTIMEOUT	php session失效注销
 */
typedef enum LogoutType {
	NORMAL,
	SESSIONTIMEOUT,
} LogoutType;

class LoginManager;
class LoginManagerCallback {
public:
	virtual ~LoginManagerCallback(){};
	virtual void OnLogin(const LoginManager* pLoginManager, bool success, const string& errnum, const string& errmsg, const LoginItem& item) = 0;
	virtual void OnLogout(const LoginManager* pLoginManager, LogoutType type) = 0;
};

typedef list<LoginManagerCallback*> LoginManagerCallbackList;
class LoginManager : public IRequestLoginCallback {
public:
	static LoginManager& GetInstance();

	LoginManager();
	virtual ~LoginManager();

	/**
	 * 登录回调
	 * Implement from IRequestLoginCallback
	 */
	void OnLogin(bool success, const string& errnum, const string& errmsg, const LoginItem& item, RequestLoginTask* task);

	/**
	 * 初始化
	 */
	void Init(HttpRequestManager *pHttpRequestManager);

	/**
	 * 增加登录结果监听器
	 * @param pCallback
	 */
	void AddCallback(LoginManagerCallback* pCallback);

	/**
	 * 删除登录结果监听器
	 * @param pCallback
	 */
	void RemoveCallback(LoginManagerCallback* pCallback);

	/**
	 * 登录
	 */
	void Login(
			string email,
			string password,
			string deviceId,
			string model,
			string manufacturer
			);

	/**
	 * 自动登录
	 */
	void AutoLogin();

	/**
	 * 注销
	 */
	void Logout(LogoutType type);

	/**
	 * 获取登录状态
	 */
	LoginStatus GetLoginStatus();

	/**
	 * 获取用户名
	 */
	const string& GetUser();

	/**
	 * 获取密码
	 */
	const string& GetPassword();

private:
	LoginStatus mLoginStatus;
	KMutex mStatusLock;

	string mUser;
	string mPassword;

	HttpRequestManager* mpHttpRequestManager;
	RequestLoginTask mLoginTask;

	LoginManagerCallbackList mLoginManagerCallbackList;
	KMutex mCallbackListLock;
};

#endif /* LOGINMANAGER_H_ */
