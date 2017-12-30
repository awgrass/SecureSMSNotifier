package com.securesms.acn.securesmsclient;

import java.util.List;

import android.content.SharedPreferences;

class AppData {
    private AppData() {
    }  // Prevents instantiation

    static String timingFormat = "HH:mm, dd/MM/yyyy";
    static List<Server> serverList;
    static SharedPreferences preferences = null;
    static boolean editServer = false;
    static boolean DEBUG = false;
    static final int ASK_MULTIPLE_PERMISSION_REQUEST_CODE = 123;
    static final int REQUEST_PERMISSION_SETTING = 100;
    static final String ZING = "com.google.zxing.client.android";
    static final String ZING_SCAN = ZING + ".SCAN";
    static final String prefKeyServerList = "ServerList";
    static final String prefKeyEditServer = "EditServer";
    static final String prefKeyDebug = "IsDebug";
    static final String TAG = "SecureSMSClient";
}
