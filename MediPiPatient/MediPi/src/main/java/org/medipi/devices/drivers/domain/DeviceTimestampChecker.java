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

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.ButtonBar.ButtonData;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.layout.GridPane;
import javafx.util.Pair;
import org.medipi.MediPi;
import org.medipi.devices.Element;

/**
 * Class to check through the timestamps from a device and assess them against
 * thresholds set within the properties file
 *
 * There are 2 tests:
 *
 * Realtime devices - the threshold is designed to test the timestamp for
 * devices which transmit their data immediately the measurement is made i.e.
 * the expectation is that the timestamp when tested should be within a very
 * short period of time <5mins
 *
 * Stored time devices - the threshold is designed to test the timestamp for
 * devices which store their data and subsequently transfer the measurement to
 * MediPi i.e. the expectation is that the timestamp will not neccessarily be
 * very immediately taken but will be recent e.g. within the last hour
 *
 * These tests may result in the device requiring resetting to the correct time.
 * The TimestampalidationInterface is used to call the specific guide 
 * 
 * @author rick@robinsonhq.com
 */
public class DeviceTimestampChecker {

    /**
     * Period of time within which MediPi will accept data from the device
     * (device's reported time vs MediPi's time). Otherwise the device's time
     * will need resetting. Period of time in minutes
     *
     */
    private int storedDeviceTimestampDeviation = -1;
    private int realtimeDeviceTimestampDeviation = -1;
    private Element element;
    private String deviceNamespace;

    public DeviceTimestampChecker(MediPi medipi, Element elem) throws Exception {
        element = elem;
        deviceNamespace = MediPi.ELEMENTNAMESPACESTEM + element.getClassTokenName();
        try {
            String time = medipi.getProperties().getProperty(deviceNamespace + ".storeddevicetimestampdeviation");
            if (time == null || time.trim().length() == 0) {
                storedDeviceTimestampDeviation = -1;
            } else {
                storedDeviceTimestampDeviation = Integer.parseInt(time);
            }
        } catch (NumberFormatException e) {
            throw new Exception("Unable to set the period of time within which MediPi will accept data from the device - make sure that " + deviceNamespace + ".storeddevicetimestampdeviation property is set correctly");
        }
        try {
            String time = medipi.getProperties().getProperty(deviceNamespace + ".realtimedevicetimestampdeviation");
            if (time == null || time.trim().length() == 0) {
                realtimeDeviceTimestampDeviation = -1;
            } else {
                realtimeDeviceTimestampDeviation = Integer.parseInt(time);
            }
        } catch (NumberFormatException e) {
            throw new Exception("Unable to set the period of time within which MediPi will accept data from the device - make sure that " + deviceNamespace + ".realtimedevicetimestampdeviation property is set correctly");
        }
    }

    public ArrayList<ArrayList<String>> checkTimestamp(ArrayList<ArrayList<String>> list) {
        ArrayList<ArrayList<String>> result = new ArrayList<ArrayList<String>>();
        boolean withinRealTimeDeviation = false;
        boolean withinStoredDeviation = false;
        if (list.isEmpty()) {
            return result;
        }
        if (storedDeviceTimestampDeviation == -1 && realtimeDeviceTimestampDeviation == -1) {
            return list;
        }

        if (realtimeDeviceTimestampDeviation != -1 && storedDeviceTimestampDeviation != -1) {
            result = isRealTimeInPeriod(list);
            if (result == null) {
                callDialogue();
                return null;
            } else {
                return isStoredInPeriod(result, realtimeDeviceTimestampDeviation);
            }
        }
        if (realtimeDeviceTimestampDeviation != -1) {
            result = isRealTimeInPeriod(list);
            if (result == null) {
                callDialogue();
                return null;
            } else {
                return result;
            }

        }
        if (storedDeviceTimestampDeviation != -1) {
            return isStoredInPeriod(list, 0);
        }
        return result;
        // sort the list in reverse order
//        Collections.sort(list, new Comparator<ArrayList<String>>() {
//            @Override
//            public int compare(ArrayList<String> o1, ArrayList<String> o2) {
//                Instant i1 = Instant.parse(o1.get(0));
//                Instant i2 = Instant.parse(o2.get(0));
//                return i2.compareTo(i1);
//            }
//        });
//        for (ArrayList<String> a : list) {
//            System.out.println(a.get(0));
//        }
    }

