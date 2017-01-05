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
 *
 * @author rick@robinsonhq.com
 */
@Component
public class QuestionnaireTest implements AttributeThresholdTest {

    private static final String GREEN_FLAG = "GREEN_FLAG";
    private static final String RED_FLAG = "RED_FLAG";
    private static final String QUESTIONNAIRE_TYPE = "__QUESTIONNAIRE_TYPE__";
    private static final String MEDIPICLINICALALERTPASSEDTESTTEXT = "medipi.clinical.alert.questionnairetest.passedtesttext";
    private static final String MEDIPICLINICALALERTFAILEDTESTTEXT = "medipi.clinical.alert.questionnairetest.failedtesttext";

    private String failedTestText = null;
    private String passedTestText = null;
    private String questionnaireType = null;

    /**
     * Initialises the threshold test setting the parameters for its use
     *
     * @param properties properties class
     * @param attributeThreshold to obtain attributes from the DB
     * @throws Exception
     */
    @Override
    public void init(Properties properties, AttributeThreshold attributeThreshold) throws Exception {
        failedTestText = properties.getProperty(MEDIPICLINICALALERTFAILEDTESTTEXT);
        if (failedTestText == null || failedTestText.trim().length() == 0) {
            throw new Exception("Cannot find failed test text");
        }
        passedTestText = properties.getProperty(MEDIPICLINICALALERTPASSEDTESTTEXT);
        if (passedTestText == null || passedTestText.trim().length() == 0) {
            throw new Exception("Cannot find passed test text");
        }
    }

    /**
     * Method to test if a new measurement is in or out of threshold
     *
     * @param rdd data to be tested as part of a RecordingDeviceData object
     * @return returns true if test in within bounds false if test is out of
     * bounds
     */
    @Override
    public Boolean test(RecordingDeviceData rdd) {
        questionnaireType = rdd.getAttributeId().getTypeId().getDisplayName();
        switch (rdd.getDataValue()) {
            case GREEN_FLAG:
                return true;
            case RED_FLAG:
                return false;
            default:
                // QUESTION: the structure of the questionnaire is to have multiple 
                //data points for each qurtion and answer. This means that each of 
                //these will trigger a test for questionnaireTest and therefore all 
                //except the last data point will return null whihc is equivalent 
                //to "CANNOT_CALCULATE" - a failure condition. Should the whole 
                //questionnaire be concatenated into one string line ?
                return null;
        }

    }

    /**
     * Method to return a list of measurements representing the thresholds for
     * that data point time
     *
     * @return list of threshold points
     */
    @Override
    public List<Double> getThreshold(RecordingDeviceData rdd) throws Exception {
        return getThreshold(rdd.getAttributeId().getAttributeId(), rdd.getPatientUuid().getPatientUuid(), rdd.getDataValueTime(), rdd.getDataValue());
    }

    @Override
    public List<Double> getThreshold(int attributeId, String patientUuid, Date dataValueTime, String dataValue) throws Exception {
        return null;
    }

    /**
     * Method to return a descriptive string taken from the properties file and
     * substituted with values from the measurement data describing a
     * non-failure condition
     *
     * @return descriptive string of the alert
     */
    @Override
    public String getFailedTestText() {
        String response = failedTestText
                .replace(QUESTIONNAIRE_TYPE, String.valueOf(questionnaireType));
        return response;
    }

    @Override
    public String getPassedTestText() {
        String response = passedTestText
                .replace(QUESTIONNAIRE_TYPE, String.valueOf(questionnaireType));
        return response;
    }

}
