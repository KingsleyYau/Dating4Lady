/*
 * com_qpidnetwork_request_RequestJniAuthorization.cpp
 *
 *  Created on: 2015-2-27
 *      Author: Max.Chiu
 *      Email: Kingsleyyau@gmail.com
 */
#include "com_qpidnetwork_request_ConfigManagerJni.h"
#include "com_qpidnetwork_request_RequestJni_GobalFunc.h"
#include "ConfigManager.h"

typedef map<jobject, bool> ConfigCallbackMap;
using namespace std;
ConfigCallbackMap gConfigCallbackMap;
KMutex gConfigCallbackMapLock;

/**
 * Callback
 */
jobject gConfigCallback = NULL;
class RequestConfigCallback : public ConfigManagerCallback {
	void onSynConfigCallback(
			const ConfigManager* pConfigManager,
			bool isSuccess,
			const string& errnum,
			const string& errmsg,
			const SynConfigItem& item
			) {
		FileLog("httprequest","JNI::onSynConfigCallback( success : %s )", isSuccess ? "true" : "false");
		/* turn object to java object here */
		JNIEnv* env;
		jint iRet = JNI_ERR;
		gJavaVM->GetEnv((void**) &env, JNI_VERSION_1_4);
		if (env == NULL) {
			iRet = gJavaVM->AttachCurrentThread((JNIEnv **) &env, NULL);
		}

		jobject jItem = NULL;
		JavaItemMap::iterator itr = gJavaItemMap.find(OTHER_SYN_CONFIG_CLASS);
		if(itr != gJavaItemMap.end()){
			jclass jItemClass = env->GetObjectClass(itr->second);
			string itemInitString = "(Ljava/lang/String;ILjava/lang/String;ILjava/lang/String;[Ljava/lang/String;"
					"ILjava/lang/String;Ljava/lang/String;Ljava/lang/String;)V";
			jmethodID jItemInit = env->GetMethodID(jItemClass, "<init>", itemInitString.c_str());

			//languages
			jobjectArray jLanguages = env->NewObjectArray(item.translateLanguage.size(), env->FindClass("java/lang/String"), NULL);
			list<string>::const_iterator languageItr;
			int iLanguageIndex;
			for(iLanguageIndex=0, languageItr=item.translateLanguage.begin(); languageItr!=item.translateLanguage.end(); iLanguageIndex++, languageItr++){
				env->SetObjectArrayElement(jLanguages, iLanguageIndex, env->NewStringUTF((*languageItr).c_str()));
			}
			jItem = env->NewObject(jItemClass, jItemInit, env->NewStringUTF(item.socketHost.c_str()),
					item.socketPort, env->NewStringUTF(item.socketVersion.c_str()), item.socketFromId,
					env->NewStringUTF(item.translateUrl.c_str()), jLanguages, item.apkVersionCode, env->NewStringUTF(item.apkVersionName.c_str()),
					env->NewStringUTF(item.apkVersionUrl.c_str()), env->NewStringUTF(item.siteUrl.c_str()));
			env->DeleteLocalRef(jLanguages);
		}

		/* real callback java */
		gConfigCallbackMapLock.lock();
		for( ConfigCallbackMap::iterator itr = gConfigCallbackMap.begin(); itr != gConfigCallbackMap.end(); itr++ ) {
			jobject callbackObj = itr->first;
			if ( callbackObj != NULL ) {
				jclass callbackCls = env->GetObjectClass(callbackObj);

				string signure = "(ZLjava/lang/String;Ljava/lang/String;";
				signure	+= 	"L";
				signure	+= 	OTHER_SYN_CONFIG_CLASS;
				signure	+= 	";)V";
				jmethodID callback = env->GetMethodID(callbackCls, "OnSynConfig", signure.c_str());

				if ( callback != NULL) {
					jstring jerrno = env->NewStringUTF(errnum.c_str());
					jstring jerrmsg = env->NewStringUTF(errmsg.c_str());

					FileLog("httprequest","JNI::onSynConfigCallback( CallObjectMethod )");

					env->CallVoidMethod(callbackObj, callback, isSuccess, jerrno, jerrmsg, jItem);

					env->DeleteGlobalRef(callbackObj);

					env->DeleteLocalRef(jerrno);
					env->DeleteLocalRef(jerrmsg);
				}
			}
		}
		gConfigCallbackMap.clear();
		gConfigCallbackMapLock.unlock();

		env->DeleteLocalRef(jItem);

		if (iRet == JNI_OK) {
			gJavaVM->DetachCurrentThread();
		}
	}
};
RequestConfigCallback gRequestConfigCallback;

/*
 * Class:     com_qpidnetwork_request_ConfigManagerJni
 * Method:    AddCallback
 * Signature: (Lcom/qpidnetwork/request/OnConfigManagerCallback;)V
 */
JNIEXPORT void JNICALL Java_com_qpidnetwork_request_ConfigManagerJni_AddCallback
  (JNIEnv *env, jclass, jobject callback) {
	if( callback != NULL ) {
		gConfigCallbackMapLock.lock();
		gConfigCallbackMap.insert(ConfigCallbackMap::value_type(env->NewGlobalRef(callback), true));
		gConfigCallbackMapLock.unlock();
	}
}

/*
 * Class:     com_qpidnetwork_request_ConfigManagerJni
 * Method:    RemoveCallback
 * Signature: (Lcom/qpidnetwork/request/OnConfigManagerCallback;)V
 */
JNIEXPORT void JNICALL Java_com_qpidnetwork_request_ConfigManagerJni_RemoveCallback
  (JNIEnv *env, jclass, jobject callback) {
	gConfigCallbackMapLock.lock();
	ConfigCallbackMap::iterator itr = gConfigCallbackMap.find(callback);
	if( itr != gConfigCallbackMap.end() ) {
		gConfigCallbackMap.erase(itr);

		if( callback != NULL ) {
			env->DeleteGlobalRef(itr->first);
		}
	}
	gConfigCallbackMapLock.unlock();

}

/*
 * Class:     com_qpidnetwork_request_ConfigManagerJni
 * Method:    Sync
 * Signature: ()V
 */
JNIEXPORT void JNICALL Java_com_qpidnetwork_request_ConfigManagerJni_Sync
  (JNIEnv *env, jclass) {
	ConfigManager::GetInstance().RemoveCallback(&gRequestConfigCallback);
	ConfigManager::GetInstance().AddCallback(&gRequestConfigCallback);
	ConfigManager::GetInstance().Sync();
}
