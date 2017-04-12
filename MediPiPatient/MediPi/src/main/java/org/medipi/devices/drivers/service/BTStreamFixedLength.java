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
package org.medipi.devices.drivers.service;

import java.io.IOException;
import java.time.Instant;
import javax.microedition.io.Connector;
import javax.microedition.io.StreamConnection;
import org.medipi.devices.Element;

/**
 * Class to communicate with a bluetooth serial port device and take data of
 * fixed length and pass it to a Measurement class for parsing
 *
 * @author rick@robinsonhq.com
 */
public class BTStreamFixedLength extends BluetoothService {

    private final Element element;
    private boolean keepLookingForBTConnection = true;

    public BTStreamFixedLength(Element e) throws Exception {
        element = e;
    }

    /**
     * read url via stream connection
     *
     * @param url
     * @param messageLength
     * @return
     */
    public byte[] getFixedLengthStream(String url, int messageLength) {
        byte[] output = new byte[messageLength];
        Instant timer = Instant.now();
        keepLookingForBTConnection = true;
        try {
            while (keepLookingForBTConnection) {
                element.setB2Label(searchingMessage);
                if (timer.plusSeconds(btConnectionAttemptPeriod).isBefore(Instant.now())) {
                    return new byte[-1];
                }
                try {
                    c = (StreamConnection) Connector.open(url);
                    is = c.openInputStream();
                    keepLookingForBTConnection = false;
                    element.setB2Label(connectionMessage);
                    byte b;
                    int counter = 0;
                    while ((b = (byte) is.read()) != -1) {
                        element.setB2Label(downloadingMessage);
                        output[counter] = b;
                        if (counter >= messageLength - 1) {
                            return output;
                        } else {
                            counter++;
                        }
                        System.out.println((char) b + "char" + b);
                    }
                    System.out.println(String.valueOf(output));
                } catch (IOException e) {
                    System.out.println("attempt connection: " + Instant.now() + e.getMessage());
                    // do nothing as this is thrown when there is an attempted connection and the BT device is switched off
                }
            }
//            // reset keepLookingForBTConnection
//            keepLookingForBTConnection = true;
        } finally {
            if (is != null) {
                try {
                    is.close();
                } catch (IOException ex) {
                    //do nothing as if the connection is null it doesnt need closing
                }
            }
            if (c != null) {
                try {
                    c.close();
                } catch (IOException ex) {
                    //do nothing as if the connection is null it doesnt need closing
                }
            }
        }
        return null;
    }

    public void StopLookingForBTConnection() {
        keepLookingForBTConnection = false;
    }
}
