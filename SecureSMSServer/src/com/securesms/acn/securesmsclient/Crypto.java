package com.securesms.acn.securesmsclient;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import java.security.NoSuchAlgorithmException;

public class Crypto {
    private KeyGenerator keyGen;
    private SecretKey secretKey;
    private Cipher aesCipher;

    public Crypto(){
        try{
            keyGen = KeyGenerator.getInstance("AES");
            keyGen.init(256);
            secretKey = keyGen.generateKey();
            Cipher.getInstance("AES");
            aesCipher.init(Cipher.ENCRYPT_MODE,secretKey);
        }
        catch(Exception e){e.printStackTrace();}
    }

    public void encrypt(String msg){

    }
}
