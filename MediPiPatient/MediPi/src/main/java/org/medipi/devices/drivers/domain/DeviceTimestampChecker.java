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
import java.util.Collections;
import java.util.Comparator;
import org.medipi.MediPi;
import org.medipi.devices.Element;
import org.medipi.devices.Scheduler;

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
    private StringBuilder dataResponseMessage = null;
    private MediPi medipi;
    private boolean enforceWithinCurrentScheduledPeriod = false;
    private boolean latestValueOnly = false;
    private boolean tooOldMessage = false;
    private boolean outsideThresholdPeriodMessage = false;
    private boolean futureMessage = false;

    public DeviceTimestampChecker(MediPi m, Element elem) throws Exception {
        element = elem;
        medipi = m;
        deviceNamespace = MediPi.ELEMENTNAMESPACESTEM + element.getClassTokenName();
        String enforce = medipi.getProperties().getProperty(deviceNamespace + ".enforcewithincurrentscheduledperiod");
        if (enforce != null) {
            if (enforce.trim().toLowerCase().startsWith("y")) {
                enforceWithinCurrentScheduledPeriod = true;
            } else {
                enforceWithinCurrentScheduledPeriod = false;
            }
        } else {
            enforceWithinCurrentScheduledPeriod = false;
        }
        String latest = medipi.getProperties().getProperty(deviceNamespace + ".returnonlythelatestvalue");
        if (latest != null) {
            if (latest.trim().toLowerCase().startsWith("y")) {
                latestValueOnly = true;
            } else {
                latestValueOnly = false;
            }
        } else {
            latestValueOnly = false;
        }
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
        ArrayList<ArrayList<String>> result = new ArrayList<>();
        dataResponseMessage = new StringBuilder();
        tooOldMessage = false;
        outsideThresholdPeriodMessage = false;
        futureMessage = false;
        if (list.isEmpty()) {
            dataResponseMessage.append("No data is available from the device.\n");
            return result;
        }
        // sort the list in ascending date/time order
        Collections.sort(list, new Comparator<ArrayList<String>>() {
            @Override
            public int compare(ArrayList<String> o1, ArrayList<String> o2) {
                Instant i1 = Instant.parse(o1.get(0));
                Instant i2 = Instant.parse(o2.get(0));
                return i1.compareTo(i2);
            }
        });

        if (storedDeviceTimestampDeviation == -1 && realtimeDeviceTimestampDeviation == -1) {
            return latestValueOnly(isWithinCurrentScheduledPeriod(list));
        }

        if (realtimeDeviceTimestampDeviation != -1 && storedDeviceTimestampDeviation != -1) {
            result = isRealTimeInPeriod(list);
            if (result == null) {
                return null;
            } else {
                result = latestValueOnly(isStoredInPeriod(result, realtimeDeviceTimestampDeviation));
                return result;

            }
        }
        if (realtimeDeviceTimestampDeviation != -1) {
            result = latestValueOnly(isRealTimeInPeriod(list));
            return result;

        }
        if (storedDeviceTimestampDeviation != -1) {
            result = latestValueOnly(isStoredInPeriod(list, 0));
            return result;
        }
        return result;

    }

    private ArrayList<ArrayList<String>> isStoredInPeriod(ArrayList<ArrayList<String>> inArray, int futureTollerance) {
        Instant now = Instant.now();
        ArrayList<ArrayList<String>> returnList = new ArrayList<ArrayList<String>>();

        for (ArrayList<String> a : inArray) {
            Instant dataTime = Instant.parse(a.get(0));
            Instant postLimit = now.plus(futureTollerance, ChronoUnit.MINUTES);
            Instant priorLimit = now.minus(storedDeviceTimestampDeviation, ChronoUnit.MINUTES);

            if (dataTime.isAfter(priorLimit) && dataTime.isBefore(postLimit)) {
                //Physiological device clock is within stored threshold - accept
                returnList.add(a);
            } else //Physiological device clock is beyong the future limit therefore all data is questionable wrt timestamp - REJECT ALL
            {
                if (dataTime.isAfter(postLimit)) {
                    futureMessage = true;
//                    dataResponseMessage.append("A measurement taken at " + dataTime + " is in the future and therfore all data is deemed suspect. Please retake the reading.\n");
                    return null;
                } else {
                    tooOldMessage = true;
//                    dataResponseMessage.append("A measurement taken at " + dataTime + " is too old and has been ignored.\n If no measurement appears on the MediPi Patient Device, please retake.\n");
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
            Instant dataTime = Instant.parse(a.get(0));
            Instant postLimit = now.plus(realtimeDeviceTimestampDeviation, ChronoUnit.MINUTES);
            Instant priorLimit = now.minus(realtimeDeviceTimestampDeviation, ChronoUnit.MINUTES);

            if (dataTime.isAfter(priorLimit) && dataTime.isBefore(postLimit)) {
                //Physiological device clock is within REALTIME threshold - accept
                requiredResult = true;
            } else //Physiological device clock is beyong the future limit therefore all data is questionable wrt timestamp - REJECT ALL
            {
                if (dataTime.isAfter(postLimit)) {
                    futureMessage = true;
//                    dataResponseMessage.append("A measurement taken at " + dataTime + " is in the future and therfore all data is deemed suspect. Please retake the reading.\n");
                    unacceptableResult = true;
                } else {
                    tooOldMessage = true;
//                    dataResponseMessage.append("A measurement taken at " + dataTime + " is too old and has been ignored.\n If no measurement appears on the MediPi Patient Device, please retake.\n");
                }
            }

        }
        // After having tested all results, provided that there is 1 acceptable 
        // result and 0 unacceptable results, we pass all data points. 
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

    private ArrayList<ArrayList<String>> isWithinCurrentScheduledPeriod(ArrayList<ArrayList<String>> list) {
        Scheduler scheduler = null;
        if ((scheduler = medipi.getScheduler()) != null && enforceWithinCurrentScheduledPeriod) {
            ArrayList<ArrayList<String>> returnList = new ArrayList<>();

            for (ArrayList<String> a : list) {
                Instant dataTime = Instant.parse(a.get(0));

                if (scheduler.getCurrentScheduleStartTime().isBefore(dataTime) && scheduler.getCurrentScheduleExpiryTime().isAfter(dataTime)) {
                    //Physiological device clock is within scheduled period - accept
                    returnList.add(a);
                } else {
                    outsideThresholdPeriodMessage = true;
//                    dataResponseMessage.append("A measurement taken at " + dataTime + " is outside the current scheduled period and have been ignored.\n If no measurement appears on the MediPi Patient Device, please retake.\n");
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
        } else {
            return list;
        }
    }

    private ArrayList<ArrayList<String>> latestValueOnly(ArrayList<ArrayList<String>> list) {
        if (latestValueOnly && list != null) {
            ArrayList<ArrayList<String>> returnList = new ArrayList<>();

            Instant latestDataTime = Instant.EPOCH;
            ArrayList<String> latestValue = null;
            for (ArrayList<String> a : list) {
                if (latestDataTime.isBefore(Instant.parse(a.get(0)))) {
                    latestDataTime = Instant.parse(a.get(0));
                    latestValue = a;
                }
            }
            if (!latestValue.isEmpty()) {
                returnList.add(latestValue);
            }
            if (returnList.isEmpty()) {
                return null;
            } else {
                return returnList;
            }
        } else {
            return list;
        }
    }

    public String getMessages() {
        if (tooOldMessage) {
            dataResponseMessage.append("Some measurements taken were too old and have been ignored.\n If no measurement appears on the MediPi Patient Device, please retake.\n");
        }
        if (outsideThresholdPeriodMessage) {
            dataResponseMessage.append("Some measurements taken were outside the current scheduled period and have been ignored.\n If no measurement appears on the MediPi Patient Device, please retake.\n");
        }
        if (futureMessage) {
            dataResponseMessage.append("Some measurements taken were marked as being in the future and therfore deemed suspect. Please retake the reading.\n");
        }

        if (dataResponseMessage.toString() == null || dataResponseMessage.toString().equals("")) {
            return null;
        } else {
            return dataResponseMessage.toString();
        }
    }
}
