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
// returning null from the test method means that the test has not been able to be calculated
// This test will take the scheduled test period of the new data and calculate the period in which to test from the mesaurementPeriod variable.
// The last measurement taken in this historical measurement period will be considered the record to test against.
// The data value of this record is then tested against the threshold change value
//
// non scheduled data will not be considered
package org.medipi.clinical.threshold;

import java.util.ArrayList;
import java.util.Calendar;
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
 * Class to measure the change in a measurement over time. This is done using
 * least squares regression method of determining whether the most recent
 * measurement is above or below the calculated least squares prediction as
 * determined by previous measurements.
 *
 * The class also has a method for calculating the threshold boundaries for any
 * single point which can be used to create graphical representations of the
 * data
 *
 * @author rick@robinsonhq.com
 */
@Component
public class ChangeOverTimeTest implements AttributeThresholdTest {

    private static final String MEASUREMENT_PERIOD = "__MEASUREMENT_PERIOD__";
    private static final String MEASUREMENT_CHANGE_THRESHOLD = "__MEASUREMENT_CHANGE_THRESHOLD__";
    private static final String DATA_VALUE = "__DATA_VALUE__";
    private static final String ATTRIBUTE_UNITS = "__ATTRIBUTE_UNITS__";
    private static final String MEDIPICLINICALALERTPASSEDTESTTEXT = "medipi.clinical.alert.changeovertimetest.passedtesttext";
    private static final String MEDIPICLINICALALERTFAILEDTESTTEXT = "medipi.clinical.alert.changeovertimetest.failedtesttext";
    private static final String MEDIPICLINICALALERTCANTCALCULATETESTTEXT = "medipi.clinical.alert.changeovertimetest.cantcalculatetesttext";
    private static final String MEDIPICLINICALFEWESTNUMBEROFDATAPOINTSTOCALCULATEFROM = "medipi.clinical.alert.changeovertimetest.fewestnumberofdatapointstocalculatefrom";

    @Autowired
    private RecordingDeviceDataDAOImpl recordingDeviceDataDAOImpl;

    @Autowired
    private AttributeThresholdDAOImpl attributeThresholdDAOImpl;
    
    @Autowired
    private DirectMessageTextDAOImpl directMessageTextDAOImpl;

    private AttributeThreshold attributeThreshold;
    // This measures the number of hours' period the change should be measured over - IT IS AN INTEGER
    private int measurementPeriod;
    // This measure the maximum allowable change which is acceptable
    private double measurementChangeThreshold;

    private double currentValue;
    private String attributeUnits = null;

    private String failedTestText = null;
    private String cantCalculateTestText = null;
    private String passedTestText = null;
    private int fewestCalculatingPoints = 3;

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

