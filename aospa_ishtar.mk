#
# Copyright (C) 2023 The LineageOS Project
#
# SPDX-License-Identifier: Apache-2.0
#

# Inherit from those products. Most specific first.
$(call inherit-product, $(SRC_TARGET_DIR)/product/core_64_bit.mk)
$(call inherit-product, $(SRC_TARGET_DIR)/product/full_base_telephony.mk)

# Inherit some common AOSPA stuff.
$(call inherit-product, vendor/aospa/target/product/aospa-target.mk)

# Inherit from ishtar device
$(call inherit-product, device/xiaomi/ishtar/device.mk)

PRODUCT_DEVICE := ishtar
PRODUCT_NAME := aospa_ishtar
PRODUCT_BRAND := Xiaomi
PRODUCT_MODEL := 2304FPN6DG
PRODUCT_MANUFACTURER := xiaomi

PRODUCT_GMS_CLIENTID_BASE := android-xiaomi

PRODUCT_BUILD_PROP_OVERRIDES += \
    PRIVATE_BUILD_DESC="ishtar_global-user 13 TKQ1.221114.001 V14.0.2.0.TMAMIXM release-keys"

BUILD_FINGERPRINT := Xiaomi/ishtar_global/ishtar:13/TKQ1.221114.001/V14.0.2.0.TMAMIXM:user/release-keys

# Boot Animation
TARGET_BOOT_ANIMATION_RES := 1080
