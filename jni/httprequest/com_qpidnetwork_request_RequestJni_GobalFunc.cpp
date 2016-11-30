/*
 * RequestJni_GobalFunc.cpp
 *
 *  Created on: 2015-3-4
 *      Author: Max.Chiu
 *      Email: Kingsleyyau@gmail.com
 */

#include "com_qpidnetwork_request_RequestJni_GobalFunc.h"

JavaVM* gJavaVM;

CallbackMap gCallbackMap;

RequestMap gRequestMap;

KMutex gRequestMapMutex;

JavaItemMap gJavaItemMap;

jobject requestFinishCallback;

HttpRequestManager gHttpRequestManager;

HttpRequestHostManager gHttpRequestHostManager;

string JString2String(JNIEnv* env, jstring str) {
	string result("");
	if (NULL != str) {
		const char* cpTemp = env->GetStringUTFChars(str, 0);
		result = cpTemp;
		env->ReleaseStringUTFChars(str, cpTemp);
	}
	return result;
}

void InitEnumHelper(JNIEnv *env, const char *path, jobject *objptr) {
	FileLog("httprequest", "InitEnumHelper( path : %s )", path);
    jclass cls = env->FindClass(path);
    if( !cls ) {
    	FileLog("httprequest", "InitEnumHelper( !cls )");
        return;
    }

    jmethodID constr = env->GetMethodID(cls, "<init>", "(Ljava/lang/String;I)V");
    if( !constr ) {
    	FileLog("httprequest", "InitEnumHelper( !constr )");
        return;
    }

    jobject obj = env->NewObject(cls, constr, NULL, 0);
    if( !obj ) {
    	FileLog("httprequest", "InitEnumHelper( !obj )");
        return;
    }

    (*objptr) = env->NewGlobalRef(obj);
}

void InitClassHelper(JNIEnv *env, const char *path, jobject *objptr) {
	FileLog("httprequest", "InitClassHelper( path : %s )", path);
    jclass cls = env->FindClass(path);
    if( !cls ) {
    	FileLog("httprequest", "InitClassHelper( !cls )");
        return;
    }

    jmethodID constr = env->GetMethodID(cls, "<init>", "()V");
    if( !constr ) {
    	FileLog("httprequest", "InitClassHelper( !constr )");
        constr = env->GetMethodID(cls, "<init>", "(Ljava/lang/String;I)V");
        if( !constr ) {
        	FileLog("httprequest", "InitClassHelper( !constr )");
            return;
        }
        return;
    }

    jobject obj = env->NewObject(cls, constr);
    if( !obj ) {
    	FileLog("httprequest", "InitClassHelper( !obj )");
        return;
    }

    (*objptr) = env->NewGlobalRef(obj);
}

jclass GetJClass(JNIEnv* env, const char* classPath)
{
	jclass jCls = NULL;
	JavaItemMap::iterator itr = gJavaItemMap.find(classPath);
	if( itr != gJavaItemMap.end() ) {
		jobject jItemObj = itr->second;
		jCls = env->GetObjectClass(jItemObj);
	}
	return jCls;
}

