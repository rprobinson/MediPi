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
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
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
    protected Label b2Label = new Label();

    /**
     * when this Element is called this defines if it is being called as part of
     * a scheduled chain of measurements
     */
    protected BooleanProperty isThisElementPartOfAScheduleExecution = new SimpleBooleanProperty(false);

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
        bottom.setMinHeight(50);
        bottom.setMaxHeight(50);
        bottom.setPadding(new Insets(0, 10, 10, 10));
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
     * Allows the device window to be made visible or invisible.
     *
     */
    public void callDeviceWindow() {
        // == null : normal mode when no schedule is being run
        ImageView iw = medipi.utils.getImageView("medipi.images.arrow", 20, 20);
        iw.setRotate(180);
        Button b1 = new Button("Back", iw);
        b1.setId("button-back");
        b1.setAlignment(Pos.CENTER_RIGHT);
        b1.setOnAction((ActionEvent t) -> {
            if (confirmation()) {
                medipi.callDashboard();
            }
        });
        // This dummy button is present to allow binding against it for disabling the next button in schedule mode
        Button b3 = new Button("Dummy");
        b3.setVisible(false);
        isThisElementPartOfAScheduleExecution.set(false);
        setButton1(b1);
        setButton3(b3);
        medipi.hideAllWindows();
        window.setVisible(true);
    }

    /**
     * Allows the device window to be made visible or invisible. This is a
     * recursive class which calls itself as many times as ElementClass Tokens
     * there are in the chain
     *
     * @param fwdClassTokenChain an array list of Element Class Tokens defining
     * the order of the scheduled measurements
     */
    public void callDeviceWindow(ArrayList<String> bckClassTokenChain, ArrayList<String> fwdClassTokenChain) {
        // as part of a scheduled list of element Class Tokens
        // The Cancel Schedule button doesn't get disabled when any of the 
        // Element tasks are in operation - should these be stopped if cancel
        // is activated? 
        //where there are >1 scheduled elements left to call

        if (bckClassTokenChain.isEmpty()) {
            // first token
            ImageView iw = medipi.utils.getImageView("medipi.images.arrow", 20, 20);
            iw.setRotate(180);
            Button b1 = new Button("Back", iw);
            b1.setId("button-back");
            b1.setAlignment(Pos.CENTER_RIGHT);
            b1.setOnAction((ActionEvent t) -> {
                if (confirmation()) {
                    medipi.getScheduler().callDeviceWindow();
                    medipi.getScheduler().runningProperty().set(false);
                }
            });
            isThisElementPartOfAScheduleExecution.set(true);
            Button b3 = makeNextButton(fwdClassTokenChain, bckClassTokenChain);
            setButton1(b1);
            setButton3(b3);
        } else if (fwdClassTokenChain.isEmpty()) {
            // Last token
            isThisElementPartOfAScheduleExecution.set(true);
            Button b1 = makeBackButton(bckClassTokenChain, fwdClassTokenChain);
            setButton1(b1);
            setButton3(null);
        } else {

            Button b1 = makeBackButton(bckClassTokenChain, fwdClassTokenChain);

            Button b3 = makeNextButton(fwdClassTokenChain, bckClassTokenChain);

            isThisElementPartOfAScheduleExecution.set(true);
            setButton3(b3);
            setButton1(b1);

        }

        medipi.hideAllWindows();
        window.setVisible(true);
    }

    private Button makeNextButton(ArrayList<String> fwdClassTokenChain, ArrayList<String> bckClassTokenChain) {
        Button b3 = new Button("Next", medipi.utils.getImageView("medipi.images.arrow", 20, 20));
        b3.setId("button-next");
        b3.setAlignment(Pos.CENTER);
        b3.setOnAction((ActionEvent t) -> {
            if (confirmation()) {
                String nextDevice = fwdClassTokenChain.get(0);
                ArrayList<String> remainingDevices = new ArrayList(fwdClassTokenChain.subList(1, fwdClassTokenChain.size()));
                ArrayList<String> previousDevices = bckClassTokenChain;
                previousDevices.add(getClassTokenName());
                Element e = medipi.getElement(nextDevice);
                e.callDeviceWindow(previousDevices, remainingDevices);
                //NEED TO DO SOMETHING TO RE_IMPLEMENT ADDITION OF SCHEDIULER METADATA MEASURED
                if (Device.class.isAssignableFrom(Element.this.getClass())) {
                    Device d = (Device) Element.this;
                    if (d.hasDataProperty().get()) {
                        medipi.getScheduler().addScheduleData(Scheduler.MEASURED, Instant.now(), new ArrayList<>(Arrays.asList(getClassTokenName())));
                    }
                }
            }
        });
        return b3;
    }

    private Button makeBackButton(ArrayList<String> bckClassTokenChain, ArrayList<String> fwdClassTokenChain) {
        //where there are >1 scheduled elements left to call

        ImageView iw = medipi.utils.getImageView("medipi.images.arrow", 20, 20);
        iw.setRotate(180);
        Button b1 = new Button("Back", iw);
        b1.setId("button-back");
        b1.setAlignment(Pos.CENTER_RIGHT);
        b1.setOnAction((ActionEvent t) -> {
            if (confirmation()) {
                String prevDevice = bckClassTokenChain.get(bckClassTokenChain.size() - 1);
                ArrayList<String> remainingDevices = fwdClassTokenChain;
                remainingDevices.add(0, getClassTokenName());
                ArrayList<String> previousDevices = new ArrayList(bckClassTokenChain.subList(0, bckClassTokenChain.size() - 1));
                Element e = medipi.getElement(prevDevice);
                e.callDeviceWindow(previousDevices, remainingDevices);
            }
        });
        return b1;
    }

    private boolean confirmation() {
        if (confirm) {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION, "", ButtonType.YES, ButtonType.NO);
            alert.setTitle(getSpecificDeviceDisplayName());
            alert.setHeaderText(null);
            alert.getDialogPane().setMaxSize(400, 300);
            alert.getDialogPane().getStylesheets().add("file:///" + medipi.getCssfile());
            alert.getDialogPane().setId("message-box");
            VBox vb = new VBox();
            Text text = new Text("Confirm that the input value from " + getSpecificDeviceDisplayName() + " is correct?");
            text.setWrappingWidth(400);
            text.setTextAlignment(TextAlignment.CENTER);
            vb.getChildren().add(text);
            vb.setAlignment(Pos.CENTER);
            ImageView iw = medipi.utils.getImageView("medipi.images.doctor", 80, 80);
            alert.setGraphic(iw);
            alert.setX(50);
            alert.setY(150);
            alert.getDialogPane().setContent(vb);

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
            b2hb.setMinWidth(400);
            b2hb.setSpacing(10);
            b2hb.getChildren().addAll(
                    button2,
                    b2Label,
                    progressIndicator);
            b2hb.setAlignment(Pos.CENTER_LEFT);
            b2hb.setPadding(new Insets(0, 10, 0, 50));
            bottom.setCenter(b2hb);
        }
    }

    public void setButton2Name(String name) {
        setButton2Name(name, null);
    }

    public void setB2Label(String name) {
        if (Platform.isFxApplicationThread()) {
            b2Label.setText(name);
            b2Label.setId("element-text");
        } else {
            Platform.runLater(() -> {
                b2Label.setText(name);
                b2Label.setId("element-text");
            });
        }
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
            button1.setAlignment(Pos.CENTER_LEFT);
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
            button3.setAlignment(Pos.CENTER_RIGHT);
            bottom.setRight(button3);
        }
    }

    /**
     * getter for this Element's image
     *
     * @return and imageView for this Element
     */
    public ImageView getImage() {
        return medipi.utils.getImageView(MediPi.ELEMENTNAMESPACESTEM + classToken + ".image", null, null, false);
    }

    /**
     * setter for this Element's title
     *
     *
     */
    public void setElementTitle() {
        Label title = new Label(this.getSpecificDeviceDisplayName());
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
    public abstract String getSpecificDeviceDisplayName();

    /**
     * Abstract method to get the dashboard tile
     *
     * @return the tile for inserting in the home Dashboard view
     * @throws Exception
     */
    public abstract BorderPane getDashboardTile() throws Exception;

    /**
     * method to get the generic Type of the device e.g."Blood Pressure"
     *
     * @return generic type of device
     */
    public abstract String getGenericDeviceDisplayName();

}
