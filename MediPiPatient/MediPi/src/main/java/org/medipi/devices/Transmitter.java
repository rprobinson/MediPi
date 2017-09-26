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

import java.io.ByteArrayOutputStream;
import org.medipi.security.UploadEncryptionAdapter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Optional;
import java.util.UUID;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.BooleanBinding;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import org.medipi.DashboardTile;
import org.medipi.MediPi;
import org.medipi.MediPiMessageBox;
import org.medipi.MediPiProperties;
import org.medipi.security.CertificateDefinitions;
import org.medipi.model.DevicesPayloadDO;
import org.medipi.model.EncryptedAndSignedUploadDO;
import org.medipi.utilities.Utilities;

/**
 * Class to display and handle the functionality for transmitting the data
 * collected by other elements to a known endpoint .
 *
 * The transmitter element shows a list of all the elements loaded into MediPi,
 * a summary of the data recorded by these devices (if present otherwise the
 * device name) with checkboxes which are enabled if data is present/ready to be
 * transmitted.
 *
 * It is allied with Scheduler class and must be instantiated AFTER all the
 * elements at startup when the scheduler is to be used. This is defined in the
 * .properties file and transmitter should be placed last in the list of
 * medipi.elementclasstokens
 *
 * Transmitter will take all available and selected data and will use the
 * EncryptedAnsSigned format to contain the data. The available data from each
 * of the elements is taken as a DeviceDataDO and added to a DevicesPayloadDO.
 * The data is then encrypted and signed using the patient's certificate and
 * transmitted using the device certificate to communicate to the concentrator
 * using TLSMA
 *
 *
 * There is some functionality left in here to be used in the future but is not
 * currently employed: Transmit Status
 *
 *
 * @author rick@robinsonhq.com
 */
public abstract class Transmitter extends Element {

    private VBox transmitterWindow;

    private static final String INTERACTION = "urn:nhs-itk:interaction:MediPi";
    private static final String OUTBOUNDPAYLOAD = "medipi.outboundpayload";
    private static final String NAME = "Transmitter";
    private static final String DISPLAYNAME = "MediPi Transmitter";

    private final ScrollPane transmitDataBoxSc = new ScrollPane();
    private final BooleanProperty isTransmitting = new SimpleBooleanProperty(false);
    private final HashMap<String, CheckBox> deviceCheckBox = new HashMap<>();
    protected String senderAddress;
    protected String recipientAddress;
    protected String auditIdentity;
    private ArrayList<Element> transmitterElementList = new ArrayList<>();
    private final BooleanProperty taskRunningProperty = new SimpleBooleanProperty(false);

    /**
     * Transmit button
     */
    public Button transmitButton;

