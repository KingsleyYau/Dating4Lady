/* RequestAlbumEditPhotoTask.h
 *
 *  Created on: 2016.7.19
 *      Author: Hunter.Mun
 *  Description: 4.7 修改Photo
 */
#ifndef _REQUESTALBUMEDITPHOTOTASK_H
#define _REQUESTALBUMEDITPHOTOTASK_H

using namespace std;
#include "RequestBaseTask.h"
#include "RequestAlbumDefine.h"

class RequestAlbumEditPhotoTask;

class IRequestAlbumEditPhotoTaskCallback{
public:
	virtual ~IRequestAlbumEditPhotoTaskCallback(){}
	virtual void OnAlbumEditPhoto(bool success, const string& errnum, const string& errmsg, RequestAlbumEditPhotoTask* task) = 0;
};

class RequestAlbumEditPhotoTask : public RequestBaseTask{
public:
	RequestAlbumEditPhotoTask();
	~RequestAlbumEditPhotoTask();

	// Implement RequestBaseTask
	bool HandleCallback(const string& url, bool requestRet, const char* buf, int size);

	void SetCallback(IRequestAlbumEditPhotoTaskCallback* pCallback);

	/**
	 * photoId 照片id
	 * photoTitle：照片名称
	 */
	void SetParam(
			string photoId,
			string photoTitle);

protected:
	IRequestAlbumEditPhotoTaskCallback* mpCallback;
};

#endif/*_REQUESTALBUMEDITPHOTOTASK_H*/
