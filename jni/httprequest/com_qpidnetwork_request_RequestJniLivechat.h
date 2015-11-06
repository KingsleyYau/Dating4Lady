/* DO NOT EDIT THIS FILE - it is machine generated */
#include <jni.h>
/* Header for class com_qpidnetwork_request_RequestJniLivechat */

#ifndef _Included_com_qpidnetwork_request_RequestJniLivechat
#define _Included_com_qpidnetwork_request_RequestJniLivechat
#ifdef __cplusplus
extern "C" {
#endif
/*
 * Class:     com_qpidnetwork_request_RequestJniLivechat
 * Method:    GetMyCustomTemplate
 * Signature: (Lcom/qpidnetwork/request/OnLCCustomTemplateCallback;)J
 */
JNIEXPORT jlong JNICALL Java_com_qpidnetwork_request_RequestJniLivechat_GetMyCustomTemplate
  (JNIEnv *, jclass, jobject);

/*
 * Class:     com_qpidnetwork_request_RequestJniLivechat
 * Method:    GetSystemTemplate
 * Signature: (Lcom/qpidnetwork/request/OnLCSystemTemplateCallback;)J
 */
JNIEXPORT jlong JNICALL Java_com_qpidnetwork_request_RequestJniLivechat_GetSystemTemplate
  (JNIEnv *, jclass, jobject);

/*
 * Class:     com_qpidnetwork_request_RequestJniLivechat
 * Method:    AddCustomTemplate
 * Signature: (Ljava/lang/String;Lcom/qpidnetwork/request/OnRequestCallback;)J
 */
JNIEXPORT jlong JNICALL Java_com_qpidnetwork_request_RequestJniLivechat_AddCustomTemplate
  (JNIEnv *, jclass, jstring, jobject);

/*
 * Class:     com_qpidnetwork_request_RequestJniLivechat
 * Method:    DelCustomTemplates
 * Signature: (Ljava/lang/String;Lcom/qpidnetwork/request/OnRequestCallback;)J
 */
JNIEXPORT jlong JNICALL Java_com_qpidnetwork_request_RequestJniLivechat_DelCustomTemplates
  (JNIEnv *, jclass, jstring, jobject);

#ifdef __cplusplus
}
#endif
#endif