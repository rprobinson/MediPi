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
import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * 
 * @author rick@robinsonhq.com
 */
@Entity
@Table(name = "direct_message_text")
@XmlRootElement
@NamedQueries({
//added

    @NamedQuery(name = "DirectMessageText.findAll", query = "SELECT a FROM DirectMessageText a"),
    @NamedQuery(name = "DirectMessageText.findByDirectMessageTextId", query = "SELECT a FROM DirectMessageText a WHERE a.directMessageTextId = :directMessageTextId")})
public class DirectMessageText implements Serializable {


    private static final long serialVersionUID = 1L;
    @Id
    @Basic(optional = false)
    @NotNull
    @Size(min = 1, max = 200)
    @Column(name = "direct_message_text_id")
    private String directMessageTextId;
    @Basic(optional = false)
    @NotNull
    @Size(min = 1, max = 5000)
    @Column(name = "direct_message_text")
    private String directMessageText;

    public DirectMessageText() {
    }

    public DirectMessageText(String directMessageTextId) {
        this.directMessageTextId = directMessageTextId;
    }

    public DirectMessageText(String directMessageTextId, String directMessageText) {
        this.directMessageTextId = directMessageTextId;
        this.directMessageText = directMessageText;
    }

    public String getDirectMessageTextId() {
        return directMessageTextId;
    }

    public void setDirectMessageTextId(String directMessageTextId) {
        this.directMessageTextId = directMessageTextId;
    }

    public String getDirectMessageText() {
        return directMessageText;
    }

    public void setDirectMessageText(String directMessageText) {
        this.directMessageText = directMessageText;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (directMessageTextId != null ? directMessageTextId.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof DirectMessageText)) {
            return false;
        }
        DirectMessageText other = (DirectMessageText) object;
        if ((this.directMessageTextId == null && other.directMessageTextId != null) || (this.directMessageTextId != null && !this.directMessageTextId.equals(other.directMessageTextId))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "org.medipi.clinical.entities.DirectMessageText[ directMessageTextId=" + directMessageTextId + " ]";
    }
    
}
