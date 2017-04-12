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
package org.medipi.devices.drivers.domain;

import javafx.scene.Node;

/**
 * Interface to allow the DeviceTimestampchecker to call the individual time
 * reset guide for a device when the timestamp is assessed to need resetting
 *
 * @author rick@robinsonhq.com
 */
public interface DeviceTimestampUpdateInterface {

    public Node getDeviceTimestampUpdateMessageBoxContent();
}
