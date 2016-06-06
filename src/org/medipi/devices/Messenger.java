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
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.Separator;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import org.medipi.DashboardTile;
import org.medipi.MediPi;
import org.medipi.MediPiMessageBox;
import org.medipi.MediPiProperties;


/**
 * Class to display and handle the functionality for a incoming read-only
 * message display utility.
 *
 * This is a simple viewer of incoming messages from the clinician. It shows the
 * message title in a list of all messages received and displays the contents of
 * the selected message. As MediPi does not expose any inbound ports, incoming
 * messaging is achieved through periodic polling of a secure location. Any new
 * messages received are digested and the UI is updated. A new unread message
 * alerts the dashboard Tile class to superimpose an alert image. All messages
 * are persisted locally to a configurable file location.
 *
 * There is no view mode for this UI.
 *
 * @author rick@robinsonhq.com
 */

public class Messenger extends Element {

    private static final String NAME = "Clinician's Messages";
    private static final String MEDIPIIMAGESEXCLAIM = "medipi.images.exclaim";
    private TextArea messageView;
    private VBox messengerWindow;
    // not sure what the best setting for this thread pool is but 3 seems to work
    private static final int TIMER_THREAD_POOL_SIZE = 3;
    private String messageDir;
    private ImageView alertImageView;

    private final BooleanProperty alertBooleanProperty = new SimpleBooleanProperty(false);

    private TableView<Message> messageList;

    /**
     * Constructor for Messenger
     *
     */
    public Messenger() {

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
        messengerWindow = new VBox();
        messengerWindow.setPadding(new Insets(0, 5, 0, 5));
        messengerWindow.setSpacing(5);
        messengerWindow.setMinSize(800, 350);
        messengerWindow.setMaxSize(800, 350);
        // get the image file for the alert image to be superimposed on the dashboard tile when a new message is received
        String alertImageFile = medipi.getProperties().getProperty(MEDIPIIMAGESEXCLAIM);
        alertImageView = new ImageView("file:///" + alertImageFile);

        // Create the view of the message content - scrollable
        messageView = new TextArea();
        messageView.setWrapText(true);
        messageView.isResizable();
        messageView.setEditable(false);
        messageView.setId("messenger-messagecontent");
        messageView.setMaxHeight(100);
        messageView.setMinHeight(100);
        ScrollPane viewSP = new ScrollPane();
        viewSP.setContent(messageView);
        viewSP.setFitToWidth(true);
        viewSP.setFitToHeight(true);
        viewSP.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        viewSP.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        // location of the persistent message store
        messageDir = medipi.getProperties().getProperty(MediPi.ELEMENTNAMESPACESTEM + uniqueDeviceName + ".incomingmessagedirectory");
        if (messageDir == null || messageDir.trim().length() == 0) {
            throw new Exception("Message Directory parameter not configured");
        }
        Path dir = Paths.get(messageDir);
        messageList = new TableView<>();
        messageList.setId("messenger-messagelist");

        //Create the table of message
        // Message title list - scrollable and selectable 
        ObservableList<Message> items = FXCollections.observableArrayList();
        TableColumn messageTitleTC = new TableColumn("Message Title");
        messageTitleTC.setMinWidth(400);
        messageTitleTC.setCellValueFactory(
                new PropertyValueFactory<>("messageTitle"));
        TableColumn timeTC = new TableColumn("Time");
        timeTC.setMinWidth(100);
        timeTC.setCellValueFactory(
                new PropertyValueFactory<>("time"));

        File list[] = new File(dir.toString()).listFiles();

        // Comparitor to sort the messages by date in the message list - This is possible because 
        // the messages have a filename of format yyyyMMddHHmmss-messagename.txt 
        Arrays.sort(list, (File f1, File f2) -> Long.valueOf(f2.lastModified()).compareTo(f1.lastModified()));
        for (File f : list) {
            items.add(new Message(f.getName()));
        }
        messageList.setMinHeight(140);
        messageList.setMaxHeight(140);
        messageList.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
        // Update the message text area when a new message is selected
        messageList.getSelectionModel().selectedItemProperty().addListener((ObservableValue<? extends Message> ov, Message old_val, Message new_val) -> {
            try {
                File file2 = new File(dir.toString(), new_val.getFileName());
                messageView.setText(readFile(file2.getPath(), StandardCharsets.UTF_8));
            } catch (Exception ex) {
                messageView.clear();
            }
        });
        messageList.setItems(items);
        messageList.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        messageList.getColumns().addAll(messageTitleTC, timeTC);
        ScrollPane listSP = new ScrollPane();
        listSP.setContent(messageList);
        listSP.setFitToWidth(true);
        listSP.setPrefHeight(140);
        listSP.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        listSP.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        messageList.getSelectionModel().select(0);
        // Call the MessageWatcher class which will update the message list if 
        // a new txt file appears in the configured incoming message directory
        try {
            MessageWatcher mw = new MessageWatcher(dir, this);
        } catch (IOException ioe) {
            return "Message Watcher failed to initialise" + ioe.getMessage();
        }
        Label listLabel = new Label("Message List");
        listLabel.setId("messenger-list");
        Label textLabel = new Label("Message Text");
        textLabel.setId("messenger-text");

        messengerWindow.getChildren().addAll(
                listLabel,
                messageList,
                new Separator(Orientation.HORIZONTAL),
                textLabel,
                messageView,
                new Separator(Orientation.HORIZONTAL)
        );

        // set main Element window
        window.setCenter(messengerWindow);

        // if the dashboard tile has an alert superimposed remove it upon showing this element
        window.visibleProperty().addListener((ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) -> {
            if (newValue) {
                alertBooleanProperty.setValue(false);
            }
        });

        // Start the incoming message timer. This wakes up every definable period (default set to 30s) 
        // and performs functions to interrogate a secure remote directory to check for new files(messages)
        try {
            String time = medipi.getProperties().getProperty(MediPi.ELEMENTNAMESPACESTEM + uniqueDeviceName + ".pollincomingmsgperiod");
            if (time == null || time.trim().length() == 0) {
                time = "30";
            }
            String url = medipi.getProperties().getProperty(MediPi.ELEMENTNAMESPACESTEM + uniqueDeviceName + ".messageserver");
            if (url == null || url.trim().length() == 0) {
                throw new Exception("Unable to start the incoming message service - Message server URL is not set in configuration");
            }
            Integer incomingMessageCheckPeriod = Integer.parseInt(time);
            ScheduledThreadPoolExecutor timer = new ScheduledThreadPoolExecutor(TIMER_THREAD_POOL_SIZE);
            PollIncomingMessage pim = new PollIncomingMessage(dir, medipi.getPatientNHSNumber(), url);
            timer.scheduleAtFixedRate(pim, (long) 1, (long) incomingMessageCheckPeriod, TimeUnit.SECONDS);
        } catch (NumberFormatException e) {
            return "Unable to start the incoming message service - make sure that " + MediPi.ELEMENTNAMESPACESTEM + uniqueDeviceName + ".pollincomingmsgperiod property is set correctly";
        }

        // successful initiation of the this class results in a null return
        return null;
    }

