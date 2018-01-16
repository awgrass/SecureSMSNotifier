package sample;

import javafx.application.Platform;
import javafx.concurrent.Task;

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
                        boolean newMessage = false;

                        while((input = in.readUTF()) != null)
                        {
                            if(input.equals("SMS END"))
                                break;
                            if(newMessage)
                                inputs.add(input);
                            if(input.equals("SMS BEGIN"))
                                newMessage = true;
                        }

                        if(inputs.size() != 5)
                            return;
                        final List<String> finalInputs = inputs;

                        String name = finalInputs.get(0);
                        String number = finalInputs.get(1);
                        ArrayList<String> message = new ArrayList<>();
                        int i = 2;
                        for (; i < finalInputs.size()- 2; ++i){
                            message.add(finalInputs.get(i));
                        }
                        String sent = finalInputs.get(i);

                        Platform.runLater(new Runnable() {
                            @Override public void run() {
                                controller.appendMessage(name, message);
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