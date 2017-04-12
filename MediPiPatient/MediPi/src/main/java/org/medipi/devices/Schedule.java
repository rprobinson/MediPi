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

import java.io.Serializable;
import java.time.Instant;
import java.util.ArrayList;
import java.util.UUID;

/**
 * Class to contain the bluetooth properties for devices which may have been
 * paired but MediPi needs MAC address for in order to communicate serially with
 *
 * @author rick@robinsonhq.com
 */
public class Schedule implements Serializable {

    private static final long serialVersionUID = 1L;
    private UUID uuid;
    private String eventType;
    private Instant time;
    private int repeat;
    private ArrayList<String> deviceSched;

    /**
     * Constructor
     */
    public Schedule() {
    }

    public Schedule(UUID uuid, String eventType, Instant time, int repeat, ArrayList<String> deviceSched) {
        this.uuid = uuid;
        this.eventType = eventType;
        this.time = time;
        this.repeat = repeat;
        this.deviceSched = deviceSched;
    }
    public Schedule(ScheduleItem scheduleItem) {
        this.uuid = scheduleItem.getUUID();
        this.eventType = scheduleItem.getEventTypeDisp();
        this.time = Instant.ofEpochMilli(scheduleItem.getTime());
        this.repeat = scheduleItem.getRepeat();
        this.deviceSched = scheduleItem.getDeviceSched();
    }

    public UUID getUuid() {
        return uuid;
    }

    public void setUuid(UUID uuid) {
        this.uuid = uuid;
    }

    public String getEventType() {
        return eventType;
    }

    public void setEventType(String eventType) {
        this.eventType = eventType;
    }

    public Instant getTime() {
        return time;
    }

    public void setTime(Instant time) {
        this.time = time;
    }

    public int getRepeat() {
        return repeat;
    }

    public void setRepeat(int repeat) {
        this.repeat = repeat;
    }

    public ArrayList<String> getDeviceSched() {
        return deviceSched;
    }

    public void setDeviceSched(ArrayList<String> deviceSched) {
        this.deviceSched = deviceSched;
    }



    @Override
    public int hashCode() {
        int hash = 0;
        hash += (uuid != null ? uuid.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof Schedule)) {
            return false;
        }
        Schedule other = (Schedule) object;
        if ((this.uuid == null && other.uuid != null) || (this.uuid != null && !this.uuid.equals(other.uuid))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "org.medipi.model.BluetoothProperties [ uuid=" + uuid + " ]";
    }

}
