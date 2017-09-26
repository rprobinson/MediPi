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
package org.medipi.clinical.dao;

import org.medipi.clinical.entities.DirectMessageText;
import org.springframework.stereotype.Repository;

/**
 * Implementation of Data Access Object for directMessageText
 * @author rick@robinsonhq.com
 */
@Repository
public class DirectMessageTextDAOImpl extends GenericDAOImpl<DirectMessageText> implements DirectMessageTextDAO {
    @Override
    public DirectMessageText findByDirectMessageTextId(String directMessageTextId) {
        return this.getEntityManager().createNamedQuery("DirectMessageText.findByDirectMessageTextId", DirectMessageText.class)
                .setParameter("directMessageTextId", directMessageTextId)
                .getSingleResult();
    }
}