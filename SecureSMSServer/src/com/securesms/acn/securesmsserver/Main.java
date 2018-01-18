package com.securesms.acn.securesmsserver;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.awt.*;

public class Main extends Application {

    Controller controller = null;
    Crypto crypto = null;

    @Override
    public void start(Stage primaryStage) throws Exception{
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("start.fxml"));
            Parent root = loader.load();
            primaryStage.setTitle("SecureSmsNotifier");
            primaryStage.setScene(new Scene(root, 770, 600));
            primaryStage.show();

            crypto  = new Crypto();

            //init Controller
            controller = loader.getController();
            controller.initStage(primaryStage, crypto);


        }
        catch (Exception e){
            System.out.println("Error" + e);
        }

        try
        {
            System.setProperty("apple.awt.UIElement", "true");
        }
        catch(Exception e)
        {
            e.printStackTrace();
        }


        EventQueue.invokeLater(new Runnable() {
            public void run()
            {
                try
                {
                    //RemoteServer window =
                    new SecureSMSServer(controller, crypto);
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                }
            }
        });
    }



    public static void main(String[] args) {
        launch(args);
    }
}
