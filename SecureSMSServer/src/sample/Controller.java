package sample;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Pane;
import javafx.scene.canvas.Canvas;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;


public class Controller {
    @FXML
    private AnchorPane contentPane;
    @FXML
    private AnchorPane root;
    @FXML
    private Button loadQr;
    @FXML
    private ImageView qrView;
    @FXML
    private Pane header;
    @FXML
    private Label headerLabel;
    @FXML
    private ScrollPane fromPane;

    @FXML
    private ListView fromView;
    @FXML
    private ListView messageView;

    private ListView fromListView;
    private ObservableList<String> fromListViewItems;
    private ListView messageListView;
    private Stage stage;

    private HashMap<String, ObservableList<String>> fromToMessagesMap = new HashMap<>();




    void initStage(Stage stage){
        this.stage = stage;
        //Take a snapshot of the canvas and set it as an image in the ImageView control
        String qrData = constructQrData();
        WritableImage snapshot = getQRCodeImage(qrData, 250, 250);
        qrView.setImage(snapshot);
        //loadMessageFrame();
    }

    void loadMessageFrame(){
        contentPane.getChildren().clear();
        try{
            FXMLLoader loader = new FXMLLoader(getClass().getResource("MessageView.fxml"));
            AnchorPane messageView = loader.load();
            AnchorPane.setTopAnchor(messageView, 0.0);
            AnchorPane.setLeftAnchor(messageView, 0.0);
            AnchorPane.setRightAnchor(messageView, 0.0);
            AnchorPane.setBottomAnchor(messageView, 0.0);
            contentPane.getChildren().add(messageView);
        }
        catch (IOException e){e.printStackTrace();}

        AnchorPane ap = (AnchorPane) contentPane.getChildren().get(0);
        SplitPane sp = (SplitPane) ap.getChildren().get(0);
        AnchorPane apLeft = (AnchorPane) sp.getItems().get(0);
        AnchorPane apRight = (AnchorPane) sp.getItems().get(1);

        fromListView = (ListView)apLeft.getChildren().get(0);
        fromListViewItems = FXCollections.observableArrayList();
        fromListView.setItems(fromListViewItems);
        messageListView = (ListView)apRight.getChildren().get(0);
        messageListView.setItems(FXCollections.observableArrayList());

        fromListView.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                messageListView.setItems(fromToMessagesMap.get(newValue));
            }
        });
    }

    public void appendMessage(String name, ArrayList<String> message){
        String composedMessage = "";
        for (String m: message){
            composedMessage += m + "\n";
        }
        if (!fromToMessagesMap.containsKey(name)){
            fromListViewItems.add(name);
            fromListViewItems.sort(String::compareToIgnoreCase);
            fromToMessagesMap.put(name, FXCollections.observableArrayList());
        }
        fromToMessagesMap.get(name).add(composedMessage);
        messageListView.setItems(fromToMessagesMap.get(name));

    }

    private WritableImage getQRCodeImage(String qrData, int width, int height) {
        QRCodeWriter qrCodeWriter = new QRCodeWriter();
        Canvas canvas = null;
        try {
            BitMatrix bitMatrix = qrCodeWriter.encode(qrData, BarcodeFormat.QR_CODE, width, height);

            //Create a Canvas (a place to draw on), with a 2D Graphic (a kind of drawing)
            canvas = new Canvas(width, height);
            GraphicsContext gc2D = canvas.getGraphicsContext2D();

            //in white, paint a rectangle on it, with the full size
            gc2D.setFill(javafx.scene.paint.Color.WHITE);
            gc2D.fillRect(0, 0, width, height);

            //start painting in black: each bit/pixel set in the bitMatrix
            gc2D.setFill(javafx.scene.paint.Color.BLACK);
            for (int v = 0; v < height; v++) {
                for (int h = 0; h < width; h++) {
                    if (bitMatrix.get(v, h)) {
                        gc2D.fillRect(v, h, 1, 1);
                    }
                }
            }
        } catch (WriterException e) {
            e.printStackTrace();
        }
        return canvas.snapshot(null, null);
    }

    private String constructQrData(){
        String ip = null;
        String computerName = null;
        try {
            InetAddress localHost = InetAddress.getLocalHost();
            ip = localHost.getHostAddress();
            computerName = localHost.getHostName();
        } catch (UnknownHostException e) {
            System.out.println("Error: " + e);
        }

        String qrData = computerName + "|X4Eg5jKo0Xw4|" + ip + "|" + SecureSMSServer.socketServerPORT + "|1";
        return qrData;
    }

    @FXML
    private void handleButtonAction() {



    }


}

