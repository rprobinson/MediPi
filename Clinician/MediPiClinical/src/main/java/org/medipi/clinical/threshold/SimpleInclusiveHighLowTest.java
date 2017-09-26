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

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Properties;
import org.medipi.clinical.dao.AttributeThresholdDAOImpl;
import org.medipi.clinical.dao.DirectMessageTextDAOImpl;
import org.medipi.clinical.dao.RecordingDeviceDataDAOImpl;
import org.medipi.clinical.entities.AttributeThreshold;
import org.medipi.clinical.entities.RecordingDeviceData;
import org.medipi.clinical.logging.MediPiLogger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 *
 * @author rick@robinsonhq.com
 */
@Component
public class SimpleInclusiveHighLowTest implements AttributeThresholdTest {

    private static final String LOWER_THRESHOLD_LIMIT = "__LOWER_THRESHOLD_LIMIT__";
    private static final String UPPER_THRESHOLD_LIMIT = "__UPPER_THRESHOLD_LIMIT__";
    private static final String DATA_VALUE = "__DATA_VALUE__";
    private static final String ATTRIBUTE_UNITS = "__ATTRIBUTE_UNITS__";
    private static final String MEDIPICLINICALALERTPASSEDTESTTEXT = "medipi.clinical.alert.simpleinclusivehighlowtest.passedtesttext";
    private static final String MEDIPICLINICALALERTFAILEDTESTTEXT = "medipi.clinical.alert.simpleinclusivehighlowtest.failedtesttext";
    private static final String MEDIPICLINICALALERTCANTCALCULATETESTTEXT = "medipi.clinical.alert.simpleinclusivehighlowtest.cantcalculatetesttext";

    @Autowired
    private RecordingDeviceDataDAOImpl recordingDeviceDataDAOImpl;

    @Autowired
    private AttributeThresholdDAOImpl attributeThresholdDAOImpl;

    @Autowired
    private DirectMessageTextDAOImpl directMessageTextDAOImpl;

    private Properties properties;
    private AttributeThreshold attributeThreshold;
    private double lowValue;
    private double highValue;
    private double dataValue;
    private String attributeUnits = null;

    private String failedTestText = null;
    private String passedTestText = null;
    private String cantCalculateTestText = null;

    /**
     * Initialises the threshold test setting the parameters for its use
     *
     * @param properties properties class
     * @param attributeThreshold to obtain attributes from the DB
     * @throws Exception
     */
    @Override
    public void init(Properties properties, AttributeThreshold attributeThreshold) throws Exception {
        failedTestText = this.directMessageTextDAOImpl.findByDirectMessageTextId(MEDIPICLINICALALERTFAILEDTESTTEXT).getDirectMessageText();
        cantCalculateTestText = this.directMessageTextDAOImpl.findByDirectMessageTextId(MEDIPICLINICALALERTCANTCALCULATETESTTEXT).getDirectMessageText();
        passedTestText = this.directMessageTextDAOImpl.findByDirectMessageTextId(MEDIPICLINICALALERTPASSEDTESTTEXT).getDirectMessageText();

        lowValue = getLowValue(attributeThreshold.getThresholdLowValue());
        highValue = getHighValue(attributeThreshold.getThresholdHighValue());

    }

    private double getLowValue(String low) throws Exception {
        try {
            return Double.valueOf(low);
        } catch (NumberFormatException nfe) {
            MediPiLogger.getInstance().log(SimpleInclusiveHighLowTest.class.getName() + "error", "Error in converting the low threshold values to a double " + nfe.getLocalizedMessage());
            System.out.println("Error in converting the low threshold values to a double" + nfe.getLocalizedMessage());
            throw new Exception("Error in converting the low threshold values to a double");
        }
    }

