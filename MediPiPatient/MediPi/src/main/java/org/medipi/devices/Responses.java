/*
Copyright 2016  Richard Robinson @ NHS Digital <rrobinson@nhs.net>

 Licensed under the Apache License, Version 2.0 (the "License");
 you may not use this file except in compliance with the License.
 You may obtain a copy of the License at

 http://www.apache.org/licenses/LICENSE-2.0

 Unless required by applicable law or agreed to in writing, software
 distributed under the License is distributed on an "AS IS" BASIS,
 WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 See the License for the specific language governing permissions and
 limitations under the License.
 */
package org.medipi.devices;

import java.io.File;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.control.ScrollPane;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import org.medipi.DashboardTile;
import org.medipi.MediPi;
import org.medipi.MediPiMessageBox;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableMap;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import org.medipi.AlertBanner;
import org.medipi.MediPiProperties;
import org.medipi.authentication.UnlockConsumer;
import org.medipi.downloadable.handlers.DownloadableHandlerManager;
import org.medipi.downloadable.handlers.MessageHandler;
import org.medipi.logging.MediPiLogger;
import org.medipi.model.AlertListDO;
import org.medipi.model.AlertDO;
import org.medipi.security.CertificateDefinitions;
import org.medipi.model.EncryptedAndSignedUploadDO;
import org.medipi.security.UploadEncryptionAdapter;
import org.medipi.utilities.Utilities;

/**
 * Class to display and handle the functionality for a incoming read-only
 * message display utility.
 *
 * This is a simple viewer of incoming messages from the clinician. It shows the
 * message title in a list of all messages received and displays the contents of
 * the selected message. As MediPi does not expose any inbound ports, incoming
 * messaging is achieved through periodic polling of a secure location. Any new
 * messages received are digested and the UI is updated. A new unread message
 * alerts the dashboard Tile class to superimpose an notification image. All
 * messages are persisted locally to a configurable file location.
 *
 * The messages are encrypted using each patient's certificate and must be
 * decrypted
 *
 * @author rick@robinsonhq.com
 */
public class Responses extends Element implements UnlockConsumer, MessageReceiver, SchedulerCallbacksInterface {

    public static final int CANNOT_CALCULATE = 2;
    public static final int NO_RESULT = 1;
    public static final int OUT_OF_THRESHOLD = 3;
    public static final int IN_THRESHOLD = 0;
    public static final String OUT_OF_THRESHOLD_STATUS = "OUT_OF_THRESHOLD";
    public static final String IN_THRESHOLD_STATUS = "IN_THRESHOLD";
    public static final String NO_RESULT_STATUS = "NO_RESULT";
    public static final String CANNOT_CALCULATE_STATUS = "CANNOT_CALCULATE";

    private static final String NAME = "Responses";
    private static final String DISPLAYNAME = "MediPi Notification Responses";
    private static final String MEDIPIIMAGESEXCLAIM = "medipi.images.exclaim";
    private static final String MEDIPIIMAGESALLINTHRESHOLD = "medipi.images.allinthreshold";
    private static final String MEDIPIIMAGESHIGHESTERROROUTOFTHRESHOLD = "medipi.images.highesterror.outofthreshold";
    private static final String MEDIPIIMAGESHIGHESTERRORCANTCALCULATE = "medipi.images.highesterror.cantcalculate";
    private static final String MEDIPIIMAGESHIGHESTERRORMISSING = "medipi.images.highesterror.missing";

    private Image inThresholdImageView = null;
    private Image outOfThresholdImageView = null;
    private Image cantCalculateImageView = null;
    private Image missingImageView = null;
    private ObjectProperty<Image> notificationImageView = new SimpleObjectProperty<>();
    private VBox responsesWindow;

    private final BooleanProperty notificationBooleanProperty = new SimpleBooleanProperty(true);

    private ObservableList<Response> responseItems = FXCollections.observableArrayList();
    private ObservableList<Message> items = FXCollections.observableArrayList();
    private ObservableMap<Device, Response> responseInstanceMap = FXCollections.observableHashMap();

