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

import org.medipi.concentrator.dao.PatientDAOImpl;
import org.medipi.concentrator.dao.HardwareDAOImpl;
import org.medipi.concentrator.entities.Patient;
import org.medipi.concentrator.entities.Hardware;
import org.medipi.concentrator.exception.NotAcceptable406Exception;
import org.medipi.concentrator.logging.MediPiLogger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service class to Register a new patient
 *
 * The database is checked for the patient existing. If it is then check that it
 * is unassociated with any other device before registering it with the device
 * in the request
 *
 * @author rick@robinsonhq.com
 */
@Service
public class RegisterNewPatientService {

    @Autowired
    private MediPiLogger logger;

    @Autowired
    private HardwareDAOImpl hardwareDAO;

    @Autowired
    private PatientDAOImpl patientDAO;

    @Transactional(rollbackFor = RuntimeException.class)
    public ResponseEntity<Patient> registerNewPatient(Patient p) {

        Patient patient = this.patientDAO.findByPrimaryKey(p.getPatientUuid());
        if (patient == null) {
            this.patientDAO.save(p);
            //return a status of 202 when data is persisted as a new patient has been created
            return new ResponseEntity<>(p, HttpStatus.CREATED);
        } else {
            // Patient already in DB - but need to check if it's associated with another Hardware device
            logger.log(PatientUploadService.class.getName() + ".dbInfo", "Attempt to create a new patientUuid: " + p.getPatientUuid() + " in Patient table or but failed as patient already exists");
            Hardware otherHardware;
            try {
                 otherHardware = this.hardwareDAO.findByPatientUuid(p.getPatientUuid());
            } catch (EmptyResultDataAccessException erdae) {
                // as there is no harware device associated with this patient Id - free to continue
                return new ResponseEntity<>(patient, HttpStatus.OK);
            }
            // if there is a result then the patientUuid is already associated with another deviceID
            logger.log(PatientUploadService.class.getName() + ".dbIssue", "Attempt to register a patientUuid: " + p.getPatientUuid() + " but this patientUuid is already associated with deviceId: " + otherHardware.getHardwareName());
            throw new NotAcceptable406Exception("patientUuid: " + p.getPatientUuid() + "is already associated with another hardware device");

        }

    }
}
