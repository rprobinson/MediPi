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

import javafx.beans.property.BooleanProperty;
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
     * method to get the generic Type of the device e.g."Blood Pressure"
     *
     * @return generic type of device
     */
    public abstract String getType();

    /**
     * Method to get the data payload including the metadata
     *
     * @return DeviceDataDO representation of the data
     */
    public abstract DeviceDataDO getData();

    /**
     * Method which returns a booleanProperty which UI elements can be bound to,
     * to discover whether there is data to be downloaded
     *
     * @return BooleanProperty signalling the presence of downloaded data
     */
    public abstract BooleanProperty hasDataProperty();

    /**
     * Method to reset a device and initialise it
     *
     */
    public abstract void resetDevice();

    /**
     * abstract Getter for Profile ID to be used to identify the data as part of
     * the message structure of the DistributionEnvelope
     *
     * @return profile ID
     */
    public abstract String getProfileId();

}
