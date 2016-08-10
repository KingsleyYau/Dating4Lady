/*
 * RequestLCSendVoiceTask.h
 *
 *  Created on: 2015-10-12
 *      Author: Samson
 * Description: 上传语音文件
 */

#ifndef REQUESTLCSENDVOICETASK_H_
#define REQUESTLCSENDVOICETASK_H_

#include "RequestBaseTask.h"
#include "RequestLCDefine.h"

class RequestLCSendVoiceTask;

class IRequestLCSendVoiceCallback {
public:
	virtual ~IRequestLCSendVoiceCallback(){};
	virtual void OnSendVoice(bool success
							, const string& errnum
							, const string& errmsg
							, const string& voiceId
							, RequestLCSendVoiceTask* task) = 0;
};

class RequestLCSendVoiceTask : public RequestBaseTask {
public:
	RequestLCSendVoiceTask();
	virtual ~RequestLCSendVoiceTask();

	// set request param
	void SetParam(const string& voiceCode
				, const string& inviteId
				, const string& manId
				, const string& womanId
				, const string& type
				, int voiceLen
				, const string& siteId
				, const string& filePath);

	// Implement RequestBaseTask
	bool HandleCallback(const string& url, bool requestRet, const char* buf, int size);

	void SetCallback(IRequestLCSendVoiceCallback* pCallback);

protected:
	IRequestLCSendVoiceCallback* mpCallback;
	string m_filePath;
};

#endif /* REQUESTLCSENDVOICETASK_H_ */
