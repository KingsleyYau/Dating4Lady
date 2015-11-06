/*
 * ManRecentViewListItem.h
 *
 *  Created on: 2015-3-2
 *      Author: Max.Chiu
 *      Email: Kingsleyyau@gmail.com
 */

#ifndef MANRECENTVIEWLISTITEM_H_
#define MANRECENTVIEWLISTITEM_H_

#include <string>
using namespace std;

#include <common/CommonFunc.h>

#include <json/json/json.h>

#include "../RequestEnumDefine.h"
#include "../RequestManDefine.h"

class ManRecentViewListItem {
public:
	void Parse(const Json::Value& root) {
		if( root.isObject() ) {
			if( root[MAN_RECENT_VIEW_LIST_MAN_ID].isString() ) {
				man_id = root[MAN_RECENT_VIEW_LIST_MAN_ID].asString();
			}

			if( root[MAN_RECENT_VIEW_LIST_LAST_TIME].isInt() ) {
				last_time = root[MAN_RECENT_VIEW_LIST_LAST_TIME].asInt();
			}
		}
	}

	ManRecentViewListItem() {		man_id = "";
		last_time = -1;
	}

	virtual ~ManRecentViewListItem() {

	}

	/**
	 * 获取男士列表结构体
	 * @param man_id		男士ID
	 * @param last_time		最后访问时间的秒数
	 */
	string man_id;
	int last_time;

};

#endif /* MANRECENTVIEWLISTITEM_H_ */