    private ObservableMap<Element, AlertDO> alertMap = FXCollections.observableHashMap();
    private Integer highestStatus = 0;
    private boolean locked = true;
    private Path responsesDirPath;
    private TableView<Response> responsesList;
    private Label resultsTitle = new Label();

    /**
     * Constructor for Messenger
     *
     */
    public Responses() {

    }

    /**
     * Initiation method called for this Element.
     *
     * Successful initiation of the this class results in a null return. Any
     * other response indicates a failure with the returned content being a
     * reason for the failure
     *
     * @return populated or null for whether the initiation was successful
     * @throws java.lang.Exception
     */
    @Override
    public String init() throws Exception {

        String uniqueDeviceName = getClassTokenName();
        //ascertain if this element is to be displayed on the dashboard
        String b = MediPiProperties.getInstance().getProperties().getProperty(MediPi.ELEMENTNAMESPACESTEM + uniqueDeviceName + ".showdashboardtile");
        if (b == null || b.trim().length() == 0) {
            showTile = new SimpleBooleanProperty(true);
        } else {
            showTile = new SimpleBooleanProperty(!b.toLowerCase().startsWith("n"));
        }
        // location of the persistent message store
        String messageDir = medipi.getProperties().getProperty(MediPi.ELEMENTNAMESPACESTEM + uniqueDeviceName + ".incomingmessagedirectory");
        if (messageDir == null || messageDir.trim().length() == 0) {
            throw new Exception("Message Directory parameter not configured");
        }
        responsesDirPath = Paths.get(messageDir);
        // get the image file for the notification image to be superimposed on the dashboard tile when a new message is received
        String allInThresholdImageFile = medipi.getProperties().getProperty(MEDIPIIMAGESALLINTHRESHOLD);
        if (allInThresholdImageFile == null) {
            return "Cannot find image for " + MEDIPIIMAGESALLINTHRESHOLD;
        }
        inThresholdImageView = new Image("file:///" + allInThresholdImageFile);

        String oob = medipi.getProperties().getProperty(MEDIPIIMAGESHIGHESTERROROUTOFTHRESHOLD);
        if (oob == null) {
            return "Cannot find image for " + MEDIPIIMAGESHIGHESTERROROUTOFTHRESHOLD;
        }
        outOfThresholdImageView = new Image("file:///" + oob);
        String cc = medipi.getProperties().getProperty(MEDIPIIMAGESHIGHESTERRORCANTCALCULATE);
        if (cc == null) {
            return "Cannot find image for " + MEDIPIIMAGESHIGHESTERRORCANTCALCULATE;
        }
        cantCalculateImageView = new Image("file:///" + cc);
        String missing = medipi.getProperties().getProperty(MEDIPIIMAGESHIGHESTERRORMISSING);
        if (missing == null) {
            return "Cannot find image for " + MEDIPIIMAGESHIGHESTERRORMISSING;
        }
        missingImageView = new Image("file:///" + missing);

        notificationImageView.set(missingImageView);
        notificationBooleanProperty.set(true);

        responsesWindow = new VBox();
        responsesWindow.setPadding(new Insets(0, 5, 0, 5));
        responsesWindow.setSpacing(5);
        responsesWindow.setMinSize(800, 300);
        responsesWindow.setMaxSize(800, 300);

        // Call the MessageWatcher class which will update the message list if 
        // a new txt file appears in the configured incoming message directory
        try {
            MessageWatcher mw = new MessageWatcher(responsesDirPath, this, medipi);
        } catch (IOException ioe) {
            return "Message Watcher failed to initialise" + ioe.getMessage();
        }

        File list[] = new File(responsesDirPath.toString()).listFiles();
        for (File f : list) {
            if (!f.getName().contains("Alert")) {
                continue;
            }
            Message m;
            try {
                m = new Message(f.getName());
            } catch (Exception e) {
                continue;
            }

            items.add(m);
        }

        //Comparator for dates stamps on the Messages to order them in 
        // REVERSE - most recent first to reduce the amount of work the updateTable method
        Comparator<? super Message> comparatorMessageDates = new Comparator<Message>() {
            @Override
            public int compare(Message o1, Message o2) {
                return o2.getTime().compareTo(o1.getTime());
            }
        };
        Collections.sort(items, comparatorMessageDates);
        // Create the table
        responsesList = new TableView<>();
        responsesList.setId("messenger-messagelist");
        responsesList.prefHeightProperty().bind(Bindings.size(responsesList.getItems()).multiply(responsesList.getFixedCellSize()).add(30));

        TableColumn<Response, BorderPane> imageTC = new TableColumn<>();
        imageTC.setCellValueFactory(cellData -> cellData.getValue().deviceImageProperty());
        imageTC.setMinWidth(50);
        imageTC.setMaxWidth(50);
        imageTC.setCellFactory(column -> {
            return new TableCell<Response, BorderPane>() {
                @Override
                protected void updateItem(BorderPane item, boolean empty) {
                    super.updateItem(item, empty);

                    if (item == null || empty) {
                        setText(null);
                    } else {
                        // Format date.
                        setGraphic(item);
                    }
                }
            };
        });

        TableColumn deviceNameTC = new TableColumn("Name");
        deviceNameTC.setMinWidth(160);
        deviceNameTC.setId("responsestabletext");

        deviceNameTC.setCellValueFactory(
                new PropertyValueFactory<>("deviceName"));

        TableColumn<Response, Instant> timeTC = new TableColumn<>("Measurement Time");
        timeTC.setCellValueFactory(cellData -> cellData.getValue().measurementTimeProperty());
        timeTC.setId("responsestabletext");
        timeTC.setMinWidth(200);
        timeTC.setCellFactory(column -> {
            return new TableCell<Response, Instant>() {
                @Override
                protected void updateItem(Instant item, boolean empty) {
                    super.updateItem(item, empty);

                    if (item == null || empty) {
                        setText(null);
                    } else {
                        // Format date.
                        setText(Utilities.DISPLAY_FORMAT_LOCALTIME.format(item));
                    }
                }
            };
        });

        TableColumn<Response, String> resultTC = new TableColumn<>("Result");
        resultTC.setMinWidth(130);
        resultTC.setMaxWidth(130);
        resultTC.setId("responsestabletext");
        resultTC.setCellValueFactory(new PropertyValueFactory<>("result"));
        resultTC.setCellFactory(column -> {
            return new TableCell<Response, String>() {
                @Override
                protected void updateItem(String item, boolean empty) {
                    super.updateItem(item, empty);

                    TableRow<Response> currentRow = getTableRow();
                    if (item == null || empty) {
                        System.out.println("empty");
                        currentRow.setStyle("-fx-background-color:white");
                    } else {
                        switch (item) {
                            case OUT_OF_THRESHOLD_STATUS:
                                setText("Outside limits");
                                currentRow.setStyle("-fx-background-color:lightcoral");
                                break;
                            case CANNOT_CALCULATE_STATUS:
                                setText("Can't calculate");
                                currentRow.setStyle("-fx-background-color:yellow");
                                break;
                            case IN_THRESHOLD_STATUS:
                                setText("Within limits");
                                currentRow.setStyle("-fx-background-color:lightgreen");
                                break;
                            case NO_RESULT_STATUS:
                                setText("No response");
                                currentRow.setStyle("-fx-background-color:lightgray");
                            default:
                                setText("No response");
                                currentRow.setStyle("-fx-background-color:lightgray");
                                break;
                        }
                    }
                    setGraphic(null);

                }
            };
        });

        TableColumn<Response, Button> detailsButtonTC = new TableColumn<>("Details");
        detailsButtonTC.setCellValueFactory(cellData -> cellData.getValue().detailsButtonProperty());
        detailsButtonTC.setMinWidth(100);
        detailsButtonTC.setMaxWidth(100);
        detailsButtonTC.setCellFactory(column -> {
            return new TableCell<Response, Button>() {
                @Override
                protected void updateItem(Button item, boolean empty) {
                    super.updateItem(item, empty);

                    if (item == null || empty) {
                        setText(null);
                    } else {
                        // Format date.
                        setGraphic(item);
                    }
                }
            };
        });

        responsesList.setItems(responseItems);
        responsesList.setMinHeight(270);
        responsesList.setMaxHeight(270);
        responsesList.setFixedCellSize(40);
        responsesList.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);

