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

import java.util.ArrayList;
import java.util.Optional;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.StringProperty;
import javafx.geometry.Pos;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import org.medipi.model.DeviceDataDO;

/**
 * Main abstract class for Medical devices.
 *
 * All Medical device implementations consist of (apart from the Element Class):
 *
 * 1. Abstract Device subclass (this class)
 *
 * 2. Abstract generic device class - e.g.Oximeter, Scale which gives
 * functionality for the device data in a common format collected through a
 * common interface. It also provides the UI for that data
 *
 * 3. Concrete class specific to a particular make and model of a device
 * transposing the raw data from the device into a common format which can be
 * passed to its generic abstract class.
 *
 * @author rick@robinsonhq.com
 */
public abstract class Device extends Element {

    // property to indicate whether data has bee recorded for this device
    protected final BooleanProperty hasData = new SimpleBooleanProperty(false);

    /**
     * Method which returns a booleanProperty which UI elements can be bound to,
     * to discover whether there is data to be downloaded
     *
     * @return BooleanProperty signalling the presence of downloaded data
     */
    public BooleanProperty hasDataProperty() {
        return hasData;
    }

    /**
     * Abstract initiation method called for this Element.
     *
     * Successful initiation of the this class results in a null return. Any
     * other response indicate a failure with the returned content being a
     * reason for the failure
     *
     * @return populated or null for whether the initiation was successful
     * @throws java.lang.Exception
     */
    @Override
    public abstract String init() throws Exception;

    /**
     * Method to get the data payload including the metadata
     *
     * @return DeviceDataDO representation of the data
     */
    public abstract DeviceDataDO getData() throws Exception;

    /**
     * Method to set the data payload
     *
     * @param deviceData
     */
    public abstract void setData(ArrayList<ArrayList<String>> deviceData);

    /**
     * Method to reset a device and initialise it
     *
     */
    public abstract void resetDevice();

    /**
     * abstract Getter for Profile ID to be used to identify the data
     *
     * @return profile ID
     */
    public abstract String getProfileId();

    /**
     * abstract Getter for Make of device
     *
     * @return make
     */
    public abstract String getMake();

    /**
     * abstract Getter for Model of device
     *
     * @return model
     */
    public abstract String getModel();

    /**
     * abstract Getter for Summary Overview of the results of device
     *
     * @return model
     */
    public abstract StringProperty getResultsSummary();

    /**
     * Method to confirm that the user is happy to reset previously recorded
     * untransmitted data.
     *
     * @return boolean indicator of assent or dissent
     */
    protected boolean confirmReset() {
        if (!hasData.get()) {
            return true;
        }
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle(getSpecificDeviceDisplayName());
        alert.setHeaderText(null);
        alert.getDialogPane().getStylesheets().add("file:///" + medipi.getCssfile());
        alert.getDialogPane().setMaxSize(500, 300);
        alert.getDialogPane().setId("message-box");
        VBox vb = new VBox();
        Text text = new Text(getSpecificDeviceDisplayName() + "\n\n" + getResultsSummary().getValue() + "\nThis reading has not been transmitted.\nAre you sure you want to delete this measurement?");
        text.setWrappingWidth(500);
        text.setTextAlignment(TextAlignment.CENTER);
        vb.getChildren().add(text);
        vb.setAlignment(Pos.CENTER);
        alert.getDialogPane().setContent(vb);
        ImageView iw = medipi.utils.getImageView("medipi.images.doctor", 80, 80);
        alert.setGraphic(iw);
        Optional<ButtonType> result = alert.showAndWait();
        if (result.get() == ButtonType.OK) {
            return true;
        } else {
            return false;
        }
    }

}