/* JNI_OnLoad */
jint JNI_OnLoad(JavaVM* vm, void* reserved) {
	FileLog("httprequest", "JNI_OnLoad( httprequest.so JNI_OnLoad )");
	gJavaVM = vm;

	// Get JNI
	JNIEnv* env;
	if (JNI_OK != vm->GetEnv(reinterpret_cast<void**> (&env),
                           JNI_VERSION_1_4)) {
		FileLog("httprequest", "JNI_OnLoad ( httprequest.so could not get JNI env )");
		return -1;
	}

	/* 2.认证登录 */
	jobject jLoginItem;
	InitClassHelper(env, LOGIN_ITEM_CLASS, &jLoginItem);
	gJavaItemMap.insert(JavaItemMap::value_type(LOGIN_ITEM_CLASS, jLoginItem));

	/* 3.男士信息 */
	jobject jManListItem;
	InitClassHelper(env, MAN_LIST_ITEM_CLASS, &jManListItem);
	gJavaItemMap.insert(JavaItemMap::value_type(MAN_LIST_ITEM_CLASS, jManListItem));

	jobject jManDetailItem;
	InitClassHelper(env, MAN_DETAIL_ITEM_CLASS, &jManDetailItem);
	gJavaItemMap.insert(JavaItemMap::value_type(MAN_DETAIL_ITEM_CLASS, jManDetailItem));

	jobject jManRecentChatListItem;
	InitClassHelper(env, MAN_RECENT_CHAT_LIST_ITEM_CLASS, &jManRecentChatListItem);
	gJavaItemMap.insert(JavaItemMap::value_type(MAN_RECENT_CHAT_LIST_ITEM_CLASS, jManRecentChatListItem));

	jobject jManRecentViewListItem;
	InitClassHelper(env, MAN_RECENT_VIEW_LIST_ITEM_CLASS, &jManRecentViewListItem);
	gJavaItemMap.insert(JavaItemMap::value_type(MAN_RECENT_VIEW_LIST_ITEM_CLASS, jManRecentViewListItem));

	/* 4.My Album */
	jobject jAlbumListItem;
	InitClassHelper(env, ALBUM_LIST_ITEM_CLASS, &jAlbumListItem);
	gJavaItemMap.insert(JavaItemMap::value_type(ALBUM_LIST_ITEM_CLASS, jAlbumListItem));

	jobject jAlbumPhotoItem;
	InitClassHelper(env, ALBUM_PHOTO_ITEM_CLASS, &jAlbumPhotoItem);
	gJavaItemMap.insert(JavaItemMap::value_type(ALBUM_PHOTO_ITEM_CLASS, jAlbumPhotoItem));

	jobject jAlbumVideoItem;
	InitClassHelper(env, ALBUM_VIDEO_ITEM_CLASS, &jAlbumVideoItem);
	gJavaItemMap.insert(JavaItemMap::value_type(ALBUM_VIDEO_ITEM_CLASS, jAlbumVideoItem));

	/*5.Livechat*/
	jobject jLCCustomTemplateListItem;
	InitClassHelper(env, LIVE_CHAT_CUSTOM_TEMPLATE_CLASS, &jLCCustomTemplateListItem);
	gJavaItemMap.insert(JavaItemMap::value_type(LIVE_CHAT_CUSTOM_TEMPLATE_CLASS, jLCCustomTemplateListItem));

	jobject jLCChatHistoryListItem;
	InitClassHelper(env, LIVE_CHAT_LADY_CHAT_LIST_CLASS, &jLCChatHistoryListItem);
	gJavaItemMap.insert(JavaItemMap::value_type(LIVE_CHAT_LADY_CHAT_LIST_CLASS, jLCChatHistoryListItem));

	jobject jLCChatMsgListItem;
	InitClassHelper(env, LIVE_CHAT_LADY_INVITE_MSG_CLASS, &jLCChatMsgListItem);
	gJavaItemMap.insert(JavaItemMap::value_type(LIVE_CHAT_LADY_INVITE_MSG_CLASS, jLCChatMsgListItem));

	jobject jLCGetPhotoAlbunListItem;
	InitClassHelper(env, LIVE_CHAT_GET_PHOTO_LIST_ALBUM_CLASS, &jLCGetPhotoAlbunListItem);
	gJavaItemMap.insert(JavaItemMap::value_type(LIVE_CHAT_GET_PHOTO_LIST_ALBUM_CLASS, jLCGetPhotoAlbunListItem));

	jobject jLCGetPhotoListItem;
	InitClassHelper(env, LIVE_CHAT_GET_PHOTO_LIST_PHOTO_CLASS, &jLCGetPhotoListItem);
	gJavaItemMap.insert(JavaItemMap::value_type(LIVE_CHAT_GET_PHOTO_LIST_PHOTO_CLASS, jLCGetPhotoListItem));

	jobject jLCGetPhotoGroupListItem;
	InitClassHelper(env, LIVE_CHAT_GET_PHOTO_LIST_GROUP_CLASS, &jLCGetPhotoGroupListItem);
	gJavaItemMap.insert(JavaItemMap::value_type(LIVE_CHAT_GET_PHOTO_LIST_GROUP_CLASS, jLCGetPhotoGroupListItem));

	jobject jLCGetVideoListItem;
	InitClassHelper(env, LIVE_CHAT_GET_PHOTO_LIST_VIDEO_CLASS, &jLCGetVideoListItem);
	gJavaItemMap.insert(JavaItemMap::value_type(LIVE_CHAT_GET_PHOTO_LIST_VIDEO_CLASS, jLCGetVideoListItem));

	jobject jLCMagicIconItem;
	InitClassHelper(env, LIVECHAT_MAGIC_ICON_TIME_CLASS, &jLCMagicIconItem);
	gJavaItemMap.insert(JavaItemMap::value_type(LIVECHAT_MAGIC_ICON_TIME_CLASS, jLCMagicIconItem));

	jobject jLCMagicTypeItem;
	InitClassHelper(env, LIVECHAT_MAGIC_TYPE_TIME_CLASS, &jLCMagicTypeItem);
	gJavaItemMap.insert(JavaItemMap::value_type(LIVECHAT_MAGIC_TYPE_TIME_CLASS, jLCMagicTypeItem));

	jobject jLCMagicConfigItem;
	InitClassHelper(env, LIVECHAT_MAGIC_CONFIG_ITEM_CLASS, &jLCMagicConfigItem);
	gJavaItemMap.insert(JavaItemMap::value_type(LIVECHAT_MAGIC_CONFIG_ITEM_CLASS, jLCMagicConfigItem));

	/*6.其他协议*/
	jobject jEmotionConfigItem;
	InitClassHelper(env, OTHER_EMOTION_CONFIG_CLASS, &jEmotionConfigItem);
	gJavaItemMap.insert(JavaItemMap::value_type(OTHER_EMOTION_CONFIG_CLASS, jEmotionConfigItem));

	jobject jEmotionConfigTagItem;
	InitClassHelper(env, OTHER_EMOTION_CONFIG_TAG_CLASS, &jEmotionConfigTagItem);
	gJavaItemMap.insert(JavaItemMap::value_type(OTHER_EMOTION_CONFIG_TAG_CLASS, jEmotionConfigTagItem));

	jobject jEmotionConfigTypeItem;
	InitClassHelper(env, OTHER_EMOTION_CONFIG_TYPE_CLASS, &jEmotionConfigTypeItem);
	gJavaItemMap.insert(JavaItemMap::value_type(OTHER_EMOTION_CONFIG_TYPE_CLASS, jEmotionConfigTypeItem));

	jobject jEmotionItem;
	InitClassHelper(env, OTHER_EMOTION_CONFIG_EMOTION_CLASS, &jEmotionItem);
	gJavaItemMap.insert(JavaItemMap::value_type(OTHER_EMOTION_CONFIG_EMOTION_CLASS, jEmotionItem));

	jobject jCheckVersionItem;
	InitClassHelper(env, OTHER_CHECK_VERSION_CLASS, &jCheckVersionItem);
	gJavaItemMap.insert(JavaItemMap::value_type(OTHER_CHECK_VERSION_CLASS, jCheckVersionItem));

	jobject jSynConfigItem;
	InitClassHelper(env, OTHER_SYN_CONFIG_CLASS, &jSynConfigItem);
	gJavaItemMap.insert(JavaItemMap::value_type(OTHER_SYN_CONFIG_CLASS, jSynConfigItem));

	jobject jAgentInfoItem;
	InitClassHelper(env, OTHER_AGENTINFO_CLASS, &jAgentInfoItem);
	gJavaItemMap.insert(JavaItemMap::value_type(OTHER_AGENTINFO_CLASS, jAgentInfoItem));

	jobject jMyProfileItem;
	InitClassHelper(env, OTHER_MYPROFILE_CLASS, &jMyProfileItem);
	gJavaItemMap.insert(JavaItemMap::value_type(OTHER_MYPROFILE_CLASS, jMyProfileItem));

	// 初始化http请求实例
	requestFinishCallback = NULL;
	gHttpRequestManager.SetHostManager(&gHttpRequestHostManager);

	// 初始化登录实例
	LoginManager::GetInstance().Init(&gHttpRequestManager);
	ConfigManager::GetInstance().Init(&gHttpRequestManager);

	return JNI_VERSION_1_4;
}

