/*
 * Copyright (C) 2023 Paranoid Android
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package com.xiaomi.settings.touch;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.ContentObserver;
import android.net.Uri;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.ServiceManager;
import android.os.SystemProperties;
import android.os.UserHandle;
import android.provider.Settings.Secure;
import android.util.Log;

import vendor.aospa.xiaomitouch.IXiaomiTouch;

public class AlwaysOnFingerprintService extends Service {

    private static final String TAG = "XiaomiPartsAlwaysOnFingerprintService";
    private static final boolean DEBUG = true;

    private static final String SECURE_KEY_TAP = "doze_tap_gesture";
    private static final String SECURE_KEY_UDFPS = "screen_off_udfps_enabled";
    private static final String ITOUCHFEATUREL_AIDL_INTERFACE =
            "vendor.aospa.xiaomitouch.IXiaomiTouch/default";

    private boolean mIsAofEnabled;

    private ContentResolver mContentResolver;
    private ScreenStateReceiver mScreenStateReceiver;
    private SettingsObserver mSettingsObserver;
    private IXiaomiTouch mXiaomiTouch;
    private final Handler mHandler = new Handler();

    @Override
    public void onCreate() {
        super.onCreate();
        if (DEBUG) Log.d(TAG, "Creating service");
        mContentResolver = getContentResolver();
        mScreenStateReceiver = new ScreenStateReceiver();
        mSettingsObserver = new SettingsObserver(mHandler);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startid) {
        if (DEBUG) Log.d(TAG, "Starting service");
        mScreenStateReceiver.register();
        mSettingsObserver.register();
        mSettingsObserver.update();
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        if (DEBUG) Log.d(TAG, "Destroying service");
        super.onDestroy();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private void setXiaomiTouchMode(int mode, int value) {
        final IXiaomiTouch touchFeature = getXiaomiTouch();
        if (touchFeature == null) {
            Log.e(TAG, "setXiaomiTouchMode: touchFeature is null!");
            return;
        }
        if (DEBUG) Log.d(TAG, "setXiaomiTouchMode: mode: " + mode + ", value: " + value);
        try {
            touchFeature.setModeValue(mode, value);
        } catch (Exception e) {
            Log.e(TAG, "setXiaomiTouchMode failed!", e);
        }
    }

    private IXiaomiTouch getXiaomiTouch() {
        if (mXiaomiTouch == null) {
            if (DEBUG) Log.d(TAG, "getXiaomiTouch: mXiaomiTouch=null");
            try {
                IBinder binder = ServiceManager.getService(ITOUCHFEATUREL_AIDL_INTERFACE);
                if (binder == null)
                    throw new Exception("daemon binder null");
                mXiaomiTouch = IXiaomiTouch.Stub.asInterface(binder);
                if (mXiaomiTouch == null)
                    throw new Exception("AIDL daemon interface null");
            } catch (Exception e) {
                Log.e(TAG, "getXiaomiTouch failed!", e);
            }
        }
        return mXiaomiTouch;
    }

    private final class ScreenStateReceiver extends BroadcastReceiver {

        public void register() {
            if (DEBUG) Log.d(TAG, "CustomBroadcastReceiver: register");
            if (DEBUG) Log.d(TAG, "ScreenStateReceiver: register");
            registerReceiver(mScreenStateReceiver, new IntentFilter(Intent.ACTION_SCREEN_ON));
            registerReceiver(mScreenStateReceiver, new IntentFilter(Intent.ACTION_SCREEN_OFF));
        }

        @Override
        public void onReceive(Context context, Intent intent) {
            switch (intent.getAction()) {
                case Intent.ACTION_SCREEN_ON:
                    if (DEBUG) Log.d(TAG, "Received ACTION_SCREEN_ON");
                    setXiaomiTouchMode(10, 0);
                    setXiaomiTouchMode(16, 0);
                    break;
                case Intent.ACTION_SCREEN_OFF:
                    if (DEBUG) Log.d(TAG, "Received ACTION_SCREEN_OFF");
                    setXiaomiTouchMode(10, mIsAofEnabled ? 1 : 0);
                    setXiaomiTouchMode(16, mIsAofEnabled ? 1 : 0);
                    break;
            }
        }
    }

    private final class SettingsObserver extends ContentObserver {
        SettingsObserver(Handler handler) {
            super(handler);
        }

        public void register() {
            if (DEBUG) Log.d(TAG, "SettingsObserver: register");
            mContentResolver.registerContentObserver(Secure.getUriFor(SECURE_KEY_TAP), false, this);
            mContentResolver.registerContentObserver(Secure.getUriFor(SECURE_KEY_UDFPS), false, this);
        }

        void update() {
            boolean st2w = Secure.getInt(mContentResolver, SECURE_KEY_TAP, 0) != 0;
            boolean udfps = Secure.getInt(mContentResolver, SECURE_KEY_UDFPS, 0) != 0;
            if (DEBUG) Log.d(TAG, "SettingsObserver: SECURE_KEY_TAP: " + st2w + ", SECURE_KEY_UDFPS: " + udfps);
            mIsAofEnabled = st2w || udfps;
        }

        @Override
        public void onChange(boolean selfChange, Uri uri) {
            if (DEBUG) Log.d(TAG, "SettingsObserver: onChange: " + uri.toString());
            if (uri.equals(Secure.getUriFor(SECURE_KEY_TAP))
                    || uri.equals(Secure.getUriFor(SECURE_KEY_UDFPS))) {
                update();
            }
        }
    }
}