    private void callDialogue() {
        if (Platform.isFxApplicationThread()) {
            showDialogue();
        } else {
            Platform.runLater(() -> {
                showDialogue();
            });
        }
    }

    private ArrayList<ArrayList<String>> isStoredInPeriod(ArrayList<ArrayList<String>> inArray, int futureTollerance) {
        Instant now = Instant.now();
        ArrayList<ArrayList<String>> returnList = new ArrayList<ArrayList<String>>();

        for (ArrayList<String> a : inArray) {
            Instant i = Instant.parse(a.get(0));
            Instant postLimit = now.plus(futureTollerance, ChronoUnit.MINUTES);
            Instant priorLimit = now.minus(storedDeviceTimestampDeviation, ChronoUnit.MINUTES);

            if (i.isAfter(priorLimit) && i.isBefore(postLimit)) {
                //Physiological device clock is within stored threshold - accept
                returnList.add(a);
            } else //Physiological device clock is beyong the future limit therefore all data is questionable wrt timestamp - REJECT ALL
            {
                if (i.isAfter(postLimit)) {
                    return null;
                }
            }

        }
        // After having tested all results return those which are in the acceptable timeframe.
        // If any resuilts are before the prior threshold these are excluded
        // If any results are before the post threshold the whole list is abandoned
        if (returnList.isEmpty()) {
            return null;
        } else {
            return returnList;
        }
    }

    private ArrayList<ArrayList<String>> isRealTimeInPeriod(ArrayList<ArrayList<String>> inArray) {
        Instant now = Instant.now();
        boolean requiredResult = false;
        boolean unacceptableResult = false;

        for (ArrayList<String> a : inArray) {
            Instant i = Instant.parse(a.get(0));
            Instant postLimit = now.plus(realtimeDeviceTimestampDeviation, ChronoUnit.MINUTES);
            Instant priorLimit = now.minus(realtimeDeviceTimestampDeviation, ChronoUnit.MINUTES);

            if (i.isAfter(priorLimit) && i.isBefore(postLimit)) {
                //Physiological device clock is within REALTIME threshold - accept
                requiredResult = true;
            } else //Physiological device clock is beyong the future limit therefore all data is questionable wrt timestamp - REJECT ALL
            {
                if (i.isAfter(postLimit)) {
                    unacceptableResult = true;
                }
            }

        }
        // After having tested all results provided that there is 1 acceptable 
        // result and 0 unacceptable results we pass all data points. 
        // Otherwise the whole list is abandoned
        if (unacceptableResult || !requiredResult) {
            return null;
        } else if (requiredResult) {
            return inArray;
        } else {
            //unreachable but required
            return null;
        }
    }

    private void showDialogue() {
        TimestampValidationInterface tvi = (TimestampValidationInterface) element;
        Node guide = tvi.getTimestampMessageBoxContent();
        // Create the custom dialog.
        Dialog<Pair<String, String>> dialog = new Dialog<>();
        dialog.setTitle("Time Synchronisation");
        dialog.setHeaderText(element.getDisplayName() + " requires time synchronisation");

// Set the button types.
        ButtonType okButton = new ButtonType("OK", ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(okButton);

// Create the username and password labels and fields.
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(10, 10, 10, 10));

        grid.add(guide, 0, 0);

        dialog.getDialogPane().setContent(grid);
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == okButton) {
                return null;
            }
            return null;
        });

        dialog.showAndWait();
    }
}
