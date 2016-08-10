/*
 * com_qpidnetwork_livechat_jni_LiveChatClient.cpp
 *
 *  Created on: 2015-08-03
 *      Author: Samson.Fan
 * Description:	女士端LiveChat JNI接口
 */

#include "com_qpidnetwork_livechat_jni_LiveChatClient.h"
#include <jni.h>
#include <livechat/ILiveChatClient.h>
#include <map>
#include <common/CommonFunc.h>
#include <livechat/JniIntToType.h>	// 处理 jint 转  枚举type
#include <livechat/LiveChatJniCallbackItemDef.h>
#include <common/KLog.h>
#include <AndroidCommon/DeviceJniIntToType.h>

using namespace std;

// -------------- java --------------
static JavaVM* gJavaVM = NULL;

/* java listener object */
static jobject gListener = NULL;

/* java data item */
typedef map<string, jobject> JavaItemMap;
static JavaItemMap gJavaItemMap;

bool GetEnv(JNIEnv** env, bool* isAttachThread);
bool ReleaseEnv(bool isAttachThread);

static ILiveChatClient* g_liveChatClient = NULL;
static string gDeviceId = "";
// -------------- c++ ----------------
string GetJString(JNIEnv* env, jstring str)
{
	string result("");
	if (NULL != str) {
		const char* cpTemp = env->GetStringUTFChars(str, 0);
		result = cpTemp;
		env->ReleaseStringUTFChars(str, cpTemp);
	}
	return result;
}

void InitClassHelper(JNIEnv *env, const char *path, jobject *objptr) {
	FileLog("LiveChatClientJni", "InitClassHelper( path : %s )", path);
    jclass cls = env->FindClass(path);
    if( !cls ) {
    	FileLog("LiveChatClientJni", "InitClassHelper( !cls )");
        return;
    }

    jmethodID constr = env->GetMethodID(cls, "<init>", "()V");
    if( !constr ) {
    	FileLog("LiveChatClientJni", "InitClassHelper( !constr )");
        constr = env->GetMethodID(cls, "<init>", "(Ljava/lang/String;I)V");
        if( !constr ) {
        	FileLog("LiveChatClientJni", "InitClassHelper( !constr )");
            return;
        }
        return;
    }

    jobject obj = env->NewObject(cls, constr);
    if( !obj ) {
    	FileLog("LiveChatClientJni", "InitClassHelper( !obj )");
        return;
    }

    (*objptr) = env->NewGlobalRef(obj);
}


void InsertJObjectClassToMap(JNIEnv* env, const char* classPath)
{
	jobject jTempObject;
	InitClassHelper(env, classPath, &jTempObject);
	gJavaItemMap.insert(JavaItemMap::value_type(classPath, jTempObject));
}

jclass GetJObjectClassWithMap(JNIEnv* env, const char* classPath)
{
	jclass cls = NULL;
	JavaItemMap::iterator itr = gJavaItemMap.find(classPath);
	if( itr != gJavaItemMap.end() ) {
		jobject jItemObj = itr->second;
		cls = env->GetObjectClass(jItemObj);
	}
	return cls;
}

jobject GetTalkUserListItem(JNIEnv* env, const TalkUserListItem& item)
{
	jobject jItem = NULL;
	jclass jItemCls = GetJObjectClassWithMap(env, LIVECHAT_TALKUSERLISTTIME_CLASS);
	if (NULL != jItemCls) {
		jmethodID init = env->GetMethodID(jItemCls, "<init>"
				, "(Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;IILjava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;ZIIIIIIILjava/lang/String;ZLjava/lang/String;Ljava/lang/String;ZI)V");

		int sexType = UserSexTypeToInt(item.sexType);
		int marryType = MarryTypeToInt(item.marryType);
		int statusType = UserStatusTypeToInt(item.status);
		int userType = UserTypeToInt(item.userType);
		int deviceType = DeviceTypeToInt(item.deviceType);
		int clientType = ClientTypeToInt(item.clientType);
		int transStatus = UserStatusTypeToInt(item.transStatus);

		FileLog("LiveChatClientJni", "GetTalkUserListItem() item.userType:%d, item.deviceType:%d", item.userType, item.deviceType);
		FileLog("LiveChatClientJni", "GetTalkUserListItem() userType:%d, deviceType:%d", userType, deviceType);

		jstring jUserId = env->NewStringUTF(item.userId.c_str());
		jstring jUserName = env->NewStringUTF(item.userName.c_str());
		jstring jServer = env->NewStringUTF(item.server.c_str());
		jstring jImgUrl = env->NewStringUTF(item.imgUrl.c_str());
		jstring jWeight = env->NewStringUTF(item.weight.c_str());
		jstring jHeight = env->NewStringUTF(item.height.c_str());
		jstring jCountry = env->NewStringUTF(item.country.c_str());
		jstring jProvince = env->NewStringUTF(item.province.c_str());
		jstring jClientVersion = env->NewStringUTF(item.clientVersion.c_str());
		jstring jTransUserId = env->NewStringUTF(item.transUserId.c_str());
		jstring jTransUserName = env->NewStringUTF(item.transUserName.c_str());

		jItem = env->NewObject(jItemCls, init,
					jUserId,
					jUserName,
					jServer,
					jImgUrl,
					sexType,
					item.age,
					jWeight,
					jHeight,
					jCountry,
					jProvince,
					item.videoChat,
					item.videoCount,
					marryType,
					statusType,
					userType,
					item.orderValue,
					deviceType,
					clientType,
					jClientVersion,
					item.needTrans,
					jTransUserId,
					jTransUserName,
					item.transBind,
					transStatus
					);

		env->DeleteLocalRef(jUserId);
		env->DeleteLocalRef(jServer);
		env->DeleteLocalRef(jImgUrl);
		env->DeleteLocalRef(jWeight);
		env->DeleteLocalRef(jHeight);
		env->DeleteLocalRef(jCountry);
		env->DeleteLocalRef(jProvince);
		env->DeleteLocalRef(jClientVersion);
		env->DeleteLocalRef(jTransUserId);
		env->DeleteLocalRef(jTransUserName);
	}

	return jItem;
}

jobjectArray GetTalkUserList(JNIEnv* env, const TalkUserList& list)
{
	jobjectArray array = NULL;
	jclass jItemCls = GetJObjectClassWithMap(env, LIVECHAT_TALKUSERLISTTIME_CLASS);
	if (NULL != jItemCls) {
		array = env->NewObjectArray(list.size(), jItemCls, NULL);
		if (NULL != array) {
			int i = 0;
			for(TalkUserList::const_iterator itr = list.begin();
				itr != list.end();
				itr++)
			{
				jobject jItem = GetTalkUserListItem(env, (*itr));
				if (NULL != jItem) {
					env->SetObjectArrayElement(array, i, jItem);
					i++;
				}
				env->DeleteLocalRef(jItem);
			}
		}
	}

	return array;
}

jobject GetTalkSessionListItem(JNIEnv* env, const TalkSessionListItem& item)
{
	jobject jItem = NULL;
	jclass jItemCls = GetJObjectClassWithMap(env, LIVECHAT_TALKSESSIONLISTTIME_CLASS);
	if (NULL != jItemCls) {
		jmethodID init = env->GetMethodID(jItemCls, "<init>"
				, "(Ljava/lang/String;Ljava/lang/String;ZI)V");

		jstring jTargetId = env->NewStringUTF(item.targetId.c_str());
		jstring jInviteId = env->NewStringUTF(item.invitedId.c_str());
		jItem = env->NewObject(jItemCls, init,
					jTargetId,
					jInviteId,
					item.charget,
					item.chatTime
					);
		env->DeleteLocalRef(jTargetId);
		env->DeleteLocalRef(jInviteId);
	}

	return jItem;
}

jobjectArray GetTalkSessionList(JNIEnv* env, const TalkSessionList& list)
{
	jobjectArray array = NULL;
	jclass jItemCls = GetJObjectClassWithMap(env, LIVECHAT_TALKSESSIONLISTTIME_CLASS);
	if (NULL != jItemCls) {
		array = env->NewObjectArray(list.size(), jItemCls, NULL);
		if (NULL != array) {
			int i = 0;
			for(TalkSessionList::const_iterator itr = list.begin();
				itr != list.end();
				itr++)
			{
				jobject jItem = GetTalkSessionListItem(env, (*itr));
				if (NULL != jItem) {
					env->SetObjectArrayElement(array, i, jItem);
					i++;
				}
				env->DeleteLocalRef(jItem);
			}
		}
	}

	return array;
}

