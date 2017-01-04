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
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

/**
 * Entity class encapsulating RecordingDeviceType database table
 * @author rick@robinsonhq.com
 */

@Entity
@Table(name = "recording_device_type")
@NamedQueries({
    //Added
    @NamedQuery(name = "RecordingDeviceType.findByTypeMakeModelDisplayName", query = "SELECT r FROM RecordingDeviceType r WHERE r.type = :type AND r.make = :make AND r.model = :model AND r.displayName = :displayname"),
    @NamedQuery(name = "RecordingDeviceType.findByPatient", query = "SELECT t.type FROM RecordingDeviceData d, RecordingDeviceAttribute a, RecordingDeviceType t WHERE d.attributeId = a.attributeId AND a.typeId = t.typeId AND d.patientUuid.patientUuid = :patientUuid GROUP BY t.type"),
    //
    @NamedQuery(name = "RecordingDeviceType.findAll", query = "SELECT r FROM RecordingDeviceType r"),
    @NamedQuery(name = "RecordingDeviceType.findByTypeId", query = "SELECT r FROM RecordingDeviceType r WHERE r.typeId = :typeId"),
    @NamedQuery(name = "RecordingDeviceType.findByType", query = "SELECT r FROM RecordingDeviceType r WHERE r.type = :type"),
    @NamedQuery(name = "RecordingDeviceType.findByMake", query = "SELECT r FROM RecordingDeviceType r WHERE r.make = :make"),
    @NamedQuery(name = "RecordingDeviceType.findByModel", query = "SELECT r FROM RecordingDeviceType r WHERE r.model = :model"),
    @NamedQuery(name = "RecordingDeviceType.findByDisplayName", query = "SELECT r FROM RecordingDeviceType r WHERE r.displayName = :displayname")})
public class RecordingDeviceType implements Serializable {

    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "type_id")
    private Integer typeId;
    @Basic(optional = false)
    @NotNull
    @Size(min = 1, max = 100)
    @Column(name = "type")
    private String type;
    @Basic(optional = false)
    @NotNull
    @Size(min = 1, max = 100)
    @Column(name = "make")
    private String make;
    @Basic(optional = false)
    @NotNull
    @Size(min = 1, max = 100)
    @Column(name = "model")
    private String model;
    @Basic(optional = false)
    @NotNull
    @Size(min = 1, max = 1000)
    @Column(name = "display_name")
    private String displayName;

    public RecordingDeviceType() {
    }

    public RecordingDeviceType(Integer typeId) {
        this.typeId = typeId;
    }

    public RecordingDeviceType(Integer typeId, String type, String make, String model, String displayName) {
        this.typeId = typeId;
        this.type = type;
        this.make = make;
        this.model = model;
        this.displayName = displayName;
    }

    public Integer getTypeId() {
        return typeId;
    }

    public void setTypeId(Integer typeId) {
        this.typeId = typeId;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getMake() {
        return make;
    }

    public void setMake(String make) {
        this.make = make;
    }
    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }
    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (typeId != null ? typeId.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof RecordingDeviceType)) {
            return false;
        }
        RecordingDeviceType other = (RecordingDeviceType) object;
        if ((this.typeId == null && other.typeId != null) || (this.typeId != null && !this.typeId.equals(other.typeId))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "org.medipi.concentrator.RecordingDeviceType[ typeId=" + typeId + " ]";
    }


}
