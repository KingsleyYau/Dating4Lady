/*
 * ConfigManager.h
 *
 *  Created on: 2015-10-30
 *      Author: Max
 */

#ifndef CONFIGMANAGER_H_
#define CONFIGMANAGER_H_

#include "BaseTask.h"
#include "RequestSynConfigTask.h"

class ConfigManager;
class ConfigManagerCallback {
public:
	virtual ~ConfigManagerCallback(){};
	virtual void onSynConfigCallback(
			const ConfigManager* pConfigManager,
			bool isSuccess,
			const string& errnum,
			const string& errmsg,
			const SynConfigItem& item
			) = 0;
};

typedef list<ConfigManagerCallback*> ConfigManagerCallbackList;
class ConfigManager : public IRequestSynConfigTaskCallback {
public:
	static ConfigManager& GetInstance();

	ConfigManager();
	virtual ~ConfigManager();

	/**
	 * 同步配置回调
	 * Implement from IRequestLoginCallback
	 */
	void onSynConfigCallback(
			bool isSuccess,
			const string& errnum,
			const string& errmsg,
			const SynConfigItem& item,
			RequestSynConfigTask* task
			);

	/**
	 * 初始化
	 */
	void Init(HttpRequestManager *pHttpRequestManager);

	/**
	 * 增加登录结果监听器
	 * @param pCallback
	 */
	void AddCallback(ConfigManagerCallback* pCallback);

	/**
	 * 删除登录结果监听器
	 * @param pCallback
	 */
	void RemoveCallback(ConfigManagerCallback* pCallback);

	/**
	 * 同步
	 */
	void Sync();

private:
	SynConfigItem mSynConfigItem;
	RequestSynConfigTask mRequestSynConfigTask;
	HttpRequestManager* mpHttpRequestManager;

	ConfigManagerCallbackList mConfigManagerCallbackList;
	KMutex mCallbackListLock;

	bool mbRunning;
	bool mbFinish;
	KMutex mMutex;

};

#endif /* CONFIGMANAGER_H_ */
