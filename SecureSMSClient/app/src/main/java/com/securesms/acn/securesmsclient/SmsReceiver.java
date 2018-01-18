package com.securesms.acn.securesmsclient;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.text.SimpleDateFormat;
import java.util.Locale;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.telephony.SmsMessage;
import android.util.Log;
import android.widget.Toast;

import static com.securesms.acn.securesmsclient.AppData.*;

public class SmsReceiver extends BroadcastReceiver {
    Thread clientThread = null;

    @SuppressWarnings("deprecation")
    @Override
    public void onReceive(Context context, Intent intent) {
        Bundle bundle = intent.getExtras();
        String name, phoneNumber, received = null, receivedTime, sentTime;

        if (bundle != null) {
            //---retrieve the SMS message received---
            Object[] pdus = (Object[]) bundle.get("pdus");
            if (pdus == null)
                return;
            SmsMessage[] messages = new SmsMessage[pdus.length];
            for (int i = 0; i < pdus.length; i++)
                messages[i] = SmsMessage.createFromPdu((byte[]) pdus[i]);

            SmsMessage sms = messages[0];
            if (sms == null)
                return;
            try {
                phoneNumber = sms.getOriginatingAddress();
            } catch (NullPointerException e) {
                return;
            }
            SimpleDateFormat format = new SimpleDateFormat(timingFormat, Locale.getDefault());
            sentTime = format.format(sms.getTimestampMillis());
            receivedTime = format.format(System.currentTimeMillis());

            try {
                StringBuilder bodyText = new StringBuilder();
                for (SmsMessage m : messages) {
                    bodyText.append(m.getMessageBody());
                }
                received = bodyText.toString();
            } catch (Exception e) {
                e.printStackTrace();
            }

            if (received == null)
                return;

            if (isOrderedBroadcast())
                abortBroadcast();

            name = getContactName(phoneNumber, context);
            if (name == null || name.length() <= 0)
                name = phoneNumber;

            SecureMessage secureMessage = new SecureMessage(received, name, phoneNumber, receivedTime, sentTime);

            clientThread = new Thread(new ClientThread(secureMessage));
            clientThread.start();

            if (BuildConfig.DEBUG && DEBUG) {
                Log.i(TAG, "SMS received from " + name + "(" + phoneNumber + ")");
                Log.i(TAG, received);
                Toast.makeText(context, received, Toast.LENGTH_LONG).show();
            }
        }
    }

    public static String getContactName(final String number, Context context) {
        String phoneNumber = number.replace("+", "00");
        Uri uri;
        String[] projection;

        //if (Build.VERSION.SDK_INT >= 5)
        {
            uri = Uri.parse("content://com.android.contacts/phone_lookup");
            projection = new String[]{"display_name"};
        }
        /*else
        {
			uri = Uri.parse("content://contacts/phones/filter");
			projection = new String[] { "name" }; 
		}*/

        uri = Uri.withAppendedPath(uri, Uri.encode(phoneNumber));
        Cursor cursor = context.getContentResolver().query(uri, projection, null, null, null);

        Log.i(TAG, "Contact was " + (cursor == null ? "" : "not ") + "found");
        if (cursor == null)
            return null;

        String contactName = "";


        if (cursor.moveToFirst())
            contactName = cursor.getString(0);

        cursor.close();

        return contactName;
    }

    public void sendToServer(final Socket socket, final SecureMessage output, final Server server) {
        if (socket != null) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        Crypto crypto = new Crypto(server.getKey(), server.getNonceCounter());
                        DataOutputStream out = new DataOutputStream(socket.getOutputStream());
                        out.writeUTF("SMS BEGIN");
                        out.writeUTF(Crypto.encodeBase64(crypto.getNonce()));
                        out.writeUTF(crypto.encryptAndEncode(output.getSender()));
                        out.writeUTF(crypto.encryptAndEncode(output.getNumber()));
                        out.writeUTF(crypto.encryptAndEncode(output.getMessage()));
                        out.writeUTF(crypto.encryptAndEncode(output.getReceivedTime()));
                        out.writeUTF(crypto.encryptAndEncode(output.getSentTime()));
                        //out.writeUTF("SMS END");
                        out.close();
                        socket.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }).start();
        }
    }

    class ClientThread implements Runnable {
        private final SecureMessage output;

        ClientThread(final SecureMessage output) {
            this.output = output;
        }

        @Override
        public void run() {
            for (Server server : serverList) {
                if (!server.isEnabled())
                    continue;
                try {

                    Socket socket = new Socket(server.getIp(), server.getPort());
                    sendToServer(socket, output, server);

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }
}