/*
 * RequestLCGetVideoPhotoTask.h
 *
 *  Created on: 2016-01-06
 *      Author: Max
 * Description: 5.14.	获取微视频图片
 */

#ifndef REQUESTLCGETVIDEOPHOTOTASK_H_
#define REQUESTLCGETVIDEOPHOTOTASK_H_

#include "RequestBaseTask.h"
#include "RequestLCDefine.h"

class RequestLCGetVideoPhotoTask;

class IRequestLCGetVideoPhotoCallback {
public:
	virtual ~IRequestLCGetVideoPhotoCallback(){};
	virtual void OnGetVideoPhoto(
			bool success,
			const string& errnum,
			const string& errmsg,
			const string& videoId,
			GETVIDEOPHOTO_SIZE_TYPE sizeType,
			const string& filePath,
			RequestLCGetVideoPhotoTask* task) = 0;
};

class RequestLCGetVideoPhotoTask : public RequestBaseTask {
public:
	RequestLCGetVideoPhotoTask();
	virtual ~RequestLCGetVideoPhotoTask();

	// set request param
	void SetParam(
			const string& targetId,
			const string& videoId,
			GETVIDEOPHOTO_SIZE_TYPE sizeType,
			const string& sid,
			const string& userId,
			const string& filePath
				);

	// Implement RequestBaseTask
	bool HandleCallback(const string& url, bool requestRet, const char* buf, int size);
	void onReceiveBody(long requestId, string path, const char* buf, int size);

	void SetCallback(IRequestLCGetVideoPhotoCallback* pCallback);

private:
	string GetTempFilePath();

protected:
	IRequestLCGetVideoPhotoCallback* mpCallback;
	string m_videoId;
	GETVIDEOPHOTO_SIZE_TYPE	m_sizeType;
	string m_filePath;
};

#endif /* REQUESTLCGETVIDEOPHOTOTASK_H_ */
