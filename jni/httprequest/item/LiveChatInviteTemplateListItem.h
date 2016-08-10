/*
 * LiveChatInviteTemplateListItem.h
 *
 *  Created on: 2015-10-12
 *      Author: Samson
 * Description: 个人邀请模板item
 */

#ifndef LIVECHATINVITETEMPLATELISTITEM_H_
#define LIVECHATINVITETEMPLATELISTITEM_H_

#include <string>
#include <list>
using namespace std;

#include <common/CommonFunc.h>

#include <json/json/json.h>

#include "../RequestEnumDefine.h"
#include "../RequestLCDefine.h"

class LiveChatInviteTemplateListItem
{
public:
	LiveChatInviteTemplateListItem()
	{		tempId = "";
		tempContent = "";
		tempStatus = TEMPSTATUS_DEFAULT;
	}

	virtual ~LiveChatInviteTemplateListItem()
	{

	}

public:
	bool Parse(const Json::Value& root)
	{
		bool result = false;
		if( root.isObject() )
		{
			if( root[LC_INVITETEMPLATE_TEMPID].isString() )
			{
				tempId = root[LC_INVITETEMPLATE_TEMPID].asString();
			}

			if( root[LC_INVITETEMPLATE_TEMPCONTENT].isString() )
			{
				tempContent = root[LC_INVITETEMPLATE_TEMPCONTENT].asString();
			}

			if( root[LC_INVITETEMPLATE_STATUS].isString() )
			{
				string status = root[LC_INVITETEMPLATE_STATUS].asString();
				int iStatus = atoi(status.c_str());
				tempStatus = GetTempStatusWithInt(iStatus);
			}

			if ( !tempId.empty() )
			{
				result = true;
			}
		}

		return result;
	}

public:
	string tempId;
	string tempContent;
	TEMP_STATUS tempStatus;

};

typedef list<LiveChatInviteTemplateListItem> LiveChatInviteTemplateList;

#endif /* LIVECHATINVITETEMPLATELISTITEM_H_ */
