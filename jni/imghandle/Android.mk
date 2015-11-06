# Copyright (C) 2014 The QpidNetwork Project
# ImageHandle Module Makefile
#
# Created on: 2015/05/11
# Author: Samson Fan
#

LOCAL_PATH := $(call my-dir)

include $(CLEAR_VARS)

LOCAL_MODULE    := imghandle-interface

LOCAL_MODULE_FILENAME := libimghandle-interface

LOCAL_C_INCLUDES += $(LIBRARY_PATH)

LOCAL_LDLIBS += -llog
LOCAL_LDLIBS += -lz

LOCAL_STATIC_LIBRARIES += common
LOCAL_STATIC_LIBRARIES += imghandle

LOCAL_CPPFLAGS  := -std=c++11

REAL_PATH := $(realpath $(LOCAL_PATH))
LOCAL_SRC_FILES := $(call all-cpp-files-under, $(REAL_PATH))

include $(BUILD_SHARED_LIBRARY)