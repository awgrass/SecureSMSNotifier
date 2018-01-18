package com.securesms.acn.securesmsserver;

import ch.swingfx.twinkle.NotificationBuilder;
import ch.swingfx.twinkle.event.NotificationEvent;
import ch.swingfx.twinkle.event.NotificationEventAdapter;
import ch.swingfx.twinkle.style.INotificationStyle;
import ch.swingfx.twinkle.style.theme.DarkDefaultNotification;
import ch.swingfx.twinkle.window.Positions;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Cursor;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.image.ImageView;
import javafx.scene.image.WritableImage;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.scene.canvas.Canvas;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import javafx.stage.Stage;
import javafx.util.Callback;

import javax.crypto.SecretKey;
import java.awt.*;
import java.io.*;
import java.net.InetAddress;
import java.net.URISyntaxException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.prefs.Preferences;


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
    private BorderPane header;
    @FXML
    private Label headerLabel;
    @FXML
    private ScrollPane fromPane;

    @FXML
    private ListView fromView;
    @FXML
    private ListView messageView;

    @FXML
    private ImageView openQRView;

    @FXML
    private ImageView headerClose;
    @FXML
    private ImageView headerMin;


    private ListView fromListView;
    private ObservableList<String> fromListViewItems;
    private ListView messageListView;
    private Stage stage;

    private HashMap<String, ObservableList<SecureMessage>> fromToMessagesMap = new HashMap<>();
    private final String PREF_IP_KEY = "QRCODE_FOUND";
    private final String PREF_KEY_KEY = "KEYS_FOUND";

    private boolean isMessageView;
    private Crypto crypto;


    Crypto initStage(Stage stage){
        this.stage = stage;
        ResizeHelper.addResizeListener(stage);

        headerClose.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                stage.close();
                Platform.exit();
                System.exit(0);
            }
        });
        headerMin.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                stage.setIconified(true);
            }
        });

        openQRView.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                if(isMessageView) {
                    loadQRFrame();
                }
                else {
                    loadMessageFrame();
                }
            }
        });
        openQRView.setVisible(false);

        final Point dragDelta = new Point(0,0);
        header.setOnMousePressed(new EventHandler<MouseEvent>() {
            @Override public void handle(MouseEvent mouseEvent) {
                // record a delta distance for the drag and drop operation.
                dragDelta.x = (int)(stage.getX() - mouseEvent.getScreenX());
                dragDelta.y = (int)(stage.getY() - mouseEvent.getScreenY());
            }
        });
        header.setOnMouseDragged(new EventHandler<MouseEvent>() {
            @Override public void handle(MouseEvent mouseEvent) {
                stage.setX(mouseEvent.getScreenX() + dragDelta.getX());
                stage.setY(mouseEvent.getScreenY() + dragDelta.getY());
            }
        });

        String ip = getIPAddress();
        String key = alreadyExists(ip, false);
        if(ip != null && key != null) {
            this.crypto = new Crypto(key);
            loadMessageFrame();
        }
        else {
            this.crypto = new Crypto(null);
            loadQRFrame();
        }
        return crypto;
    }

    public void loadMessageFrame(){
        isMessageView = true;
        if(!openQRView.isVisible())
            openQRView.setVisible(true);
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
        fromListView.setStyle("-fx-focus-color: transparent;");
        messageListView = (ListView)apRight.getChildren().get(0);
        messageListView.setItems(FXCollections.observableArrayList());
        messageListView.setStyle("-fx-focus-color: transparent;");

        fromListView.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                messageListView.setItems(fromToMessagesMap.get(newValue));
            }
        });

        fromListView.setCellFactory(new Callback<ListView<String>, ListCell<String>>() {

            @Override
            public ListCell<String> call(ListView<String> arg0) {
                return new ListCell<String>() {

                    @Override
                    protected void updateItem(String item, boolean bln) {
                        super.updateItem(item, bln);
                        if (item != null && !bln) {
                            VBox vBox = new VBox(new Text(item)); //, new Text(item.getSender()));
                            setGraphic(vBox);
                            try {
                                ImageView imageView = new ImageView(SecureSMSServer.class.getResource("user.png").toURI().toString());
                                imageView.setFitHeight(16);
                                imageView.setFitWidth(16);
                                HBox hBox = new HBox(imageView, vBox);
                                hBox.setSpacing(10);
                                hBox.setCursor(Cursor.HAND);
                                setGraphic(hBox);
                            }
                            catch(URISyntaxException e) {
                                vBox.setCursor(Cursor.HAND);
                                setGraphic(vBox);
                            }
                            return;
                        }
                        setGraphic(null);
                    }

                };
            }

        });

        messageListView.setCellFactory(new Callback<ListView<SecureMessage>, ListCell<SecureMessage>>() {

            @Override
            public ListCell<SecureMessage> call(ListView<SecureMessage> arg0) {
                return new ListCell<SecureMessage>() {
                    {
                        prefWidthProperty().bind(messageListView.widthProperty().subtract(2));
                        setMaxWidth(Control.USE_PREF_SIZE);
                    }
                    @Override
                    protected void updateItem(SecureMessage item, boolean bln) {
                        super.updateItem(item, bln);
                        if (item != null && !bln) {
                            Text message = new Text(item.getMessage());
                            message.wrappingWidthProperty().bind(widthProperty().subtract(10));
                            Text time = new Text(item.getReceivedTime());
                            time.setTextAlignment(TextAlignment.RIGHT);

                            VBox vBox = new VBox(message, time);
                            vBox.setCursor(Cursor.HAND);
                            setGraphic(vBox);
                            //setText(item.getMessage() + "\n"  + item.getReceivedTime());
                            return;
                        }
                        setGraphic(null);
                    }

                };
            }

        });
        //appendMessage(new SecureMessage("This is an example SMS!", "Max Musterman", "+431234567890", "00:00, 01.01.2018", ""));
        //appendMessage(new SecureMessage("This is a second example SMS!", "Max Musterman", "+431234567890", "00:01, 01.01.2018", ""));
        //appendMessage(new SecureMessage("This is an other example SMS!", "Felix Musterman", "+431234567890", "00:01, 01.01.2018", ""));
    }

    public void loadQRFrame() {
        isMessageView = false;
        contentPane.getChildren().clear();
        try{
            FXMLLoader loader = new FXMLLoader(getClass().getResource("QRView.fxml"));
            AnchorPane messageView = loader.load();
            AnchorPane.setTopAnchor(messageView, 0.0);
            AnchorPane.setLeftAnchor(messageView, 0.0);
            AnchorPane.setRightAnchor(messageView, 0.0);
            AnchorPane.setBottomAnchor(messageView, 0.0);
            contentPane.getChildren().add(messageView);
        }
        catch (IOException e){e.printStackTrace();}

        AnchorPane ap = (AnchorPane) contentPane.getChildren().get(0);
        BorderPane bp = (BorderPane) ap.getChildren().get(0);
        qrView = (ImageView) bp.getCenter();

        String qrData = constructQrData();
        WritableImage snapshot = getQRCodeImage(qrData, 250, 250);
        qrView.setImage(snapshot);
    }

    public String alreadyExists(String ip, boolean add) {
        String key = crypto.convertKeyToBase64();
        String found_key = null;
        List<String> ips = new ArrayList<>();
        List<String> keys = new ArrayList<>();
        Preferences prefs = Preferences.userNodeForPackage(Controller.class);
        // TODO: uncomment for clean server list
        // prefs.putByteArray(PREF_IP_KEY, new byte[0]);
        // prefs.putByteArray(PREF_KEY_KEY, new byte[0]);
        byte[] bytes = prefs.getByteArray(PREF_IP_KEY, null);
        byte[] bytes_keys = prefs.getByteArray(PREF_KEY_KEY, null);
        if(bytes != null) {
            DataInputStream in = new DataInputStream(new ByteArrayInputStream(bytes));
            DataInputStream in_keys = new DataInputStream(new ByteArrayInputStream(bytes_keys));
            try {
                while (in.available() > 0) {
                    String input = in.readUTF();
                    String inputKey = in_keys.readUTF();
                    if(ip.equals(input))
                        found_key = inputKey;
                    ips.add(input);
                    keys.add(inputKey);
                }
            }
            catch(EOFException e) {
                e.printStackTrace();
            }
            catch(IOException e) {
                e.printStackTrace();
            }
        }
        if(found_key == null && add) {
            ips.add(ip);
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            DataOutputStream out = new DataOutputStream(baos);
            for (String element : ips) {
                try {
                    out.writeUTF(element);
                }
                catch (IOException e) {
                    e.printStackTrace();
                }
            }
            prefs.putByteArray(PREF_IP_KEY, baos.toByteArray());

            keys.add(key);
            ByteArrayOutputStream baos_keys = new ByteArrayOutputStream();
            DataOutputStream out_keys = new DataOutputStream(baos_keys);
            for (String element : keys) {
                try {
                    out_keys.writeUTF(element);
                }
                catch (IOException e) {
                    e.printStackTrace();
                }
            }
            prefs.putByteArray(PREF_KEY_KEY, baos_keys.toByteArray());
        }
        return found_key;
    }

    public void appendMessage(SecureMessage message){
        final String name = message.getSender(); // + " (" + message.getNumber() + ")";
        final String composedMessage = message.getMessage() + "\n\n" + message.getReceivedTime() + " (" + message.getSentTime() + ")";
        if (!fromToMessagesMap.containsKey(name)){
            fromListViewItems.add(name);
            fromListViewItems.sort(String::compareToIgnoreCase);
            fromToMessagesMap.put(name, FXCollections.observableArrayList());
        }
        if(fromToMessagesMap.get(name) != null) {
            fromToMessagesMap.get(name).add(message);
            messageListView.setItems(fromToMessagesMap.get(name));
            if(!stage.isFocused())
                showNotification(message);
        }
    }

    private void showNotification(SecureMessage secureMessage) {
        final String message = secureMessage.getMessage() + "\n\n" + secureMessage.getReceivedTime() + " (" + secureMessage.getSentTime() + ")";
        final String title = "SMS from " + secureMessage.getSender() + " (" + secureMessage.getNumber() +")";

        System.setProperty("swing.aatext", "true");
        INotificationStyle style = new DarkDefaultNotification()
                .withWidth(400)
                .withAlpha(0.9f)
                ;
        final Controller controller = this;
        new NotificationBuilder()
                .withStyle(style)
                .withTitle(title)
                .withMessage(message)
                .withIcon(new javax.swing.ImageIcon(SecureSMSServer.class.getResource("notification.png")))
                .withDisplayTime(5000)
                .withPosition(Positions.NORTH_EAST)
                .withListener(new NotificationEventAdapter() {
                    public void closed(NotificationEvent event) {
                    }

                    public void clicked(NotificationEvent event) {
                        Platform.runLater(new Runnable() {
                            public void run() {
                                controller.stage.setAlwaysOnTop(true);
                                controller.stage.toFront();
                                controller.stage.requestFocus();
                                controller.stage.setAlwaysOnTop(false);
                            }
                        });
                    }
                }).showNotification();
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

        String qrData = computerName + "|" + crypto.convertKeyToBase64() + "|" + ip + "|" + SecureSMSServer.socketServerPORT + "|1";
        return qrData;
    }

    private String getIPAddress() {
        String ip;
        try {
            InetAddress localHost = InetAddress.getLocalHost();
            ip = localHost.getHostAddress();
        } catch (UnknownHostException e) {
            e.printStackTrace();
            return null;
        }
        return ip;
    }

    @FXML
    private void handleButtonAction() {



    }


}

