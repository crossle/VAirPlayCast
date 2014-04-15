LOCAL_PATH := $(call my-dir)

include $(CLEAR_VARS)
LOCAL_MODULE := libmicrohttpd
LOCAL_SRC_FILES := libmicrohttpd.a
include $(PREBUILT_STATIC_LIBRARY)

include $(CLEAR_VARS)
LOCAL_WHOLE_STATIC_LIBRARIES += libmicrohttpd
LOCAL_LDLIBS    += -llog
LOCAL_SRC_FILES := vcast_jni.c
LOCAL_MODULE    := vcast
include $(BUILD_SHARED_LIBRARY)

