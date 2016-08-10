/*
 * com_qpidnetwork_request_RequestJniAuthorization.cpp
 *
 *  Created on: 2015-2-27
 *      Author: Max.Chiu
 *      Email: Kingsleyyau@gmail.com
 */
#include "com_qpidnetwork_request_LoginManagerJni.h"
#include "com_qpidnetwork_request_RequestJni_GobalFunc.h"

#include <crashhandler/CrashHandler.h>

/**
 * Callback
 */
jobject gLoginCallback = NULL;
class RequestLoginCallback : public LoginManagerCallback {
	void OnLogin(const LoginManager* pLoginManager,
			bool success,
			const string& errnum,
			const string& errmsg,
			const LoginItem& item
			) {
		FileLog("httprequest", "JNI::OnLogin( success : %s )", success?"true":"false");

		// Add for crash dump
		if( success ) {
			CrashHandler::GetInstance()->SetUser(item.lady_id);
		}

		/* turn object to java object here */
		JNIEnv* env;
		jint iRet = JNI_ERR;
		gJavaVM->GetEnv((void**)&env, JNI_VERSION_1_4);
		if( env == NULL ) {
			iRet = gJavaVM->AttachCurrentThread((JNIEnv **)&env, NULL);
		}

		jobject jItem = NULL;
		JavaItemMap::iterator itr = gJavaItemMap.find(LOGIN_ITEM_CLASS);
		if( itr != gJavaItemMap.end() ) {
			jclass cls = env->GetObjectClass(itr->second);
			if( cls != NULL) {
				jmethodID init = env->GetMethodID(cls, "<init>", "("
						"Ljava/lang/String;"
						"Ljava/lang/String;"
						"Ljava/lang/String;"
						"Ljava/lang/String;"
						"Ljava/lang/String;"
						"Ljava/lang/String;"

						"Z"
						"Z"
						"Z"
						"Z"
						"Z"
						")V"
						);

				FileLog("httprequest", "JNI::OnLogin( GetMethodID <init> : %p )", init);

				if( init != NULL ) {

					jstring ladyid = env->NewStringUTF(item.lady_id.c_str());
					jstring sid = env->NewStringUTF(item.sid.c_str());
					jstring firstname = env->NewStringUTF(item.firstname.c_str());
					jstring lastname = env->NewStringUTF(item.lastname.c_str());
					jstring photo_url = env->NewStringUTF(item.photo_url.c_str());
					jstring agent = env->NewStringUTF(item.agent.c_str());

					jItem = env->NewObject(cls, init,
							ladyid,
							sid,
							firstname,
							lastname,
							photo_url,
							agent,

							item.login,
							item.search,
							item.admirermail,
							item.livechat,
							item.video
							);

					env->DeleteLocalRef(ladyid);
					env->DeleteLocalRef(sid);
					env->DeleteLocalRef(firstname);
					env->DeleteLocalRef(lastname);
					env->DeleteLocalRef(photo_url);
					env->DeleteLocalRef(agent);

					FileLog("httprequest", "JNI::OnLogin( NewObject : %p )", jItem);
				}
			}
		}

		/* real callback java */
		jobject callbackObj = gLoginCallback;//gCallbackMap.Erase((long long)task);
		if( callbackObj != NULL ) {
			jclass callbackCls = env->GetObjectClass(callbackObj);

			string signure = "(ZLjava/lang/String;Ljava/lang/String;";
			signure += "L";
			signure += LOGIN_ITEM_CLASS;
			signure += ";";
			signure += ")V";
			jmethodID callback = env->GetMethodID(callbackCls, "OnLogin", signure.c_str());
			FileLog("httprequest", "JNI::OnLogin( callbackCls : %p, callback : %p, signure : %s )",
					callbackCls, callback, signure.c_str());

			if( callbackObj != NULL && callback != NULL ) {
				jstring jerrno = env->NewStringUTF(errnum.c_str());
				jstring jerrmsg = env->NewStringUTF(errmsg.c_str());

				FileLog("httprequest", "JNI::OnLogin( CallObjectMethod "
						"jItem : %p )", jItem);

				env->CallVoidMethod(callbackObj, callback, success, jerrno, jerrmsg, jItem);

				env->DeleteLocalRef(jerrno);
				env->DeleteLocalRef(jerrmsg);
			}
		}

		if( jItem != NULL ) {
			env->DeleteLocalRef(jItem);
		}

		if( iRet == JNI_OK ) {
			gJavaVM->DetachCurrentThread();
		}
	}

