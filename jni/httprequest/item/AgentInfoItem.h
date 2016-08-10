/*
 * AgentInfoItem.h
 *
 *  Created on: 2015-11-20
 *      Author: Samson
 */
#ifndef AGENTINFO_ITEM_H
#define AGENTINFO_ITEM_H

using namespace std;

#include <list>
#include <string>
#include <common/StringHandle.h>
#include <common/CommonFunc.h>
#include <json/json/json.h>
#include "../RequestOtherDefine.h"

class AgentInfoItem
{
public:
	AgentInfoItem()
	{
		name = "";
		id = "";
		city = "";
		addr = "";
		email = "";
		tel = "";
		fax = "";
		contact = "";
		postcode = "";
	};

	AgentInfoItem(const AgentInfoItem& item)
	{
		name = item.name;
		id = item.id;
		city = item.city;
		addr = item.addr;
		email = item.email;
		tel = item.tel;
		fax = item.fax;
		contact = item.contact;
		postcode = item.postcode;
	}

	~AgentInfoItem() {};

public:
	bool Parsing(const Json::Value& data)
	{
		bool result = false;
		if(data.isObject())
		{
			if(data[GET_AGENTINFO_NAME].isString()) {
				name = data[GET_AGENTINFO_NAME].asString();
			}
			if(data[GET_AGENTINFO_ID].isString()) {
				id = data[GET_AGENTINFO_ID].asString();
			}
			if(data[GET_AGENTINFO_CITY].isString()) {
				city = data[GET_AGENTINFO_CITY].asString();
			}
			if(data[GET_AGENTINFO_ADDR].isString()) {
				addr = data[GET_AGENTINFO_ADDR].asString();
			}
			if(data[GET_AGENTINFO_EMAIL].isString()) {
				email = data[GET_AGENTINFO_EMAIL].asString();
			}
			if(data[GET_AGENTINFO_TEL].isString()) {
				tel = data[GET_AGENTINFO_TEL].asString();
			}
			if(data[GET_AGENTINFO_FAX].isString()) {
				fax = data[GET_AGENTINFO_FAX].asString();
			}
			if(data[GET_AGENTINFO_CONTACT].isString()) {
				contact = data[GET_AGENTINFO_CONTACT].asString();
			}
			if(data[GET_AGENTINFO_POSTCODE].isString()){
				postcode = data[GET_AGENTINFO_POSTCODE].asString();
			}

			if(!id.empty()){
				result = true;
			}
		}
		return result;
	}
public:
	string name;
	string id;
	string city;
	string addr;
	string email;
	string tel;
	string fax;
	string contact;
	string postcode;
};

#endif/*AGENTINFO_ITEM_H*/
