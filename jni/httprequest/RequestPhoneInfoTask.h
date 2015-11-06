/*
 * RequestPhoneInfoTask.h
 *
 *  Created on: 2015-10-23
 *      Author: Hunter
 */
#ifndef REQUESTPHONEINFOTASK_H
#define REQUESTPHONEINFOTASK_H

using namespace std;
#include "RequestBaseTask.h"
#include "RequestOtherDefine.h"

class RequestPhoneInfoTask;

class IRequestPhoneInfoTaskCallback{
public:
	virtual ~IRequestPhoneInfoTaskCallback(){};
	virtual void onPhoneInfoCallback(bool isSuccess, const string& errnum, const string& errmsg, RequestPhoneInfoTask* task)=0;
};

class RequestPhoneInfoTask : public RequestBaseTask{
public:
	RequestPhoneInfoTask();
	~RequestPhoneInfoTask();

	//Implements
	bool HandleCallback(const string& url, bool requestRet, const char* buf, int size);

	/**
	 * 6.4 收集手机硬件信息
	 * @param model 移动设备型号
	 * @param manufacturer 制造厂商
	 * @param os 操作系统类型
	 * @param release 操作系统版本号
	 * @param sdk SDK版本号
	 * @param densityDpi 屏幕DPI
	 * @param width 屏幕宽度
	 * @param height 屏幕高度
	 * @param data 当前用户帐号
	 * @param versionName 客户端显示版本号
	 * @param language 设备当前使用语言
	 * @param strCountry 设备当前国家
	 * @param siteid 站点ID
	 * @param action 新用户类型（1：新安装，2：新用户）
	 * @param device_id 设备唯一标识
	 */
	void setParams(const string& model,
			const string& manufacturer,
			const string& os,
			const string& release,
			const string& sdk,
			const string& densityDpi,
			int width,
			int height,
			const string& data,
			const string& versionName,
			const string& language,
			const string& strCountry,
			int siteid,
			int action,
			const string& device_id);

	void setCallback(IRequestPhoneInfoTaskCallback* pCallback);

protected:
	IRequestPhoneInfoTaskCallback* mpCallback;

};

#endif/*REQUESTPHONEINFOTASK_H*/