	void OnLogout(const LoginManager* pLoginManager, LogoutType type) {
		FileLog("httprequest", "JNI::OnLogout( type : %d )", type);

		/* turn object to java object here */
		JNIEnv* env;
		jint iRet = JNI_ERR;
		gJavaVM->GetEnv((void**)&env, JNI_VERSION_1_4);
		if( env == NULL ) {
			iRet = gJavaVM->AttachCurrentThread((JNIEnv **)&env, NULL);
		}

		/* real callback java */
		jobject callbackObj = gLoginCallback;
		if( callbackObj != NULL ) {
			jclass callbackCls = env->GetObjectClass(callbackObj);

			string signure = "(I)V";
			jmethodID callback = env->GetMethodID(callbackCls, "OnLogout", signure.c_str());
			FileLog("httprequest", "JNI::OnLogout( callbackCls : %p, callback : %p, signure : %s )",
					callbackCls, callback, signure.c_str());

			if( callbackObj != NULL && callback != NULL ) {
				FileLog("httprequest", "JNI::OnLogout( CallObjectMethod )");
				env->CallVoidMethod(callbackObj, callback, type);
			}
		}

		if( iRet == JNI_OK ) {
			gJavaVM->DetachCurrentThread();
		}
	}
};
RequestLoginCallback gRequestLoginCallback;

/*
 * Class:     com_qpidnetwork_request_LoginManagerJni
 * Method:    SetLoginCallback
 * Signature: (Lcom/qpidnetwork/request/OnLoginManagerCallback;)V
 */
JNIEXPORT void JNICALL Java_com_qpidnetwork_request_LoginManagerJni_SetLoginCallback
  (JNIEnv *env, jclass cls, jobject callback) {
	if( gLoginCallback != NULL ) {
		LoginManager::GetInstance().RemoveCallback(&gRequestLoginCallback);
		env->DeleteGlobalRef(gLoginCallback);
		gLoginCallback = NULL;
	}

	if( callback != NULL ) {
		gLoginCallback = env->NewGlobalRef(callback);
		LoginManager::GetInstance().AddCallback(&gRequestLoginCallback);
	}
}
/*
 * Class:     com_qpidnetwork_request_LoginManagerJni
 * Method:    Login
 * Signature: (Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;)V
 */
JNIEXPORT void JNICALL Java_com_qpidnetwork_request_LoginManagerJni_Login
  (JNIEnv *env, jclass, jstring email, jstring password, jstring deviceId, jstring model, jstring manufacturer) {
	LoginManager::GetInstance().Login(
			JString2String(env, email),
			JString2String(env, password),
			JString2String(env, deviceId),
			JString2String(env, model),
			JString2String(env, manufacturer)
			);
}

/*
 * Class:     com_qpidnetwork_request_LoginManagerJni
 * Method:    Logout
 * Signature: (jint)V
 */
JNIEXPORT void JNICALL Java_com_qpidnetwork_request_LoginManagerJni_Logout
  (JNIEnv *, jclass cls, jint type) {
	LoginManager::GetInstance().Logout(type);
}

/*
 * Class:     com_qpidnetwork_request_LoginManagerJni
 * Method:    GetUser
 * Signature: ()Ljava/lang/String;
 */
JNIEXPORT jstring JNICALL Java_com_qpidnetwork_request_LoginManagerJni_GetUser
  (JNIEnv *env, jclass) {
	return env->NewStringUTF(LoginManager::GetInstance().GetUser().c_str());
}

/*
 * Class:     com_qpidnetwork_request_LoginManagerJni
 * Method:    GetPassword
 * Signature: ()Ljava/lang/String;
 */
JNIEXPORT jstring JNICALL Java_com_qpidnetwork_request_LoginManagerJni_GetPassword
  (JNIEnv *env, jclass) {
	return env->NewStringUTF(LoginManager::GetInstance().GetPassword().c_str());
}