class LiveChatClientListener : public ILiveChatClientListener {
public:
	LiveChatClientListener() {};
	virtual ~LiveChatClientListener() {};

public:
	// 客户端主动请求
	// 回调函数的参数在 err 之前的为请求参数，在 errmsg 之后为返回参数
	virtual void OnLogin(LCC_ERR_TYPE err, const string& errmsg) {
		JNIEnv* env = NULL;
		bool isAttachThread = false;
		GetEnv(&env, &isAttachThread);

		FileLog("LiveChatClientJni", "OnLogin() callback, env:%p, isAttachThread:%d", env, isAttachThread);

		jclass jCallbackCls = env->GetObjectClass(gListener);

		string signure = "(ILjava/lang/String;)V";
		jmethodID jCallback = env->GetMethodID(jCallbackCls, "OnLogin", signure.c_str());
		if (NULL != gListener && NULL != jCallback)
		{
			FileLog("LiveChatClientJni", "OnLogin() callback now");

			int errType = LccErrTypeToInt(err);
			jstring jerrmsg = env->NewStringUTF(errmsg.c_str());
			env->CallVoidMethod(gListener, jCallback, errType, jerrmsg);
			env->DeleteLocalRef(jerrmsg);

			FileLog("LiveChatClientJni", "OnLogin() callback ok");
		}

		ReleaseEnv(isAttachThread);
	}
	virtual void OnLogout(LCC_ERR_TYPE err, const string& errmsg) {
		JNIEnv* env = NULL;
		bool isAttachThread = false;
		GetEnv(&env, &isAttachThread);

		FileLog("LiveChatClientJni", "OnLogout() callback begin, gListener:%p, env:%p, isAttachThread:%d", gListener, env, isAttachThread);

		jclass jCallbackCls = env->GetObjectClass(gListener);

		FileLog("LiveChatClientJni", "OnLogout() callback jCallbackCls:%p", jCallbackCls);

		string signure = "(ILjava/lang/String;)V";
		jmethodID jCallback = env->GetMethodID(jCallbackCls, "OnLogout", signure.c_str());

		FileLog("LiveChatClientJni", "OnLogout() callback jCallback:%p, signure:%s", jCallbackCls, signure.c_str());

		if (NULL != gListener && NULL != jCallback)
		{
			FileLog("LiveChatClientJni", "OnLogout() callback now");

			int errType = LccErrTypeToInt(err);
			jstring jerrmsg = env->NewStringUTF(errmsg.c_str());
			env->CallVoidMethod(gListener, jCallback, errType, jerrmsg);
			env->DeleteLocalRef(jerrmsg);

			FileLog("LiveChatClientJni", "OnLogout() callback ok");
		}

		ReleaseEnv(isAttachThread);

		FileLog("LiveChatClientJni", "OnLogout() callback end");
	}
	virtual void OnSetStatus(LCC_ERR_TYPE err, const string& errmsg) {
		JNIEnv* env = NULL;
		bool isAttachThread = false;
		GetEnv(&env, &isAttachThread);

		FileLog("LiveChatClientJni", "OnSetStatus() callback, env:%p, isAttachThread:%d", env, isAttachThread);

		jclass jCallbackCls = env->GetObjectClass(gListener);

		string signure = "(ILjava/lang/String;)V";
		jmethodID jCallback = env->GetMethodID(jCallbackCls, "OnSetStatus", signure.c_str());
		if (NULL != gListener && NULL != jCallback)
		{
			FileLog("LiveChatClientJni", "OnSetStatus() callback now");

			int errType = LccErrTypeToInt(err);
			jstring jerrmsg = env->NewStringUTF(errmsg.c_str());
			env->CallVoidMethod(gListener, jCallback, errType, jerrmsg);
			env->DeleteLocalRef(jerrmsg);

			FileLog("LiveChatClientJni", "OnSetStatus() callback ok");
		}

		ReleaseEnv(isAttachThread);
	}
	virtual void OnEndTalk(const string& inUserId, LCC_ERR_TYPE err, const string& errmsg) {
		JNIEnv* env = NULL;
		bool isAttachThread = false;
		GetEnv(&env, &isAttachThread);

		FileLog("LiveChatClientJni", "OnEndTalk() callback, env:%p, isAttachThread:%d", env, isAttachThread);

		jclass jCallbackCls = env->GetObjectClass(gListener);

		string signure = "(ILjava/lang/String;Ljava/lang/String;)V";
		jmethodID jCallback = env->GetMethodID(jCallbackCls, "OnEndTalk", signure.c_str());
		if (NULL != gListener && NULL != jCallback)
		{
			FileLog("LiveChatClientJni", "OnEndTalk() callback now");

			int errType = LccErrTypeToInt(err);
			jstring jerrmsg = env->NewStringUTF(errmsg.c_str());
			jstring juserId = env->NewStringUTF(inUserId.c_str());
			env->CallVoidMethod(gListener, jCallback, errType, jerrmsg, juserId);
			env->DeleteLocalRef(juserId);
			env->DeleteLocalRef(jerrmsg);

			FileLog("LiveChatClientJni", "OnEndTalk() callback ok");
		}

		ReleaseEnv(isAttachThread);
	}
	virtual void OnGetUserStatus(const UserIdList& inList, LCC_ERR_TYPE err, const string& errmsg, const UserStatusList& list) {
		// 女士端没有用
	}
	virtual void OnGetTalkInfo(const string& inUserId, LCC_ERR_TYPE err, const string& errmsg, const string& userId, const string& invitedId, bool charge, unsigned int chatTime) {
		// 女士端没有用
	}
	virtual void OnSendTextMessage(const string& inUserId, const string& inMessage, int inTicket, LCC_ERR_TYPE err, const string& errmsg) {
		JNIEnv* env = NULL;
		bool isAttachThread = false;
		GetEnv(&env, &isAttachThread);

		FileLog("LiveChatClientJni", "OnSendTextMessage() callback, env:%p, isAttachThread:%d", env, isAttachThread);

		jclass jCallbackCls = env->GetObjectClass(gListener);
		string signure = "(ILjava/lang/String;Ljava/lang/String;Ljava/lang/String;I)V";
		jmethodID jCallback = env->GetMethodID(jCallbackCls, "OnSendMessage", signure.c_str());
		if (NULL != gListener && NULL != jCallback)
		{
			FileLog("LiveChatClientJni", "OnSendTextMessage() callback now");

			int errType = LccErrTypeToInt(err);
			jstring jerrmsg = env->NewStringUTF(errmsg.c_str());
			jstring juserId = env->NewStringUTF(inUserId.c_str());
			jstring jmessage = env->NewStringUTF(inMessage.c_str());

			env->CallVoidMethod(gListener, jCallback, errType, jerrmsg, juserId, jmessage, inTicket);

			env->DeleteLocalRef(jmessage);
			env->DeleteLocalRef(juserId);
			env->DeleteLocalRef(jerrmsg);

			FileLog("LiveChatClientJni", "OnSendTextMessage() callback ok");
		}

		ReleaseEnv(isAttachThread);
	}
	virtual void OnSendEmotion(const string& inUserId, const string& inEmotionId, int inTicket, LCC_ERR_TYPE err, const string& errmsg) {
		JNIEnv* env = NULL;
		bool isAttachThread = false;
		GetEnv(&env, &isAttachThread);

		FileLog("LiveChatClientJni", "OnSendEmotion() callback, env:%p, isAttachThread:%d", env, isAttachThread);

		jclass jCallbackCls = env->GetObjectClass(gListener);
		string signure = "(ILjava/lang/String;Ljava/lang/String;Ljava/lang/String;I)V";
		jmethodID jCallback = env->GetMethodID(jCallbackCls, "OnSendEmotion", signure.c_str());
		if (NULL != gListener && NULL != jCallback)
		{
			FileLog("LiveChatClientJni", "OnSendEmotion() callback now");

			int errType = LccErrTypeToInt(err);
			jstring jerrmsg = env->NewStringUTF(errmsg.c_str());
			jstring juserId = env->NewStringUTF(inUserId.c_str());
			jstring jemotionId = env->NewStringUTF(inEmotionId.c_str());

			env->CallVoidMethod(gListener, jCallback, errType, jerrmsg, juserId, jemotionId, inTicket);

			env->DeleteLocalRef(jemotionId);
			env->DeleteLocalRef(juserId);
			env->DeleteLocalRef(jerrmsg);

			FileLog("LiveChatClientJni", "OnSendEmotion() callback ok");
		}

		ReleaseEnv(isAttachThread);
	}
	virtual void OnSendVGift(const string& inUserId, const string& inGiftId, int ticket, LCC_ERR_TYPE err, const string& errmsg) {
		// 没有用
	}
	virtual void OnGetVoiceCode(const string& inUserId, int ticket, LCC_ERR_TYPE err, const string& errmsg, const string& voiceCode) {
		JNIEnv* env = NULL;
		bool isAttachThread = false;
		GetEnv(&env, &isAttachThread);

		FileLog("LiveChatClientJni", "OnGetVoiceCode() callback, env:%p, isAttachThread:%d", env, isAttachThread);

		jclass jCallbackCls = env->GetObjectClass(gListener);
		string signure = "(ILjava/lang/String;Ljava/lang/String;ILjava/lang/String;)V";
		jmethodID jCallback = env->GetMethodID(jCallbackCls, "OnGetVoiceCode", signure.c_str());
		if (NULL != gListener && NULL != jCallback)
		{
			FileLog("LiveChatClientJni", "OnGetVoiceCode() callback now");

			int errType = LccErrTypeToInt(err);
			jstring jerrmsg = env->NewStringUTF(errmsg.c_str());
			jstring juserId = env->NewStringUTF(inUserId.c_str());
			jstring jvoiceCode = env->NewStringUTF(voiceCode.c_str());

			env->CallVoidMethod(gListener, jCallback, errType, jerrmsg, juserId, ticket, jvoiceCode);

			env->DeleteLocalRef(jvoiceCode);
			env->DeleteLocalRef(juserId);
			env->DeleteLocalRef(jerrmsg);

			FileLog("LiveChatClientJni", "OnGetVoiceCode() callback ok");
		}

		ReleaseEnv(isAttachThread);
	}
	virtual void OnSendVoice(const string& inUserId, const string& inVoiceId, int ticket, LCC_ERR_TYPE err, const string& errmsg) {
		JNIEnv* env = NULL;
		bool isAttachThread = false;
		GetEnv(&env, &isAttachThread);

		FileLog("LiveChatClientJni", "OnSendVoice() callback, env:%p, isAttachThread:%d", env, isAttachThread);

		jclass jCallbackCls = env->GetObjectClass(gListener);
		string signure = "(ILjava/lang/String;Ljava/lang/String;Ljava/lang/String;I)V";
		jmethodID jCallback = env->GetMethodID(jCallbackCls, "OnSendVoice", signure.c_str());
		if (NULL != gListener && NULL != jCallback)
		{
			FileLog("LiveChatClientJni", "OnSendVoice() callback now");

			int errType = LccErrTypeToInt(err);
			jstring jerrmsg = env->NewStringUTF(errmsg.c_str());
			jstring juserId = env->NewStringUTF(inUserId.c_str());
			jstring jvoiceId = env->NewStringUTF(inVoiceId.c_str());

			env->CallVoidMethod(gListener, jCallback, errType, jerrmsg, juserId, jvoiceId, ticket);

			env->DeleteLocalRef(jvoiceId);
			env->DeleteLocalRef(juserId);
			env->DeleteLocalRef(jerrmsg);

			FileLog("LiveChatClientJni", "OnSendVoice() callback ok");
		}

		ReleaseEnv(isAttachThread);
	}
	virtual void OnUseTryTicket(const string& inUserId, LCC_ERR_TYPE err, const string& errmsg, const string& userId, TRY_TICKET_EVENT tickEvent) {
		JNIEnv* env = NULL;
		bool isAttachThread = false;
		GetEnv(&env, &isAttachThread);

		FileLog("LiveChatClientJni", "OnUseTryTicket() callback, env:%p, isAttachThread:%d", env, isAttachThread);

		jclass jCallbackCls = env->GetObjectClass(gListener);
		string signure = "(ILjava/lang/String;Ljava/lang/String;I)V";
		jmethodID jCallback = env->GetMethodID(jCallbackCls, "OnUseTryTicket", signure.c_str());
		if (NULL != gListener && NULL != jCallback)
		{
			FileLog("LiveChatClientJni", "OnUseTryTicket() callback now");

			int errType = LccErrTypeToInt(err);
			jstring jerrmsg = env->NewStringUTF(errmsg.c_str());
			jstring juserId = env->NewStringUTF(inUserId.c_str());
			int eventType = TryTicketEventTypeToInt(tickEvent);

			env->CallVoidMethod(gListener, jCallback, errType, jerrmsg, juserId, eventType);

			env->DeleteLocalRef(juserId);
			env->DeleteLocalRef(jerrmsg);

			FileLog("LiveChatClientJni", "OnUseTryTicket() callback ok");
		}

		ReleaseEnv(isAttachThread);
	}
	virtual void OnGetTalkList(int inListType, LCC_ERR_TYPE err, const string& errmsg, const TalkListInfo& talkListInfo) {
		JNIEnv* env = NULL;
		bool isAttachThread = false;
		GetEnv(&env, &isAttachThread);

		FileLog("LiveChatClientJni", "OnGetTalkList() err:%d, errmsg:%s, env:%p, isAttachThread:%d", err, errmsg.c_str(), env, isAttachThread);

		// invite列表
		jobjectArray jInviteArray = GetTalkUserList(env, talkListInfo.invite);
		// inviteSession列表
		jobjectArray jInviteSessionArray = GetTalkSessionList(env, talkListInfo.inviteSession);
		// invited列表
		jobjectArray jInvitedArray = GetTalkUserList(env, talkListInfo.invited);
		// inivtedSession列表
		jobjectArray jInvitedSessionArray = GetTalkSessionList(env, talkListInfo.invitedSession);
		// chating列表
		jobjectArray jChatingArray = GetTalkUserList(env, talkListInfo.chating);
		// chatingSession列表
		jobjectArray jChatingSessionArray = GetTalkSessionList(env, talkListInfo.chatingSession);
		// pause列表
		jobjectArray jPauseArray = GetTalkUserList(env, talkListInfo.pause);
		// pauseSession列表
		jobjectArray jPauseSessionArray = GetTalkSessionList(env, talkListInfo.pauseSession);

		FileLog("LiveChatClientJni", "OnGetTalkList() create array ok");

		// Create info
		jobject jTalkListInfoItem = NULL;
		jclass jTalkListInfoCls = GetJObjectClassWithMap(env, LIVECHAT_TALKLISTINFO_CLASS);
		if (NULL != jTalkListInfoCls) {
			string initSig = "(";
			// Invite
			initSig += "[L";
			initSig += LIVECHAT_TALKUSERLISTTIME_CLASS;
			initSig += ";";
			initSig += "[L";
			initSig += LIVECHAT_TALKSESSIONLISTTIME_CLASS;
			initSig += ";";
			// Invited
			initSig += "[L";
			initSig += LIVECHAT_TALKUSERLISTTIME_CLASS;
			initSig += ";";
			initSig += "[L";
			initSig += LIVECHAT_TALKSESSIONLISTTIME_CLASS;
			initSig += ";";
			// chating
			initSig += "[L";
			initSig += LIVECHAT_TALKUSERLISTTIME_CLASS;
			initSig += ";";
			initSig += "[L";
			initSig += LIVECHAT_TALKSESSIONLISTTIME_CLASS;
			initSig += ";";
			// pause
			initSig += "[L";
			initSig += LIVECHAT_TALKUSERLISTTIME_CLASS;
			initSig += ";";
			initSig += "[L";
			initSig += LIVECHAT_TALKSESSIONLISTTIME_CLASS;
			initSig += ";";
			// end
			initSig += ")V";

			jmethodID init = env->GetMethodID(jTalkListInfoCls, "<init>"
							, initSig.c_str());

			jTalkListInfoItem = env->NewObject(jTalkListInfoCls, init,
									jInviteArray,
									jInviteSessionArray,
									jInvitedArray,
									jInvitedSessionArray,
									jChatingArray,
									jChatingSessionArray,
									jPauseArray,
									jPauseSessionArray
									);
		}

		FileLog("LiveChatClientJni", "OnGetTalkList() create info ok");

		// 释放列表
		if (NULL != jInviteArray) {
			env->DeleteLocalRef(jInviteArray);
		}
		if (NULL != jInviteSessionArray) {
			env->DeleteLocalRef(jInviteSessionArray);
		}
		if (NULL != jInvitedArray) {
			env->DeleteLocalRef(jInvitedArray);
		}
		if (NULL != jInvitedSessionArray) {
			env->DeleteLocalRef(jInvitedSessionArray);
		}
		if (NULL != jChatingArray) {
			env->DeleteLocalRef(jChatingArray);
		}
		if (NULL != jChatingSessionArray) {
			env->DeleteLocalRef(jChatingSessionArray);
		}
		if (NULL != jPauseArray) {
			env->DeleteLocalRef(jPauseArray);
		}
		if (NULL != jPauseSessionArray) {
			env->DeleteLocalRef(jPauseSessionArray);
		}

		jclass jCallbackCls = env->GetObjectClass(gListener);
		string signure = "(ILjava/lang/String;I";
		signure += "L";
		signure += LIVECHAT_TALKLISTINFO_CLASS;
		signure += ";)V";
		jmethodID jCallback = env->GetMethodID(jCallbackCls, "OnGetTalkList", signure.c_str());
		if (NULL != gListener && NULL != jCallback)
		{
			FileLog("LiveChatClientJni", "OnGetTalkList() callback now");

			int errType = LccErrTypeToInt(err);
			jstring jerrmsg = env->NewStringUTF(errmsg.c_str());
			env->CallVoidMethod(gListener, jCallback, errType, jerrmsg, inListType, jTalkListInfoItem);
			env->DeleteLocalRef(jerrmsg);

			FileLog("LiveChatClientJni", "OnGetTalkList() callback ok");
		}

		if (NULL != jTalkListInfoItem) {
			env->DeleteLocalRef(jTalkListInfoItem);
		}

		ReleaseEnv(isAttachThread);
	}
	virtual void OnSendPhoto(LCC_ERR_TYPE err, const string& errmsg, int ticket) {
		// 没有用
	}
	virtual void OnSendLadyPhoto(LCC_ERR_TYPE err, const string& errmsg, int ticket) {
		JNIEnv* env = NULL;
		bool isAttachThread = false;
		GetEnv(&env, &isAttachThread);

		FileLog("LiveChatClientJni", "OnSendLadyPhoto() callback, env:%p, isAttachThread:%d", env, isAttachThread);

		jclass jCallbackCls = env->GetObjectClass(gListener);
		string signure = "(ILjava/lang/String;I)V";
		jmethodID jCallback = env->GetMethodID(jCallbackCls, "OnSendLadyPhoto", signure.c_str());
		if (NULL != gListener && NULL != jCallback)
		{
			FileLog("LiveChatClientJni", "OnSendLadyPhoto() callback now");

			int errType = LccErrTypeToInt(err);
			jstring jerrmsg = env->NewStringUTF(errmsg.c_str());

			env->CallVoidMethod(gListener, jCallback, errType, jerrmsg, ticket);

			env->DeleteLocalRef(jerrmsg);

			FileLog("LiveChatClientJni", "OnSendLadyPhoto() callback ok");
		}

		ReleaseEnv(isAttachThread);
	}
	virtual void OnShowPhoto(LCC_ERR_TYPE err, const string& errmsg, int ticket)
	{
		// 女士端没有用
	}
	virtual void OnPlayVideo(LCC_ERR_TYPE err, const string& errmsg, int ticket)
	{

	}
	virtual void OnSendLadyVideo(LCC_ERR_TYPE err, const string& errmsg, int ticket)
	{
		JNIEnv* env = NULL;
		bool isAttachThread = false;
		GetEnv(&env, &isAttachThread);

		FileLog("LiveChatClientJni", "OnSendLadyVideo() callback, env:%p, isAttachThread:%d", env, isAttachThread);

		jclass jCallbackCls = env->GetObjectClass(gListener);
		string signure = "(ILjava/lang/String;I)V";
		jmethodID jCallback = env->GetMethodID(jCallbackCls, "OnSendLadyVideo", signure.c_str());
		if (NULL != gListener && NULL != jCallback)
		{
			FileLog("LiveChatClientJni", "OnSendLadyVideo() callback now");

			int errType = LccErrTypeToInt(err);
			jstring jerrmsg = env->NewStringUTF(errmsg.c_str());

			env->CallVoidMethod(gListener, jCallback, errType, jerrmsg, ticket);

			env->DeleteLocalRef(jerrmsg);

			FileLog("LiveChatClientJni", "OnSendLadyVideo() callback ok");
		}

		ReleaseEnv(isAttachThread);
	}
	virtual void OnGetLadyCondition(const string& inUserId, LCC_ERR_TYPE err, const string& errmsg, const LadyConditionItem& item)
	{

	}
	virtual void OnGetLadyCustomTemplate(const string& inUserId, LCC_ERR_TYPE err, const string& errmsg, const vector<string>& contents, const vector<bool>& flags)
	{

	}
	virtual void OnSendMagicIcon(const string& inUserId, const string& inIconId, int inTicket, LCC_ERR_TYPE err, const string& errmsg) {}
	virtual void OnGetUserInfo(const string& inUserId, LCC_ERR_TYPE err, const string& errmsg, const UserInfoItem& item)
	{
		JNIEnv* env = NULL;
		bool isAttachThread = false;
		GetEnv(&env, &isAttachThread);

		FileLog("LiveChatClientJni", "OnGetUserInfo() callback, inUserId:%s", inUserId.c_str());
		jobject jItem = GetTalkUserListItem(env, item);

		jclass jCallbackCls = env->GetObjectClass(gListener);
		string signure = "(ILjava/lang/String;Ljava/lang/String;";
		signure += "L";
		signure += LIVECHAT_TALKUSERLISTTIME_CLASS;
		signure += ";";
		signure += ")V";
		jmethodID jCallback = env->GetMethodID(jCallbackCls, "OnGetUserInfo", signure.c_str());
		if (NULL != gListener && NULL != jCallback)
		{
			FileLog("LiveChatClientJni", "OnGetUserInfo() callback now");

			jstring jInUserId = env->NewStringUTF(inUserId.c_str());
			int errType = LccErrTypeToInt(err);
			jstring jerrmsg = env->NewStringUTF(errmsg.c_str());

			env->CallVoidMethod(gListener, jCallback, errType, jerrmsg, jInUserId, jItem);

			env->DeleteLocalRef(jInUserId);
			env->DeleteLocalRef(jerrmsg);

			FileLog("LiveChatClientJni", "OnGetUserInfo() callback ok");
		}

		if (NULL != jItem) {
			env->DeleteLocalRef(jItem);
		}

		ReleaseEnv(isAttachThread);
	}
	virtual void OnGetUsersInfo(LCC_ERR_TYPE err, const string& errmsg, int seq, const UserInfoList& userList)
	{
		JNIEnv* env = NULL;
		bool isAttachThread = false;
		GetEnv(&env, &isAttachThread);

		FileLog("LiveChatClientJni", "OnGetUsersInfo() callback, env:%p, isAttachThread:%d", env, isAttachThread);
		FileLog("LiveChatClientJni", "OnGetUsersInfo() err:%d, errmsg:%s, userList.size:%d", err, errmsg.c_str(), userList.size());

		jclass jCallbackCls = env->GetObjectClass(gListener);

		jobjectArray jArray = GetTalkUserList(env, userList);

		string signure = "(ILjava/lang/String;I";
		signure += "[L";
		signure += LIVECHAT_TALKUSERLISTTIME_CLASS;
		signure += ";";
		signure += ")V";
		jmethodID jCallback = env->GetMethodID(jCallbackCls, "OnGetUsersInfo", signure.c_str());

		if (NULL != gListener && NULL != jCallback)
		{
			FileLog("LiveChatClientJni", "OnGetUsersInfo() callback now");

			int errType = LccErrTypeToInt(err);
			jstring jerrmsg = env->NewStringUTF(errmsg.c_str());
			env->CallVoidMethod(gListener, jCallback, errType, jerrmsg, seq, jArray);
			env->DeleteLocalRef(jerrmsg);

			FileLog("LiveChatClientJni", "OnGetUsersInfo() callback ok");
		}

		if (NULL != jArray) {
			env->DeleteLocalRef(jArray);
		}

		ReleaseEnv(isAttachThread);
	}
	// 获取联系人/黑名单列表
	virtual void OnGetContactList(CONTACT_LIST_TYPE inListType, LCC_ERR_TYPE err, const string& errmsg, const TalkUserList& list)
	{
		JNIEnv* env = NULL;
		bool isAttachThread = false;
		GetEnv(&env, &isAttachThread);

		FileLog("LiveChatClientJni", "OnGetContactList() err:%d, errmsg:%s, env:%p, isAttachThread:%d", err, errmsg.c_str(), env, isAttachThread);

		jobjectArray jListArray = GetTalkUserList(env, list);

		jclass jCallbackCls = env->GetObjectClass(gListener);
		string signure = "(ILjava/lang/String;";
		signure += "[L";
		signure += LIVECHAT_TALKUSERLISTTIME_CLASS;
		signure += ";";
		signure += ")V";

		string strMethod("");
		if (inListType == CONTACT_LIST_BLOCK) {
			strMethod = "OnGetBlockList";
			jmethodID jCallback = env->GetMethodID(jCallbackCls, strMethod.c_str(), signure.c_str());
			if (NULL != gListener && NULL != jCallback)
			{
				FileLog("LiveChatClientJni", "OnGetContactList() callback now");

				int errType = LccErrTypeToInt(err);
				jstring jerrmsg = env->NewStringUTF(errmsg.c_str());

				env->CallVoidMethod(gListener, jCallback, errType, jerrmsg, jListArray);

				env->DeleteLocalRef(jerrmsg);

				FileLog("LiveChatClientJni", "OnGetContactList() callback ok");
			}
		}

		if (NULL != jListArray) {
			env->DeleteLocalRef(jListArray);
		}

		ReleaseEnv(isAttachThread);
	}
	// 获取被屏蔽女士列表
	virtual void OnGetBlockUsers(LCC_ERR_TYPE err, const string& errmsg, const list<string>& users)
	{
		JNIEnv* env = NULL;
		bool isAttachThread = false;
		GetEnv(&env, &isAttachThread);

		FileLog("LiveChatClientJni", "OnGetBlockUsers() err:%d, errmsg:%s, env:%p, isAttachThread:%d", err, errmsg.c_str(), env, isAttachThread);

		jclass jStringCls = env->FindClass("java/lang/String");
		jobjectArray jArray = env->NewObjectArray(users.size(), jStringCls, NULL);
		if (NULL != jArray) {
			int i = 0;
			for (list<string>::const_iterator itr = users.begin()
				; itr != users.end()
				; itr++, i++)
			{
				jstring userId = env->NewStringUTF((*itr).c_str());
				env->SetObjectArrayElement(jArray, i, userId);
				env->DeleteLocalRef(userId);
			}
		}

		jclass jCallbackCls = env->GetObjectClass(gListener);
		string signure = "(ILjava/lang/String;[Ljava/lang/String;)V";

		jmethodID jCallback = env->GetMethodID(jCallbackCls, "OnGetBlockUsers", signure.c_str());
		if (NULL != gListener && NULL != jCallback)
		{
			FileLog("LiveChatClientJni", "OnGetBlockUsers() callback now");

			int errType = LccErrTypeToInt(err);
			jstring jerrmsg = env->NewStringUTF(errmsg.c_str());

			env->CallVoidMethod(gListener, jCallback, errType, jerrmsg, jArray);

			env->DeleteLocalRef(jerrmsg);

			FileLog("LiveChatClientJni", "OnGetBlockUsers() callback ok");
		}

		if (NULL != jArray) {
			env->DeleteLocalRef(jArray);
		}

		ReleaseEnv(isAttachThread);
	}
	virtual void OnSearchOnlineMan(LCC_ERR_TYPE err, const string& errmsg, const list<string>& userList)
	{
		JNIEnv* env = NULL;
		bool isAttachThread = false;
		GetEnv(&env, &isAttachThread);

		FileLog("LiveChatClientJni", "OnSearchOnlineMan() callback, env:%p, isAttachThread:%d", env, isAttachThread);
		FileLog("LiveChatClientJni", "OnSearchOnlineMan() err:%d, errmsg:%s, userList.size:%d", err, errmsg.c_str(), userList.size());

		jclass jCallbackCls = env->GetObjectClass(gListener);

		// userList
		jclass jStringCls = env->FindClass("java/lang/String");
		jobjectArray jArray = env->NewObjectArray(userList.size(), jStringCls, NULL);
		if (NULL != jArray) {
			int i = 0;
			for (list<string>::const_iterator itr = userList.begin()
				; itr != userList.end()
				; itr++, i++)
			{
				jstring userId = env->NewStringUTF((*itr).c_str());
				env->SetObjectArrayElement(jArray, i, userId);
				env->DeleteLocalRef(userId);
			}
		}

		string signure = "(ILjava/lang/String;[Ljava/lang/String;)V";
		jmethodID jCallback = env->GetMethodID(jCallbackCls, "OnSearchOnlineMan", signure.c_str());
		if (NULL != gListener && NULL != jCallback)
		{
			FileLog("LiveChatClientJni", "OnSearchOnlineMan() callback now");

			int errType = LccErrTypeToInt(err);
			jstring jerrmsg = env->NewStringUTF(errmsg.c_str());
			env->CallVoidMethod(gListener, jCallback, errType, jerrmsg, jArray);
			env->DeleteLocalRef(jerrmsg);

			FileLog("LiveChatClientJni", "OnSearchOnlineMan() callback ok");
		}

		if (NULL != jArray) {
			env->DeleteLocalRef(jArray);
		}

		ReleaseEnv(isAttachThread);
	}
	virtual void OnReplyIdentifyCode(LCC_ERR_TYPE err, const string& errmsg)
	{
		JNIEnv* env = NULL;
		bool isAttachThread = false;
		GetEnv(&env, &isAttachThread);

		FileLog("LiveChatClientJni", "OnReplyIdentifyCode() callback, env:%p, isAttachThread:%d", env, isAttachThread);

		jclass jCallbackCls = env->GetObjectClass(gListener);

		string signure = "(ILjava/lang/String;)V";
		jmethodID jCallback = env->GetMethodID(jCallbackCls, "OnReplyIdentifyCode", signure.c_str());
		if (NULL != gListener && NULL != jCallback)
		{
			FileLog("LiveChatClientJni", "OnReplyIdentifyCode() callback now");

			int errType = LccErrTypeToInt(err);
			jstring jerrmsg = env->NewStringUTF(errmsg.c_str());
			env->CallVoidMethod(gListener, jCallback, errType, jerrmsg);
			env->DeleteLocalRef(jerrmsg);

			FileLog("LiveChatClientJni", "OnReplyIdentifyCode() callback ok");
		}

		ReleaseEnv(isAttachThread);
	}
	virtual void OnGetRecentContactList(LCC_ERR_TYPE err, const string& errmsg, const list<string>& userList)
	{
		// 女士端没有用
	}
	virtual void OnGetFeeRecentContactList(LCC_ERR_TYPE err, const string& errmsg, const list<string>& userList)
	{
		JNIEnv* env = NULL;
		bool isAttachThread = false;
		GetEnv(&env, &isAttachThread);

		FileLog("LiveChatClientJni", "OnGetFeeRecentContactList() callback, env:%p, isAttachThread:%d", env, isAttachThread);

		jclass jCallbackCls = env->GetObjectClass(gListener);

		// userList
		jclass jStringCls = env->FindClass("java/lang/String");
		jobjectArray jArray = env->NewObjectArray(userList.size(), jStringCls, NULL);
		if (NULL != jArray) {
			int i = 0;
			for (list<string>::const_iterator itr = userList.begin()
				; itr != userList.end()
				; itr++, i++)
			{
				jstring userId = env->NewStringUTF((*itr).c_str());
				env->SetObjectArrayElement(jArray, i, userId);
				env->DeleteLocalRef(userId);
			}
		}

		string signure = "(ILjava/lang/String;[Ljava/lang/String;)V";
		jmethodID jCallback = env->GetMethodID(jCallbackCls, "OnGetFeeRecentContactList", signure.c_str());
		if (NULL != gListener && NULL != jCallback)
		{
			FileLog("LiveChatClientJni", "OnGetFeeRecentContactList() callback now");

			int errType = LccErrTypeToInt(err);
			jstring jerrmsg = env->NewStringUTF(errmsg.c_str());
			env->CallVoidMethod(gListener, jCallback, errType, jerrmsg, jArray);
			env->DeleteLocalRef(jerrmsg);

			FileLog("LiveChatClientJni", "OnGetFeeRecentContactList() callback ok");
		}

		if (NULL != jArray) {
			env->DeleteLocalRef(jArray);
		}

		ReleaseEnv(isAttachThread);
	}
	virtual void OnGetLadyChatInfo(LCC_ERR_TYPE err, const string& errmsg, const list<string>& chattingList, const list<string>& chattingInviteIdList, const list<string>& missingList, const list<string>& missingInviteIdList)
	{
		JNIEnv* env = NULL;
		bool isAttachThread = false;
		GetEnv(&env, &isAttachThread);

		FileLog("LiveChatClientJni", "OnGetLadyChatInfo() callback, env:%p, isAttachThread:%d", env, isAttachThread);

		jclass jCallbackCls = env->GetObjectClass(gListener);
		jclass jStringCls = env->FindClass("java/lang/String");

		// chattingList
		jobjectArray jChattingArray = env->NewObjectArray(chattingList.size(), jStringCls, NULL);
		if (NULL != jChattingArray) {
			int i = 0;
			for (list<string>::const_iterator itr = chattingList.begin()
				; itr != chattingList.end()
				; itr++, i++)
			{
				jstring userId = env->NewStringUTF((*itr).c_str());
				env->SetObjectArrayElement(jChattingArray, i, userId);
				env->DeleteLocalRef(userId);
			}
		}

		// chattingInviteIdList
		jobjectArray jChattingInviteIdArray = env->NewObjectArray(chattingInviteIdList.size(), jStringCls, NULL);
		if (NULL != jChattingInviteIdArray) {
			int i = 0;
			for (list<string>::const_iterator itr = chattingInviteIdList.begin()
				; itr != chattingInviteIdList.end()
				; itr++, i++)
			{
				jstring inviteId = env->NewStringUTF((*itr).c_str());
				env->SetObjectArrayElement(jChattingInviteIdArray, i, inviteId);
				env->DeleteLocalRef(inviteId);
			}
		}

		// missingList
		jobjectArray jMissingArray = env->NewObjectArray(missingList.size(), jStringCls, NULL);
		if (NULL != jMissingArray) {
			int i = 0;
			for (list<string>::const_iterator itr = missingList.begin()
				; itr != missingList.end()
				; itr++, i++)
			{
				jstring userId = env->NewStringUTF((*itr).c_str());
				env->SetObjectArrayElement(jMissingArray, i, userId);
				env->DeleteLocalRef(userId);
			}
		}

		// missingInviteIdList
		jobjectArray jMissingInviteIdArray = env->NewObjectArray(missingInviteIdList.size(), jStringCls, NULL);
		if (NULL != jMissingInviteIdArray) {
			int i = 0;
			for (list<string>::const_iterator itr = missingInviteIdList.begin()
				; itr != missingInviteIdList.end()
				; itr++, i++)
			{
				jstring inviteId = env->NewStringUTF((*itr).c_str());
				env->SetObjectArrayElement(jMissingInviteIdArray, i, inviteId);
				env->DeleteLocalRef(inviteId);
			}
		}

		string signure = "(ILjava/lang/String;[Ljava/lang/String;[Ljava/lang/String;[Ljava/lang/String;[Ljava/lang/String;)V";
		jmethodID jCallback = env->GetMethodID(jCallbackCls, "OnGetLadyChatInfo", signure.c_str());
		if (NULL != gListener && NULL != jCallback)
		{
			FileLog("LiveChatClientJni", "OnGetLadyChatInfo() callback now");

			int errType = LccErrTypeToInt(err);
			jstring jerrmsg = env->NewStringUTF(errmsg.c_str());
			env->CallVoidMethod(gListener, jCallback, errType, jerrmsg
					, jChattingArray, jChattingInviteIdArray, jMissingArray, jMissingInviteIdArray);
			env->DeleteLocalRef(jerrmsg);

			FileLog("LiveChatClientJni", "OnGetLadyChatInfo() callback ok");
		}

		if (NULL != jChattingArray) {
			env->DeleteLocalRef(jChattingArray);
		}

		if (NULL != jChattingInviteIdArray) {
			env->DeleteLocalRef(jChattingInviteIdArray);
		}

		if (NULL != jMissingArray) {
			env->DeleteLocalRef(jMissingArray);
		}

		if (NULL != jMissingInviteIdArray) {
			env->DeleteLocalRef(jMissingInviteIdArray);
		}

		ReleaseEnv(isAttachThread);
	}
	// 服务器主动请求
	virtual void OnRecvMessage(const string& toId, const string& fromId, const string& fromName, const string& inviteId, bool charge, int ticket, TALK_MSG_TYPE msgType, const string& message) {
		JNIEnv* env = NULL;
		bool isAttachThread = false;
		GetEnv(&env, &isAttachThread);

		FileLog("LiveChatClientJni", "OnRecvMessage() callback, toId:%s, fromId:%s, fromName:%s"
				", inviteId:%s, charge:%d, ticket:%d, msgType:%d, message:%s, env:%p, isAttachThread:%d"
				, toId.c_str(), fromId.c_str(), fromName.c_str()
				, inviteId.c_str(), charge, ticket, msgType, message.c_str(), env, isAttachThread);

		jclass jCallbackCls = env->GetObjectClass(gListener);
		string signure = "(Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;ZIILjava/lang/String;)V";
		jmethodID jCallback = env->GetMethodID(jCallbackCls, "OnRecvMessage", signure.c_str());
		if (NULL != gListener && NULL != jCallback)
		{
			FileLog("LiveChatClientJni", "OnRecvMessage() callback now");

			jstring jtoId = env->NewStringUTF(toId.c_str());
			jstring jfromId = env->NewStringUTF(fromId.c_str());
			jstring jfromName = env->NewStringUTF(fromName.c_str());
			jstring jinviteId = env->NewStringUTF(inviteId.c_str());
			int iMsgType = TalkMsgTypeToInt(msgType);
			jstring jmessage = env->NewStringUTF(message.c_str());

			env->CallVoidMethod(gListener, jCallback, jtoId, jfromId, jfromName, jinviteId, charge, ticket, iMsgType, jmessage);

			env->DeleteLocalRef(jtoId);
			env->DeleteLocalRef(jfromId);
			env->DeleteLocalRef(jfromName);
			env->DeleteLocalRef(jinviteId);
			env->DeleteLocalRef(jmessage);

			FileLog("LiveChatClientJni", "OnRecvMessage() callback ok");
		}

		ReleaseEnv(isAttachThread);
	}
	virtual void OnRecvEmotion(const string& toId, const string& fromId, const string& fromName, const string& inviteId, bool charge, int ticket, TALK_MSG_TYPE msgType, const string& emotionId) {
		JNIEnv* env = NULL;
		bool isAttachThread = false;
		GetEnv(&env, &isAttachThread);

		FileLog("LiveChatClientJni", "OnRecvEmotion() callback, toId:%s, fromId:%s, fromName:%s, inviteId:%s"
					", charge:%d, ticket:%d, msgType:%d, emotionId:%s, env:%p, isAttachThread:%d"
					, toId.c_str(), fromId.c_str(), fromName.c_str(), inviteId.c_str()
					, charge, ticket, msgType, emotionId.c_str(), env, isAttachThread);

		jclass jCallbackCls = env->GetObjectClass(gListener);
		string signure = "(Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;ZIILjava/lang/String;)V";
		jmethodID jCallback = env->GetMethodID(jCallbackCls, "OnRecvEmotion", signure.c_str());
		if (NULL != gListener && NULL != jCallback)
		{
			FileLog("LiveChatClientJni", "OnRecvEmotion() callback now");

			jstring jtoId = env->NewStringUTF(toId.c_str());
			jstring jfromId = env->NewStringUTF(fromId.c_str());
			jstring jfromName = env->NewStringUTF(fromName.c_str());
			jstring jinviteId = env->NewStringUTF(inviteId.c_str());
			int iMsgType = TalkMsgTypeToInt(msgType);
			jstring jemotionId = env->NewStringUTF(emotionId.c_str());

			env->CallVoidMethod(gListener, jCallback, jtoId, jfromId, jfromName, jinviteId, charge, ticket, iMsgType, jemotionId);

			env->DeleteLocalRef(jtoId);
			env->DeleteLocalRef(jfromId);
			env->DeleteLocalRef(jfromName);
			env->DeleteLocalRef(jinviteId);
			env->DeleteLocalRef(jemotionId);

			FileLog("LiveChatClientJni", "OnRecvEmotion() callback ok");
		}

		ReleaseEnv(isAttachThread);
	}
	virtual void OnRecvVoice(const string& toId, const string& fromId, const string& fromName, const string& inviteId, bool charge, TALK_MSG_TYPE msgType, const string& voiceId, const string& fileType, int timeLen) {
		JNIEnv* env = NULL;
		bool isAttachThread = false;
		GetEnv(&env, &isAttachThread);

		FileLog("LiveChatClientJni", "OnRecvVoice() callback, toId:%s, fromId:%s, fromName:%s"
				", inviteId:%s, charge:%d, msgType:%d, voiceId:%s, env:%p, isAttachThread:%d"
				, toId.c_str(), fromId.c_str(), fromName.c_str()
				, inviteId.c_str(), charge, msgType, voiceId.c_str(), env, isAttachThread);

		jclass jCallbackCls = env->GetObjectClass(gListener);
		string signure = "(Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;ZILjava/lang/String;Ljava/lang/String;I)V";
		jmethodID jCallback = env->GetMethodID(jCallbackCls, "OnRecvVoice", signure.c_str());
		if (NULL != gListener && NULL != jCallback)
		{
			FileLog("LiveChatClientJni", "OnRecvVoice() callback now");

			jstring jtoId = env->NewStringUTF(toId.c_str());
			jstring jfromId = env->NewStringUTF(fromId.c_str());
			jstring jfromName = env->NewStringUTF(fromName.c_str());
			jstring jinviteId = env->NewStringUTF(inviteId.c_str());
			int iMsgType = TalkMsgTypeToInt(msgType);
			jstring jvoiceId = env->NewStringUTF(voiceId.c_str());
			jstring jfileType = env->NewStringUTF(fileType.c_str());

			env->CallVoidMethod(gListener, jCallback, jtoId, jfromId, jfromName, jinviteId, charge, iMsgType, jvoiceId, jfileType, timeLen);

			env->DeleteLocalRef(jtoId);
			env->DeleteLocalRef(jfromId);
			env->DeleteLocalRef(jfromName);
			env->DeleteLocalRef(jinviteId);
			env->DeleteLocalRef(jvoiceId);
			env->DeleteLocalRef(jfileType);

			FileLog("LiveChatClientJni", "OnRecvVoice() callback ok");
		}

		ReleaseEnv(isAttachThread);
	}
	virtual void OnRecvWarning(const string& toId, const string& fromId, const string& fromName, const string& inviteId, bool charge, int ticket, TALK_MSG_TYPE msgType, const string& message) {
		JNIEnv* env = NULL;
		bool isAttachThread = false;
		GetEnv(&env, &isAttachThread);

		FileLog("LiveChatClientJni", "OnRecvWarning() callback, toId:%s, fromId:%s, fromName:%s"
				", inviteId:%s, charge:%d, ticket:%d, msgType:%d, message:%s, env:%p, isAttachThread:%d"
				, toId.c_str(), fromId.c_str(), fromName.c_str()
				, inviteId.c_str(), charge, ticket, msgType, message.c_str(), env, isAttachThread);

		jclass jCallbackCls = env->GetObjectClass(gListener);
		string signure = "(Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;ZIILjava/lang/String;)V";
		jmethodID jCallback = env->GetMethodID(jCallbackCls, "OnRecvWarning", signure.c_str());
		if (NULL != gListener && NULL != jCallback)
		{
			FileLog("LiveChatClientJni", "OnRecvWarning() callback now");

			jstring jtoId = env->NewStringUTF(toId.c_str());
			jstring jfromId = env->NewStringUTF(fromId.c_str());
			jstring jfromName = env->NewStringUTF(fromName.c_str());
			jstring jinviteId = env->NewStringUTF(inviteId.c_str());
			int iMsgType = TalkMsgTypeToInt(msgType);
			jstring jmessage = env->NewStringUTF(message.c_str());

			env->CallVoidMethod(gListener, jCallback, jtoId, jfromId, jfromName, jinviteId, charge, ticket, iMsgType, jmessage);

			env->DeleteLocalRef(jtoId);
			env->DeleteLocalRef(jfromId);
			env->DeleteLocalRef(jfromName);
			env->DeleteLocalRef(jinviteId);
			env->DeleteLocalRef(jmessage);

			FileLog("LiveChatClientJni", "OnRecvWarning() callback ok");
		}

		ReleaseEnv(isAttachThread);
	}
	virtual void OnUpdateStatus(const string& userId, const string& server, CLIENT_TYPE clientType, USER_STATUS_TYPE statusType) {
		JNIEnv* env = NULL;
		bool isAttachThread = false;
		GetEnv(&env, &isAttachThread);

		FileLog("LiveChatClientJni", "OnUpdateStatus() callback, env:%p, isAttachThread:%d", env, isAttachThread);

		jclass jCallbackCls = env->GetObjectClass(gListener);
		string signure = "(Ljava/lang/String;Ljava/lang/String;II)V";
		jmethodID jCallback = env->GetMethodID(jCallbackCls, "OnUpdateStatus", signure.c_str());
		if (NULL != gListener && NULL != jCallback)
		{
			FileLog("LiveChatClientJni", "OnUpdateStatus() callback now");

			jstring juserId = env->NewStringUTF(userId.c_str());
			jstring jserver = env->NewStringUTF(server.c_str());
			int iClientType = ClientTypeToInt(clientType);
			int iStatusType = UserStatusTypeToInt(statusType);

			env->CallVoidMethod(gListener, jCallback, juserId, jserver, iClientType, iStatusType);

			env->DeleteLocalRef(juserId);
			env->DeleteLocalRef(jserver);

			FileLog("LiveChatClientJni", "OnUpdateStatus() callback ok");
		}

		ReleaseEnv(isAttachThread);
	}
	virtual void OnUpdateTicket(const string& fromId, int ticket) {
		JNIEnv* env = NULL;
		bool isAttachThread = false;
		GetEnv(&env, &isAttachThread);

		FileLog("LiveChatClientJni", "OnUpdateTicket() callback, env:%p, isAttachThread:%d", env, isAttachThread);

		jclass jCallbackCls = env->GetObjectClass(gListener);
		string signure = "(Ljava/lang/String;I)V";
		jmethodID jCallback = env->GetMethodID(jCallbackCls, "OnUpdateTicket", signure.c_str());
		if (NULL != gListener && NULL != jCallback)
		{
			FileLog("LiveChatClientJni", "OnUpdateTicket() callback now");

			jstring jfromId = env->NewStringUTF(fromId.c_str());

			env->CallVoidMethod(gListener, jCallback, jfromId, ticket);

			env->DeleteLocalRef(jfromId);

			FileLog("LiveChatClientJni", "OnUpdateTicket() callback ok");
		}

		ReleaseEnv(isAttachThread);
	}
	virtual void OnRecvEditMsg(const string& fromId) {
		JNIEnv* env = NULL;
		bool isAttachThread = false;
		GetEnv(&env, &isAttachThread);

		FileLog("LiveChatClientJni", "OnRecvEditMsg() callback, env:%p, isAttachThread:%d", env, isAttachThread);

		jclass jCallbackCls = env->GetObjectClass(gListener);
		string signure = "(Ljava/lang/String;)V";
		jmethodID jCallback = env->GetMethodID(jCallbackCls, "OnRecvEditMsg", signure.c_str());
		if (NULL != gListener && NULL != jCallback)
		{
			FileLog("LiveChatClientJni", "OnRecvEditMsg() callback now");

			jstring jfromId = env->NewStringUTF(fromId.c_str());

			env->CallVoidMethod(gListener, jCallback, jfromId);

			env->DeleteLocalRef(jfromId);

			FileLog("LiveChatClientJni", "OnRecvEditMsg() callback ok");
		}

		ReleaseEnv(isAttachThread);
	}
	virtual void OnRecvTalkEvent(const string& userId, TALK_EVENT_TYPE eventType) {
		JNIEnv* env = NULL;
		bool isAttachThread = false;
		GetEnv(&env, &isAttachThread);

		FileLog("LiveChatClientJni", "OnRecvTalkEvent() callback, env%p, isAttachThread:%d", env, isAttachThread);

		jclass jCallbackCls = env->GetObjectClass(gListener);
		string signure = "(Ljava/lang/String;I)V";
		jmethodID jCallback = env->GetMethodID(jCallbackCls, "OnRecvTalkEvent", signure.c_str());
		if (NULL != gListener && NULL != jCallback)
		{
			FileLog("LiveChatClientJni", "OnRecvTalkEvent() callback now");

			jstring juserId = env->NewStringUTF(userId.c_str());
			int iEventType = TalkEventTypeToInt(eventType);

			env->CallVoidMethod(gListener, jCallback, juserId, iEventType);

			env->DeleteLocalRef(juserId);

			FileLog("LiveChatClientJni", "OnRecvTalkEvent() callback ok");
		}

		ReleaseEnv(isAttachThread);
	}
	virtual void OnRecvTryTalkBegin(const string& toId, const string& fromId, int time) {
		// 女士端没有用
	}
	virtual void OnRecvTryTalkEnd(const string& userId) {
		// 女士端没有用
	}
	virtual void OnRecvEMFNotice(const string& fromId, TALK_EMF_NOTICE_TYPE noticeType) {
		JNIEnv* env = NULL;
		bool isAttachThread = false;
		GetEnv(&env, &isAttachThread);

		FileLog("LiveChatClientJni", "OnRecvEMFNotice() callback, env:%p, isAttachThread:%d", env, isAttachThread);

		jclass jCallbackCls = env->GetObjectClass(gListener);
		string signure = "(Ljava/lang/String;I)V";
		jmethodID jCallback = env->GetMethodID(jCallbackCls, "OnRecvEMFNotice", signure.c_str());
		if (NULL != gListener && NULL != jCallback)
		{
			FileLog("LiveChatClientJni", "OnRecvEMFNotice() callback now");

			jstring jfromId = env->NewStringUTF(fromId.c_str());
			int iNoticeType = TalkEmfNoticeTypeToInt(noticeType);

			env->CallVoidMethod(gListener, jCallback, jfromId, iNoticeType);

			env->DeleteLocalRef(jfromId);

			FileLog("LiveChatClientJni", "OnRecvEMFNotice() callback ok");
		}

		ReleaseEnv(isAttachThread);
	}
	virtual void OnRecvKickOffline(KICK_OFFLINE_TYPE kickType) {
		JNIEnv* env = NULL;
		bool isAttachThread = false;
		GetEnv(&env, &isAttachThread);

		FileLog("LiveChatClientJni", "OnRecvKickOffline() callback, env:%p, isAttachThread:%d", env, isAttachThread);

		jclass jCallbackCls = env->GetObjectClass(gListener);
		string signure = "(I)V";
		jmethodID jCallback = env->GetMethodID(jCallbackCls, "OnRecvKickOffline", signure.c_str());
		if (NULL != gListener && NULL != jCallback)
		{
			FileLog("LiveChatClientJni", "OnRecvKickOffline() callback now");

			int iKickType = KickOfflineTypeToInt(kickType);
			env->CallVoidMethod(gListener, jCallback, iKickType);

			FileLog("LiveChatClientJni", "OnRecvKickOffline() callback ok");
		}

		ReleaseEnv(isAttachThread);
	}
	virtual void OnRecvPhoto(const string& toId, const string& fromId, const string& fromName, const string& inviteId, const string& photoId, const string& sendId, bool charge, const string& photoDesc, int ticket) 
	{
		JNIEnv* env = NULL;
		bool isAttachThread = false;
		GetEnv(&env, &isAttachThread);

		FileLog("LiveChatClientJni", "OnRecvPhoto() callback, env:%p, isAttachThread:%d", env, isAttachThread);

		jclass jCallbackCls = env->GetObjectClass(gListener);
		string signure = "(Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;ZLjava/lang/String;I)V";
		jmethodID jCallback = env->GetMethodID(jCallbackCls, "OnRecvPhoto", signure.c_str());
		if (NULL != gListener && NULL != jCallback)
		{
			FileLog("LiveChatClientJni", "OnRecvPhoto() callback now");

			jstring jtoId = env->NewStringUTF(toId.c_str());
			jstring jfromId = env->NewStringUTF(fromId.c_str());
			jstring jfromName = env->NewStringUTF(fromName.c_str());
			jstring jinviteId = env->NewStringUTF(inviteId.c_str());
			jstring jphotoId = env->NewStringUTF(photoId.c_str());
			jstring jsendId = env->NewStringUTF(sendId.c_str());
			jstring jphotoDesc = env->NewStringUTF(photoDesc.c_str());

			env->CallVoidMethod(gListener, jCallback, jtoId, jfromId, jfromName, jinviteId, jphotoId, jsendId, charge, jphotoDesc, ticket);

			env->DeleteLocalRef(jtoId);
			env->DeleteLocalRef(jfromId);
			env->DeleteLocalRef(jfromName);
			env->DeleteLocalRef(jinviteId);
			env->DeleteLocalRef(jphotoId);
			env->DeleteLocalRef(jsendId);
			env->DeleteLocalRef(jphotoDesc);

			FileLog("LiveChatClientJni", "OnRecvPhoto() callback ok");
		}

		ReleaseEnv(isAttachThread);
	}
	virtual void OnRecvShowPhoto(const string& toId, const string& fromId, const string& fromName, const string& inviteId, const string& photoId, const string& sendId, bool charge, const string& photoDesc, int ticket) 
	{
		JNIEnv* env = NULL;
		bool isAttachThread = false;
		GetEnv(&env, &isAttachThread);

		FileLog("LiveChatClientJni", "OnRecvShowPhoto() callback, env:%p, isAttachThread:%d", env, isAttachThread);

		jclass jCallbackCls = env->GetObjectClass(gListener);
		string signure = "(Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;ZLjava/lang/String;I)V";
		jmethodID jCallback = env->GetMethodID(jCallbackCls, "OnRecvShowPhoto", signure.c_str());
		if (NULL != gListener && NULL != jCallback)
		{
			FileLog("LiveChatClientJni", "OnRecvShowPhoto() callback now");

			jstring jtoId = env->NewStringUTF(toId.c_str());
			jstring jfromId = env->NewStringUTF(fromId.c_str());
			jstring jfromName = env->NewStringUTF(fromName.c_str());
			jstring jinviteId = env->NewStringUTF(inviteId.c_str());
			jstring jphotoId = env->NewStringUTF(photoId.c_str());
			jstring jsendId = env->NewStringUTF(sendId.c_str());
			jstring jphotoDesc = env->NewStringUTF(photoDesc.c_str());

			env->CallVoidMethod(gListener, jCallback, jtoId, jfromId, jfromName, jinviteId, jphotoId, jsendId, charge, jphotoDesc, ticket);

			env->DeleteLocalRef(jtoId);
			env->DeleteLocalRef(jfromId);
			env->DeleteLocalRef(jfromName);
			env->DeleteLocalRef(jinviteId);
			env->DeleteLocalRef(jphotoId);
			env->DeleteLocalRef(jsendId);
			env->DeleteLocalRef(jphotoDesc);

			FileLog("LiveChatClientJni", "OnRecvShowPhoto() callback ok");
		}

		ReleaseEnv(isAttachThread);
	}
	virtual void OnRecvVideo(const string& toId, const string& fromId, const string& fromName, const string& inviteId, const string& videoId, const string& sendId, bool charge, const string& videoDesc, int ticket)
	{
		
	}
	virtual void OnRecvShowVideo(const string& toId, const string& fromId, const string& fromName, const string& inviteId, const string& videoId, const string& sendId, bool charge, const string& videoDesc, int ticket)
	{
		JNIEnv* env = NULL;
		bool isAttachThread = false;
		GetEnv(&env, &isAttachThread);

		FileLog("LiveChatClientJni", "OnRecvShowVideo() callback, env:%p, isAttachThread:%d", env, isAttachThread);

		jclass jCallbackCls = env->GetObjectClass(gListener);
		string signure = "(Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;ZLjava/lang/String;I)V";
		jmethodID jCallback = env->GetMethodID(jCallbackCls, "OnRecvShowVideo", signure.c_str());
		if (NULL != gListener && NULL != jCallback)
		{
			FileLog("LiveChatClientJni", "OnRecvShowVideo() callback now");

			jstring jtoId = env->NewStringUTF(toId.c_str());
			jstring jfromId = env->NewStringUTF(fromId.c_str());
			jstring jfromName = env->NewStringUTF(fromName.c_str());
			jstring jinviteId = env->NewStringUTF(inviteId.c_str());
			jstring jvideoId = env->NewStringUTF(videoId.c_str());
			jstring jsendId = env->NewStringUTF(sendId.c_str());
			jstring jvideoDesc = env->NewStringUTF(videoDesc.c_str());

			env->CallVoidMethod(gListener, jCallback, jtoId, jfromId, jfromName, jinviteId, jvideoId, jsendId, charge, jvideoDesc, ticket);

			env->DeleteLocalRef(jtoId);
			env->DeleteLocalRef(jfromId);
			env->DeleteLocalRef(jfromName);
			env->DeleteLocalRef(jinviteId);
			env->DeleteLocalRef(jvideoId);
			env->DeleteLocalRef(jsendId);
			env->DeleteLocalRef(jvideoDesc);

			FileLog("LiveChatClientJni", "OnRecvShowVideo() callback ok");
		}

		ReleaseEnv(isAttachThread);
	}
	virtual void OnRecvLadyVoiceCode(const string& voiceCode)
	{
		JNIEnv* env = NULL;
		bool isAttachThread = false;
		GetEnv(&env, &isAttachThread);

		FileLog("LiveChatClientJni", "OnRecvLadyVoiceCode() callback, env:%p, isAttachThread:%d", env, isAttachThread);

		jclass jCallbackCls = env->GetObjectClass(gListener);

		string signure = "(Ljava/lang/String;)V";
		jmethodID jCallback = env->GetMethodID(jCallbackCls, "OnRecvLadyVoiceCode", signure.c_str());
		if (NULL != gListener && NULL != jCallback)
		{
			FileLog("LiveChatClientJni", "OnRecvLadyVoiceCode() callback now");

			jstring jvoiceCode = env->NewStringUTF(voiceCode.c_str());
			env->CallVoidMethod(gListener, jCallback, jvoiceCode);
			env->DeleteLocalRef(jvoiceCode);

			FileLog("LiveChatClientJni", "OnRecvLadyVoiceCode() callback ok");
		}

		ReleaseEnv(isAttachThread);
	}

