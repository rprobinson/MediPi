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

import java.security.NoSuchAlgorithmException;
import static org.springframework.hateoas.mvc.ControllerLinkBuilder.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import ma.glasnost.orika.MapperFacade;
import org.medipi.concentrator.controllers.DownloadServiceController;
import org.medipi.concentrator.dao.AllHardwareDownloadableDAOImpl;
import org.medipi.concentrator.dao.HardwareDownloadableDAOImpl;
import org.medipi.concentrator.dao.PatientDownloadableDAOImpl;
import org.medipi.security.CertificateDefinitions;
import org.medipi.security.UploadEncryptionAdapter;
import org.medipi.concentrator.entities.AllHardwareDownloadable;
import org.medipi.concentrator.entities.HardwareDownloadable;
import org.medipi.concentrator.entities.PatientDownloadable;
import org.medipi.concentrator.exception.InternalServerError500Exception;
import org.medipi.concentrator.exception.NotFound404Exception;
import org.medipi.concentrator.logging.MediPiLogger;
import org.medipi.concentrator.model.DownloadableDO;
import org.medipi.concentrator.utilities.Utilities;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service class to return a list of downloadable data objects for the given
 * patient.
 *
 * 1st assess that the patient device and patient user are related in the DB
 * Then return the list of downloadable Data Objects for the patient. The
 * patient downloadables are currently signed by the clinical system and passed
 * through the concentrator. The hardware downloadables are signed in this class
 * The downloadable list employs a HATEOAS link for the MediPi Patient device to
 * subsequently download
 *
 * The "all hardware" updates are global updates intended for all the MediPi
 * Patient devices connected to the concentrator
 *
 * @author rick@robinsonhq.com
 */
@Service
public class DownloadableListService {

    @Autowired
    private MediPiLogger logger;

    @Autowired
    private Utilities utils;

    @Autowired
    private PatientDownloadableDAOImpl patientDownloadableDAOImpl;

    @Autowired
    private HardwareDownloadableDAOImpl hardwareDownloadableDAOImpl;

    @Autowired
    private AllHardwareDownloadableDAOImpl allHardwareDownloadableDAOImpl;

    @Autowired
    private PatientDeviceValidationService patientDeviceValidationService;

    @Autowired
    private MapperFacade mapperFacade;

