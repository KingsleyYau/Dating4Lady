/* RequestAlbumListTask.h
 *
 *  Created on: 2016.7.18
 *      Author: Hunter.Mun
 *  Description: 4.1 查询Album列表
 */

#ifndef REQUESTALBUMLISTTASK_H_
#define REQUESTALBUMLISTTASK_H_

#include <list>
using namespace std;

#include "RequestBaseTask.h"
#include "RequestAlbumDefine.h"
#include "item/AlbumItem.h"

class RequestAlbumListTask;

class IRequestAlbumListTaskCallback {
public:
	virtual ~IRequestAlbumListTaskCallback(){};
	virtual void OnQueryAlbumList(bool success, const string& errnum, const string& errmsg, const list<AlbumItem>& itemList, RequestAlbumListTask* task) = 0;
};

class RequestAlbumListTask : public RequestBaseTask {
public:
	RequestAlbumListTask();
	virtual ~RequestAlbumListTask();

	// Implement RequestBaseTask
	bool HandleCallback(const string& url, bool requestRet, const char* buf, int size);

	void SetCallback(IRequestAlbumListTaskCallback* pCallback);

protected:
	IRequestAlbumListTaskCallback* mpCallback;
};


#endif/*REQUESTALBUMLISTTASK_H_*/
