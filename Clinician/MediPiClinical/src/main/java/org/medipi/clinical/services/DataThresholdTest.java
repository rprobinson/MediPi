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
package org.medipi.clinical.services;

import java.util.Date;
import org.medipi.clinical.dao.AlertDAOImpl;
import org.medipi.clinical.dao.AttributeThresholdDAOImpl;
import org.medipi.clinical.dao.RecordingDeviceDataDAOImpl;
import org.medipi.clinical.entities.Alert;
import org.medipi.clinical.entities.AttributeThreshold;
import org.medipi.clinical.entities.Patient;
import org.medipi.clinical.entities.RecordingDeviceAttribute;
import org.medipi.clinical.entities.RecordingDeviceData;
import org.medipi.clinical.logging.MediPiLogger;
import org.medipi.clinical.threshold.AttributeThresholdTest;
import org.medipi.clinical.threshold.ThresholdTestFactory;
import org.medipi.clinical.utilities.Utilities;
import org.medipi.model.AlertDO;
import org.medipi.model.AlertListDO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 *
 * @author rick@robinsonhq.com
 */
@Component
public class DataThresholdTest {

    @Autowired
    private RecordingDeviceDataDAOImpl recordingDeviceDataDAOImpl;
    @Autowired
    private AttributeThresholdDAOImpl attributeThresholdDAOImpl;
    @Autowired
    private AlertDAOImpl alertDAOImpl;
    @Autowired
    private Utilities utils;
    @Autowired
    private ThresholdTestFactory thresholdTestFactory;

    @Autowired
    private MediPiLogger logger;


    public void testNewData(RecordingDeviceAttribute rda, Patient patient, RecordingDeviceData rddSet, AlertListDO alertListDO) throws InstantiationException, ClassNotFoundException, IllegalAccessException {
        // find the latest threshold type using the attribute
        AttributeThreshold at = this.attributeThresholdDAOImpl.findLatestByAttributeAndPatientAndDate(rda.getAttributeId(), patient.getPatientUuid(), rddSet.getDataValueTime());
        if (at != null) {
            String testType = at.getThresholdType();
            // if there is an unrecognised test type returned
            if (testType != null) {
                AttributeThresholdTest thresholdTest = thresholdTestFactory.getInstance(testType);
                try {
                    thresholdTest.init(utils.getProperties(), at);
                    String testStatus;
                    Boolean result = thresholdTest.test(rddSet);
                    if (result == null) {
                        //This means the result is not calculatable
                        testStatus = "CANNOT_CALCULATE";
                    } else if (!result) {
                        // send alert
                        System.out.println("ALERT TO BE SENT");
                        testStatus = "OUT_OF_THRESHOLD";

                        //create the Alert
                        Alert alert = new Alert();
                        String alertText = thresholdTest.getFailedTestText()
                                .replace("__ATTRIBUTE_NAME__", rddSet.getAttributeId().getAttributeName())
                                .replace("__MEASUREMENT_DATE__", Utilities.DISPLAY_FORMAT.format(rddSet.getDataValueTime()));
                        alert.setAlertText(alertText);
                        alert.setAlertTime(new Date());
                        alert.setDataId(rddSet);
                        alert.setPatientUuid(patient);
                        try {
                            Alert updatedAlert = alertDAOImpl.save(alert);
                            //create the alert data object to be serialised to the concentrator
                            AlertDO alertDO = new AlertDO(alert.getPatientUuid().getPatientUuid());
                            alertDO.setAlertId(updatedAlert.getAlertId());
                            alertDO.setAlertText(alert.getAlertText());
                            alertDO.setAlertTime(alert.getAlertTime());
                            alertDO.setType(rddSet.getAttributeId().getTypeId().getType());
                            alertDO.setAttributeName(rddSet.getAttributeId().getAttributeName());
                            alertDO.setDataValue(rddSet.getDataValue());
                            alertDO.setDataValueTime(rddSet.getDataValueTime());
                            alertListDO.addAlert(alertDO);
                        } catch (Exception e) {
                            logger.log(DataThresholdTest.class.getName() + ".dbIssue", "Attempt to write alert for dataId" + rddSet.getDataId() + " to DB failed");

                        }

                    } else {
                        System.out.println("No ALERT NECESSARY");
                        testStatus = "IN_THRESHOLD";
                    }
                    rddSet.setAlertStatus(testStatus);
                    this.recordingDeviceDataDAOImpl.update(rddSet);

                } catch (Exception e) {
                    MediPiLogger.getInstance().log(DataThresholdTest.class.getName() + "error", e.getLocalizedMessage());
                    System.out.println(e.getLocalizedMessage());
                }
            }
        } else {
            // if there is no associated test for a data attribute type - no action
        }
    }

}