        responsesList.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        responsesList.getColumns().addAll(imageTC, deviceNameTC, timeTC, resultTC, detailsButtonTC);
        ScrollPane listSP = new ScrollPane();
        listSP.setContent(responsesList);
        listSP.setFitToWidth(true);
        listSP.setPrefHeight(300);
        listSP.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        listSP.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        responsesList.getSelectionModel().select(0);
        responsesList.setId("responses-table");
        responsesWindow.getChildren().addAll(
                resultsTitle,
                responsesList
        );
        // set main Element window
        window.setCenter(responsesWindow);

        // Register this device with the handler
        DownloadableHandlerManager dhm = medipi.getDownloadableHandlerManager();
        dhm.addHandler("PATIENTMESSAGE", new MessageHandler(medipi.getProperties(), responsesDirPath));

        medipi.getMediPiWindow().registerForAuthenticationCallback(this);
        medipi.getScheduler().registerScheduleCallbacks(this);

        recreateTable();
        // if the dashboard tile has an notification superimposed remove it upon showing this element
        window.visibleProperty().addListener((ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) -> {
            if (newValue) {
                AlertBanner.getInstance().removeAlert("messagewatcher");

            }
        });
        return null;
    }

    // Method to refresh the screen, recalculating from the message list
    // This methos is to be called at initialisation and whenever the schedule period changes
    private void recreateTable() {
        responseItems.clear();
        responseInstanceMap.clear();
        if (medipi.getScheduler().getCurrentScheduleStartTime() != Instant.EPOCH && medipi.getScheduler().getCurrentScheduleStartTime().isBefore(Instant.now())) {
            resultsTitle = new Label("Results from Current Schedule (since " + Utilities.DISPLAY_FORMAT_LOCALTIME.format(medipi.getScheduler().getCurrentScheduleStartTime()) + ")");
        }

        try {
            ArrayList<String> counter = new ArrayList<>();
            // loop through all elements which the scheduler is assigned
            // This relies on this class being instantiated AFTER the scheduler
            for (Element e : medipi.getScheduler().getScheduledElements()) {
                if (Device.class.isAssignableFrom(e.getClass())) {
                    Device d = (Device) e;
                    ImageView image = e.getImage();
                    image.setFitHeight(30);
                    image.setFitWidth(30);
                    BorderPane imagePane = new BorderPane(image);
                    imagePane.setMinSize(35, 35);
                    imagePane.setId("transmitter-component");
                    Button detailsButton = new Button("Details");
                    detailsButton.setId("button-back");
                    counter.add(d.getGenericDeviceDisplayName());

                    Response response = new Response(imagePane, d.getGenericDeviceDisplayName(), null, Response.NO_RESULT, "No Results");
                    responseItems.add(response);
                    responseInstanceMap.put(d, response);
                }
            }

        } catch (Exception ex) {
            MediPiMessageBox.getInstance().makeErrorMessage(ex.getLocalizedMessage(), ex);
        }
        if (!locked) {
            updateTableContents();
        }
    }

    private void updateTableContents() {
        try {
            resetList(responseItems);
            highestStatus = 0;
            Scheduler scheduler = medipi.getScheduler();
            for (Message m : items) {
                // get the time message was received at MediPi Concentrator and use 
                // as a rough filter. No alert message BEFORE the current scheduled 
                // start date can be elligable
                Instant i = m.getTime();
                if (scheduler.getCurrentScheduleStartTime().isBefore(i) && scheduler.getCurrentScheduleStartTime() != Instant.EPOCH) {
                    // Accessing files on disk and decrypting take processing and time
                    File file = new File(responsesDirPath.toString(), m.getFileName());
                    AlertListDO alertListDO = readJSONNotificationMessage(file);
                    // Loop through each AlertList
                    for (AlertDO ado : alertListDO.getAlert()) {
                        
                        //dont read any positive feedback messages
                        if(ado.getStatus().equals("MESSAGE")){
                            continue;
                        }
                        // Only choose those alerts whose dataValueTime is within our schedule time
                        if (ado.getDataValueTime().toInstant().isAfter(scheduler.getCurrentScheduleStartTime())
                                && ado.getDataValueTime().toInstant().isBefore(scheduler.getCurrentScheduleExpiryTime())) {
                            //loop through all registered elements in this MediPi Patient and test to see if there is any alert info for them
                            for (Element e : medipi.getElements()) {
                                if (Device.class.isAssignableFrom(e.getClass())) {
                                    Device d = (Device) e;
                                    // A match is made when type, Make and Model match
                                    String type = d.getProfileId().substring(d.getProfileId().lastIndexOf(":") + 1, d.getProfileId().length());
                                    if (type.equals(ado.getType())
                                            && d.getMake().equals(ado.getMake())
                                            && d.getModel().equals(ado.getModel())) {
                                        //we've now got a result that's relevant for one of our Devices find the latest datapoint time
                                        Response response = null;
                                        if ((response = responseInstanceMap.get(d)) != null) {
                                            response.updateAlert(ado);
                                        } else {
                                            MediPiMessageBox.getInstance().makeErrorMessage("Results returned for an unrecognised device: " + ado.getType(), null);
                                        }
                                        break;
                                    }
                                }
                            }
                        }
                    }
                }

            }
            responsesList.refresh();
            updateTileIcon(responseItems);

        } catch (Exception ex) {
            MediPiMessageBox.getInstance().makeErrorMessage("Cannot read response message content" + ex.getLocalizedMessage(), ex);
        }
    }

    private void resetList(ObservableList<Response> list) {
        for (Response r : list) {
            r.reset();
        }

    }

    /**
     * method to get the Display Name of the device
     *
     * @return displayName of device
     */
    @Override
    public String getSpecificDeviceDisplayName() {
        return DISPLAYNAME;
    }

    @Override
    public String getGenericDeviceDisplayName() {
        return NAME;
    }

    // Filters out only those messages with a title of "Alert"
    @Override
    public void setMessageList(ObservableList<Message> items) {
        ObservableList<Message> filteredItems = FXCollections.observableArrayList();
        items.forEach(s -> {
            if (s.getMessageTitle().contains("Alert")) {
                filteredItems.add(s);
            }
        });
        this.items = filteredItems;
        if (!locked) {
            updateTableContents();
        }
    }

    /**
     * Method called when a new message has arrived
     *
     */
    @Override
    public void newMessageReceived(File file) {
        if(!file.getName().contains("Alert")){
            return;
        }
        notificationBooleanProperty.set(true);
        AlertBanner.getInstance().addAlert("messagewatcher", "A new Clinician's notification has arrived");
        MediPiMessageBox.getInstance().makeMessage("A new clinician's message has arrived");
    }
    
    private AlertListDO readJSONNotificationMessage(File file) throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        EncryptedAndSignedUploadDO encryptedAndSignedUploadDO = mapper.readValue(file, EncryptedAndSignedUploadDO.class);
        // instantiate the clinician encryption adapter
        UploadEncryptionAdapter clinicianEncryptionAdapter = new UploadEncryptionAdapter();

        CertificateDefinitions clinicianCD = new CertificateDefinitions(medipi.getProperties());
        clinicianCD.setSIGNTRUSTSTORELOCATION("medipi.json.sign.truststore.clinician.location", CertificateDefinitions.INTERNAL);
        clinicianCD.setSIGNTRUSTSTOREPASSWORD("medipi.json.sign.truststore.clinician.password", CertificateDefinitions.INTERNAL);
        clinicianCD.setENCRYPTKEYSTORELOCATION("medipi.patient.cert.location", CertificateDefinitions.INTERNAL);
        clinicianCD.setENCRYPTKEYSTOREALIAS("medipi.patient.cert.alias", CertificateDefinitions.INTERNAL);
        clinicianCD.setENCRYPTKEYSTOREPASSWORD("medipi.patient.cert.password", CertificateDefinitions.SYSTEM);

        String clinicianAdapterError;
        AlertListDO alertListDO = null;
        try {
            clinicianAdapterError = clinicianEncryptionAdapter.init(clinicianCD, UploadEncryptionAdapter.SERVERMODE);
            if (clinicianAdapterError != null) {
                MediPiLogger.getInstance().log(Responses.class.getName() + ".error", "Failed to instantiate Clinician Encryption Adapter: " + clinicianAdapterError);
                throw new Exception("Failed to instantiate Clinician Encryption Adapter: " + clinicianAdapterError);
            }
            alertListDO = (AlertListDO) clinicianEncryptionAdapter.decryptAndVerify(encryptedAndSignedUploadDO);
            return alertListDO;
        } catch (Exception e) {
            MediPiLogger.getInstance().log(Responses.class.getName() + ".error", "Notification Decryption exception: " + e.getLocalizedMessage());
            throw new Exception("Notification Decryption exception: " + e.getLocalizedMessage());
        }

    }

    /**
     * method to return the component to the dashboard
     *
     * @return @throws Exception
     */
    @Override
    public BorderPane getDashboardTile() throws Exception {
        DashboardTile dashComponent = new DashboardTile(this, showTile);
        dashComponent.addTitle(NAME);
        dashComponent.addOverlay(notificationImageView, notificationBooleanProperty);
        return dashComponent.getTile();
    }

    /**
     * A method to allow callback failure of the messageWatcher
     *
     * @param failureMessage
     * @param e exception
     */
    @Override
    public void callFailure(String failureMessage, Exception e) {
        MediPiMessageBox.getInstance().makeErrorMessage(failureMessage, e);
    }

    @Override
    public void unlocked() {
        locked = false;
        updateTableContents();
    }

    @Override
    public void locked() {
        locked = true;
    }

    @Override
    public void ScheduleRefreshed() {
        Platform.runLater(() -> {
            recreateTable();
        });
    }

    @Override
    public void ScheduleExpired() {
        Platform.runLater(() -> {
            if (!locked) {
                updateTableContents();
            }
        });
    }

