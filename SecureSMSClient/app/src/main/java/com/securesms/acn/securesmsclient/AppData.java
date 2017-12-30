package com.securesms.acn.securesmsclient;

import java.net.InetAddress;
import java.net.Socket;
import java.util.List;

import android.app.Application;
import android.content.SharedPreferences;

public class AppData extends Application {
    public static String timingFormat = "HH:mm, dd/MM/yyyy";
    public static final int ASK_MULTIPLE_PERMISSION_REQUEST_CODE = 123;
    public static final int REQUEST_PERMISSION_SETTING = 100;
    public static List<Server> serverList;
    public static boolean editServer;
    static final String ZING = "com.google.zxing.client.android";
    static final String ZING_SCAN = ZING + ".SCAN";
    public static SharedPreferences preferences = null;
    public static String prefKeyServerList = "ServerList";
    public static String prefKeyEditServer = "EditServer";
    public static String TAG = "SecureSMSClient";
}
