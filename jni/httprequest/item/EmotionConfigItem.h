/*
 * EmotionConfigItem.h
 *
 *  Created on: 2015-10-23
 *      Author: Hunter
 */
#ifndef EMOTIONCONFIGITEM_H
#define EMOTIONCONFIGITEM_H

using namespace std;

#include <list>
#include <string>
#include <common/CommonFunc.h>
#include <json/json/json.h>
#include "../RequestOtherDefine.h"

class EmotionConfigItem{
public:
	// type节点
	class TypeItem {
	public:
		TypeItem() {
			toflag = 0;
			typeId = "";
			typeName = "";
		}
		virtual ~TypeItem() {}
	public:
		bool Parsing(const Json::Value& data) {
			bool result = false;
			if (data.isObject()) {
				if (data[OTHER_EMOTIONCONFIG_TOFLAG].isString()) {
					string strToFlag = data[OTHER_EMOTIONCONFIG_TOFLAG].asString();
					toflag = atoi(strToFlag.c_str());
				}

				if (data[OTHER_EMOTIONCONFIG_TYPEID].isString()) {
					typeId = data[OTHER_EMOTIONCONFIG_TYPEID].asString();
				}

				if (data[OTHER_EMOTIONCONFIG_TYPENAME].isString()) {
					typeName = data[OTHER_EMOTIONCONFIG_TYPENAME].asString();
				}

				if (!typeId.empty()) {
					result = true;
				}
			}
			return result;
		}
	public:
		int		toflag;		// 终端使用标志
		string	typeId;		// 分类ID
		string	typeName;	// 分类名称
	};
	typedef list<TypeItem> TypeList;

	// tag节点
	class TagItem {
	public:
		TagItem() {
			toflag = 0;
			typeId = "";
			tagId = "";
			tagName = "";
		}
		virtual ~TagItem() {}
	public:
		bool Parsing(const Json::Value& data) {
			bool result = false;
			if (data.isObject()) {
				if (data[OTHER_EMOTIONCONFIG_TOFLAG].isString()) {
					string strToFlag = data[OTHER_EMOTIONCONFIG_TOFLAG].asString();
					toflag = atoi(strToFlag.c_str());
				}

				if (data[OTHER_EMOTIONCONFIG_TYPEID].isString()) {
					typeId = data[OTHER_EMOTIONCONFIG_TYPEID].asString();
				}

				if (data[OTHER_EMOTIONCONFIG_TAGSID].isString()) {
					tagId = data[OTHER_EMOTIONCONFIG_TAGSID].asString();
				}

				if (data[OTHER_EMOTIONCONFIG_TAGSNAME].isString()) {
					tagName = data[OTHER_EMOTIONCONFIG_TAGSNAME].asString();
				}

				if (!tagId.empty()) {
					result = true;
				}
			}
			return result;
		}
	public:
		int		toflag;		// 终端使用标志
		string	typeId;		// 分类ID
		string	tagId;		// tag ID
		string	tagName;	// tag名称
	};
	typedef list<TagItem> TagList;

