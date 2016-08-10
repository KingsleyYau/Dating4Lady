/* RequestAlbumVideoListTask.h
 *
 *  Created on: 2016.7.19
 *      Author: Hunter.Mun
 *  Description: 4.8 查询Video列表
 */
#ifndef _REQUESTALBUMVIDEOLISTTASK_H
#define _REQUESTALBUMVIDEOLISTTASK_H

using namespace std;
#include "RequestBaseTask.h"
#include "RequestAlbumDefine.h"
#include "item/AlbumVideoItem.h"

class RequestAlbumVideoListTask;

class IRequestAlbumVideoListTaskCallback{
public:
	virtual ~IRequestAlbumVideoListTaskCallback(){}
	virtual void OnAlbumVideoList(bool success, const string& errnum, const string& errmsg, const list<AlbumVideoItem>& itemlist, RequestAlbumVideoListTask* task) = 0;
};

class RequestAlbumVideoListTask : public RequestBaseTask{
public:
	RequestAlbumVideoListTask();
	~RequestAlbumVideoListTask();

	// Implement RequestBaseTask
	bool HandleCallback(const string& url, bool requestRet, const char* buf, int size);

	void SetCallback(IRequestAlbumVideoListTaskCallback* pCallback);

	/**
	 * albumId album的id
	 */
	void SetParam(string albumId);

protected:
	IRequestAlbumVideoListTaskCallback* mpCallback;
};

#endif/*_REQUESTALBUMVIDEOLISTTASK_H*/
