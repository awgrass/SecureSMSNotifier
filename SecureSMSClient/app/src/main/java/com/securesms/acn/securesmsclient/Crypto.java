package com.securesms.acn.securesmsclient;


import android.util.Base64;

import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;

public class Crypto {
    private static int GCM_TAG_LENGTH = 16;
    public static int GCM_NONCE_LENGTH = 8;

    private SecretKey secretKey;
    private Cipher gcmCipher;
    private byte[] nonce = new byte[12];
    GCMParameterSpec spec;


    public Crypto(String keyBase64, long nonce){
        try{
            setKey(keyBase64);
            gcmCipher = Cipher.getInstance("AES/GCM/NoPadding");
            this.nonce = convertLongToBytes(nonce);
            setNewSpec();
        }
        catch(NoSuchAlgorithmException | NoSuchPaddingException e){e.printStackTrace();}
    }

    public byte[] getNonce() {
        return nonce;
    }

    public void setKey(String keyBase64){
        byte[] encodedKey     = Base64.decode(keyBase64, Base64.DEFAULT);
        this.secretKey = new SecretKeySpec(encodedKey, 0, encodedKey.length, "AES");
    }

    private byte[] convertLongToBytes(long number){
        byte[] bytes = new byte[GCM_NONCE_LENGTH];
        ByteBuffer buffer = ByteBuffer.allocate(Long.BYTES);
        buffer.putLong(number);
        return buffer.array();
    }

    public void setNonce(long nonce){
        ByteBuffer b = ByteBuffer.allocate(Long.BYTES);
        b.putLong(nonce);
        this.nonce = b.array();
    }

    private void incNonce(){
        ByteBuffer getBuffer = ByteBuffer.wrap(nonce);
        ByteBuffer putBuffer = ByteBuffer.allocate(Long.BYTES);
        long l = getBuffer.getLong();
        l += 1;
        putBuffer.putLong(l);
        nonce = putBuffer.array();
    }

    private void setNewSpec(){
        this.spec = new GCMParameterSpec(GCM_TAG_LENGTH * 8, nonce);
        try{
            gcmCipher.init(Cipher.ENCRYPT_MODE, secretKey, spec);
        }
        catch(InvalidKeyException | InvalidAlgorithmParameterException e){e.printStackTrace();}
    }

    static public String encodeBase64(byte[] unencodedBytes){
        return Base64.encodeToString(unencodedBytes, Base64.DEFAULT);
    }

    public byte[] encrypt(byte[] plaintext){
        byte[] ciphertext = null;
        try{
            ciphertext = this.gcmCipher.doFinal(plaintext);
        }
        catch(IllegalBlockSizeException | BadPaddingException e){e.printStackTrace();}
        return ciphertext;
    }

    public String encryptAndEncode(String plaintext){
        String encodedCiphertext = encodeBase64(encrypt(plaintext.getBytes()));
        incNonce();
        setNewSpec();
        return encodedCiphertext;

    }



}
