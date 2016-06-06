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

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextArea;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import org.medipi.DashboardTile;
import org.medipi.MediPi;
import org.medipi.MediPiProperties;

/**
 * Class to display and handle alteration certain properties to control the
 * application. It also controls closing the application and shows credits
 *
 * This class gives control over certain properties from the medipi.properties
 * file. It also shows a view of the credits.txt file and gives a mechanism for
 * closing the application when no window decoration is present
 *
 *
 * There is no view mode for this UI.
 *
 * TODO: currently no properties are controlled - the main purpose is to give a
 * mechanism for closing the application
 *
 * @author rick@robinsonhq.com
 */
public class Settings extends Element {

    private static final String NAME = "Settings";
    private TextArea creditsView;
    private VBox settingsWindow;

    /**
     * Constructor for Messenger
     *
     */
    public Settings() {

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
        settingsWindow = new VBox();
        settingsWindow.setPadding(new Insets(0, 5, 0, 5));
        settingsWindow.setSpacing(5);
        settingsWindow.setMinSize(800, 350);
        settingsWindow.setMaxSize(800, 350);
        // Create the view of the message content - scrollable
        creditsView = new TextArea();
        creditsView.setWrapText(true);
        creditsView.isResizable();
        creditsView.setEditable(false);
        creditsView.setId("settings-creditscontent");
        creditsView.setMaxHeight(200);
        creditsView.setMinHeight(200);
        creditsView.setEditable(false);
        Label versionLabel = new Label();
        versionLabel.setId("settings-creditscontent");
        ScrollPane viewSP = new ScrollPane();
        viewSP.setContent(creditsView);
        viewSP.setFitToWidth(true);
        viewSP.setFitToHeight(true);
        viewSP.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        viewSP.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);

        // location of the credits file
        String creditsDir = medipi.getProperties().getProperty(MediPi.ELEMENTNAMESPACESTEM + uniqueDeviceName + ".credits");
        if (creditsDir == null || creditsDir.trim().length() == 0) {
            throw new Exception("credits file location parameter not configured");
        }

        versionLabel.setText("MediPi Version: " + medipi.getVersion());
        creditsView.setText(readFile(creditsDir, StandardCharsets.UTF_8));

        Label creditsLabel = new Label("Credits");
        creditsLabel.setId("settings-text");

        Button closeButton = new Button("Close MediPi", medipi.utils.getImageView("medipi.images.cancel", 20, 20));
        closeButton.setId("button-closemedipi");
        closeButton.setOnAction((ActionEvent t) -> {
            exit();
        });

        settingsWindow.getChildren().addAll(
                creditsLabel,
                creditsView,
                versionLabel
        );

        // set main Element window
        window.setCenter(settingsWindow);

        setButton2(closeButton);

        // successful initiation of the this class results in a null return
        return null;
    }

    /**
     * Method to close the JavaFX application
     */
    public void exit() {
        Platform.runLater(() -> {
            System.exit(0);
        });
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
        return dashComponent.getTile();
    }

}