    /**
     * Method which returns a booleanProperty which UI elements can be bound to,
     * to discover whether a new message has arrived
     *
     * @return BooleanProperty signalling the presence of a new message
     */
    protected BooleanProperty getAlertBooleanProperty() {
        return alertBooleanProperty;
    }

    /**
     * getter method for the messageList so that the MessageWatcher can update it
     * @return
     */
    protected TableView<Message> getMessageList() {
        return messageList;
    }

    private static String readFile(String path, Charset encoding)
            throws IOException {
        byte[] encoded = Files.readAllBytes(Paths.get(path));
        return new String(encoded, encoding);
    }

    /**
     * getter for the Element Name
     *
     * @return String representation of the Element's Name
     */
    @Override
    public String getName() {
        return NAME;
    }


    /**
     * method to return the component to the dashboard
     *
     * @return @throws Exception
     */
    @Override
    public BorderPane getDashboardTile() throws Exception {
        DashboardTile dashComponent = new DashboardTile(this);
        dashComponent.addTitle(getName());
        dashComponent.addOverlay(alertImageView, alertBooleanProperty);
        return dashComponent.getTile();
    }

    /**
     * A method to allow callback failure of the messageWatcher
     *
     * @param failureMessage
     * @param e exception
     */
    protected void callFailure(String failureMessage, Exception e) {
        MediPiMessageBox.getInstance().makeErrorMessage(failureMessage, e, Thread.currentThread());
    }

}
