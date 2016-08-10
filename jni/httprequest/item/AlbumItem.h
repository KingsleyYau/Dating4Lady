/*
 * AlbumItem.h
 *
 *  Created on: 2016.7.18
 *      Author: Hunter.Mun
 *  Description: 相册Item解析
 */

#ifndef ALBUMITEM_H_
#define ALBUMITEM_H_

#include <string>

using namespace std;

#include <common/CommonFunc.h>

#include <json/json/json.h>

#include "../RequestAlbumDefine.h"

class AlbumItem{
public:
	AlbumItem(){
		albumId = "";
		albumType = ALBUM_UNKNOWN;
		albumTitle = "";
		albumDesc = "";
		albumPhotoUrl = "";
		childCount = 0;
		createTime = 0;
	}
	~AlbumItem(){

	}

public:
	void Parse(const Json::Value& root){
		if( root.isObject() ) {

			if( root[ALBUM_GET_LIST_ID].isString() ) {
				albumId = root[ALBUM_GET_LIST_ID].asString();
			}

			if( root[ALBUM_GET_LIST_TYPE].isInt() ) {
				int tempType = root[ALBUM_GET_LIST_TYPE].asInt();
				albumType = (ALBUM_PHOTO <= tempType && tempType <= ALBUM_VIDEO ? (ALBUMTYPE)tempType : ALBUM_UNKNOWN);
			}

			if( root[ALBUM_GET_LIST_TITLE].isString() ) {
				albumTitle = root[ALBUM_GET_LIST_TITLE].asString();
			}

			if( root[ALBUM_GET_LIST_DESC].isString() ) {
				albumDesc = root[ALBUM_GET_LIST_DESC].asString();
			}

			if( root[ALBUM_GET_LIST_IMAGEURL].isString() ) {
				albumPhotoUrl = root[ALBUM_GET_LIST_IMAGEURL].asString();
			}

			if( root[ALBUM_GET_LIST_COUNT].isInt() ) {
				childCount = root[ALBUM_GET_LIST_COUNT].asInt();
			}

			if ( root[ALBUM_GET_LIST_CREATETIME].isInt() ) {
				createTime = root[ALBUM_GET_LIST_CREATETIME].asInt();
			}
		}
	}

public:
	string albumId;
	ALBUMTYPE albumType;
	string albumTitle;
	string albumDesc;
	string albumPhotoUrl;
	int childCount;
	int createTime;
};

#endif/*ALBUMITEM_H_*/