	virtual void OnRecvIdentifyCode(const unsigned char* data, long dataLen)
	{
		JNIEnv* env = NULL;
		bool isAttachThread = false;
		GetEnv(&env, &isAttachThread);

		FileLog("LiveChatClientJni", "OnRecvIdentifyCode() callback, env:%p, isAttachThread:%d", env, isAttachThread);

		jclass jCallbackCls = env->GetObjectClass(gListener);

		jbyteArray dataArray = env->NewByteArray(dataLen);
		if (NULL != dataArray) {
			env->SetByteArrayRegion(dataArray, 0, dataLen, (const jbyte*)data);
		}

		string signure = "([B)V";
		jmethodID jCallback = env->GetMethodID(jCallbackCls, "OnRecvIdentifyCode", signure.c_str());
		if (NULL != gListener && NULL != jCallback)
		{
			FileLog("LiveChatClientJni", "OnRecvIdentifyCode() callback now");

			env->CallVoidMethod(gListener, jCallback, dataArray);

			FileLog("LiveChatClientJni", "OnRecvIdentifyCode() callback ok");
		}

		if (NULL != dataArray) {
			env->DeleteLocalRef(dataArray);
		}

		ReleaseEnv(isAttachThread);
	}

	virtual void OnRecvAutoInviteMsg(const string& womanId, const string& manId, const string& key) {}

