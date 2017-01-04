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
package org.medipi.concentrator.entities;

import java.io.Serializable;
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
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

/**
 * Entity class encapsulating RecordingDeviceAttribute database table
 * @author rick@robinsonhq.com
 */

@Entity
@Table(name = "recording_device_attribute")
@NamedQueries({
    //Added
    @NamedQuery(name = "RecordingDeviceAttribute.findByTypeUnitsFormatAndAttributeName", query = "SELECT r FROM RecordingDeviceAttribute r WHERE r.attributeName = :attributeName AND r.attributeUnits = :units AND r.attributeType = :format AND r.typeId = :typeId"),
    //
    @NamedQuery(name = "RecordingDeviceAttribute.findAll", query = "SELECT r FROM RecordingDeviceAttribute r"),
    @NamedQuery(name = "RecordingDeviceAttribute.findByAttributeId", query = "SELECT r FROM RecordingDeviceAttribute r WHERE r.attributeId = :attributeId"),
    @NamedQuery(name = "RecordingDeviceAttribute.findByAttributeName", query = "SELECT r FROM RecordingDeviceAttribute r WHERE r.attributeName = :attributeName"),
    @NamedQuery(name = "RecordingDeviceAttribute.findByAttributeType", query = "SELECT r FROM RecordingDeviceAttribute r WHERE r.attributeType = :attributeType")})
public class RecordingDeviceAttribute implements Serializable {


//    @OneToMany(cascade = CascadeType.ALL, mappedBy = "attributeId")
//    private Collection<RecordingDeviceData> recordingDeviceDataCollection;

    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "attribute_id")
    private Integer attributeId;
    @Basic(optional = false)
    @NotNull
    @Size(min = 1, max = 100)
    @Column(name = "attribute_name")
    private String attributeName;
    @Basic(optional = false)
    @NotNull
    @Size(min = 1, max = 100)
    @Column(name = "attribute_type")
    private String attributeType;
    @JoinColumn(name = "type_id", referencedColumnName = "type_id")
    @ManyToOne(optional = false)
    private RecordingDeviceType typeId;
    @Size(max = 100)
    @Column(name = "attribute_units")
    private String attributeUnits;

    public RecordingDeviceAttribute() {
    }

    public RecordingDeviceAttribute(Integer attributeId) {
        this.attributeId = attributeId;
    }

    public RecordingDeviceAttribute(Integer attributeId, String attributeName, String attributeType) {
        this.attributeId = attributeId;
        this.attributeName = attributeName;
        this.attributeType = attributeType;
    }

    public Integer getAttributeId() {
        return attributeId;
    }

    public void setAttributeId(Integer attributeId) {
        this.attributeId = attributeId;
    }

    public String getAttributeName() {
        return attributeName;
    }

    public void setAttributeName(String attributeName) {
        this.attributeName = attributeName;
    }

    public String getAttributeType() {
        return attributeType;
    }

    public void setAttributeType(String attributeType) {
        this.attributeType = attributeType;
    }

    public RecordingDeviceType getTypeId() {
        return typeId;
    }

    public void setTypeId(RecordingDeviceType typeId) {
        this.typeId = typeId;
    }
    public String getAttributeUnits() {
        return attributeUnits;
    }

    public void setAttributeUnits(String attributeUnits) {
        this.attributeUnits = attributeUnits;
    }

//    public Collection<RecordingDeviceData> getRecordingDeviceDataCollection() {
//        return recordingDeviceDataCollection;
//    }
//
//    public void setRecordingDeviceDataCollection(Collection<RecordingDeviceData> recordingDeviceDataCollection) {
//        this.recordingDeviceDataCollection = recordingDeviceDataCollection;
//    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (attributeId != null ? attributeId.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof RecordingDeviceAttribute)) {
            return false;
        }
        RecordingDeviceAttribute other = (RecordingDeviceAttribute) object;
        if ((this.attributeId == null && other.attributeId != null) || (this.attributeId != null && !this.attributeId.equals(other.attributeId))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "entities.RecordingDeviceAttribute[ attributeId=" + attributeId + " ]";
    }

//    @XmlTransient
//    public Collection<RecordingDeviceData> getRecordingDeviceDataCollection() {
//        return recordingDeviceDataCollection;
//    }
//
//    public void setRecordingDeviceDataCollection(Collection<RecordingDeviceData> recordingDeviceDataCollection) {
//        this.recordingDeviceDataCollection = recordingDeviceDataCollection;
//    }
//    

}
