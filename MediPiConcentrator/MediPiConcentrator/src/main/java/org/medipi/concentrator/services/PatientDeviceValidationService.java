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
package org.medipi.concentrator.services;

import javax.persistence.TransactionRequiredException;
import org.medipi.concentrator.dao.HardwareDAOImpl;
import org.medipi.concentrator.entities.Patient;
import org.medipi.concentrator.entities.Hardware;
import org.medipi.concentrator.exception.BadRequest400Exception;
import org.medipi.concentrator.exception.InternalServerError500Exception;
import org.medipi.concentrator.exception.NotAcceptable406Exception;
import org.medipi.concentrator.logging.MediPiLogger;
import org.medipi.concentrator.utilities.Utilities;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service class to assess that the incoming request is for a valid device and
 * patient
 *
 * 1. checks that the incoming parameters are present exist in the database and
 * relate to each other. If not then a new patient may be created and registered
 * to the device being used
 *
 * @author rick@robinsonhq.com
 */
@Service
public class PatientDeviceValidationService {

    @Autowired
    private MediPiLogger logger;

    @Autowired
    private HardwareDAOImpl hardwareDAO;

    @Autowired
    private RegisterNewPatientService registerNewPatient;

    @Value("${medipi.concentrator.db.createpatientforunassociateddevices}")
    private boolean createpatientforunassociateddevices;
    
    /**
     * Validates that the hardware and patient uuids are related on the
     * Concentrator DB If hardware has no patient associated then if the
     * configuration allows a new one will be created and associated
     *
     * @param hardware_name incoming deviceId parameter from RESTful message
     * @param patientUuid incoming patientUuid parameter from RESTful message
     * @return Response from the message
     */
    @Transactional(rollbackFor = RuntimeException.class)
    public ResponseEntity<?> validate(String hardware_name, String patientUuid) {
        // Establish that deviceId and patientUuid are present
        boolean deviceAndPatientVerified = false;
        ResponseEntity<?> positiveResponse = new ResponseEntity<>("Data added to MediPi Concentrator", HttpStatus.OK);
        if (hardware_name == null || hardware_name.trim().length() == 0) {
            logger.log(PatientDeviceValidationService.class.getName() + ".dataValidationIssue", "deviceId is not populated");
            throw new BadRequest400Exception("deviceId is not populated");
        }
        if (patientUuid == null || patientUuid.trim().length() == 0) {
            logger.log(PatientDeviceValidationService.class.getName() + ".dataValidationIssue", "patientUuid is not populated");
            throw new BadRequest400Exception("patientUuid is not populated");
        }

        // Check that the device is on the DB - If not present will return null
        final Hardware hardware = this.hardwareDAO.findByPrimaryKey(hardware_name);
        if (hardware != null) {
            // Device has been found on DB
//Removed to Reduce Logs size            logger.log(PatientDeviceValidationService.class.getName() + ".dbinfo", "deviceId: " + hardware_name + " found in Hardware Table");
            //Check that there is a patient registered to the device
            if (hardware.getPatientUuid() == null || hardware.getPatientUuid().getPatientUuid().trim().length() == 0) {
                //No patient is associated with this device
                logger.log(PatientDeviceValidationService.class.getName() + ".dbIssue", "Device (deviceId: " + hardware_name + ") has been found in DB but no patient is associated with this device");
                //should a new patient be created and associated with this device?
                if (createpatientforunassociateddevices) {
                    //REGISTER NEW PATIENT - this should prob have its own method/class

                    try {
                        // Using the incoming patientUuid create the new patient on DB (no NHS Number is associated)
                        Patient p = new Patient(patientUuid);

                        ResponseEntity<Patient> r = registerNewPatient.registerNewPatient(p);
                        if (r.getStatusCode() == HttpStatus.CREATED) {
                            positiveResponse = new ResponseEntity("New patient record created, Data added to MediPi Concentrator", HttpStatus.ACCEPTED);
                        }
                        // associate patient with the device
                        hardware.setPatientUuid(p);
                        // write hardware device Id to Hardware table in DB
                        this.hardwareDAO.update(hardware);
                        deviceAndPatientVerified = true;
                    } catch (IllegalArgumentException | TransactionRequiredException e) {
                        //Rollback if there is a failure
                        logger.log(PatientDeviceValidationService.class.getName() + ".dbIssue", "Attempt to create a new patientUuid: " + patientUuid + " in Patient table or attempt to update Hardware table with patientUuid failed");
                        throw new InternalServerError500Exception("Attempt to create new patientUuid failed");
                    }

                } else {
                    // if a new patient is NOT to be created respond with the approriate error
                    logger.log(PatientDeviceValidationService.class.getName() + ".dbIssue", "No patientUuid is associated with the device:" + hardware_name + " but the configuration does not allow for a new patient to be created and registered");
                    throw new NotAcceptable406Exception("server configuration will not allow a new patientUuid to be registered automatically with this device");

                }
            } else if (patientUuid.toLowerCase().trim().equals(hardware.getPatientUuid().getPatientUuid().toLowerCase())) {
                //Patient has been registered with the device
//Removed to Reduce Logs size               logger.log(PatientDeviceValidationService.class.getName() + ".dbinfo", "patientUuid: " + patientUuid + "found in Patient Table and matched to deviceId: " + hardware_name);
                deviceAndPatientVerified = true;
            } else {
                //if the device is registered to another patient
                logger.log(PatientDeviceValidationService.class.getName() + ".dbIssue", "deviceId: " + hardware_name + "is registered to " + hardware.getPatientUuid().getPatientUuid() + " and not to " + patientUuid);
                throw new BadRequest400Exception("The deviceId: " + hardware_name + " is not registered with the patientUuid: " + patientUuid);
            }
        } else {
            // Device does NOT exist in the database
            logger.log(PatientDeviceValidationService.class.getName() + ".dbInfo", "deviceId: " + hardware_name + "is not present on the DB");
            throw new NotAcceptable406Exception("DeviceId: " + hardware_name + " is not registered with this concentrator");
        }
        // OK device and patient are now veried as being registered with each other
        if (deviceAndPatientVerified) {
            return positiveResponse;
        }
        return new ResponseEntity(HttpStatus.INTERNAL_SERVER_ERROR);
    }

}
