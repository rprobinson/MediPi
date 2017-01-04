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
import org.medipi.concentrator.entities.PatientDownloadable;

/**
 * Data Access Object interface for PatientDownloadable
 * @author rick@robinsonhq.com
 */
public interface PatientDownloadableDAO extends GenericDAO<PatientDownloadable> {
    public List<PatientDownloadable> getPatientDownloads(String patientUuid);
    public PatientDownloadable getPatientDownload(String downloadUuid);
}
