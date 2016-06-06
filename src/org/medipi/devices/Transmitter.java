/*
 Copyright 2016  Richard Robinson @ HSCIC <rrobinson@hscic.gov.uk, rrobinson@nhs.net>

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
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.value.ObservableValue;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import org.warlock.itk.distributionenvelope.DistributionEnvelope;
import org.warlock.itk.distributionenvelope.Payload;
import org.medipi.DashboardTile;
import org.medipi.MediPiMessageBox;
import org.medipi.MediPiProperties;
import org.medipi.utilities.Utilities;

/**
 * Class to display and handle the functionality for transmitting the data
 * collected by other elements to a known endpoint .
 *
 * The transmitter element shows a list of all the elements loaded into MediPi
 * with checkboxes which are enabled if data is present/ready to be transmitted.
 *
 * It is allied with Scheduler class and must be instantiated AFTER all the
 * elements at startup when the scheduler is to be used. This is defined in the
 * .properties file and transmitter should be placed last in the list of
 * medipi.elementclasstokens
 *
 * Transmitter will take all available and selected data and will use the
 * Distribution Envelope format to contain the data. The available data from
 * each of the elements is taken, compressed if applicable and individually
 * encrypted. These payloads are added to the Distribution Envelope structure
 * with their own profile Id. The Distribution Envelope is then transmitted
 * using the transport method defined
 *
 * There is no view mode for this UI.
 *
 * There is some functionality left in here to be used in the future but is not
 * currently employed: Transmit Status
 *
 *
 * @author rick@robinsonhq.com
 */
public abstract class Transmitter extends Element {

    private VBox transmitterWindow;

    private static final String[] TRANSMITLABELSTATUS = {"Select data to transmit and press Transmit", "Transmitting data...", "Completed"};
    private static final String SERVICE = "urn:nhs-itk:services:201005:MediPi";
    private static final String INTERACTION = "urn:nhs-itk:interaction:MediPi";
    private static final String BUSINTERACTION = "urn:nhs-itk:ns:201005:ackrequested";
    private static final String INFINTERACTION = "urn:nhs-itk:ns:201005:infackrequested";
    private static final String OUTBOUNDPAYLOAD = "medipi.outboundpayload";
    private static final String CLEARALLAFTERTRANSMISSION = "medipi.element.Transmitter.clearallaftertransmission";
    private static final String NAME = "Transmitter";

    private Label transmitStatus;
    private VBox transmitDataBox;
    private final BooleanProperty isTransmitting = new SimpleBooleanProperty(false);
    private final HashMap<String, CheckBox> deviceCheckBox = new HashMap<>();
    private boolean clearAllAfterTransmission = false;
    protected String senderAddress;
    protected String recipientAddress;
    protected String auditIdentity;

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

        transmitterWindow = new VBox();
        transmitterWindow.setPadding(new Insets(0, 5, 0, 5));
        transmitterWindow.setSpacing(5);
        transmitterWindow.setMinSize(800, 350);
        transmitterWindow.setMaxSize(800, 350);

        transmitStatus = new Label(TRANSMITLABELSTATUS[0]);
        transmitStatus.setMinWidth(400);
        ImageView iw = medipi.utils.getImageView("medipi.images.arrow", 20, 20);
        iw.setRotate(-90);
        transmitButton = new Button("Transmit", iw);
        transmitButton.setId("button-transmit");

        //assume that there's nothing to transmit when the application is first opened
        transmitButton.setDisable(true);
        transmitDataBox = new VBox();
        transmitDataBox.setSpacing(5);
        Label title = new Label("Device Data to be transmitted");
        title.setId("transmitter-text");
        transmitDataBox.getChildren().add(title);

        // fundamental UI decisions made from the properties
        String b = medipi.getProperties().getProperty(CLEARALLAFTERTRANSMISSION);
        clearAllAfterTransmission = !(b == null || b.trim().length() == 0 || b.toLowerCase().startsWith("n"));

