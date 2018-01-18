package com.securesms.acn.securesmsclient;

import java.nio.ByteBuffer;

public class Server {
    private static int GCM_NONCE_LENGTH = 12;

    private String name;
    private String ip;
    private int port;
    private String time;

    private String keyBase64;
    private long nonce_counter = 0;
    private boolean enabled;


    //return nonce counter and increment by 5 for next message
    public long getNonceCounter() {
        long n = nonce_counter;
        nonce_counter += 5;
        return n;
    }



    private ServerType type = ServerType.PC;

    enum ServerType {
        PC, NOTEBOOK
    }

    public Server() {
        this.name = "";
        this.keyBase64 = "";
        this.ip = null;
        this.port = -1;
        this.time = "";
        this.type = null;
        this.enabled = false;
    }

    public Server(String name, String key, String ip, int port, String time, ServerType type, boolean enabled){
        this.name = name;
        this.keyBase64 = key;
        this.ip = ip;
        this.port = port;
        this.time = time;
        this.type = type;
        this.enabled = enabled;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getKey() {
        return keyBase64;
    }

    public void setKey(String key) {
        this.keyBase64 = key;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public ServerType getType() {
        return type;
    }

    public void setType(ServerType type) {
        this.type = type;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }
}
