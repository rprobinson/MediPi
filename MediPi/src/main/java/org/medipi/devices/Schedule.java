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

import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.UUID;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleLongProperty;
import javafx.beans.property.SimpleStringProperty;
import org.medipi.MediPiMessageBox;
import org.medipi.utilities.Utilities;

/**
 * Class to encapsulate the data for the Scheduler Class table.
 *
 * There are getter methods for different formats of the same data because of
 * the way this class is tied with the scheduler List table. This requires
 * investigation
 *
 * @author rick@robinsonhq.com
 */
public class Schedule {

    private SimpleStringProperty uuid;
    private SimpleStringProperty eventType;
    private SimpleLongProperty time;
    private SimpleStringProperty deviceSched;
    private SimpleIntegerProperty repeat;
    private StringBuilder devices = new StringBuilder();

    /**
     * Constructor which takes the untokenised line of the schedule table and
     * parses it into a meaningful table
     *
     * @param u UUID
     * @param type Type of schedule row
     * @param d Instant string
     * @param r repeat string
     * @param dl list of devices to be scheduled
     */
    public Schedule(UUID u, String type, Instant d, int r, ArrayList<String> dl) {
        try {
            //unique event number
            this.uuid = new SimpleStringProperty(u.toString());
            // event status
            this.eventType = new SimpleStringProperty(type);
            this.time = new SimpleLongProperty(d.toEpochMilli());
            //repeat rate in mins
            this.repeat = new SimpleIntegerProperty(r);
            //devices to be called
            for (String s : dl) {
                devices.append(s);
                devices.append(" ");
            }
            this.deviceSched = new SimpleStringProperty(devices.toString().trim());
        } catch (Exception ex) {
            MediPiMessageBox.getInstance().makeErrorMessage("failure to tokenise the schedule: ", ex);
        }

    }

    public String getUUIDDisp() {
        return uuid.get();
    }

    public UUID getUUID() {
        return UUID.fromString(uuid.get());
    }

    public String getEventTypeDisp() {
        return eventType.get();
    }

    public String getTimeDisp() {
        Instant instant = Instant.ofEpochMilli(time.get());
        return Utilities.DISPLAY_SCHEDULE_FORMAT_LOCALTIME.format(instant);
    }
    
    public Long getTime() {
        return time.get();
    }

    public String getRepeatDisp() {
        return String.valueOf(repeat.get());
    }

    public int getRepeat() {
        return repeat.get();
    }

    public String getDeviceSchedDisp() {
        return deviceSched.get();
    }

    public ArrayList<String> getDeviceSched() {
        return new ArrayList<>(Arrays.asList(deviceSched.get().split("\\s+")));
    }

}