	virtual void OnRecvAutoChargeResult(const string& manId, double money, TAUTO_CHARGE_TYPE type, bool result, const string& code, const string& msg) {}

	virtual void OnRecvMagicIcon(const string& toId, const string& fromId, const string& fromName, const string& inviteId, bool charge, int ticket, TALK_MSG_TYPE msgType, const string& iconId) {}

	virtual void OnGetPaidTheme(const string& inUserId, LCC_ERR_TYPE err, const string& errmsg, const ThemeInfoList& themeList){}

	virtual void OnGetAllPaidTheme(LCC_ERR_TYPE err, const string& errmsg, const ThemeInfoList& themeInfoList){}

	virtual void OnManFeeTheme(const string& inUserId, const string& inThemeId, LCC_ERR_TYPE err, const string& errmsg, const ThemeInfoItem& item){}

	virtual void OnManApplyTheme(const string& inUserId, const string& inThemeId, LCC_ERR_TYPE err, const string& errmsg, const ThemeInfoItem& item){}

	virtual void OnPlayThemeMotion(const string& inUserId, const string& inThemeId, LCC_ERR_TYPE err, const string& errmsg, bool success){}

	virtual void OnRecvThemeMotion(const string& themeId, const string& manId, const string& womanId){}

	virtual void OnRecvThemeRecommend(const string& themeId, const string& manId, const string& womanId){}
};
static LiveChatClientListener g_listener;

