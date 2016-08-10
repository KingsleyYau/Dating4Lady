/*
 * RequestLCGetVoiceTask.h
 *
 *  Created on: 2015-10-12
 *      Author: Samson
 * Description: 获取语音文件
 */

#ifndef REQUESTLCGETVOICETASK_H_
#define REQUESTLCGETVOICETASK_H_

#include "RequestBaseTask.h"
#include "RequestLCDefine.h"

class RequestLCGetVoiceTask;

class IRequestLCGetVoiceCallback {
public:
	virtual ~IRequestLCGetVoiceCallback(){};
	virtual void OnGetVoice(bool success
							, const string& errnum
							, const string& errmsg
							, const string& filePath
							, RequestLCGetVoiceTask* task) = 0;
};

class RequestLCGetVoiceTask : public RequestBaseTask {
public:
	RequestLCGetVoiceTask();
	virtual ~RequestLCGetVoiceTask();

	// set request param
	void SetParam(const string& voiceId, const string& siteId, const string& filePath);

	// Implement RequestBaseTask
	bool HandleCallback(const string& url, bool requestRet, const char* buf, int size);
	void onReceiveBody(long requestId, string path, const char* buf, int size);

	void SetCallback(IRequestLCGetVoiceCallback* pCallback);

private:
	string GetTempFilePath();

protected:
	IRequestLCGetVoiceCallback* mpCallback;
	string m_filePath;
};

#endif /* REQUESTLCGETVOICETASK_H_ */
