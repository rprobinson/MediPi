/*
 Copyright 2016 Richard Robinson @ HSCIC <rrobinson@hscic.gov.uk, rrobinson@nhs.net>

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

import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Optional;
import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.event.ActionEvent;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.text.Text;
import org.medipi.MediPi;

/**
 * Element Class
 *
 * This is the primary abstract class which is the fundamental building block of
 * all Elements.
 *
 *
 * These elements are represented on the Dashboard screen with tiles and when
 * clicked, these tiles call the Element window. Each Element window uses the
 * whole screen except for the permanent banner at the top. Depending on
 * subsequent abstract classes and the concrete class, an Element can control
 * messages from the clinician, Transmission of data to the clinician or an
 * instance of a medical device. All Elements consist of this class and one or
 * more other abstract classes defining functionality of the Element. The last
 * class in the chain is a concrete class (which for example in the case of the
 * medical devices interacts with the USB enabled device itself)
 *
 * @author rick@robinsonhq.com
 */
public abstract class Element {

    private String classToken;

    /**
     * Reference to main class
     */
    protected MediPi medipi;

    /**
     * The main Element Window
     */
    protected BorderPane window = new BorderPane();

    /**
     * The bottom banner containing buttons
     */
    protected BorderPane bottom = new BorderPane();

    /**
     * The left button (1) on the bottom banner
     */
    protected Button button1 = new Button();

    /**
     * The right button (3) on the bottom banner
     */
    protected Button button3 = null;

    /**
     * The centre button (2) on the bottom banner
     */
    protected Button button2 = null;

    /**
     * Reference to the Scheduler class if present
     */
    protected Scheduler scheduler = null;

    /**
     * when this Element is called this defines if it is being called as part of
     * a scheduled chain of measurements
     */
    protected BooleanProperty isSchedule = new SimpleBooleanProperty(false);

    protected ProgressIndicator progressIndicator = new ProgressIndicator(ProgressIndicator.INDETERMINATE_PROGRESS);
    protected boolean confirm = false;
    protected BooleanProperty showTile;
    private HBox b2hb = new HBox();

    /**
     * Constructor for the Class
     */
    public Element() {

        window.setBottom(bottom);
        window.setId("background-colour");

        //bind the visibility property so that when not visible the panel doesnt take any space
        window.managedProperty().bind(window.visibleProperty());
        // set up the bottom button banner
        bottom.setMinHeight(70);
        bottom.setMaxHeight(70);
        bottom.setPadding(new Insets(0, 10, 0, 10));
        progressIndicator.setMinSize(40, 40);
        progressIndicator.setMaxSize(40, 40);
        progressIndicator.setVisible(false);
        progressIndicator.managedProperty().bind(progressIndicator.visibleProperty());
    }

    /**
     * Setter for main MediPi Class
     *
     * @param m reference to MediPi
     */
    public void setMediPi(MediPi m) {
        medipi = m;
    }

    /**
     * Allows the device window and the buttons added by this class to be
     * retrieved and placed into a calling window
     *
     * @return a node representing the specified device window and the bottom
     * button banner
     */
    public Node getWindowComponent() {
        return window;
    }

    ;
    /**
     * Allows the device window to be made visible or invisible
     *
     */
    public void hideDeviceWindow() {
        window.setVisible(false);
    }

    /**
     * setter for this Element's Class Token
     *
     * @param token
     */
    public void setClassToken(String token) {
        classToken = token;
    }

    /**
     * getter for this Element's Class Token
     *
     * @return this Element's class token
     */
    public String getClassTokenName() {
        return classToken;
    }

    /**
     * setter for an instance of Scheduler
     *
     * @param s the instance of scheduler to be set
     */
    public void setScheduler(Scheduler s) {
        scheduler = s;
    }