// -------------- jni function ----------------
bool GetEnv(JNIEnv** env, bool* isAttachThread)
{
	bool result = false;

	*isAttachThread = false;

	FileLog("LiveChatClientJni", "GetEnv() begin, env:%p", env);
	jint iRet = JNI_ERR;
	iRet = gJavaVM->GetEnv((void**)env, JNI_VERSION_1_4);
	FileLog("LiveChatClientJni", "GetEnv() gJavaVM->GetEnv(&gEnv, JNI_VERSION_1_4), iRet:%d", iRet);
	if( iRet == JNI_EDETACHED ) {
		iRet = gJavaVM->AttachCurrentThread(env, NULL);
		*isAttachThread = (iRet == JNI_OK);
	}

	result = (iRet == JNI_OK);
	FileLog("LiveChatClientJni", "GetEnv() end, env:%p, gIsAttachThread:%d, iRet:%d", *env, *isAttachThread, iRet);

	return result;
}

bool ReleaseEnv(bool isAttachThread)
{
	FileLog("LiveChatClientJni", "ReleaseEnv(bool) begin, isAttachThread:%d", isAttachThread);
	if (isAttachThread) {
		gJavaVM->DetachCurrentThread();
	}
	FileLog("LiveChatClientJni", "ReleaseEnv(bool) end");
	return true;
}