        // loop through all loaded elements and add checkboxes and images to the window
        for (Element e : medipi.getElements()) {
            if (Device.class.isAssignableFrom(e.getClass())) {
                Device d = (Device) e;
                String classTokenName = e.getClassTokenName();
                ImageView image = e.getImage();
                image.setFitHeight(45);
                image.setFitWidth(45);
                BorderPane imagePane = new BorderPane(image);
                imagePane.setMinSize(55, 55);
                imagePane.setId("transmitter-component");
                CheckBox tcb = new CheckBox();
                tcb.setMinWidth(300);
                tcb.setId("transmitter-text");
                tcb.setText(d.getName());
                tcb.disableProperty().bind(d.hasDataProperty().not());
                //check the initial state of hasDataProperty and update checkbox accordingly
                tcb.setSelected(!tcb.isDisable());
                // when data arrives make sure the checkbox is ticked as a default
                tcb.disableProperty().addListener((ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) -> {
                    tcb.setSelected(!tcb.isDisable());
                });
                tcb.selectedProperty().addListener((ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) -> {
                    transmitButtonDisableDecider();
                });
                HBox hb = new HBox();
                hb.setAlignment(Pos.CENTER_LEFT);
                hb.getChildren().addAll(
                        tcb,
                        imagePane
                );
                hb.setPadding(new Insets(0, 0, 0, 200));
                // when Scheduler is loaded and transmitter is accessed as part of a schedule, show the scheduler checkbox
                if (Scheduler.class.isAssignableFrom(d.getClass())) {
                    Scheduler sched = (Scheduler) d;
                    scheduler = sched;
                    hb.visibleProperty().bind(sched.runningProperty());
                }

                deviceCheckBox.put(classTokenName, tcb);
                transmitDataBox.getChildren().add(hb);

            }
        }
        // set the transmit button initial disable state based on the initial state of the checkboxes
        transmitButtonDisableDecider();

        transmitterWindow.getChildren()
                .addAll(
                        transmitDataBox,
                        new Separator(Orientation.HORIZONTAL)
                );

        // set main Element window
        window.setCenter(transmitterWindow);

        setButton2(transmitButton);

        transmitButton();