// Method to update the tile on he dashboard to the status of the most severe
    private void updateTileIcon(ObservableList<Response> responseItems) {
        for (Response r : responseItems) {
            switch (r.getResult()) {
                case OUT_OF_THRESHOLD_STATUS:
                    if (highestStatus <= OUT_OF_THRESHOLD) {
                        highestStatus = OUT_OF_THRESHOLD;
                        notificationImageView.set(outOfThresholdImageView);
                        notificationBooleanProperty.set(true);
                    }
                    break;
                case IN_THRESHOLD_STATUS:
                    if (highestStatus <= IN_THRESHOLD) {
                        highestStatus = IN_THRESHOLD;
                        notificationImageView.set(inThresholdImageView);
                        notificationBooleanProperty.set(true);
                    }
                    break;
                case CANNOT_CALCULATE_STATUS:
                    if (highestStatus <= CANNOT_CALCULATE) {
                        highestStatus = CANNOT_CALCULATE;
                        notificationImageView.set(cantCalculateImageView);
                        notificationBooleanProperty.set(true);
                    }
                    break;
                case NO_RESULT_STATUS:
                    if (highestStatus <= NO_RESULT) {
                        highestStatus = NO_RESULT;
                        notificationImageView.set(missingImageView);
                        notificationBooleanProperty.set(true);
                    }
                    break;
                default:
                    MediPiMessageBox.getInstance().makeErrorMessage("Unexpected device Status of: " + r.getResult() + " can't calculate tile status", null);
                    notificationImageView.set(missingImageView);
                    notificationBooleanProperty.set(true);
                    break;
            }
        }
    }

}
