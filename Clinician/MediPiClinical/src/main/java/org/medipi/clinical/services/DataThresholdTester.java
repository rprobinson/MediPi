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

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import javax.annotation.PostConstruct;
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
import org.medipi.model.DirectPatientMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 *
 * @author rick@robinsonhq.com
 */
@Component
public class DataThresholdTester implements Tester {

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
    //Path for posting alerts for patients

    @Value("${medipi.clinical.alert.resourcepath}")
    private String alertPatientResourcePath;

    private static final String MEDIPICLINICALALERTENABLED = "medipi.clinical.alert.enabled";
    private static final String MEDIPICLINICALALERTSENDPOSITIVEALERTS = "medipi.clinical.alert.sendpositivealerts";
    private static final String MEDIPICLINICALALERTSENDNEGATIVEALERTS = "medipi.clinical.alert.sendnegativealerts";
    private static final String MEDIPICLINICALALERTSENDCANNOTCALCULATEALERTS = "medipi.clinical.alert.sendcannotcalculatealerts";
    private static final String MEDIPICLINICALMAXNUMBEROFRETRIES = "medipi.clinical.alert.maxnumberofretries";
    private boolean enabled = false;
    private boolean sendPositiveAlerts = false;
    private boolean sendNegativeAlerts = false;
    private boolean sendCannotCalculateAlerts = false;

    public void init() {
        String en = utils.getProperties().getProperty(MEDIPICLINICALALERTENABLED);
        if (en != null && en.toLowerCase().startsWith("y")) {
            enabled = true;
        } else {
            enabled = false;
        }
    }

