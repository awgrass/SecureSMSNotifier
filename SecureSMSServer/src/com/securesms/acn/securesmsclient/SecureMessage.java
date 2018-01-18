package com.securesms.acn.securesmsclient;

public class SecureMessage {

    private String message;
    private String sender;
    private String number;
    private String receivedTime;
    private String sentTime;

    public SecureMessage(String message, String sender, String number, String receivedTime, String sentTime){
        this.message = message;
        this.sender = sender;
        this.number = number;
        this.receivedTime = receivedTime;
        this.sentTime = sentTime;
    }

    public byte[] getBytes(){
        int size = message.length() + sender.length() + number.length() + receivedTime.length() + sentTime.length() + 5;
        String concatination = message + "\n" + sender + "\n" + number + "\n" + receivedTime + "\n" + sentTime;
        return concatination.getBytes();
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getSender() {
        return sender;
    }

    public void setSender(String sender) {
        this.sender = sender;
    }

    public String getNumber() {
        return number;
    }

    public void setNumber(String number) {
        this.number = number;
    }

    public String getReceivedTime() {
        return receivedTime;
    }

    public void setReceivedTime(String receivedTime) {
        this.receivedTime = receivedTime;
    }

    public String getSentTime() {
        return sentTime;
    }

    public void setSentTime(String sentTime) {
        this.sentTime = sentTime;
    }
}