    /**
     * Allows the device window to be made visible or invisible. This is a
     * recursive class which calls itself as many times as ElementClass Tokens
     * there are in the chain
     *
     * @param ClassTokenChain an array list of Element Class Tokens defining the
     * order of the scheduled measurements
     */
    public void callDeviceWindow(ArrayList<String> ClassTokenChain) {
        if (ClassTokenChain == null) {
            // == null : normal mode when no schedule is being run
            ImageView iw = medipi.utils.getImageView("medipi.images.arrow", 20, 20);
            iw.setRotate(180);
            Button b3 = new Button("Back", iw);
            b3.setId("button-back");
            b3.setOnAction((ActionEvent t) -> {
                if (confirmation()) {
                    medipi.callDashboard();
                }
            });
            isSchedule.set(false);
            setButton3(b3);
            setButton1(null);
        } else {
            // as part of a scheduled list of element Class Tokens
            // The Cancel Schedule button doesn't get disabled when any of the 
            // Element tasks are in operation - should these be stopped if cancel
            // is activated? 
            Button b1 = new Button("Cancel", medipi.utils.getImageView("medipi.images.cancel", 20, 20));
            b1.setId("button-cancel");
            b1.setOnAction((ActionEvent t) -> {
                medipi.callDashboard();
                scheduler.runningProperty().set(false);
            });
            if (ClassTokenChain.isEmpty()) {
                // Last token
                isSchedule.set(true);
                setButton1(b1);
                setButton3(null);
            } else {
                //where there are >1 scheduled elements left to call
                String nextDevice = ClassTokenChain.get(0);
                ArrayList<String> remainingDevices = new ArrayList(ClassTokenChain.subList(1, ClassTokenChain.size()));

                Button b3 = new Button("Next", medipi.utils.getImageView("medipi.images.arrow", 20, 20));
                b3.setId("button-next");
                b3.setOnAction((ActionEvent t) -> {
                    if (confirmation()) {
                        Element e = medipi.getElement(nextDevice);
                        e.callDeviceWindow(remainingDevices);
                        if (Device.class.isAssignableFrom(Element.this.getClass())) {
                            Device d = (Device) Element.this;
                            if (d.hasDataProperty().get()) {
                                scheduler.addScheduleData(Scheduler.MEASURED, Instant.now(), new ArrayList<>(Arrays.asList(getClassTokenName())));
                            }
                        }
                    }
                });

                isSchedule.set(true);
                setButton3(b3);
                setButton1(b1);

            }

        }
        medipi.hideAllWindows();
        window.setVisible(true);
    }

    private boolean confirmation() {
        if (confirm) {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION, "", ButtonType.YES, ButtonType.NO);
            alert.setTitle(getDisplayName());
            alert.setHeaderText(getDisplayName());
            Text text = new Text("Confirm that the input value from " + getDisplayName() + " is correct?");
            text.setWrappingWidth(400);
            alert.getDialogPane().setContent(text);

            Optional<ButtonType> result = alert.showAndWait();
            if (result.get() == ButtonType.YES) {
                return true;
            }
            return false;

        } else {
            return true;
        }
    }

    /**
     * Set the centre button (2) on the bottom panel.
     *
     * Not very happy with this - it's clunky and needs rethinking
     *
     * @param b node which could contain a button or a HBox containing >1 node.
     * To be added to the left hand node of the bottom panel
     */
    protected void setButton2(Button b) {
        bottom.getChildren().remove(b2hb);
        if (b != null) {
            button2 = b;
            b2hb = new HBox();
            b2hb.setSpacing(10);
            b2hb.getChildren().addAll(button2, progressIndicator);
            b2hb.setAlignment(Pos.TOP_CENTER);
            bottom.setCenter(b2hb);
        }
    }

    public void setButton2Name(String name) {
        setButton2Name(name, null);
    }

    public void setButton2Name(String name, Node graphic) {
        if (Platform.isFxApplicationThread()) {
            button2.setText(name);
            button2.setGraphic(graphic);
        } else {
            Platform.runLater(() -> {
                button2.setText(name);
                button2.setGraphic(graphic);
            });
        }
    }

    /**
     * Set the Left hand button (1) on the bottom panel.
     *
     * Not very happy with this - it's clunky and needs rethinking
     *
     * @param b button to be added to the left node of the bottom panel
     */
    protected void setButton1(Button b) {
        bottom.getChildren().remove(button1);
        if (b != null) {
            button1 = b;
            bottom.setLeft(button1);
        }
    }

    /**
     * Set the Right hand button (3) on the bottom panel.
     *
     * Not very happy with this - it's clunky and needs rethinking
     *
     * @param b button to be added to the Right hand node of the bottom panel
     */
    protected void setButton3(Button b) {
        bottom.getChildren().remove(button3);
        if (b != null) {
            button3 = b;
            bottom.setRight(button3);
        }
    }

    /**
     * getter for this Element's image
     *
     * @return and imageView for this Element
     */
    public ImageView getImage() {
        return medipi.utils.getImageView(MediPi.ELEMENTNAMESPACESTEM + classToken + ".image", null, null);
    }

    /**
     * setter for this Element's title
     *
     *
     */
    public void setElementTitle() {
        Label title = new Label(this.getDisplayName());
        title.setId("element-title");
        window.setTop(title);
    }

    /**
     * Abstract initiation method called for this Element
     *
     * @return boolean of its success
     * @throws Exception which gets caught in MediPi class and results in
     * MediPiMessageBox
     */
    public abstract String init() throws Exception;

    /**
     * Abstract getter for the Element Name
     *
     * @return String representation of the Element's Name
     */
    public abstract String getDisplayName();

    /**
     * Abstract method to get the dashboard tile
     *
     * @return the tile for inserting in the home Dashboard view
     * @throws Exception
     */
    public abstract BorderPane getDashboardTile() throws Exception;

}