/* JNI_OnLoad */
jint JNI_OnLoad(JavaVM* vm, void* reserved) {
	FileLog("LiveChatClientJni", "JNI_OnLoad( httprequest.so JNI_OnLoad )");
	gJavaVM = vm;

	// Get JNI
	JNIEnv* env;
	if (JNI_OK != vm->GetEnv(reinterpret_cast<void**> (&env),
                           JNI_VERSION_1_4)) {
		FileLog("LiveChatClientJni", "JNI_OnLoad ( could not get JNI env )");
		return -1;
	}

	FileLog("LiveChatClientJni", "JNI_OnLoad()");

	// 回调Object索引表
	InsertJObjectClassToMap(env, LIVECHAT_USERSTATUS_CLASS);
	InsertJObjectClassToMap(env, LIVECHAT_TALKSESSIONLISTTIME_CLASS);
	InsertJObjectClassToMap(env, LIVECHAT_TALKUSERLISTTIME_CLASS);
	InsertJObjectClassToMap(env, LIVECHAT_TALKLISTINFO_CLASS);
	return JNI_VERSION_1_4;
}

/*
 * Class:     com_qpidnetwork_livechat_jni_LiveChatClient
 * Method:    SetLogDirectory
 * Signature: (Ljava/lang/String;)V
 */
