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
package org.medipi.clinical.model;

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
public class QuestionnaireDO implements Serializable {

    private static final long serialVersionUID = 1L;
    private String status;
    private ArrayList<String[]> conversation = new ArrayList<>();
    private String advice;

    /**
     * Constructor
     */
    public QuestionnaireDO() {
    }

    public QuestionnaireDO(String status, ArrayList<String[]> conversation, String advice) {
        this.status = status;
        this.conversation = conversation;
        this.advice = advice;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public ArrayList<String[]> getConversation() {
        return conversation;
    }

    public void addConversation(String[] conversation) {
        this.conversation.add(conversation);
    }

    public String getAdvice() {
        return advice;
    }

    public void setAdvice(String advice) {
        this.advice = advice;
    }


    @Override
    public int hashCode() {
        int hash = 0;
        hash += (status != null ? status.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof QuestionnaireDO)) {
            return false;
        }
        QuestionnaireDO other = (QuestionnaireDO) object;
        if ((this.status == null && other.status != null) || (this.status != null && !this.status.equals(other.status))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "org.medipi.clinical.model.QuestionnaireDO [ status=" + status + " ]";
    }

}
