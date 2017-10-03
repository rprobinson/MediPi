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

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import javax.annotation.PostConstruct;
import org.medipi.clinical.dao.DirectMessageTextDAOImpl;
import org.medipi.clinical.dao.RecordingDeviceDataDAOImpl;
import org.medipi.clinical.dao.SimpleMessageDAOImpl;
import org.medipi.clinical.entities.Patient;
import org.medipi.clinical.entities.SimpleMessage;
import org.medipi.clinical.logging.MediPiLogger;
import org.medipi.clinical.utilities.Utilities;
import org.medipi.model.DirectPatientMessage;
import org.medipi.model.SimpleMessageDO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.stereotype.Component;

/**
 *
 * @author rick@robinsonhq.com
 */
@Component
public class LinkedSubmissionsTester implements Tester {

    @Autowired
    private RecordingDeviceDataDAOImpl recordingDeviceDataDAOImpl;
    @Autowired
    private SimpleMessageDAOImpl simpleMessageDAOImpl;
    @Autowired
    private DirectMessageTextDAOImpl directMessageTextDAOImpl;
    @Autowired
    private Utilities utils;

    @Autowired
    private MediPiLogger logger;

    @Value("${medipi.clinical.simplemessage.resourcepath}")
    private String simpleMessagePatientResourcePath;

    private static final String MEDIPICLINICALSIMPLEMESSAGEENABLED = "medipi.clinical.simplemessage.enabled";
    private static final String MEDIPICLINICALALERTSPOSITVEFEEDBACKMESSAGE = "medipi.clinical.simplemessage.positivefeedbackmessage";
    private static final String MEDIPICLINICALMAXNUMBEROFRETRIES = "medipi.clinical.simplemessage.maxnumberofretries";
    private boolean enabled = false;
    private static final String CONSECUTIVE_SUBMISSIONS = "__CONSECUTIVE_SUBMISSIONS__";
    private static final String SUBMISSION_DATE = "__SUBMISSION_DATE__";

    public void init() {
        String spa = utils.getProperties().getProperty(MEDIPICLINICALSIMPLEMESSAGEENABLED);
        if (spa != null && spa.toLowerCase().startsWith("y")) {
            enabled = true;
        } else {
            enabled = false;
        }
    }

    public void testNewData(Patient patient, SimpleMessageDO simpleMessageDO) throws InstantiationException, ClassNotFoundException, IllegalAccessException {
        try {

            Date d = new Date();
            int consecutiveSubmissions = 0;
            boolean doSomthing = true;

            while (doSomthing) {
                try {
                    d = this.recordingDeviceDataDAOImpl.findByGroupedPatientAndScheduledTime(patient, d);
                    consecutiveSubmissions++;
                } catch (EmptyResultDataAccessException e) {
                    doSomthing = false;
                }
            }
            if (consecutiveSubmissions > 0) {
                System.out.println("Consecutive submissions:" + consecutiveSubmissions);
                String responseText;
                try {
                    responseText = this.directMessageTextDAOImpl.findByDirectMessageTextId(MEDIPICLINICALALERTSPOSITVEFEEDBACKMESSAGE + "." + consecutiveSubmissions).getDirectMessageText();
                } catch (EmptyResultDataAccessException erdae) {
                    responseText = this.directMessageTextDAOImpl.findByDirectMessageTextId(MEDIPICLINICALALERTSPOSITVEFEEDBACKMESSAGE + ".default").getDirectMessageText();
                    if (responseText == null || responseText.trim().length() == 0) {
                        throw new Exception("Cannot find default positive feedback response text");
                    }
                }
                responseText = responseText.replace(CONSECUTIVE_SUBMISSIONS, String.valueOf(consecutiveSubmissions));
                SimpleDateFormat simpleDateFormat = new SimpleDateFormat("EEE d MMM, h:mm a");

                responseText = responseText.replace(SUBMISSION_DATE, simpleDateFormat.format(new Date()));

                CreateAlert(patient, simpleMessageDO, responseText);
            }

        } catch (Exception e) {
            MediPiLogger.getInstance().log(LinkedSubmissionsTester.class.getName() + "error", e.getLocalizedMessage());
            System.out.println(e.getLocalizedMessage());
        }
    }