JNIEXPORT void JNICALL Java_com_qpidnetwork_livechat_jni_LiveChatClient_SetLogDirectory
  (JNIEnv *env, jclass cls, jstring directory)
{
	string strDirectory = GetJString(env, directory);
	KLog::SetLogDirectory(strDirectory);
}

/*
 * Class:     com_qpidnetwork_livechat_jni_LiveChatClient
 * Method:    Init
 * Signature: (Lcom/qpidnetwork/livechat/jni/LiveChatClientListener;[Ljava/lang/String;I)Z
 */
JNIEXPORT jboolean JNICALL Java_com_qpidnetwork_livechat_jni_LiveChatClient_Init
  (JNIEnv *env, jclass cls, jobject listener, jobjectArray ipArray, jint port)
{
	bool result  = false;

	FileLog("LiveChatClientJni", "Init() listener:%p, ipArray:%p, port:%d", listener, ipArray, port);

	FileLog("LiveChatClientJni", "Init() ILiveChatClient::ReleaseClient(g_liveChatClient) g_liveChatClient:%p", g_liveChatClient);

	// 释放旧的LiveChatClient
	ILiveChatClient::ReleaseClient(g_liveChatClient);
	g_liveChatClient = NULL;

	FileLog("LiveChatClientJni", "Init() env->DeleteGlobalRef(gListener) gListener:%p", gListener);

	// 释放旧的listener
	if (NULL != gListener) {
		env->DeleteGlobalRef(gListener);
		gListener = NULL;
	}

	FileLog("LiveChatClientJni", "Init() get ip array");

	// 获取IP列表
	list<string> svrIPs;
	if (NULL != ipArray) {
		for (int i = 0; i < env->GetArrayLength(ipArray); i++) {
			jstring ip = (jstring)env->GetObjectArrayElement(ipArray, i);
			string strIP = GetJString(env, ip);
			if (!strIP.empty()) {
				svrIPs.push_back(strIP);
			}
		}
	}

	FileLog("LiveChatClientJni", "Init() create client");

	if (NULL != listener
		&& !svrIPs.empty()
		&& port > 0)
	{
		if (NULL == g_liveChatClient) {
			g_liveChatClient = ILiveChatClient::CreateClient();
			FileLog("LiveChatClientJni", "Init() ILiveChatClient::CreateClient() g_liveChatClient:%p", g_liveChatClient);
		}

		if (NULL != g_liveChatClient) {
			gListener = env->NewGlobalRef(listener);
			FileLog("LiveChatClientJni", "Init() gListener:%p", gListener);
			result = g_liveChatClient->Init(svrIPs, (unsigned int)port, &g_listener);
		}
	}

	FileLog("LiveChatClientJni", "Init() result: %d", result);
	return result;
}

/*
 * Class:     com_qpidnetwork_livechat_jni_LiveChatClient
 * Method:    IsInvalidSeq
 * Signature: (I)Z
 */
JNIEXPORT jboolean JNICALL Java_com_qpidnetwork_livechat_jni_LiveChatClient_IsInvalidSeq
  (JNIEnv *env, jclass cls, jint seq)
{
	bool isInvalid = true;
	if (NULL != g_liveChatClient) {
		isInvalid = g_liveChatClient->IsInvalidSeq(seq);
	}
	return isInvalid;
}

/*
 * Class:     com_qpidnetwork_livechat_jni_LiveChatClient
 * Method:    Login
 * Signature: (Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;II)Z
 */
JNIEXPORT jboolean JNICALL Java_com_qpidnetwork_livechat_jni_LiveChatClient_Login
  (JNIEnv *env, jclass cls, jstring user, jstring password, jstring deviceId, jint clientType)
{
	bool result = false;

	FileLog("LiveChatClientJni", "Login()");

	if (NULL != g_liveChatClient) {
		string strUser = GetJString(env, user);
		string strPassword = GetJString(env, password);
		string strDeviceId = GetJString(env, deviceId);
		gDeviceId = strDeviceId;
		CLIENT_TYPE eClientType = IntToClientType(clientType);

		FileLog("LiveChatClientJni", "Login() user:%s, password:%s, deviceId:%s, clientType:%d"
				, strUser.c_str(), strPassword.c_str(), strDeviceId.c_str(), eClientType);

		result = g_liveChatClient->Login(strUser, strPassword, strDeviceId, eClientType, USER_SEX_FEMALE, AUTH_TYPE_SID);
	}

	FileLog("LiveChatClientJni", "Login() result:%d", result);

	return result;
}

/*
 * Class:     com_qpidnetwork_livechat_jni_LiveChatClient
 * Method:    Logout
 * Signature: ()Z
 */
JNIEXPORT jboolean JNICALL Java_com_qpidnetwork_livechat_jni_LiveChatClient_Logout
  (JNIEnv *env, jclass cls)
{
	bool result = false;

	FileLog("LiveChatClientJni", "Logout() begin");

	if (NULL != g_liveChatClient) {
		result =  g_liveChatClient->Logout();
	}

	FileLog("LiveChatClientJni", "Logout() end");

	return result;
}

/*
 * Class:     com_qpidnetwork_livechat_jni_LiveChatClient
 * Method:    UploadVer
 * Signature: (Ljava/lang/String;)Z
 */
