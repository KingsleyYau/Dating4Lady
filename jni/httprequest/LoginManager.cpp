/*
 * LoginManager.cpp
 *
 *  Created on: 2015-10-27
 *      Author: Max
 */

#include "LoginManager.h"

static LoginManager gLoginManager;
LoginManager& LoginManager::GetInstance() {
	return gLoginManager;
}

LoginManager::LoginManager() {
	// TODO Auto-generated constructor stub
	mLoginStatus = LOGINSTATUS_NONE;
	mUser = "";
	mPassword = "";
}

LoginManager::~LoginManager() {
	// TODO Auto-generated destructor stub
}

void LoginManager::OnLogin(bool success, const string& errnum, const string& errmsg, const LoginItem& item, RequestLoginTask* task) {
	mStatusLock.lock();
	if( success ) {
		mUser = task->GetUser();
		mPassword = task->GetPassword();
	}
	mStatusLock.unlock();

	mCallbackListLock.lock();
	for(LoginManagerCallbackList::iterator itr = mLoginManagerCallbackList.begin(); itr != mLoginManagerCallbackList.end(); itr++) {
		(*itr)->OnLogin(this, success, errnum, errmsg, item);
	}
	mCallbackListLock.unlock();

	mStatusLock.lock();
	if( success ) {
		mLoginStatus = LOGINSTATUS_LOGINED;
	} else {
		mLoginStatus = LOGINSTATUS_NONE;
	}
	mStatusLock.unlock();
}

void LoginManager::Init(HttpRequestManager *pHttpRequestManager) {
	mpHttpRequestManager = pHttpRequestManager;
}

void LoginManager::AddCallback(LoginManagerCallback* pCallback) {
	mCallbackListLock.lock();
	mLoginManagerCallbackList.push_back(pCallback);
	mCallbackListLock.unlock();
}

void LoginManager::RemoveCallback(LoginManagerCallback* pCallback) {
	mCallbackListLock.lock();
	for(LoginManagerCallbackList::iterator itr = mLoginManagerCallbackList.begin(); itr != mLoginManagerCallbackList.end(); itr++) {
		if( *itr == pCallback ) {
			mLoginManagerCallbackList.erase(itr);
			break;
		}
	}
//	mLoginManagerCallbackList.pop_front();
	mCallbackListLock.unlock();
}

void LoginManager::Login(
		string email,
		string password,
		string deviceId,
		string model,
		string manufacturer
		) {
	FileLog("httprequest", "LoginManager::Login( "
			"email : %s, "
			"password : %s, "
			"deviceId : %s, "
			"model : %s, "
			"manufacturer : %s "
			")",
			email.c_str(),
			password.c_str(),
			deviceId.c_str(),
			model.c_str(),
			manufacturer.c_str()
			);

	// 强制注销
	Logout(NORMAL);

	mStatusLock.lock();
	if( mLoginStatus != LOGINSTATUS_NONE ) {
		mStatusLock.unlock();
		return;
	} else {
		mLoginStatus = LOGINSTATUS_LOGINING;
	}
	mStatusLock.unlock();

	mLoginTask.Init(mpHttpRequestManager);
	mLoginTask.SetParam(
			email,
			password,
			deviceId,
			model,
			manufacturer
			);
	mLoginTask.SetCallback(this);
	mLoginTask.Start();
}

void LoginManager::AutoLogin() {
	FileLog("httprequest", "LoginManager::AutoLogin()");

	mStatusLock.lock();
	if( mLoginStatus != LOGINSTATUS_NONE ) {
		mStatusLock.unlock();
		return;
	} else {
		mLoginStatus = LOGINSTATUS_LOGINING;
	}
	mStatusLock.unlock();

	if( !mLoginTask.Start() ) {
		LoginItem item;
		mCallbackListLock.lock();
		for(LoginManagerCallbackList::iterator itr = mLoginManagerCallbackList.begin(); itr != mLoginManagerCallbackList.end(); itr++) {
			(*itr)->OnLogin(this, false, "", "", item);
		}
		mCallbackListLock.unlock();
	}
}

void LoginManager::Logout(LogoutType type) {
	FileLog("httprequest", "LoginManager::Logout( "
			"type : %d "
			")",
			type
			);

	mStatusLock.lock();
	if( mLoginStatus != LOGINSTATUS_LOGINED ) {
		mStatusLock.unlock();
		return;
	} else {
		mLoginStatus = LOGINSTATUS_NONE;
	}
	mStatusLock.unlock();

	HttpClient::CleanCookies();

	mCallbackListLock.lock();
	for(LoginManagerCallbackList::iterator itr = mLoginManagerCallbackList.begin(); itr != mLoginManagerCallbackList.end(); itr++) {
		(*itr)->OnLogout(this, type);
	}
	mCallbackListLock.unlock();
}

LoginStatus LoginManager::GetLoginStatus() {
	return mLoginStatus;
}

const string& LoginManager::GetUser() {
	return mUser;
}

const string& LoginManager::GetPassword() {
	return mPassword;
}
