/* RequestAlbumAddPhotoTask.h
 *
 *  Created on: 2016.7.19
 *      Author: Hunter.Mun
 *  Description: 4.6 添加Photo
 */
#ifndef _REQUESTALBUMADDPHOTOTASK_H
#define _REQUESTALBUMADDPHOTOTASK_H

using namespace std;
#include "RequestBaseTask.h"
#include "RequestAlbumDefine.h"

class RequestAlbumAddPhotoTask;

class IRequestAlbumAddPhotoTaskCallback{
public:
	virtual ~IRequestAlbumAddPhotoTaskCallback(){}
	virtual void OnAlbumAddPhoto(bool success, const string& errnum, const string& errmsg,const string& photoId, RequestAlbumAddPhotoTask* task) = 0;
};

class RequestAlbumAddPhotoTask : public RequestBaseTask{
public:
	RequestAlbumAddPhotoTask();
	~RequestAlbumAddPhotoTask();

	// Implement RequestBaseTask
	bool HandleCallback(const string& url, bool requestRet, const char* buf, int size);

	void SetCallback(IRequestAlbumAddPhotoTaskCallback* pCallback);

	/**
	 * albumId album的id
	 * type album类型（0：photo，1：video）
	 * title：名称
	 * desc：描述
	 */
	void SetParam(
			string albumId,
			string photoTitle,
			const string& filePath);

protected:
	IRequestAlbumAddPhotoTaskCallback* mpCallback;
};

#endif/*_REQUESTALBUMADDPHOTOTASK_H*/
