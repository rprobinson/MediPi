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
package org.medipi.concentrator.dataformat;

import org.medipi.concentrator.entities.Patient;
import org.medipi.model.DevicesPayloadDO;
import org.springframework.transaction.annotation.Transactional;

/**
 * Abstract class to give a common interface for all data formats
 *
 *
 * @author rick@robinsonhq.com
 */
public abstract class PatientUploadDataFormat {

    /**
     * Constructor
     */
    public PatientUploadDataFormat() {
    }

    /**
     * Sets the classtoken from the properties file.
     *
     * This is commonly used if further properties need to be called specific to
     * the data format instance
     *
     * @param classToken
     */
    public abstract void setClassToken(String classToken);

    /**
     * Initialising the class
     *
     * @return a string representation of any failures that have occurred during
     * initialisation
     */
    public abstract String init();

    /**
     * Method to process the data received as per the defined data format
     *
     * @param content the full RESTful message content
     * @param patient The patient class relating to the content
     * @return boolean representation of the outcome of the process - in general
     * any failures will throw issues and not return a false
     */
    @Transactional(rollbackFor = RuntimeException.class)
    public abstract Boolean process(DevicesPayloadDO content, Patient patient);
}
