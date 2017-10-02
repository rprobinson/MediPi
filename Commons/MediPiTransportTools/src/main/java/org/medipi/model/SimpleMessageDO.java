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
package org.medipi.model;

import java.io.Serializable;
import java.util.Date;

/**
 * Data Object for a list of Alerts
 * @author rick@robinsonhq.com
 */
public class SimpleMessageDO implements Serializable, DirectPatientMessage {

    private static final long serialVersionUID = 1L;
    private String patientUuid;
    private String simpleMessageText;
    private Long simpleMessageId;
    private Date simpleMessageTime;

    public SimpleMessageDO() {
    }

    public SimpleMessageDO(String patientUuid) {
        this.patientUuid = patientUuid;
    }

    public SimpleMessageDO(String patientUuid, String simpleMessageText) {
        this.patientUuid = patientUuid;
        this.simpleMessageText = simpleMessageText;
    }

    @Override
    public String getPatientUuid() {
        return patientUuid;
    }

    public void setPatientUuid(String patientUuid) {
        this.patientUuid = patientUuid;
    }

    public String getSimpleMessageText() {
        return simpleMessageText;
    }

    public void setSimpleMessageText(String simpleMessageText) {
        this.simpleMessageText = simpleMessageText;
    }

    public Long getSimpleMessageId() {
        return simpleMessageId;
    }

    public void setSimpleMessageId(Long simpleMessageId) {
        this.simpleMessageId = simpleMessageId;
    }

    public Date getSimpleMessageTime() {
        return simpleMessageTime;
    }

    public void setSimpleMessageTime(Date simpleMessageTime) {
        this.simpleMessageTime = simpleMessageTime;
    }


}
