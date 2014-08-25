LOCAL_PATH := $(call my-dir)

include $(CLEAR_VARS)
include /home/haipq/adt/OpenCV-2.4.9-android-sdk/sdk/native/jni/OpenCV.mk

LOCAL_LDLIBS := -llog -ljnigraphics

LOCAL_MODULE    := grayscale
LOCAL_SRC_FILES := grayscale.c 

include $(BUILD_SHARED_LIBRARY)