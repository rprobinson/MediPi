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
package org.medipi.clinical.threshold;

import java.util.Date;
import java.util.List;
import java.util.Properties;
import org.medipi.clinical.entities.AttributeThreshold;
import org.medipi.clinical.entities.RecordingDeviceData;
import org.springframework.stereotype.Component;

/**
 * Interface for Attribute threshold tests 
 * @author rick@robinsonhq.com
 */
@Component
public interface AttributeThresholdTest {
    
    /**
     * Initialises the threshold test setting the parameters for its use
     *
     * @param properties properties class
     * @param attributeThreshold to obtain attributes from the DB
     * @throws Exception
     */
    public void init(Properties properties,AttributeThreshold attributeThreshold) throws Exception;

    /**
     * Method to test if a new measurement is in or out of threshold
     *
     * @param rdd data to be tested as part of a RecordingDeviceData object
     * @return returns true if test in within bounds false if test is out of
     * bounds
     */
    public Boolean test(RecordingDeviceData rdd);

    /**
     * Method to return upper and lower boundary values of the threshold of a
     * given data measurement
     *
     * @param rdd data to be tested as part of a RecordingDeviceData object
     * @return List of threshold boundary values
     * @throws Exception
     */
    public List<Double> getThreshold(RecordingDeviceData rdd)throws Exception;

    public List<Double> getThreshold(int attributeId, String patientUuid, Date dataValueTime, String dataValue)throws Exception;

    /**
     * Method to return a descriptive string taken from the properties file and
     * substituted with values from the measurement data describing a failure condition
     *
     * @return descriptive string of the alert
     */
    public String getFailedTestText();

    /**
     * Method to return a descriptive string taken from the properties file and
     * substituted with values from the measurement data describing a cant calculate condition
     *
     * @return descriptive string of the alert
     */
    public String getCantCalculateTestText();

    /**
     * Method to return a descriptive string taken from the properties file and
     * substituted with values from the measurement data describing a non-failure condition
     *
     * @return descriptive string of the alert
     */
    public String getPassedTestText();
}
