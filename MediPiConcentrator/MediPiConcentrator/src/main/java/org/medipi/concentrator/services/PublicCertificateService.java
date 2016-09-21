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

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Date;
import org.medipi.concentrator.dao.PatientCertificateDAOImpl;
import org.medipi.concentrator.entities.PatientCertificate;
import org.medipi.concentrator.exception.InternalServerError500Exception;
import org.medipi.concentrator.exception.NotFound404Exception;
import org.medipi.concentrator.logging.MediPiLogger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service class to allow an interface for 3rd party systems to request public
 * certificates for patients on the concentrator
 *
 * @author rick@robinsonhq.com
 */
@Service
public class PublicCertificateService {

    @Autowired
    private MediPiLogger logger;

    @Autowired
    private PatientCertificateDAOImpl patientCertificateDAOImpl;

    /**
     * interface for a PEM certificate server
     *
     * @param patientUuid
     * @return byte[] format PEM certificate for patient
     */
    @Transactional(rollbackFor = RuntimeException.class)
    public ResponseEntity<byte[]> getCertificate(String patientUuid) {
        PatientCertificate patientCertificate = null;
        try {
            patientCertificate = patientCertificateDAOImpl.findByPatientUuid(patientUuid);
        } catch (EmptyResultDataAccessException e) {
            throw new NotFound404Exception("Cannot find the requested patient certificate");
        } catch (Exception e) {
            throw new InternalServerError500Exception("Internal Server Error " + e.getLocalizedMessage());
        }

        try {
            String fileName = patientCertificate.getCertificateLocation();
            if (fileName == null || fileName.isEmpty()) {
                throw new NotFound404Exception("Cannot find the certificate which has been requested");
            }
            Path p = null;
            try {
                p = Paths.get(fileName);
            } catch (InvalidPathException e) {
                logger.log(PublicCertificateService.class.getName() + ".error", new Date().toString() + " Cannot find the requested file at " + fileName);
                throw new InternalServerError500Exception("Internal Server Error " + e.getLocalizedMessage());
            }
            byte[] encoded = Files.readAllBytes(p);
            logger.log(PublicCertificateService.class.getName(), new Date().toString() + " Patient Certificate item: " + patientUuid + " downloaded");
            ResponseEntity<byte[]> response = new ResponseEntity<>(encoded, HttpStatus.OK);
            return response;
        } catch (NotFound404Exception | InternalServerError500Exception | IOException e) {
            throw new NotFound404Exception("Cannot find the resource requested download");
        }
    }
}
