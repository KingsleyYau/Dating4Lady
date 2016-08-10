/* RequestAlbumAddVideoTask.h
 *
 *  Created on: 2016.7.19
 *      Author: Hunter.Mun
 *  Description: 4.9 添加Video
 */
#ifndef _REQUESTALBUMADDVIDEOTASK_H
#define _REQUESTALBUMADDVIDEOTASK_H

using namespace std;
#include "RequestBaseTask.h"
#include "RequestAlbumDefine.h"

class RequestAlbumAddVideoTask;

class IRequestAlbumAddVideoTaskCallback{
public:
	virtual ~IRequestAlbumAddVideoTaskCallback(){}
	virtual void OnAlbumAddVideo(bool success, const string& errnum, const string& errmsg, const string& videoId, RequestAlbumAddVideoTask* task) = 0;
};

class RequestAlbumAddVideoTask : public RequestBaseTask{
public:
	RequestAlbumAddVideoTask();
	~RequestAlbumAddVideoTask();

	// Implement RequestBaseTask
	bool HandleCallback(const string& url, bool requestRet, const char* buf, int size);

	void SetCallback(IRequestAlbumAddVideoTaskCallback* pCallback);

	/**
	 * albumId album的id
	 * videoTitle video title
	 * shortVideoKey：视频唯一key
	 * hidFileMd5ID: 视频文件加密ID
	 * thumbFilePath：视频缩略图地址
	 */
	void SetParam(
			string albumId,
			string videoTitle,
			string shortVideoKey,
			string hidFileMd5ID,
			const string& thumbFilePath);

protected:
	IRequestAlbumAddVideoTaskCallback* mpCallback;
};

#endif/*_REQUESTALBUMADDVIDEOTASK_H*/
