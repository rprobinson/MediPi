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

import java.time.Instant;
import java.time.format.DateTimeParseException;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import org.medipi.MediPiMessageBox;
import org.medipi.utilities.Utilities;

/**
 * Class to encapsulate the data for the Messenger Class table
 *
 * @author rick@robinsonhq.com
 */
public class Message {

    private SimpleStringProperty messageTitle;
    private ObjectProperty<Instant> time;
    private String fileName;

    /**
     * Constructor which takes in the filename of the incoming message and
     * parses it into a meaningful date and title
     *
     * @param mTitle message file name
     * @throws java.lang.Exception
     */
    public Message(String mTitle) throws Exception {
        try {
            fileName = mTitle;
            String elements[] = mTitle.substring(0, mTitle.lastIndexOf(".")).split("-");
            if (elements.length > 2) {
                throw new Exception("too many time elements");
            }
            Long epochMillis = Long.valueOf(elements[0]);
            Instant i = Instant.ofEpochMilli(epochMillis);

            this.messageTitle = new SimpleStringProperty(elements[1]);
            this.time = new SimpleObjectProperty<>(i);
        } catch (DateTimeParseException ex) {
            MediPiMessageBox.getInstance().makeErrorMessage("failure to recognise the date format of the incoming message: " + fileName, ex);
            throw ex;
        } catch (Exception ex) {
            MediPiMessageBox.getInstance().makeErrorMessage("failure to recognise the date format of the incoming message: " + fileName, ex);
            throw ex;
        }

    }

    public String getFileName() {
        return fileName;
    }

    public String getMessageTitle() {
        return messageTitle.get();
    }

    public void setMessageTitle(String mTitle) {
        messageTitle.set(mTitle);
    }

    public Instant getTime() {
        return time.get();
    }

    public void setTime(Instant i) {
        time.set(i);
    }

    public ObjectProperty<Instant> timeProperty() {
        return time;
    }
}