    public void testNewData(RecordingDeviceAttribute rda, Patient patient, RecordingDeviceData rddSet, AlertListDO alertListDO) throws InstantiationException, ClassNotFoundException, IllegalAccessException {

        String spa = utils.getProperties().getProperty(MEDIPICLINICALALERTSENDPOSITIVEALERTS);
        if (spa != null && spa.toLowerCase().startsWith("y")) {
            sendPositiveAlerts = true;
        } else {
            sendPositiveAlerts = false;
        }
        String sna = utils.getProperties().getProperty(MEDIPICLINICALALERTSENDNEGATIVEALERTS);
        if (sna != null && sna.toLowerCase().startsWith("y")) {
            sendNegativeAlerts = true;
        } else {
            sendNegativeAlerts = false;
        }
        String scca = utils.getProperties().getProperty(MEDIPICLINICALALERTSENDCANNOTCALCULATEALERTS);
        if (scca != null && scca.toLowerCase().startsWith("y")) {
            sendCannotCalculateAlerts = true;
        } else {
            sendCannotCalculateAlerts = false;
        }
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
                        if (sendCannotCalculateAlerts) {
                            String alertText = thresholdTest.getCantCalculateTestText()
                                    .replace("__ATTRIBUTE_NAME__", rddSet.getAttributeId().getAttributeName())
                                    .replace("__MEASUREMENT_DATE__", Utilities.DISPLAY_FORMAT.format(rddSet.getDataValueTime()));
                            CreateAlert(thresholdTest, rddSet, patient, alertListDO, alertText, testStatus);
                        }
                    } else if (!result) {
                        // send alert
                        testStatus = "OUT_OF_THRESHOLD";
                        if (sendNegativeAlerts) {
                            String alertText = thresholdTest.getFailedTestText()
                                    .replace("__ATTRIBUTE_NAME__", rddSet.getAttributeId().getAttributeName())
                                    .replace("__MEASUREMENT_DATE__", Utilities.DISPLAY_FORMAT.format(rddSet.getDataValueTime()));
                            CreateAlert(thresholdTest, rddSet, patient, alertListDO, alertText, testStatus);
                        }
                    } else {
                        testStatus = "IN_THRESHOLD";
                        if (sendPositiveAlerts) {
                            String alertText = thresholdTest.getPassedTestText()
                                    .replace("__ATTRIBUTE_NAME__", rddSet.getAttributeId().getAttributeName())
                                    .replace("__MEASUREMENT_DATE__", Utilities.DISPLAY_FORMAT.format(rddSet.getDataValueTime()));
                            CreateAlert(thresholdTest, rddSet, patient, alertListDO, alertText, testStatus);
                        }
                    }
                    rddSet.setAlertStatus(testStatus);
                    this.recordingDeviceDataDAOImpl.update(rddSet);

                } catch (Exception e) {
                    MediPiLogger.getInstance().log(DataThresholdTester.class.getName() + "error", e.getLocalizedMessage());
                    System.out.println(e.getLocalizedMessage());
                }
            }
        } else {
            // if there is no associated test for a data attribute type - no action
        }
    }

    private void CreateAlert(AttributeThresholdTest thresholdTest, RecordingDeviceData rddSet, Patient patient, AlertListDO alertListDO, String alertText, String testStatus) {
        if (rddSet.getScheduleEffectiveTime().before(new Date()) && rddSet.getScheduleExpiryTime().after(new Date())) {
            System.out.println(testStatus + " ALERT TO BE SENT");
            //create the Alert
            Alert alert = new Alert();
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
                alertDO.setMake(alert.getDataId().getAttributeId().getTypeId().getMake());
                alertDO.setModel(alert.getDataId().getAttributeId().getTypeId().getModel());
                alertDO.setStatus(testStatus);
                alertDO.setAttributeName(rddSet.getAttributeId().getAttributeName());
                alertDO.setDataValue(rddSet.getDataValue());
                alertDO.setDataValueTime(rddSet.getDataValueTime());
                alertListDO.addAlert(alertDO);
            } catch (Exception e) {
                logger.log(DataThresholdTester.class.getName() + ".dbIssue", "Attempt to write alert for dataId" + rddSet.getDataId() + " to DB failed");

            }
        }
    }

    @Override
    public String getDirectPatientMessageResourcePath() {
        return alertPatientResourcePath;
    }

    @Override
    public boolean updateDirectPatientMessageTableWithSuccess(DirectPatientMessage directPatientMessage) {
        boolean doneSomething = false;
        AlertListDO ald = (AlertListDO) directPatientMessage;
        for (AlertDO ado : ald.getAlert()) {
            Alert a = alertDAOImpl.findByPrimaryKey(ado.getAlertId());
            a.setTransmitSuccessDate(new Date());
            alertDAOImpl.update(a);
            doneSomething = true;
        }
        return doneSomething;
    }

    @Override
    public void updateDirectPatientMessageTableWithFail(DirectPatientMessage directPatientMessage) {
        AlertListDO ald = (AlertListDO) directPatientMessage;
        for (AlertDO ado : ald.getAlert()) {
            Alert a = alertDAOImpl.findByPrimaryKey(ado.getAlertId());
            a.setRetryAttempts(a.getRetryAttempts() + 1);
            alertDAOImpl.update(a);
        }
    }

    @Override
    public void failDirectPatientMessageTable(DirectPatientMessage directPatientMessage) {
        AlertListDO ald = (AlertListDO) directPatientMessage;
        for (AlertDO ado : ald.getAlert()) {
            Alert a = alertDAOImpl.findByPrimaryKey(ado.getAlertId());
            a.setRetryAttempts(-1);
            alertDAOImpl.update(a);
        }
    }

    @Override
    public List<DirectPatientMessage> findDirectPatientMessagesToResend() {
        String maxRetriesString = utils.getProperties().getProperty(MEDIPICLINICALMAXNUMBEROFRETRIES);
        int maxRetries;
        if (maxRetriesString == null || maxRetriesString.trim().length() == 0) {
            maxRetries = 3;
        } else {
            try {
                maxRetries = Integer.parseInt(maxRetriesString);
            } catch (NumberFormatException numberFormatException) {
                MediPiLogger.getInstance().log(SendAlertService.class.getName() + "error", "Error - Cant read the max number of retires for an alert from the properties file: " + numberFormatException.getLocalizedMessage());
                System.out.println("Error - Cant read the max number of retires for an alert from the properties file: " + numberFormatException.getLocalizedMessage());
                maxRetries = 3;
            }
        }
        List<Alert> aList = alertDAOImpl.findByNullTransmitSuccessDate(maxRetries);
        List<DirectPatientMessage> retryAlertList = new ArrayList<>();
        if (aList != null && !aList.isEmpty()) {
            for (Alert alert : aList) {
                //create the alert data object to be serialised to the concentrator
                AlertDO alertDO = new AlertDO(alert.getPatientUuid().getPatientUuid());
                alertDO.setAlertId(alert.getAlertId());
                alertDO.setAlertText(alert.getAlertText());
                alertDO.setAlertTime(alert.getAlertTime());
                alertDO.setType(alert.getDataId().getAttributeId().getTypeId().getType());
                alertDO.setMake(alert.getDataId().getAttributeId().getTypeId().getMake());
                alertDO.setModel(alert.getDataId().getAttributeId().getTypeId().getModel());
                alertDO.setStatus(alert.getDataId().getAlertStatus());
                alertDO.setAttributeName(alert.getDataId().getAttributeId().getAttributeName());
                alertDO.setDataValue(alert.getDataId().getDataValue());
                alertDO.setDataValueTime(alert.getDataId().getDataValueTime());
                List<AlertDO> adoList = new ArrayList<>();
                adoList.add(alertDO);
                retryAlertList.add(new AlertListDO(alert.getPatientUuid().getPatientUuid(), adoList));

            }
        }
        return retryAlertList;
    }

    @Override
    public boolean isEnabled() {
        return enabled;
    }
}