        measurementPeriod = getMeasurementPeriod(attributeThreshold.getThresholdLowValue());
        measurementChangeThreshold = getMeasurementChangeThreshold(attributeThreshold.getThresholdHighValue());
        String fewestCalculatingPointsString = properties.getProperty(MEDIPICLINICALFEWESTNUMBEROFDATAPOINTSTOCALCULATEFROM);
        if (fewestCalculatingPointsString == null || fewestCalculatingPointsString.trim().length() == 0) {
            MediPiLogger.getInstance().log(ChangeOverTimeTest.class.getName() + "warning", "Warning - The fewest number of calculating points to be used is not set on the properties file (default to 3)");
            System.out.println("Warning - The fewest number of calculating points to be used is not set on the properties file (default to 3)");
            fewestCalculatingPoints = 3;
        } else {
            try {
                fewestCalculatingPoints = Integer.parseInt(fewestCalculatingPointsString);
            } catch (NumberFormatException numberFormatException) {
                MediPiLogger.getInstance().log(ChangeOverTimeTest.class.getName() + "error", "Error - Can't read the fewest number of calculating points to be used from the properties file (default to 3): " + numberFormatException.getLocalizedMessage());
                System.out.println("Error - Can't read the fewest number of calculating points to be used from the properties file (default to 3): " + numberFormatException.getLocalizedMessage());
                fewestCalculatingPoints = 3;
            }
        }

    }

    private double getMeasurementChangeThreshold(String threshold) throws Exception {
        try {
            return Double.valueOf(threshold);
        } catch (NumberFormatException nfe) {
            MediPiLogger.getInstance().log(ChangeOverTimeTest.class.getName() + "error", "Error in converting the maximum allowable change in measurement over the set period " + nfe.getLocalizedMessage());
            System.out.println("Error in converting the maximum allowable change in measurement over the set period " + nfe.getLocalizedMessage());
            throw new Exception("Error in converting the maximum allowable change in measurement over the set period ");
        }
    }

    private int getMeasurementPeriod(String period) throws Exception {
        try {
            return Integer.valueOf(period);
        } catch (NumberFormatException nfe) {
            MediPiLogger.getInstance().log(ChangeOverTimeTest.class.getName() + "error", "Error in converting the number of hours' period the measurement change should be measured over - this value must be a whole number of hours " + nfe.getLocalizedMessage());
            System.out.println("Error in converting the number of hours' period the measurement change should be measured over - this value must be a whole number of hours " + nfe.getLocalizedMessage());
            throw new Exception("Error in converting the number of hours' period the measurement change should be measured over - this value must be a whole number of hours ");
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
            currentValue = Double.valueOf(rdd.getDataValue());
            Double historicValue = getHistoricValue(rdd.getAttributeId().getAttributeId(), rdd.getPatientUuid().getPatientUuid(), rdd.getDataValueTime(), measurementPeriod);
            if (historicValue == null) {
                return null;
            } else if (historicValue + measurementChangeThreshold <= currentValue) { // Gained too much
                return false;
            } else if (historicValue - measurementChangeThreshold >= currentValue) { // lost too much
                return false;
            } else {
                return true;
            }
        } catch (NumberFormatException nfe) {
            MediPiLogger.getInstance().log(ChangeOverTimeTest.class.getName() + "error", "Error in converting the incoming data value to be tested to a double " + nfe.getLocalizedMessage());
            System.out.println("Error in converting the incoming data value to be tested to a double" + nfe.getLocalizedMessage());
            return null;
        } catch (Exception e) {
            MediPiLogger.getInstance().log(ChangeOverTimeTest.class.getName() + "error", "Error in testing the change over time: " + e.getLocalizedMessage());
            System.out.println("Error in testing the change over time: " + e.getLocalizedMessage());
            return null;
        }

    }

    private Double getHistoricValue(int attributeId, String patientUuid, Date dataValueTime, int period) {
        try {
            // Can  a date value be found for x hours ago? we will use linear least squared method
            Calendar start = Calendar.getInstance();
            start.setTime(dataValueTime);
            start.add(Calendar.HOUR, -period);
            Date periodStartTime = start.getTime();
            // Access the database and find the first  entry before the started period so that the least square method will have at least the epriod stated
            Date firstBeforePeriod = recordingDeviceDataDAOImpl.findFirstEntryBeforePeriod(patientUuid, attributeId, periodStartTime);
            if (firstBeforePeriod == null) {
                return null;
            }
            // Access the database and collect all records within this period
            List<RecordingDeviceData> historicList = recordingDeviceDataDAOImpl.findByPatientAndAttributeAndPeriod(patientUuid, attributeId, firstBeforePeriod, dataValueTime);

            if (historicList == null || historicList.isEmpty()) {
                // DONT KNOW WHAT TO DO HERE WHERE THERE IS NOT PRECEDING DATAPOINT?
                // 1. return null and count as failure - but this will fail whenever there is a new patient for n days
                // 2. return a true - however the test hagetHistoricValues not passed
                // 3. return a false - however the test has not failed
                // 4. interpolate from surrounding data and perform test - tricky and dangerous

                return null;
            } else {
                int MAXN = 1000;
                int n = 0;
                double[] x = new double[MAXN];
                double[] y = new double[MAXN];

                // first pass: read in data, compute xbar and ybar
                double sumx = 0.0, sumy = 0.0, sumx2 = 0.0;
                for (RecordingDeviceData historic : historicList) {
                    Long startMillis = firstBeforePeriod.getTime();
                    Long pointMillis = historic.getDataValueTime().getTime();
                    Long millisAfterStart = pointMillis - startMillis;
                    x[n] = Double.valueOf(millisAfterStart);
                    y[n] = Double.valueOf(historic.getDataValue());
                    sumx += x[n];
                    sumx2 += x[n] * x[n];
                    sumy += y[n];
                    n++;
                }
                //if there are fewer than 2 values its not got enough data to perform a least squarea
                if (n < fewestCalculatingPoints || n <= 1) {
                    return null;
                }
                double xbar = sumx / n;
                double ybar = sumy / n;

                // second pass: compute summary statistics
                double xxbar = 0.0, yybar = 0.0, xybar = 0.0;
                for (int i = 0; i < n; i++) {
                    xxbar += (x[i] - xbar) * (x[i] - xbar);
                    yybar += (y[i] - ybar) * (y[i] - ybar);
                    xybar += (x[i] - xbar) * (y[i] - ybar);
                }
                double beta1 = xybar / xxbar;
                double beta0 = ybar - beta1 * xbar;

                // print results
                System.out.println("y   = " + beta1 + " * x + " + beta0);
                long millisAtPeriodStartTime = dataValueTime.getTime() - periodStartTime.getTime();
                // As we are wanting the value at millisecondsAfter Start=0 need to return beta1*0+ beta0
                // y=ax * b
                Double h = (beta1 * millisAtPeriodStartTime) + beta0;
                System.out.println("millisAtPeriodStartTime=" + millisAtPeriodStartTime);
                System.out.println("weight at lastPoint==" + h);
                return h;
            }
        } catch (NumberFormatException nfe) {
            MediPiLogger.getInstance().log(ChangeOverTimeTest.class.getName() + "error", "Error in converting the incoming data value to be tested to a double " + nfe.getLocalizedMessage());
            System.out.println("Error in converting the incoming data value to be tested to a double" + nfe.getLocalizedMessage());
            return null;
        } catch (Exception e) {
            MediPiLogger.getInstance().log(ChangeOverTimeTest.class.getName() + "error", "Error in testing the change over time: " + e.getLocalizedMessage());
            System.out.println("Error in testing the change over time: " + e.getLocalizedMessage());
            return null;
        }

    }
