LOCAL_PATH := $(call my-dir)
include $(CLEAR_VARS)

LOCAL_MODULE := BBBAndroidHAL
LOCAL_LDFLAGS := -Wl,--build-id
LOCAL_LDLIBS := \
	-llog \

LOCAL_SRC_FILES := \
	/Users/matildalin/StudioProjects/spi/spiapp/app/src/main/jni/Android.mk \
	/Users/matildalin/StudioProjects/spi/spiapp/app/src/main/jni/Application.mk \
	/Users/matildalin/StudioProjects/spi/spiapp/app/src/main/jni/jni_wrapper.c \
	/Users/matildalin/StudioProjects/spi/spiapp/app/src/main/jni/main.c \
	/Users/matildalin/StudioProjects/spi/spiapp/app/src/main/jni/spi.c \

LOCAL_C_INCLUDES += /Users/matildalin/StudioProjects/spi/spiapp/app/src/main/jni
LOCAL_C_INCLUDES += /Users/matildalin/StudioProjects/spi/spiapp/app/src/release/jni

include $(BUILD_SHARED_LIBRARY)