        return null;
    }

    private void transmitButton() {
        // Setup transmit button action to run in its own thread
        transmitButton.setOnAction((ActionEvent t) -> {
            Task task = new Task<Void>() {
                @Override
                protected Void call() throws Exception {
                    updateMessage(TRANSMITLABELSTATUS[1]);
                    try {

                        // Create the DistributionEnvelope structure to put the data into.
                        DistributionEnvelope dout = DistributionEnvelope.newInstance();
                        dout.addIdentity(null, auditIdentity);
                        dout.addRecipient(null, recipientAddress);
                        dout.addSender(null, senderAddress);
                        dout.setService(SERVICE);
                        dout.addHandlingSpecification(DistributionEnvelope.INTERACTIONID, INTERACTION);
                        // infrastructure and business acks have been commented out until it's established what the messaging details are for MediPi
                        // dout.addHandlingSpecification(INFINTERACTION, "true");
                        // dout.addHandlingSpecification(BUSINTERACTION, "true");
                        boolean doneSomething = false;
                        //Loop round the Elements adding them to the DistributionEnvelope as separate payloads
                        for (Element e : medipi.getElements()) {
                            CheckBox cb = deviceCheckBox.get(e.getClassTokenName());
                            if (cb != null && cb.isSelected() && cb.isVisible()) {
                                if (Device.class.isAssignableFrom(e.getClass())) {
                                    Device d = (Device) e;
                                    byte[] b = d.getData().getBytes();
                                    //All payloads assumed to be in CSV text format
                                    Payload p = new Payload("text/csv");
                                    p.setContent(b, true);
                                    p.setProfileId(d.getProfileId());
                                    dout.addPayload(p);

                                    doneSomething = true;
                                }
                            }
                        }
                        if (doneSomething) {
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
                                        fnb.append(Utilities.INTERNAL_SPINE_FORMAT.format(new Date()));
                                        fnb.append(".log");
                                        File file = new File(fnb.toString());
                                        fop = new FileOutputStream(file);
                                        // if file doesnt exists, then create it
                                        if (!file.exists()) {
                                            file.createNewFile();
                                        }

                                        // get the content in bytes
                                        byte[] contentInBytes = dout.toString().getBytes();

                                        fop.write(contentInBytes);
                                        fop.flush();
                                        fop.close();

                                    } catch (IOException e) {
                                        MediPiMessageBox.getInstance().makeErrorMessage("Cannot save outbound message payload to local drive - check the configured directory: " + OUTBOUNDPAYLOAD, e, Thread.currentThread());
                                    } finally {
                                        try {
                                            if (fop != null) {
                                                fop.close();
                                            }
                                        } catch (IOException e) {
                                            MediPiMessageBox.getInstance().makeErrorMessage("cannot close the outbound message payload file configured by: " + OUTBOUNDPAYLOAD, e, Thread.currentThread());
                                        }
                                    }

                                }
                                // Send message
                                if (transmit(dout)) {
                                    // if it is being run as part of a schedule then write TRANSMITTED line back to Schedule
                                    if (isSchedule.get()) {
                                        ArrayList<String> transmitList = new ArrayList<>();
                                        for (Element e : medipi.getElements()) {
                                            CheckBox cb = deviceCheckBox.get(e.getClassTokenName());
                                            if (cb != null && cb.isSelected() && cb.isVisible()) {
                                                transmitList.add(e.getClassTokenName());
                                            }
                                        }
                                        scheduler.addScheduleData("TRANSMITTED", new Date(), transmitList);
                                        medipi.callDashboard();
                                    } else {
                                    }
                                    Platform.runLater(() -> {
                                        MediPiMessageBox.getInstance().makeMessage("Transmission Sucessful: " + getTransmissionResponse(), Thread.currentThread());
                                    });

                                } else {
                                    Platform.runLater(() -> {
                                        MediPiMessageBox.getInstance().makeErrorMessage("Transmission Failed: " + getTransmissionResponse(), null, Thread.currentThread());
                                    });
                                }
                            } catch (Exception ex) {
                                MediPiMessageBox.getInstance().makeErrorMessage("Error transmitting message to recipient", ex, Thread.currentThread());
                            }

                        }
                    } catch (Exception ex) {
                        MediPiMessageBox.getInstance().makeErrorMessage("Error in creating the message to be transmitted", ex, Thread.currentThread());
                    }
                    updateMessage(TRANSMITLABELSTATUS[2]);
                    return null;
                }

                @Override
                protected void succeeded() {
                    super.succeeded();
                    transmitButton.disableProperty().unbind();
                    if (isSchedule.get()) {
                        scheduler.runningProperty().set(false);
                    }
                    if (clearAllAfterTransmission) {
                        medipi.resetAllDevices();
                    }
                }

                @Override
                protected void failed() {
                    super.failed();
                    transmitButton.disableProperty().unbind();
                    if (isSchedule.get()) {
                        scheduler.runningProperty().set(false);
                    }
                }

                @Override
                protected void cancelled() {
                    super.failed();
                    transmitButton.disableProperty().unbind();
                    if (isSchedule.get()) {
                        scheduler.runningProperty().set(false);
                    }
                }
            };

            // Set up the bindings to control the UI elements during the running of the task
            // disable nodes when transmitting
            transmitDataBox.disableProperty().bind(task.runningProperty());
            transmitterWindow.disableProperty().bind(task.runningProperty());
            transmitButton.disableProperty().bind(task.runningProperty());
            button1.disableProperty().bind(
                    Bindings.when(task.runningProperty().and(isSchedule))
                    .then(true)
                    .otherwise(false)
            );
            // set the cursor to hourglass when running
            medipi.scene.cursorProperty().bind(Bindings.when(task.runningProperty())
                    .then(Cursor.WAIT)
                    .otherwise(Cursor.DEFAULT)
            );
            //tie the text to execution
            transmitStatus.textProperty().bind(
                    Bindings.when(task.runningProperty().not())
                    .then(TRANSMITLABELSTATUS[0])
                    .otherwise(task.messageProperty())
            );
            isTransmitting.bind(task.runningProperty());
            Thread th = new Thread(task);
            th.setDaemon(true);
            th.start();
        });
    }

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public BorderPane getDashboardTile() throws Exception {
        DashboardTile dashComponent = new DashboardTile(this);
        dashComponent.addTitle(getName());
        return dashComponent.getTile();
    }

    /**
     * This method decides whether based upon the checked transmission
     * checkboxes for each of the devices if the transmit button should be
     * enabled or disabled
     */
    private void transmitButtonDisableDecider() {
        boolean anyTransmitCheckboxSelected = false;
        for (Element e : medipi.getElements()) {
            CheckBox cb = deviceCheckBox.get(e.getClassTokenName());
            if (cb != null && cb.isSelected() && cb.isVisible()) {
                anyTransmitCheckboxSelected = true;
            }
        }
        transmitButton.setDisable(!anyTransmitCheckboxSelected);
    }

    /**
     * Transmit the message using the chosen method
     *
     * @param message - payload to be wrapped and transmitted
     * @return String - if transmission was successful return null
     */
    public abstract Boolean transmit(DistributionEnvelope message);

    /**
     * Get the transmission response message
     *
     * @return String - transmission response message
     */
    public abstract String getTransmissionResponse();

}
