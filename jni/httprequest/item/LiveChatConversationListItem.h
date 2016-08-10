/*
 * LiveChatConversationListItem.h
 *
 *  Created on: 2015-10-12
 *      Author: Samson
 * Description: 聊天历史记录item（聊天会话记录）
 */

#ifndef LIVECHATCONVERSATIONLISTITEM_H_
#define LIVECHATCONVERSATIONLISTITEM_H_

#include <string>
#include <list>
using namespace std;

#include <common/CommonFunc.h>

#include <json/json/json.h>

#include "../RequestEnumDefine.h"
#include "../RequestLCDefine.h"

class LiveChatConversationListItem
{
public:
	LiveChatConversationListItem()
	{		inviteId = "";
		startTime = "";
		duringTime = "";
		manId = "";
		womanName = "";
		manName = "";
		cnName = "";
		translatorId = "";
		translatorName = "";
	}

	virtual ~LiveChatConversationListItem()
	{

	}

public:
	bool Parse(const Json::Value& root)
	{
		bool result = false;
		if( root.isObject() )
		{
			if( root[LC_LADYCHATLIST_INVITEID].isString() )
			{
				inviteId = root[LC_LADYCHATLIST_INVITEID].asString();
			}

			if( root[LC_LADYCHATLIST_STARTTIME].isString() )
			{
				startTime = root[LC_LADYCHATLIST_STARTTIME].asString();
			}

			if( root[LC_LADYCHATLIST_DURINGTIME].isString() )
			{
				duringTime = root[LC_LADYCHATLIST_DURINGTIME].asString();
			}

			if( root[LC_LADYCHATLIST_MANID].isString() )
			{
				manId = root[LC_LADYCHATLIST_MANID].asString();
			}

			if( root[LC_LADYCHATLIST_MANNAME].isString() )
			{
				manName = root[LC_LADYCHATLIST_MANNAME].asString();
			}

			if( root[LC_LADYCHATLIST_WOMANNAME].isString() )
			{
				womanName = root[LC_LADYCHATLIST_WOMANNAME].asString();
			}

			if( root[LC_LADYCHATLIST_CNNAME].isString() )
			{
				cnName = root[LC_LADYCHATLIST_CNNAME].asString();
			}

			if( root[LC_LADYCHATLIST_TRANSLATORID].isString() )
			{
				translatorId = root[LC_LADYCHATLIST_TRANSLATORID].asString();
			}

			if( root[LC_LADYCHATLIST_TRANSLATORNAME].isString() )
			{
				translatorName = root[LC_LADYCHATLIST_TRANSLATORNAME].asString();
			}

			if ( !inviteId.empty() )
			{
				result = true;
			}
		}

		return result;
	}

public:
	string inviteId;
	string startTime;
	string duringTime;
	string manId;
	string womanName;
	string manName;
	string cnName;
	string translatorId;
	string translatorName;
};

typedef list<LiveChatConversationListItem> LiveChatConversationList;

#endif /* LIVECHATCONVERSATIONLISTITEM_H_ */