    /**
     * Get Download method
     *
     * @param hardware_name incoming deviceId parameter from RESTful message
     * @param patientUuid incoming patientUuid parameter from RESTful message
     * @return Downloadable list Response
     */
    @Transactional(rollbackFor = RuntimeException.class)
    public ResponseEntity<List<DownloadableDO>> getDownloadableList(String hardware_name, String patientUuid) {
        ResponseEntity<?> r = null;
        try {
            // Check that the device and patient are registered with each other
            r = this.patientDeviceValidationService.validate(hardware_name, patientUuid);
        } catch (Exception e) {
            throw new NotFound404Exception("Hardware and/or patient not registered" + e.getLocalizedMessage());
        }
        // OK device and patient are now verified as being registered with each other
        try {
            if (r != null) {
                if (r.getStatusCode() == HttpStatus.ACCEPTED || r.getStatusCode() == HttpStatus.OK) {
                    // get patient_downloadable entities first
                    List<DownloadableDO> dList = new ArrayList<>();

                    // CHECK FOR PATIENT DOWNLOADS
                    List<PatientDownloadable> pdList = patientDownloadableDAOImpl.getPatientDownloads(patientUuid);
                    // If there are no entries, an empty list is returned
                    if (!pdList.isEmpty()) {
                        logger.log(DownloadableListService.class.getName(), new Date().toString() + " Download List returned to patientUuid: " + patientUuid + " using deviceId: " + hardware_name);
                        for (PatientDownloadable pd : pdList) {
                            DownloadableDO d = this.mapperFacade.map(pd, DownloadableDO.class);
                            d.setDownloadType("PATIENTMESSAGE");
                            // Add HATEOAS return path for getting the data from each reference  
                            d.add(linkTo(methodOn(DownloadServiceController.class).getPatientDownloadable(pd.getDownloadableUuid())).withRel("next"));
                            dList.add(d);
                        }
                    }

                    // CHECK FOR HARDWARE DOWNLOADS
                    List<HardwareDownloadable> hdList = hardwareDownloadableDAOImpl.getHardwareDownloads(hardware_name);
                    // If there are no entries, an empty list is returned
                    if (!hdList.isEmpty()) {
                        logger.log(DownloadableListService.class.getName(), new Date().toString() + " Download List returned to hardwareName: " + patientUuid + " using deviceId: " + hardware_name);
                        for (HardwareDownloadable hd : hdList) {
                            DownloadableDO d = this.mapperFacade.map(hd, DownloadableDO.class);
                            d.setDownloadType("HARDWAREUPDATE");
                            // Add HATEOAS return path for getting the data from each reference  
                            d.add(linkTo(methodOn(DownloadServiceController.class).getHardwareDownloadable(hd.getDownloadableUuid())).withRel("next"));

                            d.setSignature(createSignature(d));
                            dList.add(d);
                        }
                    }

                    // CHECK FOR ALL HARDWARE DOWNLOADS
                    List<AllHardwareDownloadable> ahdList = allHardwareDownloadableDAOImpl.getHardwareDownloads(hardware_name);
                    // If there are no entries, an empty list is returned
                    if (!ahdList.isEmpty()) {
                        logger.log(DownloadableListService.class.getName(), new Date().toString() + " Download List returned to hardwareName: " + patientUuid + " using deviceId: " + hardware_name);
                        for (AllHardwareDownloadable ahd : ahdList) {
                            DownloadableDO d = this.mapperFacade.map(ahd, DownloadableDO.class);
                            d.setDownloadType("HARDWAREUPDATE");
                            // Add HATEOAS return path for getting the data from each reference  
                            d.add(linkTo(methodOn(DownloadServiceController.class).getAllHardwareDownloadable(ahd.getDownloadableUuid(), hardware_name)).withRel("next"));
                            d.setSignature(createSignature(d));
                            dList.add(d);
                        }
                    }
                    
                    return new ResponseEntity<>(dList, HttpStatus.OK);
                }

            }
        } catch (Exception e) {
            logger.log(DownloadableListService.class.getName(), new Date().toString() + " Error when trying to download List returned to hardwareName: " + patientUuid + " using deviceId: " + hardware_name + " Error: " + e.getMessage());
            throw new InternalServerError500Exception("Internal Server Error");
        }
        logger.log(DownloadableListService.class.getName(), new Date().toString() + " Error when trying to download List returned to hardwareName: " + patientUuid + " using deviceId: " + hardware_name);
        throw new InternalServerError500Exception("Internal Server Error");
    }

    private String createSignature(DownloadableDO d) throws NoSuchAlgorithmException, Exception {
        // THIS MAY NOT BE A PERMANENT SOLUTION FOR THE HARDWARE DOWNLOADABLES and may be done from a UI 
        UploadEncryptionAdapter uploadEncryptionAdapter = new UploadEncryptionAdapter();
        CertificateDefinitions cd;
        cd = fetchHardwareSigningCerts();
        String error = uploadEncryptionAdapter.init(cd, UploadEncryptionAdapter.SIGNMODE);
        if (error != null) {
            throw new Exception("Signing initailisation failed - " + error);
        }
        StringBuilder digestSubject = new StringBuilder();
        digestSubject.append(d.getDownloadType())
                .append(d.getDownloadableUuid())
                .append(d.getFileName())
                .append(d.getVersion())
                .append(d.getVersionAuthor())
                .append(d.getVersionDate().getTime());
        String signature = uploadEncryptionAdapter.signPayload(digestSubject.toString().getBytes());
        return signature;
    }

    private CertificateDefinitions fetchHardwareSigningCerts() throws Exception {
        CertificateDefinitions cd = new CertificateDefinitions(utils.getProperties());
        cd.setSIGNKEYSTORELOCATION("medipi.json.sign.keystore.hardware.location", CertificateDefinitions.INTERNAL);
        cd.setSIGNKEYSTOREALIAS("medipi.json.sign.keystore.hardware.alias", CertificateDefinitions.INTERNAL);
        cd.setSIGNKEYSTOREPASSWORD("medipi.json.sign.keystore.hardware.password", CertificateDefinitions.INTERNAL);
        return cd;
    }

}