//    private Double getHistoricValue(RecordingDeviceData rdd, int period) {
//        try {
//            // Can  a date value be found for x hours ago? This is x hours according to the scheduled time periods
//            //calculate when the comparison period was
//            Calendar schedStart = Calendar.getInstance();
//            schedStart.setTime(rdd.getScheduleEffectiveTime());
//            schedStart.add(Calendar.HOUR, -period);
//            Date startDate = schedStart.getTime();
//
//            Calendar schedEnd = Calendar.getInstance();
//            schedEnd.setTime(rdd.getScheduleExpiryTime());
//            schedEnd.add(Calendar.HOUR, -period);
//            Date endDate = schedEnd.getTime();
//
//            // Access the database and see if there is a record
//            RecordingDeviceData historicRdd = recordingDeviceDataDAOImpl.findByPatientAndScheduledTime(rdd.getPatientUuid(), startDate, endDate);
//            if (historicRdd == null) {
//                // DONT KNOW WHAT TO DO HERE WHERE THERE IS NOT PRECEDING DATAPOINT?
//                // 1. return null and count as failure - but this will fail whenever there is a new patient for n days
    //                // 2. return a true - however the test hagetHistoricValues not passed
//                // 3. return a false - however the test has not failed
//                // 4. interpolate from surrounding data and perform test - tricky and dangerous
//
//                return null;
//            } else {
//                return Double.valueOf(historicRdd.getDataValue());
//            }
//        } catch (NumberFormatException nfe) {
//            MediPiLogger.getInstance().log(ChangeOverTimeTest.class.getName() + "error", "Error in converting the incoming data value to be tested to a double " + nfe.getLocalizedMessage());
//            System.out.println("Error in converting the incoming data value to be tested to a double" + nfe.getLocalizedMessage());
//            return null;
//        } catch (Exception e) {
//            MediPiLogger.getInstance().log(ChangeOverTimeTest.class.getName() + "error", "Error in testing the change over time: " + e.getLocalizedMessage());
//            System.out.println("Error in testing the change over time: " + e.getLocalizedMessage());
//            return null;
//        }
//
//    }

    /**
     * Method to return upper and lower boundary values of the threshold of a
     * given data measurement
     *
     * @param rdd data to be tested as part of a RecordingDeviceData object
     * @return List of threshold boundary values
     * @throws Exception
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
            int period = getMeasurementPeriod(at.getThresholdLowValue());
            double threshold = getMeasurementChangeThreshold(at.getThresholdHighValue());
            List<Double> thresholdList = new ArrayList<>();
            Double historicValue = getHistoricValue(attributeId, patientUuid, dataValueTime, period);
            if (historicValue == null) {
                thresholdList.add(getDataValue(dataValue));
                thresholdList.add(getDataValue(dataValue));
            } else {
                thresholdList.add(historicValue - threshold);
                thresholdList.add(historicValue + threshold);
            }
            return thresholdList;
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
    public String getFailedTestText() {
        String response = failedTestText
                .replace(DATA_VALUE, String.valueOf(currentValue))
                .replace(ATTRIBUTE_UNITS, attributeUnits)
                .replace(MEASUREMENT_PERIOD, String.valueOf(measurementPeriod))
                .replace(MEASUREMENT_CHANGE_THRESHOLD, String.valueOf(measurementChangeThreshold));
        return response;
    }

    /**
     * Method to return a descriptive string taken from the properties file and
     * substituted with values from the measurement data describing a
     * non-failure condition
     *
     * @return descriptive string of the alert
     */
    @Override
    public String getPassedTestText() {
        String response = passedTestText
                .replace(DATA_VALUE, String.valueOf(currentValue))
                .replace(ATTRIBUTE_UNITS, attributeUnits)
                .replace(MEASUREMENT_PERIOD, String.valueOf(measurementPeriod))
                .replace(MEASUREMENT_CHANGE_THRESHOLD, String.valueOf(measurementChangeThreshold));
        return response;
    }

    /**
     * Method to return a descriptive string taken from the properties file and
     * substituted with values from the measurement data describing a cant
     * calculate condition
     *
     * @return descriptive string of the alert
     */
    @Override
    public String getCantCalculateTestText() {
        String response = cantCalculateTestText
                .replace(DATA_VALUE, String.valueOf(currentValue))
                .replace(ATTRIBUTE_UNITS, attributeUnits)
                .replace(MEASUREMENT_PERIOD, String.valueOf(measurementPeriod))
                .replace(MEASUREMENT_CHANGE_THRESHOLD, String.valueOf(measurementChangeThreshold));
        return response;
    }

}
