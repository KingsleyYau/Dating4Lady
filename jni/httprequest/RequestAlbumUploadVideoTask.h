/* RequestAlbumUploadVideoTask.h
 *
 *  Created on: 2016.7.19
 *      Author: Hunter.Mun
 *  Description: 4.8 查询Video列表
 */
#ifndef _REQUESTALBUMUPLOADVIDEOTASK_H
#define _REQUESTALBUMUPLOADVIDEOTASK_H

using namespace std;
#include "RequestBaseTask.h"
#include "RequestAlbumDefine.h"

class RequestAlbumUploadVideoTask;

class IRequestAlbumUploadVideoTaskCallback{
public:
	virtual ~IRequestAlbumUploadVideoTaskCallback(){}
	virtual void OnAlbumUploadVideo(bool success, const string& errnum, const string& errmsg, const string& identifyValues, RequestAlbumUploadVideoTask* task) = 0;
};

class RequestAlbumUploadVideoTask : public RequestBaseTask{
public:
	RequestAlbumUploadVideoTask();
	~RequestAlbumUploadVideoTask();

	// Implement RequestBaseTask
	bool HandleCallback(const string& url, bool requestRet, const char* buf, int size);

	void SetCallback(IRequestAlbumUploadVideoTaskCallback* pCallback);

	/**
	 * agencyID 女士机构Id
	 * womanID 女士ID
	 * siteId 分站ID
	 * shortVideoKey
	 * serverType
	 * filePath
	 * mimeType
	 */
	void SetParam(
			string agencyID
			, string womanID
			, int siteId
			, string shortVideoKey
			, int serverType
			, const string& filePath
			, const string& mimeType);

protected:
	IRequestAlbumUploadVideoTaskCallback* mpCallback;
};

#endif/*_REQUESTALBUMUPLOADVIDEOTASK_H*/
