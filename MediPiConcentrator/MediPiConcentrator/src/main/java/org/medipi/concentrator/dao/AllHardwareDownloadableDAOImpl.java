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
package org.medipi.concentrator.dao;

import java.util.List;
import org.medipi.concentrator.entities.AllHardwareDownloadable;
import org.springframework.stereotype.Repository;

/**
 * Implementation of Data Access Object for AllHardwareDownloadable
 * @author rick@robinsonhq.com
 */
@Repository
public class AllHardwareDownloadableDAOImpl extends GenericDAOImpl<AllHardwareDownloadable> implements AllHardwareDownloadableDAO {

    @Override
    public List<AllHardwareDownloadable> getHardwareDownloads(String hardware) {
        return this.getEntityManager().createNamedQuery("AllHardwareDownloadable.findAllDownloadable", AllHardwareDownloadable.class)
                .setParameter("hname", hardware)
                .getResultList();
    }

    @Override
    public AllHardwareDownloadable getHardwareDownload(String downloadableUuid) {
        return this.getEntityManager().createNamedQuery("AllHardwareDownloadable.findByDownloadableUuid", AllHardwareDownloadable.class)
                .setParameter("downloadableUuid", downloadableUuid)
                .getSingleResult();
    }

}