/*
 * RequestBaseTask.h
 *
 *  Created on: 2015-9-10
 *      Author: Max
 */

#ifndef REQUESTBASETASK_H_
#define REQUESTBASETASK_H_

#include <map>
using namespace std;

#include <json/json/json.h>
#include <xml/tinyxml.h>

#include <httpclient/HttpRequestManager.h>
#include "RequestDefine.h"
#include "BaseTask.h"

class RequestBaseTask;
class ErrcodeHandler {
public:
	virtual ~ErrcodeHandler(){};
	virtual bool ErrcodeHandle(const RequestBaseTask* request, const string &errnum) = 0;
};
class RequestBaseTask : public BaseTask, IHttpRequestManagerCallback {
public:
	RequestBaseTask();
	virtual ~RequestBaseTask();

	void Init(HttpRequestManager *pHttpRequestManager);
	void SetErrcodeHandler(ErrcodeHandler* pErrcodeHandler);

	// 获取下载总数据量及已收数据字节数
	void GetRecvDataCount(int& total, int& recv) const;
	// 获取上传总数据量及已收数据字节数
	void GetSendDataCount(int& total, int& send) const;

	// Implement from BaseTask
	bool Start();
	void Stop();
	bool IsFinishOK();
	const char* GetErrCode();

protected:
	unsigned long StartRequest();
	bool StopRequest();

	bool HandleResult(const char* buf, int size, string &errnum, string &errmsg, Json::Value *data, Json::Value *errdata = NULL, bool* bContinue = NULL);
	bool HandleResult(const char* buf, int size, string &errnum, string &errmsg, TiXmlDocument &doc, bool* bContinue = NULL);
	virtual bool HandleCallback(const string& url, bool requestRet, const char* buf, int size) = 0;

	// Implement IHttpRequestManagerCallback
	void onSuccess(long requestId, string path, const char* buf, int size);
	void onFail(long requestId, string path);

	// 获取 Content-Type
	string GetContentType() const;

	HttpRequestManager* mpHttpRequestManager;

	long mRequestId;
	string mUrl;
	HttpEntiy mHttpEntiy;
	SiteType mSiteType;
	bool mNoCache;

	bool mbFinishOK;
	string mErrCode;

	ErrcodeHandler* mpErrcodeHandler;
};

#endif /* REQUESTBASETASK_H_ */