JNIEXPORT jboolean JNICALL Java_com_qpidnetwork_livechat_jni_LiveChatClient_UploadVer
  (JNIEnv *env, jclass cls, jstring version)
{
	if (NULL == g_liveChatClient) {
		return false;
	}

	string strVersion = GetJString(env, version);
	return g_liveChatClient->UploadVer(strVersion);
}

/*
 * Class:     com_qpidnetwork_livechat_jni_LiveChatClient
 * Method:    SetStatus
 * Signature: (I)Z
 */
JNIEXPORT jboolean JNICALL Java_com_qpidnetwork_livechat_jni_LiveChatClient_SetStatus
  (JNIEnv *env, jclass cls, jint status)
{
	if (NULL == g_liveChatClient) {
		return false;
	}

	USER_STATUS_TYPE eStatus = IntToUserStatusType(status);
	return g_liveChatClient->SetStatus(eStatus);
}

/*
 * Class:     com_qpidnetwork_livechat_jni_LiveChatClient
 * Method:    UploadTicket
 * Signature: (Ljava/lang/String;I)Z
 */
JNIEXPORT jboolean JNICALL Java_com_qpidnetwork_livechat_jni_LiveChatClient_UploadTicket
  (JNIEnv *env, jclass cls, jstring userId, jint ticket)
{
	if (NULL == g_liveChatClient) {
		return false;
	}

	string strUserId = GetJString(env, userId);
	return g_liveChatClient->UploadTicket(strUserId, ticket);
}

/*
 * Class:     com_qpidnetwork_livechat_jni_LiveChatClient
 * Method:    SendMessage
 * Signature: (Ljava/lang/String;Ljava/lang/String;ZI)Z
 */
JNIEXPORT jboolean JNICALL Java_com_qpidnetwork_livechat_jni_LiveChatClient_SendMessage
  (JNIEnv *env, jclass cls, jstring userId, jstring message, jboolean illegal, jint ticket)
{
	if (NULL == g_liveChatClient) {
		return false;
	}

	string strUserId = GetJString(env, userId);
	string strMessage = GetJString(env, message);
	return g_liveChatClient->SendTextMessage(strUserId, strMessage, illegal, ticket);
}

/*
 * Class:     com_qpidnetwork_livechat_jni_LiveChatClient
 * Method:    SendEmotion
 * Signature: (Ljava/lang/String;Ljava/lang/String;I)Z
 */
JNIEXPORT jboolean JNICALL Java_com_qpidnetwork_livechat_jni_LiveChatClient_SendEmotion
  (JNIEnv *env, jclass cls, jstring userId, jstring emotionId, jint ticket)
{
	if (NULL == g_liveChatClient) {
		return false;
	}

	string strUserId = GetJString(env, userId);
	string strEmotionId = GetJString(env, emotionId);
	return g_liveChatClient->SendEmotion(strUserId, strEmotionId, ticket);
}

/*
 * Class:     com_qpidnetwork_livechat_jni_LiveChatClient
 * Method:    GetLadyVoiceCode
 * Signature: (Ljava/lang/String;)Z
 */
JNIEXPORT jboolean JNICALL Java_com_qpidnetwork_livechat_jni_LiveChatClient_GetLadyVoiceCode
  (JNIEnv *env, jclass cls, jstring userId)
{
	if (NULL == g_liveChatClient) {
		return false;
	}

	string strUserId = GetJString(env, userId);
	return g_liveChatClient->GetLadyVoiceCode(strUserId);
}

/*
 * Class:     com_qpidnetwork_livechat_jni_LiveChatClient
 * Method:    SendVoice
 * Signature: (Ljava/lang/String;Ljava/lang/String;II)Z
 */
JNIEXPORT jboolean JNICALL Java_com_qpidnetwork_livechat_jni_LiveChatClient_SendVoice
  (JNIEnv *env, jclass cls, jstring userId, jstring voiceId, jint voiceLength, jint ticket)
{
	if (NULL == g_liveChatClient) {
		return false;
	}

	string strUserId = GetJString(env, userId);
	string strVoiceId = GetJString(env, voiceId);
	return g_liveChatClient->SendVoice(strUserId, strVoiceId, voiceLength, ticket);
}

/*
 * Class:     com_qpidnetwork_livechat_jni_LiveChatClient
 * Method:    SendLadyPhoto
 * Signature: (Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;ZLjava/lang/String;I)Z
 */
JNIEXPORT jboolean JNICALL Java_com_qpidnetwork_livechat_jni_LiveChatClient_SendLadyPhoto
  (JNIEnv *env, jclass cls, jstring userId, jstring inviteId, jstring photoId, jstring sendId, jboolean charge, jstring photoDesc, jint ticket)
{
	if (NULL == g_liveChatClient) {
		return false;
	}

	string strUserId = GetJString(env, userId);
	string strInviteId = GetJString(env, inviteId);
	string strPhotoId = GetJString(env, photoId);
	string strSendId = GetJString(env, sendId);
	string strPhotoDesc = GetJString(env, photoDesc);
	return g_liveChatClient->SendLadyPhoto(strUserId, strInviteId, strPhotoId, strSendId, charge, strPhotoDesc, ticket);
}

/*
 * Class:     com_qpidnetwork_livechat_jni_LiveChatClient
 * Method:    SendLadyVideo
 * Signature: (Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;ZLjava/lang/String;I)Z
 */
JNIEXPORT jboolean JNICALL Java_com_qpidnetwork_livechat_jni_LiveChatClient_SendLadyVideo
  (JNIEnv *env, jclass cls, jstring userId, jstring inviteId, jstring videoId, jstring sendId, jboolean charge, jstring videoDesc, jint ticket)
{
	if (NULL == g_liveChatClient) {
		return false;
	}

	string strUserId = GetJString(env, userId);
	string strInviteId = GetJString(env, inviteId);
	string strVideoId = GetJString(env, videoId);
	string strSendId = GetJString(env, sendId);
	string strVideoDesc = GetJString(env, videoDesc);
	return g_liveChatClient->SendLadyVideo(strUserId, strInviteId, strVideoId, strSendId, charge, strVideoDesc, ticket);
}

/*
 * Class:     com_qpidnetwork_livechat_jni_LiveChatClient
 * Method:    GetUserInfo
 * Signature: (Ljava/lang/String;)Z
 */
JNIEXPORT jboolean JNICALL Java_com_qpidnetwork_livechat_jni_LiveChatClient_GetUserInfo
  (JNIEnv *env, jclass cls, jstring userId)
{
	if (NULL == g_liveChatClient) {
		return false;
	}

	string strUserId = GetJString(env, userId);
	return g_liveChatClient->GetUserInfo(strUserId);
}

/*
 * Class:     com_qpidnetwork_livechat_jni_LiveChatClient
 * Method:    GetUsersInfo
 * Signature: ([Ljava/lang/String;)I
 */
JNIEXPORT jint JNICALL Java_com_qpidnetwork_livechat_jni_LiveChatClient_GetUsersInfo
  (JNIEnv *env, jclass cls, jobjectArray usersIdArray)
{
	FileLog("LiveChatClientJni", "GetUsersInfo() client:%p"
			, g_liveChatClient);

	if (NULL == g_liveChatClient) {
		return false;
	}

	// 获取user id列表
	list<string> userIdList;
	if (NULL != usersIdArray) {
		for (int i = 0; i < env->GetArrayLength(usersIdArray); i++) {
			jstring userId = (jstring)env->GetObjectArrayElement(usersIdArray, i);
			string strUserId = GetJString(env, userId);
			if (!strUserId.empty()) {
				userIdList.push_back(strUserId);
			}
		}
	}

	FileLog("LiveChatClientJni", "GetUsersInfo() userIdList.size():%d", userIdList.size());

	return g_liveChatClient->GetUsersInfo(userIdList);
}

/*
 * Class:     com_qpidnetwork_livechat_jni_LiveChatClient
 * Method:    GetBlockList
 * Signature: ()Z
 */
JNIEXPORT jboolean JNICALL Java_com_qpidnetwork_livechat_jni_LiveChatClient_GetBlockList
  (JNIEnv *env, jclass cls)
{
	if (NULL == g_liveChatClient) {
		return false;
	}

	return g_liveChatClient->GetContactList(CONTACT_LIST_BLOCK);
}

/*
 * Class:     com_qpidnetwork_livechat_jni_LiveChatClient
 * Method:    SearchOnlineMan
 * Signature: (II)Z
 */
JNIEXPORT jboolean JNICALL Java_com_qpidnetwork_livechat_jni_LiveChatClient_SearchOnlineMan
  (JNIEnv *env, jclass cls, jint beginAge, jint endAge)
{
	FileLog("LiveChatClientJni", "SearchOnlineMan() client:%p, begin:%d, end:%d"
			, g_liveChatClient, beginAge, endAge);
	if (NULL == g_liveChatClient) {
		return false;
	}

	return g_liveChatClient->SearchOnlineMan(beginAge, endAge);
}

/*
 * Class:     com_qpidnetwork_livechat_jni_LiveChatClient
 * Method:    ReplyIdentifyCode
 * Signature: (Ljava/lang/String;)Z
 */
JNIEXPORT jboolean JNICALL Java_com_qpidnetwork_livechat_jni_LiveChatClient_ReplyIdentifyCode
  (JNIEnv *env, jclass cls, jstring code)
{
	if (NULL == g_liveChatClient) {
		return false;
	}
	string strCode = GetJString(env, code);
	return g_liveChatClient->ReplyIdentifyCode(strCode);
}

/*
 * Class:     com_qpidnetwork_livechat_jni_LiveChatClient
 * Method:    RefreshIdentifyCode
 * Signature: ()Z
 */
JNIEXPORT jboolean JNICALL Java_com_qpidnetwork_livechat_jni_LiveChatClient_RefreshIdentifyCode
  (JNIEnv *env, jclass cls)
{
	if (NULL == g_liveChatClient) {
		return false;
	}
	return g_liveChatClient->RefreshIdentifyCode();
}

/*
 * Class:     com_qpidnetwork_livechat_jni_LiveChatClient
 * Method:    RefreshInviteTemplate
 * Signature: ()Z
 */
JNIEXPORT jboolean JNICALL Java_com_qpidnetwork_livechat_jni_LiveChatClient_RefreshInviteTemplate
  (JNIEnv *env, jclass cls)
{
	if (NULL == g_liveChatClient) {
		return false;
	}
	return g_liveChatClient->RefreshInviteTemplate();
}

/*
 * Class:     com_qpidnetwork_livechat_jni_LiveChatClient
 * Method:    GetFeeRecentContactList
 * Signature: ()Z
 */
JNIEXPORT jboolean JNICALL Java_com_qpidnetwork_livechat_jni_LiveChatClient_GetFeeRecentContactList
  (JNIEnv *env, jclass cls)
{
	if (NULL == g_liveChatClient) {
		return false;
	}
	return g_liveChatClient->GetFeeRecentContactList();
}

/*
 * Class:     com_qpidnetwork_livechat_jni_LiveChatClient
 * Method:    GetLadyChatInfo
 * Signature: ()Z
 */
JNIEXPORT jboolean JNICALL Java_com_qpidnetwork_livechat_jni_LiveChatClient_GetLadyChatInfo
  (JNIEnv *env, jclass cls)
{
	if (NULL == g_liveChatClient) {
		return false;
	}
	return g_liveChatClient->GetLadyChatInfo();
}
