/*
 * RequestJni_GobalFunc.h
 *
 *  Created on: 2015-3-4
 *      Author: Max.Chiu
 *      Email: Kingsleyyau@gmail.com
 */

#ifndef REQUESTJNI_GOBALFUNC_H_
#define REQUESTJNI_GOBALFUNC_H_

#include <jni.h>

#include <string>
#include <list>
#include <map>
using namespace std;

#include <common/KSafeMap.h>

#include <httpclient/HttpRequestManager.h>
#include <httpclient/HttpRequestHostManager.h>

#include "CallbackItemAndroidDef.h"
#include "LoginManager.h"
#include "ConfigManager.h"
#include "ITask.h"

extern JavaVM* gJavaVM;

extern HttpRequestHostManager gHttpRequestHostManager;
extern HttpRequestManager gHttpRequestManager;

/* java callback object */
typedef KSafeMap<long, jobject> CallbackMap;
extern CallbackMap gCallbackMap;

/* java request map */
typedef map<long, bool> RequestMap;
extern RequestMap gRequestMap;
extern KMutex gRequestMapMutex;

extern jobject requestFinishCallback;

/* java data item */
typedef map<string, jobject> JavaItemMap;
extern JavaItemMap gJavaItemMap;

string JString2String(JNIEnv* env, jstring str);
void InitEnumHelper(JNIEnv *env, const char *path, jobject *objptr);
void InitClassHelper(JNIEnv *env, const char *path, jobject *objptr);
jclass GetJClass(JNIEnv* env, const char* classPath);

bool GetEnv(JNIEnv** env, bool* isAttachThread);
bool ReleaseEnv(bool isAttachThread);

class RequestFinishCallback;
extern RequestFinishCallback gRequestFinishCallback;

#endif /* REQUESTJNI_GOBALFUNC_H_ */
