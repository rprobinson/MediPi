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
package org.medipi.practitionerdevices;

import org.medipi.MediPi;
import org.medipi.MediPiMessageBox;
import org.medipi.MediPiProperties;
import org.medipi.DashboardTile;
import org.medipi.devices.Element;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.io.InputStreamReader;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Date;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Separator;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import org.medipi.logging.MediPiLogger;
import org.medipi.devices.drivers.BeurerBF480;
import org.medipi.utilities.Utilities;

/**
 * Class to send a simple text message to a particular mediPi recipient
 *
 * This is a simple interface to create a text message and send it to a MediPi
 * recipient using the NHS number as a unique identifier. This method of
 * identification isn't a long term solution as MediPi will not use any patient
 * identifying IDs for security reasons, instead it will use psudonynised IDs.
 * 
 * The message is written to a secure directory to be picked up by the client MediPi system
 *
 * There is no view mode for this UI.
 *
 * @author rick@robinsonhq.com
 */
public class PractitionerMessenger extends Element {

    private static final String NAME = "Clinician's Messages";
    private TextArea messageView;
    private TextField recipient;
    private VBox messengerWindow;
    private String messageDir;
    private BorderPane buttonBP;

    public PractitionerMessenger() {

    }

    @Override
    public String init() {

        String uniqueDeviceName = getClassTokenName();
        messengerWindow = new VBox();
        messengerWindow.setPadding(new Insets(5, 5, 5, 5));
        messengerWindow.setSpacing(5);
        messengerWindow.setAlignment(Pos.CENTER);
        //bind the visibility property so that when not visible the panel doesnt take any space
        messengerWindow.managedProperty().bind(messengerWindow.visibleProperty());
        HBox topBox = new HBox();
        topBox.setPadding(new Insets(5, 5, 5, 5));
        topBox.setSpacing(5);
        topBox.setAlignment(Pos.CENTER);
        Label rLabel = new Label("Recipient NHS Number");
        recipient = new TextField();
        recipient.setText(medipi.getPatientNHSNumber());
        Label msgLabel = new Label("Message Title");
        TextField msgName = new TextField();
        msgName.setPromptText("Message Title");
        Button sendButton = new Button("Send");
        topBox.getChildren().setAll(
                msgLabel,
                msgName,
                rLabel,
                recipient
        );
        messageView = new TextArea();
        messageView.setWrapText(true);
        messageView.isResizable();
        messageView.setPrefHeight(400);
        ScrollPane viewSP = new ScrollPane();
        viewSP.setContent(messageView);
        viewSP.setFitToWidth(true);
        viewSP.setFitToHeight(true);
//        viewSP.setPrefHeight(300);
        viewSP.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        viewSP.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        messageDir = medipi.getProperties().getProperty(MediPi.ELEMENTNAMESPACESTEM + uniqueDeviceName + ".outgoingmessagedirectory");
        if (messageDir == null || messageDir.trim().length() == 0) {
            String error = "Message directory " + MediPi.ELEMENTNAMESPACESTEM + uniqueDeviceName + ".outgoingmessagedirectory parameter not configured";
            MediPiLogger.getInstance().log(BeurerBF480.class.getName(), error);
            return error;
        }
        Path dir = Paths.get(messageDir);

        String dest = medipi.getProperties().getProperty(MediPi.ELEMENTNAMESPACESTEM + uniqueDeviceName + ".messagedestination");
        if (dest == null || dest.trim().length() == 0) {
            String error = "Message directory " + MediPi.ELEMENTNAMESPACESTEM + uniqueDeviceName + ".messagedestination parameter not configured";
            MediPiLogger.getInstance().log(BeurerBF480.class.getName(), error);
            return error;
        }

        buttonBP = new BorderPane();

        messengerWindow.getChildren().addAll(
                topBox,
                new Separator(Orientation.HORIZONTAL),
                messageView,
                new Separator(Orientation.HORIZONTAL),
                sendButton,
                buttonBP);

        sendButton.setOnAction((ActionEvent t) -> {
            if (recipient.getText().trim().length() == 0) {
                MediPiMessageBox.getInstance().makeErrorMessage("a recipient for the message must be entered", null, Thread.currentThread());
            } else if (msgName.getText().replaceAll("[!@#$%^&*]", "").trim().length() == 0) {
                MediPiMessageBox.getInstance().makeErrorMessage("a valid name for the message must be entered", null, Thread.currentThread());
            } else if (messageView.getText().trim().length() == 0) {
                MediPiMessageBox.getInstance().makeErrorMessage("The message has no content", null, Thread.currentThread());
            } else {
                Task task = new Task<Void>() {
                    @Override
                    protected Void call() throws Exception {
                        try {
                            StringBuilder msgTitle = new StringBuilder(Utilities.INTERNAL_FORMAT.format(new Date()));
                            msgTitle.append("-");
                            msgTitle.append(msgName.getText().replaceAll("[!@#$%^&*]", "").replaceAll(" ", "_"));
                            msgTitle.append(".txt");
                            try (FileWriter fw = new FileWriter(dir.toString() + File.separator + recipient.getText() + File.separator + msgTitle.toString())) {
                                fw.write(messageView.getText());
                            }
                            String pscp = "pscp -pw C4rtimandua \"%%PATH%%\\%%MSGTITLE%%\" %%DEST%%/%%USERNAME%%/";
                            String command = pscp.replace("%%PATH%%", dir.toString());
                            command = command.replace("%%MSGTITLE%%", msgTitle.toString());
                            command = command.replace("%%DEST%%", dest.trim());
                            command = command.replace("%%USERNAME%%", recipient.getText());
                            if (medipi.getDebugMode() == MediPi.DEBUG) {
                                System.out.println(command);
                            }
                            Process p = Runtime.getRuntime().exec(command);
                            BufferedReader stdInput = new BufferedReader(new InputStreamReader(p.getInputStream()));
                            String response = "";
                            String temp = "";
                            while ((temp = stdInput.readLine()) != null) {
                                response += temp;
                            }
                            if (medipi.getDebugMode() == MediPi.DEBUG) {
                                System.out.println("retreival for incomingmessage: " + response);
                            }
                        } catch (Exception e) {
                            MediPiMessageBox.getInstance().makeErrorMessage("Unable to retreive incoming messages", e, Thread.currentThread());
                        }
                        return null;
                    }
                    
                    @Override
                    protected void succeeded() {
                        super.succeeded();
                    }
                    
                    @Override
                    protected void scheduled() {
                        super.scheduled();
                    }
                    
                    @Override
                    protected void failed() {
                        super.failed();
                    }
                    
                    @Override
                    protected void cancelled() {
                        super.failed();
                    }
                };
                sendButton.disableProperty().bind(task.runningProperty());
                Thread th = new Thread(task);
                th.setDaemon(true);
                th.start();
            }
        });
        return null;

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

}