    /**
     * Constructor for Messenger
     *
     */
    public Transmitter() {

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

        transmitterWindow = new VBox();
        transmitterWindow.setPadding(new Insets(0, 5, 0, 5));
        transmitterWindow.setSpacing(5);
        transmitterWindow.setMinSize(800, 300);
        transmitterWindow.setMaxSize(800, 300);

        ImageView iw = medipi.utils.getImageView("medipi.images.arrow", 20, 20);
        iw.setRotate(-90);
        transmitButton = new Button("Transmit", iw);
        transmitButton.setId("button-transmit");

        //assume that there's nothing to transmit when the application is first opened
        transmitButton.setDisable(true);
        VBox transmitDataBox;
        transmitDataBox = new VBox();
        transmitDataBox.setId("transmitter-text");
        transmitDataBox.setSpacing(5);

        // fundamental UI decisions made from the properties
        // loop through all loaded elements and add checkboxes and images to the window
        // CHOICE OF WHICH ELEMENTS APPEAR IN THE LIST IS CURRENTLY NOT WORKING CORRECTLY AS TRANSMITTER IS CALLED BEFORE SCHEDULER - HAVE A THINK
        ArrayList<BooleanProperty> al = new ArrayList<>();
        if (medipi.getScheduler() == null) {
            transmitterElementList = medipi.getElements();
        } else if (medipi.getScheduler().getScheduledElements() == null) {
            transmitterElementList = medipi.getElements();
        } else {
            transmitterElementList = medipi.getScheduler().getScheduledElements();
        }
        for (Element e : transmitterElementList) {
            if (Device.class.isAssignableFrom(e.getClass())) {
                Device d = (Device) e;
                String classTokenName = e.getClassTokenName();
                ImageView image = e.getImage();
                image.setFitHeight(45);
                image.setFitWidth(45);
                BorderPane imagePane = new BorderPane(image);
                imagePane.setMinSize(50, 50);
                imagePane.setId("transmitter-component");
                CheckBox tcb = new CheckBox();
                tcb.setMinWidth(600);
                tcb.setId("transmitter-text");
                // Get device data summary if present
                tcb.textProperty().bind(
                        Bindings.when(d.getResultsSummary().isEmpty())
                        .then(d.getGenericDeviceDisplayName())
                        .otherwise(d.getResultsSummary())
                );
                al.add(tcb.selectedProperty());
                tcb.disableProperty().bind(d.hasDataProperty().not());
                //check the initial state of hasDataProperty and update checkbox accordingly
                tcb.setSelected(!tcb.isDisable());
                // when data arrives make sure the checkbox is ticked as a default
                tcb.disableProperty().addListener((ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) -> {
                    tcb.setSelected(!tcb.isDisable());
                });
                HBox hb = new HBox();
                hb.setAlignment(Pos.CENTER_LEFT);
                hb.getChildren().addAll(
                        tcb,
                        imagePane
                );
                hb.setPadding(new Insets(0, 0, 0, 50));
// This functionality to display the scheduler metadata for transmission not currently in use
                // when Scheduler is loaded and transmitter is accessed as part of a schedule, show the scheduler checkbox
                // This relies on the fact that the transmitter is called AFTER the scheduler AND all the measurement devices
//                if (Scheduler.class.isAssignableFrom(d.getClass())) {
//                    Scheduler sched = (Scheduler) d;
//                    scheduler = sched;
//                    hb.visibleProperty().bind(sched.runningProperty());
//                }

                deviceCheckBox.put(classTokenName, tcb);
                transmitDataBox.getChildren().add(hb);

            }
        }
        transmitDataBoxSc.setContent(transmitDataBox);
        transmitDataBoxSc.setFitToWidth(true);
        transmitDataBoxSc.setFitToHeight(true);
        transmitDataBoxSc.setMinHeight(300);
        transmitDataBoxSc.setMaxHeight(300);
        transmitDataBoxSc.setMinWidth(800);
        transmitDataBoxSc.setId("mainwindow-dashboard-scroll");
        transmitDataBoxSc.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        transmitDataBoxSc.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);

        ObservableList<BooleanProperty> chkList;
        chkList = FXCollections.observableList(al);
        BooleanBinding bb = conjunction(chkList);
        transmitButton.disableProperty().bind(bb.not().or(medipi.wifiSync.not()).or(taskRunningProperty));

        transmitterWindow.getChildren()
                .addAll(
                        transmitDataBoxSc
                );

        // set main Element window
        window.setCenter(transmitterWindow);

        setButton2(transmitButton);

        transmitButton();