    private double getHighValue(String high) throws Exception {
        try {
            return Double.valueOf(high);
        } catch (NumberFormatException nfe) {
            MediPiLogger.getInstance().log(SimpleInclusiveHighLowTest.class.getName() + "error", "Error in converting the high threshold values to a double " + nfe.getLocalizedMessage());
            System.out.println("Error in converting the high threshold values to a double" + nfe.getLocalizedMessage());
            throw new Exception("Error in converting the high threshold values to a double");
        }
    }    
    private double getDataValue(String data) throws Exception {
        try {
            return Double.valueOf(data);
        } catch (NumberFormatException nfe) {
            MediPiLogger.getInstance().log(SimpleInclusiveHighLowTest.class.getName() + "error", "Error in converting the data values to a double " + nfe.getLocalizedMessage());
            System.out.println("Error in converting the data values to a double" + nfe.getLocalizedMessage());
            throw new Exception("Error in converting the data values to a double");
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
        try {
            attributeUnits = rdd.getAttributeId().getAttributeUnits();
            dataValue = Double.valueOf(rdd.getDataValue());

            return !(dataValue > highValue || dataValue < lowValue);

        } catch (NumberFormatException nfe) {
            MediPiLogger.getInstance().log(SimpleInclusiveHighLowTest.class.getName() + "error", "Error in converting the incoming data value to be tested to a double " + nfe.getLocalizedMessage());
            System.out.println("Error in converting the incoming data value to be tested to a double" + nfe.getLocalizedMessage());
            return null;
        } catch (Exception e) {
            MediPiLogger.getInstance().log(ChangeOverTimeTest.class.getName() + "error", "Error in testing the simple inclusive high low value: " + e.getLocalizedMessage());
            System.out.println("Error in testing the simple inclusive high low value: " + e.getLocalizedMessage());
            return null;
        }
    }

    /**
     * Method to return a descriptive string taken from the properties file and
     * substituted with values from the measurement data describing a failure
     * condition
     *
     * @return descriptive string of the alert
     */
    @Override
    public List<Double> getThreshold(RecordingDeviceData rdd) throws Exception {
        return getThreshold(rdd.getAttributeId().getAttributeId(), rdd.getPatientUuid().getPatientUuid(), rdd.getDataValueTime(), rdd.getDataValue());
    }

    @Override
    public List<Double> getThreshold(int attributeId, String patientUuid, Date dataValueTime, String dataValue) throws Exception {
        AttributeThreshold at = attributeThresholdDAOImpl.findLatestByAttributeAndPatientAndDate(attributeId, patientUuid, dataValueTime);
        if (at == null) {
            return null;
        } else {
            double low = getLowValue(at.getThresholdLowValue());
            double high = getHighValue(at.getThresholdHighValue());
            List<Double> thresholdList = new ArrayList<>();

            thresholdList.add(low);
            thresholdList.add(high);
            return thresholdList;
        }
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
                .replace(DATA_VALUE, String.valueOf(dataValue))
                .replace(ATTRIBUTE_UNITS, attributeUnits)
                .replace(UPPER_THRESHOLD_LIMIT, String.valueOf(highValue))
                .replace(LOWER_THRESHOLD_LIMIT, String.valueOf(lowValue));
        return response;
    }

    @Override
    public String getPassedTestText() {
        String response = passedTestText
                .replace(DATA_VALUE, String.valueOf(dataValue))
                .replace(ATTRIBUTE_UNITS, attributeUnits)
                .replace(UPPER_THRESHOLD_LIMIT, String.valueOf(highValue))
                .replace(LOWER_THRESHOLD_LIMIT, String.valueOf(lowValue));
        return response;
    }

    @Override
    public String getCantCalculateTestText() {
        String response = cantCalculateTestText
                .replace(DATA_VALUE, String.valueOf(dataValue))
                .replace(ATTRIBUTE_UNITS, attributeUnits)
                .replace(UPPER_THRESHOLD_LIMIT, String.valueOf(highValue))
                .replace(LOWER_THRESHOLD_LIMIT, String.valueOf(lowValue));
        return response;
    }

}
