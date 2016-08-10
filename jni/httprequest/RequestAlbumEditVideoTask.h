/* RequestAlbumEditVideoTask.h
 *
 *  Created on: 2016.7.19
 *      Author: Hunter.Mun
 *  Description: 4.10 修改Video
 */
#ifndef _REQUESTALBUMEDITVIDEOTASK_H
#define _REQUESTALBUMEDITVIDEOTASK_H

using namespace std;
#include "RequestBaseTask.h"
#include "RequestAlbumDefine.h"

class RequestAlbumEditVideoTask;

class IRequestAlbumEditVideoTaskCallback{
public:
	virtual ~IRequestAlbumEditVideoTaskCallback(){}
	virtual void OnAlbumEditVideo(bool success, const string& errnum, const string& errmsg, RequestAlbumEditVideoTask* task) = 0;
};

class RequestAlbumEditVideoTask : public RequestBaseTask{
public:
	RequestAlbumEditVideoTask();
	~RequestAlbumEditVideoTask();

	// Implement RequestBaseTask
	bool HandleCallback(const string& url, bool requestRet, const char* buf, int size);

	void SetCallback(IRequestAlbumEditVideoTaskCallback* pCallback);

	/**
	 * videoId video id
	 * videoTitle：video名称
	 * videoThumbPath：video 缩略图路径
	 */
	void SetParam(
			string videoId,
			string videoTitle,
			const string& videoThumbPath);

protected:
	IRequestAlbumEditVideoTaskCallback* mpCallback;
};

#endif/*_REQUESTALBUMEDITVIDEOTASK_H*/