	// 表情item
	class EmotionItem {
	public:
		EmotionItem() {
			fileName = "";
			price = 0;
			isNew = false;
			isSale = false;
			sortId = 0;
			typeId = "";
			tagId = "";
			title = "";
		}
		virtual ~EmotionItem() {}
	public:
		bool Parsing(const Json::Value& data) {
			bool result = false;
			if (data.isObject()) {
				if (data[OTHER_EMOTIONCONFIG_FILENAME].isString()) {
					fileName = data[OTHER_EMOTIONCONFIG_FILENAME].asString();
				}
				if (data[OTHER_EMOTIONCONFIG_PRICE].isString()) {
					string strPrice = data[OTHER_EMOTIONCONFIG_PRICE].asString();
					price = atof(strPrice.c_str());
				}
				if (data[OTHER_EMOTIONCONFIG_ISNEW].isString()) {
					isNew = data[OTHER_EMOTIONCONFIG_ISNEW].asString() == "1" ? true : false;
				}
				if (data[OTHER_EMOTIONCONFIG_ISSALE].isString()) {
					isSale = data[OTHER_EMOTIONCONFIG_ISSALE].asString() == "1" ? true : false;
				}
				if (data[OTHER_EMOTIONCONFIG_SORTID].isString()) {
					string strSortId = data[OTHER_EMOTIONCONFIG_SORTID].asString();
					sortId = atoi(strSortId.c_str());
				}
				if (data[OTHER_EMOTIONCONFIG_TYPEID].isString()) {
					typeId = data[OTHER_EMOTIONCONFIG_TYPEID].asString();
				}
				if (data[OTHER_EMOTIONCONFIG_TAGSID].isString()) {
					tagId = data[OTHER_EMOTIONCONFIG_TAGSID].asString();
				}
				if (data[OTHER_EMOTIONCONFIG_TITLE].isString()) {
					title = data[OTHER_EMOTIONCONFIG_TITLE].asString();
				}

				if (!fileName.empty()) {
					result = true;
				}
			}
			return result;
		}
	public:
		string	fileName;	// 文件名
		double	price;		// 所需点数
		bool	isNew;		// 是否有new标志
		bool	isSale;		// 是否打折
		int		sortId;		// 排序字段（降序）
		string	typeId;		// 分类ID
		string	tagId;		// tag ID
		string	title;		// 表情标题
	};
	typedef list<EmotionItem> EmotionList;

public:
	EmotionConfigItem(){
		version = 0;
		path = "";
	}
	virtual ~EmotionConfigItem() {}

public:
	bool Parsing(const Json::Value& data)
	{
		bool result = false;
		if (data.isObject()) {
			if (data[OTHER_EMOTIONCONFIG_VERSION].isIntegral()) {
				version = data[OTHER_EMOTIONCONFIG_VERSION].asInt();
			}
			if (data[OTHER_EMOTIONCONFIG_FSPATH].isString()) {
				path = data[OTHER_EMOTIONCONFIG_FSPATH].asString();
			}
			if (data[OTHER_EMOTIONCONFIG_TYPELIST].isArray()) {
				for(int i = 0; i < data[OTHER_EMOTIONCONFIG_TYPELIST].size(); i++) {
					TypeItem item;
					if (item.Parsing(data[OTHER_EMOTIONCONFIG_TYPELIST].get(i, Json::Value::null))) {
						typeList.push_back(item);
					}
				}
			}
			if (data[OTHER_EMOTIONCONFIG_TAGSLIST].isArray()) {
				for(int i = 0; i < data[OTHER_EMOTIONCONFIG_TAGSLIST].size(); i++) {
					TagItem item;
					if (item.Parsing(data[OTHER_EMOTIONCONFIG_TAGSLIST].get(i, Json::Value::null))) {
						tagList.push_back(item);
					}
				}
			}
			if (data[OTHER_EMOTIONCONFIG_FORMANLIST].isArray()) {
				for(int i = 0; i < data[OTHER_EMOTIONCONFIG_FORMANLIST].size(); i++) {
					EmotionItem item;
					if (item.Parsing(data[OTHER_EMOTIONCONFIG_FORMANLIST].get(i, Json::Value::null))) {
						manEmotionList.push_back(item);
					}
				}
			}
			if (data[OTHER_EMOTIONCONFIG_FORLADYLIST].isArray()) {
				for(int i = 0; i < data[OTHER_EMOTIONCONFIG_FORLADYLIST].size(); i++) {
					EmotionItem item;
					if (item.Parsing(data[OTHER_EMOTIONCONFIG_FORLADYLIST].get(i, Json::Value::null))) {
						ladyEmotionList.push_back(item);
					}
				}
			}

			if (!path.empty()) {
				result = true;
			}
		}

		return result;
	}

public:
	int			version;		// 高级表情版本号
	string		path;			// 路径
	TypeList	typeList;		// 分类列表
	TagList		tagList;		// tag列表
	EmotionList	manEmotionList;	// 男士表情列表
	EmotionList	ladyEmotionList;// 女士表情列表

};

#endif/*EMOTIONCONFIGITEM_H*/
