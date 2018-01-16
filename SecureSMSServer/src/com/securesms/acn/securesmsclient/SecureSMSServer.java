package com.securesms.acn.securesmsclient;

import javafx.application.Platform;

import java.awt.Image;
import java.awt.MenuItem;
import java.awt.TrayIcon;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;


public class SecureSMSServer
{
    String startString = ". . .";
    static final int socketServerPORT = 6323;
    static final int MSG_TIME = 3000;
    Socket socket = null;
    ServerSocket inputSocket = null;
    Thread thread = null;
    TrayIcon trayIcon = null;
    MenuItem connectedItem = null;
    DataOutputStream out = null;
    DataInputStream in = null;
    Image image;
    private Controller controller;

    /**
     * Create the application.
     */
    @SuppressWarnings("resource")
    public SecureSMSServer(Controller controller)
    {
        this.controller = controller;
        OpenServer();
    }


    public void OpenServer()
    {
        thread = new Thread(new ServerThread());
        thread.start();
    }

    class ServerThread implements Runnable
    {
        @Override
        public void run()
        {
            try
            {
                inputSocket = new ServerSocket(socketServerPORT);
                while(true)
                {
                    try
                    {
                        socket = inputSocket.accept();
                        in = new DataInputStream(socket.getInputStream());
                        out = new DataOutputStream(socket.getOutputStream());
                        List<String> inputs = new ArrayList<String>();
                        String input;
                        boolean newMessage = false, newQRcode = false;

                        while((input = in.readUTF()) != null)
                        {
                            //if(input.equals("SMS END") || input.equals("QR CODE END"))
                            if(newMessage || newQRcode)
                                inputs.add(input);
                            if((newMessage && inputs.size() == 5) || (newQRcode && inputs.size() == 1))
                                break;
                            if(!newMessage && !newQRcode && input.equals("SMS BEGIN"))
                                newMessage = true;
                            if(!newMessage && !newQRcode && input.equals("QR CODE BEGIN"))
                                newQRcode = true;
                        }

                        if(newMessage && inputs.size() != 5)
                            return;
                        if(newQRcode && inputs.size() == 1) {
                            //System.out.println("QR code received: " + inputs.get(0));
                            Platform.runLater(new Runnable() {
                                public void run() {
                                    controller.loadMessageFrame();
                                }
                            });
                            return;
                        }

                        final List<String> finalInputs = inputs;

                        SecureMessage message = new SecureMessage(finalInputs.get(2), finalInputs.get(0), finalInputs.get(1), finalInputs.get(3), finalInputs.get(4));

                        Platform.runLater(new Runnable() {
                            @Override public void run() {
                                controller.appendMessage(message);
                            }
                        });

                        for (String s: finalInputs){
                            System.out.println(s);
                        }
                    }
                    catch (IOException e)
                    {
                        e.printStackTrace();
                    }
                }
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }
        }
    }

}