    private void CreateAlert(Patient patient, SimpleMessageDO simpleMessageDO, String simpleMessageText) {
        //create the Alert
        SimpleMessage simpleMessage = new SimpleMessage();
        simpleMessage.setSimpleMessageText(simpleMessageText);
        simpleMessage.setSimpleMessageTime(new Date());
        simpleMessage.setPatientUuid(patient);
        try {
            SimpleMessage updatedSimpleMessage = simpleMessageDAOImpl.save(simpleMessage);
            //create the alert data object to be serialised to the concentrator
            simpleMessageDO.setPatientUuid(simpleMessage.getPatientUuid().getPatientUuid());
            simpleMessageDO.setSimpleMessageId(updatedSimpleMessage.getSimpleMessageId());
            simpleMessageDO.setSimpleMessageText(simpleMessage.getSimpleMessageText());
            simpleMessageDO.setSimpleMessageTime(simpleMessage.getSimpleMessageTime());
        } catch (Exception e) {
            logger.log(LinkedSubmissionsTester.class.getName() + ".dbIssue", "Attempt to write simple message for patient" + patient.getPatientUuid() + " to DB failed");

        }
    }

    @Override
    public String getDirectPatientMessageResourcePath() {
        return simpleMessagePatientResourcePath;
    }

    @Override
    public boolean updateDirectPatientMessageTableWithSuccess(DirectPatientMessage directPatientMessage) {
        SimpleMessageDO smdo = (SimpleMessageDO) directPatientMessage;
        SimpleMessage sm = simpleMessageDAOImpl.findByPrimaryKey(smdo.getSimpleMessageId());
        sm.setTransmitSuccessDate(new Date());
        simpleMessageDAOImpl.update(sm);

        return true;
    }

    @Override
    public void updateDirectPatientMessageTableWithFail(DirectPatientMessage directPatientMessage) {
        SimpleMessageDO smdo = (SimpleMessageDO) directPatientMessage;
        SimpleMessage sm = simpleMessageDAOImpl.findByPrimaryKey(smdo.getSimpleMessageId());
        sm.setRetryAttempts(sm.getRetryAttempts() + 1);
        simpleMessageDAOImpl.update(sm);
    }

    @Override
    public void failDirectPatientMessageTable(DirectPatientMessage directPatientMessage) {
        SimpleMessageDO smdo = (SimpleMessageDO) directPatientMessage;
        SimpleMessage sm = simpleMessageDAOImpl.findByPrimaryKey(smdo.getSimpleMessageId());
        sm.setRetryAttempts(-1);
        simpleMessageDAOImpl.update(sm);
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
        List<SimpleMessage> aList = simpleMessageDAOImpl.findByNullTransmitSuccessDate(maxRetries);
        List<DirectPatientMessage> retryDirectMessageList = new ArrayList<>();
        if (aList != null && !aList.isEmpty()) {
            for (SimpleMessage simpleMessage : aList) {
                //create the alert data object to be serialised to the concentrator
                SimpleMessageDO simpleMessageDO = new SimpleMessageDO();
                simpleMessageDO.setPatientUuid(simpleMessage.getPatientUuid().getPatientUuid());
                simpleMessageDO.setSimpleMessageId(simpleMessage.getSimpleMessageId());
                simpleMessageDO.setSimpleMessageText(simpleMessage.getSimpleMessageText());
                simpleMessageDO.setSimpleMessageTime(simpleMessage.getSimpleMessageTime());
                retryDirectMessageList.add(simpleMessageDO);

            }
        }
        return retryDirectMessageList;
    }

    @Override
    public boolean isEnabled() {
        return enabled;
    }

}
