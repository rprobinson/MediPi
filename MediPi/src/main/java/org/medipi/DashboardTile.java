/*
 * Copyright 2016  Richard Robinson @ HSCIC <rrobinson@hscic.gov.uk, rrobinson@nhs.net>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.medipi;

import java.util.ArrayList;
import javafx.beans.property.BooleanProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import org.medipi.devices.Element;

/**
 * Class to encapsulate a Dashboard Component node which is placed in the
 * dashboard. This class creates and handles the dashboard Tile and its
 * contents, allowing the tile to be clicked and the Element to be called.
 * Overlays can be added to the tile so that dynamically changing data can be
 * displayed or an alert when actions are required in the related Element
 *
 * @author rick@robinsonhq.com
 */
public class DashboardTile {

    BorderPane component = new BorderPane();
    StackPane content = new StackPane();
    ImageView backgroundImage;
    ImageView foregroundImage = null;
    ArrayList<Label[]> labels = new ArrayList<>();

    /**
     * Constructor
     *
     * @param elem The element which this tile relates to
     */
    public DashboardTile(Element elem) {
        component.setId("mainwindow-dashboard-component");
        component.setPrefSize(190, 170);

        component.setOnMouseClicked((MouseEvent event) -> {
            elem.callDeviceWindow(null);
        });
        // add a background image to the component 
        backgroundImage = elem.getImage();
        backgroundImage.setFitHeight(80);
        backgroundImage.setFitWidth(80);

        content.setPadding(new Insets(5, 5, 5, 5));
        content.setAlignment(Pos.CENTER);
    }

    /**
     * Method to add a title to the Tile
     *
     * @param title title name
     */
    public void addTitle(String title) {

        Label t = new Label(title);
        t.setId("mainwindow-dashboard-component-title");
        HBox h = new HBox(t);
        h.setAlignment(Pos.CENTER);
        component.setTop(h);

    }

    /**
     * Method to add overlayed text to the Tile.
     *
     * Every successive addition of this overlay will add another line of text
     * on top of the tile - watch out that it doesn't exceed the limits of the
     * tile! When data is added to the StringProperty of the passed in Label,
     * the background image is made opaque
     *
     * @param measure a label containing a StringParameter so that the changing
     * data can be displayed dynamically over the image.
     * @param u units of measurement
     */
    public void addOverlay(Label measure, String u) {
        Label units = new Label(u);
        units.setVisible(false);
        units.setId("mainwindow-dashboard-component-units");
        measure.setId("mainwindow-dashboard-component-measure");
        // in order to fade the image and superimpose the recorded values when they have been taken
        measure.textProperty().addListener((ObservableValue<? extends String> ov, String oldValue, String newValue) -> {
            if (!newValue.equals("")) {
                backgroundImage.setStyle("-fx-opacity:0.2;");
                units.setVisible(true);
            } else {
                backgroundImage.setStyle("-fx-opacity:1.0;");
                units.setVisible(false);
            }
        });
        Label[] l = new Label[2];
        l[0] = measure;
        l[1] = units;
        labels.add(l);
    }

    /**
     * Method to add an overlayed Image to the Tile.
     *
     * To add an image on top of the background image
     *
     * @param image imageView of the alert to be superimposed over the tile
     * @param bp BooleanProperty to dynamically control whether the Image is
     * visible
     */
    public void addOverlay(ImageView image, BooleanProperty bp) {
        foregroundImage = image;
        foregroundImage.setFitHeight(120);
        foregroundImage.setFitWidth(120);
        if (bp.getValue()) {
            foregroundImage.setVisible(true);
        } else {
            foregroundImage.setVisible(false);
        }
        // in order to superimpose the recorded values when they have been taken
        bp.addListener((ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) -> {
            if (newValue) {
                foregroundImage.setVisible(true);
            } else {
                foregroundImage.setVisible(false);
            }
        });
    }

    /**
     * Method to return the Dashboard Tile
     *
     * @return Dashboard Tile component back to the main MediPi class
     */
    public BorderPane getTile() {

        content.getChildren().add(backgroundImage);
        if (foregroundImage != null) {
            content.getChildren().add(foregroundImage);
        }
        VBox vbox = new VBox();
        vbox.setAlignment(Pos.CENTER);
        for (Label[] l : labels) {
            HBox h = new HBox();
            h.setAlignment(Pos.CENTER);
            h.getChildren().addAll(
                    l[0],
                    l[1]);
            vbox.getChildren().add(h);
        }
        content.getChildren().add(vbox);
        component.setCenter(content);

        return component;
    }
}