bool GetEnv(JNIEnv** env, bool* isAttachThread)
{
	*isAttachThread = false;
	jint iRet = JNI_ERR;
	iRet = gJavaVM->GetEnv((void**) env, JNI_VERSION_1_4);
	if( iRet == JNI_EDETACHED ) {
		iRet = gJavaVM->AttachCurrentThread(env, NULL);
		*isAttachThread = (iRet == JNI_OK);
	}
//	if( *env == NULL ) {
//		iRet = gJavaVM->AttachCurrentThread(env, NULL);
//	}
//	if (iRet == JNI_OK) {
//		*isAttachThread = true;
//	}

	return (iRet == JNI_OK);
}

bool ReleaseEnv(bool isAttachThread)
{
	if (isAttachThread) {
		gJavaVM->DetachCurrentThread();
	}
	return true;
}

/**
 * callback 4 finish request and free memory
 */
class RequestFinishCallback : public ITaskCallback {
	void OnTaskFinish(ITask* task) {
		FileLog("httprequest", "JNI::OnTaskFinish( task : %p )", task);

//		/* turn object to java object here */
//		JNIEnv* env;
//		jint iRet = JNI_ERR;
//		gJavaVM->GetEnv((void**)&env, JNI_VERSION_1_4);
//		if( env == NULL ) {
//			iRet = gJavaVM->AttachCurrentThread((JNIEnv **)&env, NULL);
//		}
//
//		/* real callback java */
//		jclass callbackCls = env->GetObjectClass(requestFinishCallback);
//
//		string signure = "(J)V";
//		jmethodID callback = env->GetMethodID(callbackCls, "OnRequestFinish", signure.c_str());
//		FileLog("httprequest", "JNI::OnTaskFinish( callback : %p, signure : %s )",
//				callback, signure.c_str());
//
//		if( requestFinishCallback != NULL && callback != NULL ) {
//			FileLog("httprequest", "JNI::OnTaskFinish( CallObjectMethod )");
//
//			env->CallVoidMethod(requestFinishCallback, callback, (long long)task);
//		}

		gRequestMapMutex.lock();
		gRequestMap.erase((long)task);
		delete task;
		task = NULL;
		gRequestMapMutex.unlock();

//		if( iRet == JNI_OK ) {
//			gJavaVM->DetachCurrentThread();
//		}
	}
};
RequestFinishCallback gRequestFinishCallback;