        return null;
    }

    private static BooleanBinding conjunction(ObservableList<BooleanProperty> list) {
        BooleanBinding bb = new SimpleBooleanProperty(false).or(list.get(0));
        for (int i = 1; i < list.size(); i++) {
            bb = bb.or(list.get(i));
        }
        return bb;
    }

    private void transmitButton() {

        // Setup transmit button action to run in its own thread
        transmitButton.setOnAction((ActionEvent t) -> {
            try {
                // When part of a schedule, check for data present for all the devices which are part of the schedule
                if (medipi.getScheduler().runningProperty().get()) {
                    ArrayList<Element> missing = medipi.getScheduler().getMissingDataElements();
                    StringBuilder sb = new StringBuilder();
                    if (!missing.isEmpty()) {
                        for (Element e : missing) {
                            sb.append(e.getSpecificDeviceDisplayName()).append(",\n");
                        }
                        if (sb.length() > 2) {
                            //replace the last separator with a new line
                            sb.substring(0, sb.length() - 2);
                        }
                        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
                        alert.setTitle("Scheduler Transmitter");
                        alert.setHeaderText(null);
                        alert.getDialogPane().setMaxSize(600, 300);
                        alert.getDialogPane().getStylesheets().add("file:///" + medipi.getCssfile());
                        alert.getDialogPane().setId("message-box");
                        VBox vb = new VBox();
                        Text text = new Text("You have not taken measurements for the following devices: \n\n" + sb.toString() + "\nWould you still like to transmit?");
                        text.setWrappingWidth(600);
                        text.setTextAlignment(TextAlignment.CENTER);
                        vb.getChildren().add(text);
                        vb.setAlignment(Pos.CENTER);
                        ImageView iw = medipi.utils.getImageView("medipi.images.doctor", 80, 80);
                        alert.setGraphic(iw);
                        alert.getDialogPane().setContent(vb);
                        Optional<ButtonType> result = alert.showAndWait();
                        if (result.get() == ButtonType.CANCEL) {
                            return;
                        }
                    }
                }
            } catch (Exception e) {
                MediPiMessageBox.getInstance().makeErrorMessage("", e);
            }
            Task task = new Task<Void>() {
                @Override
                protected Void call() throws Exception {
                    try {

                        // create the device payload structure for all the individual device data
                        DevicesPayloadDO devicesPayload = new DevicesPayloadDO(UUID.randomUUID().toString());
                        devicesPayload.setUploadedDate(Date.from(Instant.now()));

                        boolean doneSomething = false;
                        //Loop round the Elements adding them to the DevicesPayloadDO as separate data payloads
                        for (Element e : transmitterElementList) {
                            CheckBox cb = deviceCheckBox.get(e.getClassTokenName());
                            if (cb != null && cb.isSelected() && cb.isVisible()) {
                                if (Device.class.isAssignableFrom(e.getClass())) {
                                    Device d = (Device) e;
                                    devicesPayload.addPayload(d.getData());
                                    doneSomething = true;
                                }
                            }
                        }
                        if (doneSomething) {
                            try {
                                //Set up the encryption and signing adaptor
                                UploadEncryptionAdapter uploadEncryptionAdapter = new UploadEncryptionAdapter();
                                CertificateDefinitions cd = new CertificateDefinitions(medipi.getProperties());
                                cd.setSIGNKEYSTOREPASSWORD("medipi.patient.cert.password", CertificateDefinitions.SYSTEM);
                                cd.setSIGNKEYSTOREALIAS("medipi.patient.cert.alias", CertificateDefinitions.INTERNAL);
                                cd.setSIGNKEYSTORELOCATION("medipi.patient.cert.location", CertificateDefinitions.INTERNAL);
                                String error = uploadEncryptionAdapter.init(cd, UploadEncryptionAdapter.CLIENTMODE);
                                if (error != null) {
                                    throw new Exception(error);
                                }
                                EncryptedAndSignedUploadDO encryptedMessage = uploadEncryptionAdapter.encryptAndSign(devicesPayload);
                                try {
                                    // save a copy of the data to file if required
                                    String s = medipi.getProperties().getProperty(OUTBOUNDPAYLOAD);
                                    // if there's no entry for medipi.outbound then dont save the payloads
                                    if (s != null && s.trim().length() > 0) {
                                        FileOutputStream fop = null;

                                        try {
                                            StringBuilder fnb = new StringBuilder(s);
                                            fnb.append(System.getProperty("file.separator"));
                                            fnb.append(INTERACTION.replace(":", "_"));
                                            fnb.append("_at_");
                                            fnb.append(Utilities.INTERNAL_SPINE_FORMAT_UTC.format(Instant.now()));
                                            fnb.append(".log");
                                            File file = new File(fnb.toString());
                                            fop = new FileOutputStream(file);
                                            // if file doesnt exists, then create it
                                            if (!file.exists()) {
                                                file.createNewFile();
                                            }
                                            byte[] contentInBytes;
                                            ByteArrayOutputStream bos = new ByteArrayOutputStream();
                                            ObjectOutput out = null;
                                            try {
                                                out = new ObjectOutputStream(bos);
                                                out.writeObject(encryptedMessage);
                                                contentInBytes = bos.toByteArray();
                                            } finally {
                                                try {
                                                    if (out != null) {
                                                        out.close();
                                                    }
                                                } catch (IOException ex) {
                                                    // ignore close exception
                                                }
                                                try {
                                                    bos.close();
                                                } catch (IOException ex) {
                                                    // ignore close exception
                                                }
                                            }

                                            fop.write(contentInBytes);
                                            fop.flush();
                                            fop.close();

                                        } catch (IOException e) {
                                            MediPiMessageBox.getInstance().makeErrorMessage("Cannot save outbound message payload to local drive - check the configured directory: " + OUTBOUNDPAYLOAD, e);
                                        } finally {
                                            try {
                                                if (fop != null) {
                                                    fop.close();
                                                }
                                            } catch (IOException e) {
                                                MediPiMessageBox.getInstance().makeErrorMessage("cannot close the outbound message payload file configured by: " + OUTBOUNDPAYLOAD, e);
                                            }
                                        }

                                    }
                                    // Send message
                                    if (transmit(encryptedMessage)) {
                                        // if it is being run as part of a schedule then write TRANSMITTED line back to Schedule
                                        ArrayList<String> transmitList = new ArrayList<>();
                                        for (Element e : transmitterElementList) {
                                            CheckBox cb = deviceCheckBox.get(e.getClassTokenName());
                                            if (cb != null && cb.isSelected() && cb.isVisible()) {
                                                if (isThisElementPartOfAScheduleExecution.get()) {
                                                    transmitList.add(e.getClassTokenName());
                                                }
                                                if (Device.class.isAssignableFrom(e.getClass())) {
                                                    Device d = (Device) e;
                                                    Platform.runLater(() -> {
                                                        d.resetDevice();
                                                    });
                                                }
                                            }
                                        }
                                        if (isThisElementPartOfAScheduleExecution.get()) {
                                            medipi.getScheduler().addScheduleData("TRANSMITTED", Instant.now(), transmitList);
                                        }
                                        medipi.callDashboard();

                                        Platform.runLater(() -> {
                                            medipi.resetAllDevices();
                                            MediPiMessageBox.getInstance().makeMessage(getTransmissionResponse());
                                        });

                                    } else {
                                        Platform.runLater(() -> {
                                            MediPiMessageBox.getInstance().makeErrorMessage(getTransmissionResponse(), null);
                                        });
                                    }
                                } catch (Exception ex) {
                                    MediPiMessageBox.getInstance().makeErrorMessage("Error transmitting message to recipient", ex);
                                }
                            } catch (Exception ex) {
                                MediPiMessageBox.getInstance().makeErrorMessage("Error encrypting and signing the data payload", ex);
                            }

                        }
                    } catch (Exception ex) {
                        MediPiMessageBox.getInstance().makeErrorMessage("Error in creating the message to be transmitted", ex);
                    }
                    return null;
                }

                @Override
                protected void succeeded() {
                    super.succeeded();
                    if (isThisElementPartOfAScheduleExecution.get()) {
                        medipi.getScheduler().runningProperty().set(false);
                    }
                }

                @Override
                protected void failed() {
                    super.failed();
                    if (isThisElementPartOfAScheduleExecution.get()) {
                        medipi.getScheduler().runningProperty().set(false);
                    }
                }

                @Override
                protected void cancelled() {
                    super.cancelled();
//                    transmitButton.disableProperty().unbind();
                    if (isThisElementPartOfAScheduleExecution.get()) {
                        medipi.getScheduler().runningProperty().set(false);
                    }
                }
            };

            // Set up the bindings to control the UI elements during the running of the task
            // disable nodes when transmitting
            transmitDataBoxSc.disableProperty().bind(task.runningProperty());
            transmitterWindow.disableProperty().bind(task.runningProperty());
            taskRunningProperty.bind(task.runningProperty());
            button1.disableProperty().bind(
                    Bindings.when(task.runningProperty().and(isThisElementPartOfAScheduleExecution))
                    .then(true)
                    .otherwise(false)
            );
            // set the cursor to hourglass when running
            medipi.scene.cursorProperty().bind(Bindings.when(task.runningProperty())
                    .then(Cursor.WAIT)
                    .otherwise(Cursor.DEFAULT)
            );
            isTransmitting.bind(task.runningProperty());
            Thread th = new Thread(task);
            th.setDaemon(true);
            th.start();
        });
    }

    @Override
    public String getSpecificDeviceDisplayName() {
        return DISPLAYNAME;
    }

    @Override
    public String getGenericDeviceDisplayName() {
        return NAME;
    }

    @Override
    public BorderPane getDashboardTile() throws Exception {
        DashboardTile dashComponent = new DashboardTile(this, showTile);
        dashComponent.addTitle(NAME);
        return dashComponent.getTile();
    }

    /**
     * Transmit the message using the chosen method
     *
     * @param message - payload to be wrapped and transmitted
     * @return String - if transmission was successful return null
     */
    public abstract Boolean transmit(EncryptedAndSignedUploadDO message);

    /**
     * Get the transmission response message
     *
     * @return String - transmission response message
     */
    public abstract String getTransmissionResponse();

}
