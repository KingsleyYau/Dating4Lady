/*
 * ConfigManager.cpp
 *
 *  Created on: 2015-10-30
 *      Author: Max
 */

#include "ConfigManager.h"

static ConfigManager gConfigManager;
ConfigManager& ConfigManager::GetInstance() {
	return gConfigManager;
}

ConfigManager::ConfigManager() {
	// TODO Auto-generated constructor stub
	mbRunning = false;
	mbFinish = false;
}

ConfigManager::~ConfigManager() {
	// TODO Auto-generated destructor stub
}

void ConfigManager::onSynConfigCallback(bool isSuccess, const string& errnum, const string& errmsg, const SynConfigItem& item, RequestSynConfigTask* task) {
	FileLog("httprequest", "ConfigManager::onSynConfigCallback( start )");

	mMutex.lock();
	mbRunning = false;
	if( isSuccess ) {
		mbFinish = true;
		mSynConfigItem = item;

		// 更新LiveChat语音host
		if (NULL != mpHttpRequestManager
				&& NULL != mpHttpRequestManager->GetHostManager())
		{
			HttpRequestHostManager* hostManager = mpHttpRequestManager->GetHostManager();
			hostManager->SetChatVoiceSite(mSynConfigItem.liveChatVoiceHost);
			hostManager->SetVideoUploadSite(mSynConfigItem.videoUploadHost);
		}
	}
	mMutex.unlock();

	mCallbackListLock.lock();
	for(ConfigManagerCallbackList::iterator itr = mConfigManagerCallbackList.begin(); itr != mConfigManagerCallbackList.end(); itr++) {
		(*itr)->onSynConfigCallback(this, isSuccess, errnum, errmsg, item);
	}
	mCallbackListLock.unlock();
	FileLog("httprequest", "ConfigManager::onSynConfigCallback( end )");
}

void ConfigManager::Init(HttpRequestManager *pHttpRequestManager) {
	mpHttpRequestManager = pHttpRequestManager;
}

void ConfigManager::AddCallback(ConfigManagerCallback* pCallback) {
	mCallbackListLock.lock();
	mConfigManagerCallbackList.push_back(pCallback);
	mCallbackListLock.unlock();
}

void ConfigManager::RemoveCallback(ConfigManagerCallback* pCallback) {
	mCallbackListLock.lock();
	for(ConfigManagerCallbackList::iterator itr = mConfigManagerCallbackList.begin(); itr != mConfigManagerCallbackList.end(); itr++) {
		if( *itr == pCallback ) {
			mConfigManagerCallbackList.erase(itr);
			break;
		}
	}
	mCallbackListLock.unlock();
}

/**
 * 同步
 */
void ConfigManager::Sync() {
	FileLog("httprequest", "ConfigManager::Sync()");

	mMutex.lock();
	if( mbRunning ) {
		mMutex.unlock();
		return;
	} else {
		mbRunning = true;
		if( mbFinish ) {
			mbRunning = false;
			mMutex.unlock();

			mCallbackListLock.lock();
			for(ConfigManagerCallbackList::iterator itr = mConfigManagerCallbackList.begin(); itr != mConfigManagerCallbackList.end(); itr++) {
				(*itr)->onSynConfigCallback(this, true, "", "", mSynConfigItem);
			}
			mCallbackListLock.unlock();
		} else {
			mMutex.unlock();

			mRequestSynConfigTask.Init(mpHttpRequestManager);
			mRequestSynConfigTask.setCallback(this);
			mRequestSynConfigTask.Start();
		}
	}

}
