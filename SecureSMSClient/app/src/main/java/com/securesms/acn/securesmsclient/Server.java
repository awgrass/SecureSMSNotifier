package com.securesms.acn.securesmsclient;

import java.net.InetAddress;

public class Server {
    private String name;
    private String ip;
    private int port;
    private String time;

    private String key;
    private boolean enabled;

    private ServerType type = ServerType.PC;

    enum ServerType {
        PC, NOTEBOOK
    }

    public Server() {
        this.name = "";
        this.key = "";
        this.ip = null;
        this.port = -1;
        this.time = "";
        this.type = null;
        this.enabled = false;
    }

    public Server(String name, String key, String ip, int port, String time, ServerType type, boolean enabled){
        this.name = name;
        this.key = key;
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
        return key;
    }

    public void setKey(String key) {
        this.key = key;
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
