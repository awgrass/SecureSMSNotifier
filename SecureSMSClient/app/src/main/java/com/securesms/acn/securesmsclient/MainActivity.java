package com.securesms.acn.securesmsclient;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.provider.Settings;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.telephony.PhoneNumberUtils;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Locale;
import java.util.Random;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import static com.securesms.acn.securesmsclient.AppData.*;
import static java.util.Arrays.asList;

public class MainActivity extends AppCompatActivity {

    //region Private variables
    ListViewAdapter listViewAdapter;
    ListView listView;
    FloatingActionButton fab;
    MenuItem fakeSMSItem;
    //endregion

    //region Override functions
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        loadPreferences(getApplicationContext());

        editServer = preferences.getBoolean(prefKeyEditServer, editServer);
        DEBUG = preferences.getBoolean(prefKeyDebug, DEBUG);

        loadServerList();
        listView = findViewById(R.id.listView);
        listView.setEmptyView(findViewById(R.id.listViewEmptyItem));
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                final Server server = serverList.get(position);
                String message = String.format(getResources().getString(R.string.server_popup_msg),
                        server.getName(), server.getIp(), server.getPort(), server.getTime());
                showAlert(MainActivity.this,
                        getResources().getString(R.string.details), message, true,
                        asList(server.isEnabled() ? getResources().getString(R.string.disable) :
                                        getResources().getString(R.string.enable),
                                getResources().getString(R.string.rename),
                                getResources().getString(R.string.delete)),
                        asList(new DialogInterface.OnClickListener() {
                                   @Override
                                   public void onClick(DialogInterface dialog, int which) {
                                       setServerEnabled(MainActivity.this, server, !server.isEnabled());
                                       listViewAdapter.notifyDataSetChanged();
                                   }
                               },
                                new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        renameServer(MainActivity.this, server, listViewAdapter);
                                    }
                                },
                                new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        deleteServer(MainActivity.this, server, listViewAdapter);
                                    }
                                }));
            }
        });
        listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                toggleEditServer();
                return true;
            }
        });

        listViewAdapter = new ListViewAdapter(this, R.layout.list_item, serverList);
        listView.setAdapter(listViewAdapter);

        fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                addNewServer();
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        checkForPermission();
    }

    @Override
    public boolean onCreateOptionsMenu(final Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        if (menu != null) {
            fakeSMSItem = menu.findItem(R.id.action_fakeSMS);
            fakeSMSItem.setVisible(DEBUG);
            setEditServerMenuItem(menu);
        }
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onMenuOpened(int featureId, final Menu menu) {
        if (menu != null)
            setEditServerMenuItem(menu);
        return super.onMenuOpened(featureId, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_add) {
            addNewServer();
            return true;
        } else if (id == R.id.action_about) {
            TextView view = new TextView(this);
            view.setText(R.string.action_about_msg);
            int paddingSmall = (int) getResources().getDimension(R.dimen.alert_padding);
            int paddingBig = paddingSmall * 2;
            ImageView image = new ImageView(this);
            image.setImageDrawable(getResources().getDrawable(R.mipmap.ic_launcher));
            image.setLayoutParams(new FrameLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.MATCH_PARENT));
            image.setPadding(0, 0, paddingBig, 0);
            LinearLayout layout = new LinearLayout(this);
            layout.setOrientation(LinearLayout.HORIZONTAL);
            layout.setPadding(0, paddingSmall, 0, paddingBig);
            layout.addView(image);
            layout.addView(view);
            if (BuildConfig.DEBUG) {
                layout.setOnClickListener(new View.OnClickListener() {
                    int aboutClicked = 0;

                    @Override
                    public void onClick(View v) {
                        aboutClicked++;
                        if (aboutClicked >= 7) {
                            aboutClicked = 0;
                            DEBUG = !DEBUG;
                            fakeSMSItem. setVisible(DEBUG);
                            SharedPreferences.Editor prefsEditor = preferences.edit();
                            prefsEditor.putBoolean(prefKeyDebug, DEBUG);
                            prefsEditor.commit();
                        }
                    }
                });
            }
            showAlert(this, null, null, true, layout);
            return true;
        } else if (id == R.id.action_edit_server) {
            toggleEditServer();
            return true;
        } else if (id == R.id.action_fakeSMS) {
            String number = getRandomPhoneNumber(getApplicationContext());
            if (number == null)
                number = "00123456789101";
            number = number.replace("+", "00");
            number = number.replace(" ", "");
            createFakeSms(getApplicationContext(), number);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 0) {
            if (resultCode == RESULT_OK) {
                String contents = data.getStringExtra("SCAN_RESULT");
                String format = data.getStringExtra("SCAN_RESULT_FORMAT");

                final Server server = new Server();
                server.setEnabled(true);
                server.setTime(new SimpleDateFormat(timingFormat, Locale.getDefault()).format(new Date()));

                int i = 0;
                Random random = new Random();
                for (String parameter : contents.split("\\|")) {
                    switch (i) {
                        case 0:
                            server.setName(parameter);
                            break;
                        case 1:
                            server.setKey(parameter);
                            break;
                        case 2:
                            server.setIp(parameter);
                            break;
                        case 3:
                            try {
                                server.setPort(Integer.parseInt(parameter));
                            } catch (NumberFormatException e) {
                                server.setPort(random.nextInt(65535));
                            }
                            break;
                        case 4:
                            server.setType(parameter.equals("1") ? Server.ServerType.NOTEBOOK : Server.ServerType.PC);
                            break;
                    }
                    i++;
                }
                addServer(server);
            }
        }
//      super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onRequestPermissionsResult(final int requestCode, final String permissions[], final int[] grantResults) {
        boolean ok = true;
        switch (requestCode) {
            case ASK_MULTIPLE_PERMISSION_REQUEST_CODE: {
                if (grantResults.length > 0) {
                    for (int grantResult : grantResults)
                        if (grantResult != PackageManager.PERMISSION_GRANTED)
                            ok = false;
                }
                if (!ok) {
                    showAlert(this, getResources().getString(R.string.permission_title),
                            getResources().getString(R.string.permission_msg),
                            false,
                            asList(getResources().getString(R.string.yes),
                                    getResources().getString(R.string.no),
                                    null),
                            asList(new DialogInterface.OnClickListener() {
                                       public void onClick(DialogInterface dialogInterface, int i) {
                                           boolean ok = true;
                                           for (int j = 0; j < permissions.length; j++) {
                                               String permission = permissions[j];
                                               int grantResult = grantResults[j];
                                               if (grantResult != PackageManager.PERMISSION_GRANTED && !ActivityCompat.shouldShowRequestPermissionRationale(MainActivity.this, permission)) {
                                                   showStoragePermissionRationale();
                                                   ok = false;
                                                   break;
                                               }
                                           }
                                           if (ok)
                                               checkForPermission();
                                       }
                                   },
                                    new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialogInterface, int i) {
                                            System.exit(0);
                                        }
                                    },
                                    new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialogInterface, int i) {
                                        }
                                    }));
                }
                return;
            }
        }
    }
    //endregion

    //region Functions
    private void setEditServerMenuItem(Menu menu) {
        MenuItem editServerItem = menu.findItem(R.id.action_edit_server);
        if (editServer)
            editServerItem.setTitle(getResources().getString(R.string.action_done_edit_server));
        else
            editServerItem.setTitle(getResources().getString(R.string.action_edit_server));
    }

    public boolean checkForPermission() {
        final String[] permissions = new String[]{
                Manifest.permission.ACCESS_NETWORK_STATE,
                Manifest.permission.ACCESS_WIFI_STATE,
                Manifest.permission.INTERNET,
                Manifest.permission.READ_CONTACTS,
                Manifest.permission.READ_SMS,
                Manifest.permission.RECEIVE_SMS,
                Manifest.permission.SEND_SMS,
                Manifest.permission.VIBRATE};

        final List<String> permissionsNeeded = new ArrayList<>();

        for (String perm : permissions) {
            if (ContextCompat.checkSelfPermission(this, perm) != PackageManager.PERMISSION_GRANTED) {
                permissionsNeeded.add(perm);
            }
        }

        if (permissionsNeeded.size() > 0) {
            ActivityCompat.requestPermissions(this,
                    permissionsNeeded.toArray(new String[permissionsNeeded.size()]),
                    ASK_MULTIPLE_PERMISSION_REQUEST_CODE);
            return false;
        }
        return true;
    }

    private void showStoragePermissionRationale() {
        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        Uri uri = Uri.fromParts("package", getPackageName(), null);
        intent.setData(uri);
        startActivityForResult(intent, REQUEST_PERMISSION_SETTING);
    }

    private void toggleEditServer() {
        editServer = !editServer;
        SharedPreferences.Editor prefsEditor = preferences.edit();
        prefsEditor.putBoolean(prefKeyEditServer, editServer);
        prefsEditor.commit();
        if (listViewAdapter != null)
            listViewAdapter.notifyDataSetChanged();
    }

    private void addNewServer() {
        try {
            Intent intent = new Intent(ZING_SCAN);
            intent.putExtra("SCAN_MODE", "QR_CODE_MODE");
            startActivityForResult(intent, 0);
        } catch (ActivityNotFoundException anfe) {
            showAlert(this, getResources().getString(R.string.scanner_download),
                    getResources().getString(R.string.scanner_download_msg),
                    true,
                    asList(getResources().getString(R.string.yes),
                            getResources().getString(R.string.no),
                            null),
                    asList(new DialogInterface.OnClickListener() {
                               @Override
                               public void onClick(DialogInterface dialogInterface, int i) {
                                   Uri uri = Uri.parse("market://details?id=" + ZING);
                                   Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                                   try {
                                       startActivity(intent);
                                   } catch (ActivityNotFoundException e) {
                                   }
                               }
                           },
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                }
                            },
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                }
                            }));
        }
    }

    private void addServer(Server server) {
        if (fab != null)
            Snackbar.make(fab, getResources().getString(R.string.fab_add_msg), Snackbar.LENGTH_LONG)
                    .setAction("Add", null).show();
        if (serverList == null)
            loadServerList();
        serverList.add(server);
        saveServerList(getApplicationContext());
        if (listViewAdapter != null)
            listViewAdapter.notifyDataSetChanged();
    }

    private void loadServerList() {
        serverList = null;
        if (preferences == null)
            loadPreferences(getApplicationContext());
        Gson gson = new Gson();
        String json = preferences.getString(prefKeyServerList, null);
        if (json != null) {
            Type type = new TypeToken<List<Server>>() {
            }.getType();
            serverList = gson.fromJson(json, type);
        }
        if (serverList == null)
            serverList = new ArrayList<Server>();
    }

    private static void saveServerList(Context context) {
        if (preferences == null)
            loadPreferences(context);
        SharedPreferences.Editor prefsEditor = preferences.edit();
        Gson gson = new Gson();
        String json = gson.toJson(serverList);
        prefsEditor.putString(prefKeyServerList, json);
        prefsEditor.commit();
    }

    private static void showAlert(Activity activity, String title, String message, boolean cancelable, View view,
                                  List<String> buttonTexts, List<DialogInterface.OnClickListener> buttonListeners) {
        AlertDialog.Builder alert = new AlertDialog.Builder(activity);
        alert.setTitle(title);
        //alert.setMessage(message);
        alert.setCancelable(cancelable);
        if (buttonTexts != null && buttonTexts.size() == 3) {
            if (buttonTexts.get(0) != null)
                alert.setPositiveButton(buttonTexts.get(0), buttonListeners.get(0));
            if (buttonTexts.get(1) != null)
                alert.setNegativeButton(buttonTexts.get(1), buttonListeners.get(1));
            if (buttonTexts.get(2) != null)
                alert.setNeutralButton(buttonTexts.get(2), buttonListeners.get(2));
        }

        LinearLayout linear = new LinearLayout(activity);
        linear.setOrientation(LinearLayout.VERTICAL);
        FrameLayout layout = new FrameLayout(activity);
        if (message != null && message.length() > 0) {
            Log.i(TAG, "MESSAGE\n\nMESSAGE\n\nMESSAGE\n");
            FrameLayout frameView = new FrameLayout(activity);
            TextView messageView = new TextView(activity);
            messageView.setText(message);
            frameView.addView(messageView);
            linear.addView(frameView);
        }
        if (view != null) {
            layout.addView(view);
            linear.addView(layout);
        }
        if (linear.getChildCount() > 0) {
            int paddingSmall = (int) activity.getResources().getDimension(R.dimen.alert_padding);
            int paddingBig = paddingSmall*2;
            linear.setPadding(paddingBig, paddingSmall, paddingBig, 0);
            alert.setView(linear);
        }
        AlertDialog dialog = alert.create();
        dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
        dialog.show();
        return;
    }

    private static void showAlert(Activity activity, String title, String message, boolean cancelable,
                                  List<String> buttonTexts, List<DialogInterface.OnClickListener> buttonListeners) {
        showAlert(activity, title, message, cancelable, null, buttonTexts, buttonListeners);
    }

    private static void showAlert(Activity activity, String title, String message, boolean cancelable) {
        showAlert(activity, title, message, cancelable, null, null, null);
    }

    private static void showAlert(Activity activity, String title, String message, boolean cancelable, View view) {
        showAlert(activity, title, message, cancelable, view, null, null);
    }

    private static void loadPreferences(Context context) {
        preferences = context.getSharedPreferences("com.securesms.acn.securesmsclient.prefs", Context.MODE_PRIVATE);
    }

    public static void deleteServer(final Activity activity, final Server server, final ListViewAdapter listViewAdapter) {
        showAlert(activity, activity.getResources().getString(R.string.delete),
                String.format(activity.getResources().getString(R.string.server_delete_msg), server.getName()),
                true,
                asList(activity.getResources().getString(R.string.yes), null,
                        activity.getResources().getString(R.string.no)),
                asList(new DialogInterface.OnClickListener() {
                           @Override
                           public void onClick(DialogInterface dialog, int which) {
                               if (activity instanceof MainActivity && ((MainActivity) activity).fab != null)
                                   Snackbar.make(((MainActivity) activity).fab,
                                           activity.getResources().getString(R.string.fab_delete_msg),
                                           Snackbar.LENGTH_LONG).setAction("Add", null).show();
                               serverList.remove(server);
                               saveServerList(activity.getApplicationContext());
                               if (listViewAdapter != null)
                                   listViewAdapter.notifyDataSetChanged();
                           }
                       },
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                            }
                        },
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                            }
                        }));
    }

    public static void renameServer(final Activity activity, final Server server, final ListViewAdapter listViewAdapter) {
        final EditText edittext = new EditText(activity);
        edittext.setText(server.getName());
        edittext.requestFocus();
        edittext.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_CAP_SENTENCES);
        edittext.setSelection(0, server.getName().length());
        showAlert(activity, activity.getResources().getString(R.string.rename),
                String.format(activity.getResources().getString(R.string.server_rename_msg), server.getName()),
                true, edittext,
                asList(activity.getResources().getString(R.string.save), null,
                        activity.getResources().getString(R.string.cancel)),
                asList(new DialogInterface.OnClickListener() {
                           @Override
                           public void onClick(DialogInterface dialog, int which) {
                               serverList.get(serverList.indexOf(server)).setName(edittext.getText().toString());
                               saveServerList(activity.getApplicationContext());
                               if (listViewAdapter != null)
                                   listViewAdapter.notifyDataSetChanged();
                           }
                       },
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                            }
                        },
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                            }
                        }));
    }

    public static void setServerEnabled(Activity activity, Server server, boolean enabled) {
        server.setEnabled(enabled);
        saveServerList(activity.getApplicationContext());
    }
    //endregion


    //region DEBUG
    // #############################################################################################
    // TODO: REMOVE - DEBUG ONLY
    // #############################################################################################

    private static void createFakeSms(Context context, String sender) {
        String message = "";
        Random random = new Random();
        int sentences = random.nextInt(4) + 1;
        for (int i = 0; i < sentences; i++) {
            for (int j = 0; j < random.nextInt(19) + 1; j++)
                message += ((j == 0) ? "" : " ") + randomWord(random, j == 0 || random.nextInt(2) == 0);
            message += ". " + ((random.nextInt(1) == 0 && i+1 < sentences) ? "\n" : "");
        }
        createFakeSms(context, sender, message);
    }

    private static void createFakeSms(Context context, String sender, String body) {
        byte[] pdu = null;
        byte[] scBytes = PhoneNumberUtils
                .networkPortionToCalledPartyBCD("0000000000");
        byte[] senderBytes = PhoneNumberUtils
                .networkPortionToCalledPartyBCD(sender);
        int lsmcs = scBytes.length;
        byte[] dateBytes = new byte[7];
        Calendar calendar = new GregorianCalendar();
        dateBytes[0] = reverseByte((byte) (calendar.get(Calendar.YEAR)));
        dateBytes[1] = reverseByte((byte) (calendar.get(Calendar.MONTH) + 1));
        dateBytes[2] = reverseByte((byte) (calendar.get(Calendar.DAY_OF_MONTH)));
        dateBytes[3] = reverseByte((byte) (calendar.get(Calendar.HOUR_OF_DAY)));
        dateBytes[4] = reverseByte((byte) (calendar.get(Calendar.MINUTE)));
        dateBytes[5] = reverseByte((byte) (calendar.get(Calendar.SECOND)));
        dateBytes[6] = reverseByte((byte) ((calendar.get(Calendar.ZONE_OFFSET) + calendar
                .get(Calendar.DST_OFFSET)) / (60 * 1000 * 15)));
        try {
            ByteArrayOutputStream bo = new ByteArrayOutputStream();
            bo.write(lsmcs);
            bo.write(scBytes);
            bo.write(0x04);
            bo.write((byte) sender.length());
            bo.write(senderBytes);
            bo.write(0x00);
            bo.write(0x00); // encoding: 0 for default 7bit
            bo.write(dateBytes);
            try {
                String sReflectedClassName = "com.android.internal.telephony.GsmAlphabet";
                Class cReflectedNFCExtras = Class.forName(sReflectedClassName);
                Method stringToGsm7BitPacked = cReflectedNFCExtras.getMethod(
                        "stringToGsm7BitPacked", new Class[]{String.class});
                stringToGsm7BitPacked.setAccessible(true);
                byte[] bodybytes = (byte[]) stringToGsm7BitPacked.invoke(null,
                        body);
                bo.write(bodybytes);
            } catch (Exception e) {
                e.printStackTrace();
            }

            pdu = bo.toByteArray();
        } catch (IOException e) {
            e.printStackTrace();
        }

        Intent intent = new Intent(context, SmsReceiver.class);
        intent.putExtra("pdus", new Object[]{pdu});
        intent.putExtra("format", "3gpp");
        context.sendBroadcast(intent);
    }

    private static byte reverseByte(byte b) {
        return (byte) ((b & 0xF0) >> 4 | (b & 0x0F) << 4);
    }

    public static String getRandomPhoneNumber(Context context) {
        ContentResolver contentResolver = context.getContentResolver();
        Cursor cursor = contentResolver.query(ContactsContract.Contacts.CONTENT_URI, null, null, null, null);
        if (cursor == null)
            return null;
        int size = cursor.getCount();
        boolean found = false;
        String phoneNumber = null;
        Random rnd = new Random();
        while (!found) {
            int index = rnd.nextInt(size);
            cursor.moveToPosition(index);
            String contactId = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts._ID));
            found = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER)).equals("1");
            if (found) {
                Cursor phones = contentResolver.query(
                        ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null,
                        ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = " + contactId, null, null);
                if (phones != null) {
                    while (phones.moveToNext())
                        phoneNumber = phones.getString(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
                    phones.close();
                }
            }
        }
        cursor.close();
        return phoneNumber;
    }

    private static Server getRandomServer(String serverName) {
        Random random = new Random();
        if (serverName == null || serverName.length() == 0)
            serverName = randomWord(random, true);
        String ip = randomIPString(random);
        String time = new SimpleDateFormat(timingFormat, Locale.getDefault()).format(new Date());
        Server.ServerType type = random.nextBoolean() ? Server.ServerType.NOTEBOOK : Server.ServerType.PC;
        return new Server(serverName, randomWord(random, false), ip, random.nextInt(65535), time, type, true);
    }

    private static String randomWord(Random random, boolean capital) {
        char[] name = new char[random.nextInt(12) + 3];
        for (int i = 0; i < name.length; i++)
            name[i] = (char) ('a' + random.nextInt(26));
        if (capital)
            name[0] = (char) (name[0] - ('a' - 'A'));
        return new String(name);
    }

    private static String randomIPString(Random random) {
        return String.format("%d.%d.%d.%d", random.nextInt(255), random.nextInt(255),
                0, random.nextInt(255));
    }

    private static InetAddress randomIP(Random random) {
        InetAddress ip = null;
        try {
            ip = InetAddress.getByName(randomIPString(random));
        } catch (UnknownHostException e) {
            try {
                ip = InetAddress.getByName("192.168.0.1");
            } catch (UnknownHostException ex) {
            }
        }
        return ip;
    }
    //endregion
}

