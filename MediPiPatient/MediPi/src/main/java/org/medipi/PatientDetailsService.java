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
package org.medipi;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.time.Instant;
import org.medipi.logging.MediPiLogger;

/**
 * A service allowing access to the information held in the patient details json
 * file and allowing updated data to be saved
 *
 * @author rick@robinsonhq.com
 */
public class PatientDetailsService {

    private static PatientDetailsService me = null;
    private static final String PATIENTDETAILSLOCATION = "medipi.patientdetails.location";
    private PatientDetailsDO patientDetails = null;
    private File patientDetailsFile = null;
    private final MediPiLogger logger = MediPiLogger.getInstance();

    /**
     * Constructor
     * @throws java.lang.Exception
     */
    public PatientDetailsService() throws Exception {
        String patientDetailsDirectory = MediPiProperties.getInstance().getProperties().getProperty(PATIENTDETAILSLOCATION);
        if (patientDetailsDirectory == null || patientDetailsDirectory.trim().length() == 0) {
            throw new Exception("Patient details Directory parameter not configured");
        }
        String error = "";
        if ((error = loadPatientDetails(patientDetailsDirectory)) != null) {
            throw new Exception(error);
        }
    }

    /**
     * @return the singleton instance of this class.
     * @throws java.lang.Exception
     */
    public static synchronized PatientDetailsService getInstance() throws Exception {
        if (me == null) {
            me = new PatientDetailsService();
        }
        return me;
    }

    private String loadPatientDetails(String patientDetailsDir) {
        try {
            patientDetailsFile = new File(patientDetailsDir);
            ObjectMapper mapper = new ObjectMapper();
            patientDetails = mapper.readValue(patientDetailsFile, new TypeReference<PatientDetailsDO>() {
            });
        } catch (IOException ex) {
            return "Cannot read the Patient Details file: " + patientDetailsDir + ex.getLocalizedMessage();
        }
        return null;
    }

    /**
     * Method to allow updated patient data objects to be saved to json file
     * @param pddo patient data object to be saved
     * @return String representation of any error
     */
    public String savePatientDetails(PatientDetailsDO pddo) {
        FileOutputStream fop = null;
        try {
            fop = new FileOutputStream(patientDetailsFile);
            // if file doesnt exists, then create it
            if (!patientDetailsFile.exists()) {
                String error = "Cannot find Patient Details file";
                logger.log(PatientDetailsService.class.getName() + ".error", error);
                return error;
            }
            ObjectMapper mapper = new ObjectMapper();
            mapper.writeValue(fop, pddo);
            fop.flush();
            fop.close();

            logger.log(PatientDetailsService.class.getName(), Instant.now().toString() + " Change to persisted Patient Details file");
        } catch (IOException e) {
            String error = "Cannot save Patient Details change to local drive - check the configured directory: " + patientDetailsFile;
            logger.log(PatientDetailsService.class.getName() + ".error", error);
            return error;
        } finally {
            try {
                if (fop != null) {
                    fop.close();
                }
            } catch (IOException e) {
                String error = "Cannot save Patient Details change to local drive - check the configured directory: " + patientDetailsFile;
                logger.log(PatientDetailsService.class.getName() + ".error", error);
                return error;
            }
        }
        return null;

    }

    /**
     * Accessor method for patient data object
     * @return
     */
    public PatientDetailsDO getPatientDetails() {
        return patientDetails;
    }

}
