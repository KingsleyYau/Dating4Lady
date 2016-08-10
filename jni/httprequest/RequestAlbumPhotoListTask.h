/* RequestAlbumPhotoListTask.h
 *
 *  Created on: 2016.7.18
 *      Author: Hunter.Mun
 *  Description: 4.5 查询Photo列表
 */
#ifndef _REQUESTALBUMPHOTOLISTTASK_H
#define _REQUESTALBUMPHOTOLISTTASK_H

#include<list>
using namespace std;
#include "RequestBaseTask.h"
#include "RequestAlbumDefine.h"
#include "item/AlbumPhotoItem.h"

class RequestAlbumPhotoListTask;

class IRequestAlbumPhotoListTaskCallback{
public:
	virtual ~IRequestAlbumPhotoListTaskCallback(){}
	virtual void OnAlbumPhotoList(bool success, const string& errnum, const string& errmsg, const list<AlbumPhotoItem>& photolist, RequestAlbumPhotoListTask* task) = 0;
};

class RequestAlbumPhotoListTask : public RequestBaseTask{
public:
	RequestAlbumPhotoListTask();
	~RequestAlbumPhotoListTask();

	// Implement RequestBaseTask
	bool HandleCallback(const string& url, bool requestRet, const char* buf, int size);

	void SetCallback(IRequestAlbumPhotoListTaskCallback* pCallback);

	/**
	 * albumId album的id(可无，无则表示获取审核状态为”被打回”的列表)
	 */
	void SetParam(string albumId);

protected:
	IRequestAlbumPhotoListTaskCallback* mpCallback;
};

#endif/*_REQUESTALBUMPHOTOLISTTASK_H*/
