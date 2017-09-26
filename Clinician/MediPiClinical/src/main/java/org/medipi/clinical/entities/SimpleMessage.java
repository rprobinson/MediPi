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
package org.medipi.clinical.entities;

import java.io.Serializable;
import java.util.Date;
import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * 
 * @author rick@robinsonhq.com
 */
@Entity
@Table(name = "simple_message")
@XmlRootElement
@NamedQueries({
//added
    @NamedQuery(name = "SimpleMessage.findByNullTransmitSuccessDate", query = "SELECT a FROM SimpleMessage a WHERE a.transmitSuccessDate = null AND a.retryAttempts < :maxRetries AND a.retryAttempts >=0"),


    @NamedQuery(name = "SimpleMessage.findAll", query = "SELECT a FROM SimpleMessage a"),
    @NamedQuery(name = "SimpleMessage.findBySimpleMessageId", query = "SELECT a FROM SimpleMessage a WHERE a.simpleMessageId = :simpleMessageId"),
    @NamedQuery(name = "SimpleMessage.findBySimpleMessageTime", query = "SELECT a FROM SimpleMessage a WHERE a.simpleMessageTime = :simpleMessageTime"),
    @NamedQuery(name = "SimpleMessage.findBySimpleMessageText", query = "SELECT a FROM SimpleMessage a WHERE a.simpleMessageText = :simpleMessageText")})
public class SimpleMessage implements Serializable {


    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "simple_message_id")
    private Long simpleMessageId;
    @Basic(optional = false)
    @NotNull
    @Column(name = "simple_message_time")
    @Temporal(TemporalType.TIMESTAMP)
    private Date simpleMessageTime;
    @Basic(optional = false)
    @NotNull
    @Size(min = 1, max = 5000)
    @Column(name = "simple_message_text")
    private String simpleMessageText;
    @Column(name = "transmit_success_date")
    @Temporal(TemporalType.TIMESTAMP)
    private Date transmitSuccessDate;
    @Basic(optional = false)
    @NotNull
    @Column(name = "retry_attempts")
    private int retryAttempts;
    @JoinColumn(name = "patient_uuid", referencedColumnName = "patient_uuid")
    @ManyToOne(optional = false)
    private Patient patientUuid;

    public SimpleMessage() {
    }

    public SimpleMessage(Long simpleMessageId) {
        this.simpleMessageId = simpleMessageId;
    }

    public SimpleMessage(Long simpleMessageId, Date simpleMessageTime, String simpleMessageText) {
        this.simpleMessageId = simpleMessageId;
        this.simpleMessageTime = simpleMessageTime;
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

    public String getSimpleMessageText() {
        return simpleMessageText;
    }

    public void setSimpleMessageText(String simpleMessageText) {
        this.simpleMessageText = simpleMessageText;
    }

    public Patient getPatientUuid() {
        return patientUuid;
    }

    public void setPatientUuid(Patient patientUuid) {
        this.patientUuid = patientUuid;
    }


    public Date getTransmitSuccessDate() {
        return transmitSuccessDate;
    }

    public void setTransmitSuccessDate(Date transmitSuccessDate) {
        this.transmitSuccessDate = transmitSuccessDate;
    }


    public int getRetryAttempts() {
        return retryAttempts;
    }

    public void setRetryAttempts(int retryAttempts) {
        this.retryAttempts = retryAttempts;
    }


    @Override
    public int hashCode() {
        int hash = 0;
        hash += (simpleMessageId != null ? simpleMessageId.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof SimpleMessage)) {
            return false;
        }
        SimpleMessage other = (SimpleMessage) object;
        if ((this.simpleMessageId == null && other.simpleMessageId != null) || (this.simpleMessageId != null && !this.simpleMessageId.equals(other.simpleMessageId))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "org.medipi.clinical.entities.SimpleMessage[ simpleMessageId=" + simpleMessageId + " ]";
    }
    
}
