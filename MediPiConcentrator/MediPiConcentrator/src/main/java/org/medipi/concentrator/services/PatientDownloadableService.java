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

import java.io.File;
import java.util.Date;
import ma.glasnost.orika.MapperFacade;
import org.medipi.concentrator.dao.PatientDownloadableDAOImpl;
import org.medipi.concentrator.entities.PatientDownloadable;
import org.medipi.concentrator.exception.InternalServerError500Exception;
import org.medipi.concentrator.exception.NotFound404Exception;
import org.medipi.concentrator.logging.MediPiLogger;
import org.medipi.concentrator.model.DownloadableDO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.FileSystemResource;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service class to expose interfaces for the HATEOAS links sent by the
 * DownloadableListService responses.
 *
 *
 * The methods allow downloading of the files defined in the
 * DownloadableListService response and the acknowledgement of their receipt by
 * the MediPi Patient Device.
 *
 * @author rick@robinsonhq.com
 */
@Service
public class PatientDownloadableService {

    @Autowired
    private MediPiLogger logger;

    @Autowired
    private PatientDownloadableDAOImpl patientDownloadableDAOImpl;

    @Autowired
    private MapperFacade mapperFacade;

    /**
     * Method to enable download of hardware update file from Concentrator
     *
     * @param downloadable_uuid of the download file item
     * @return File System Resource Response
     */

    @Transactional(rollbackFor = RuntimeException.class)
    public ResponseEntity<FileSystemResource> getDownload(String downloadable_uuid) {
        PatientDownloadable pd = null;
        try {
            pd = patientDownloadableDAOImpl.getPatientDownload(downloadable_uuid);
        } catch (EmptyResultDataAccessException e) {
            throw new NotFound404Exception("Cannot find the requested Patient message downloadable record UUID: " + downloadable_uuid);
        } catch (Exception e) {
            throw new InternalServerError500Exception("Internal Server Error");
        }
        try {

            String fileName = pd.getScriptLocation();
            if (fileName == null || fileName.isEmpty()) {
                throw new NotFound404Exception("Cannot find the downloadable resource which has been requested");
            }
            File f = new File(fileName);
            FileSystemResource fsr = new FileSystemResource(fileName);

            HttpHeaders header = new HttpHeaders();
            header.setContentType(MediaType.APPLICATION_OCTET_STREAM);
            header.set("Content-Disposition",
                    "attachment; filename=" + fileName.replace(" ", "_"));
            header.setContentLength(fsr.contentLength());

            logger.log(PatientDownloadableService.class.getName(), new Date().toString() + " Patient Downloadable item: " + downloadable_uuid + " downloaded");
            ResponseEntity<FileSystemResource> response = new ResponseEntity<>(fsr, header, HttpStatus.OK);
            return response;
        } catch (Exception e) {
            throw new NotFound404Exception("Cannot find the resource quested download");
        }
    }
    /**
     * Method to enable acknowledgement of the patient update files from
     * Concentrator
     *
     * @param downloadable_uuid of the download file item
     * @return updated DownloadableDO Response
     */
    @Transactional(rollbackFor = RuntimeException.class)
    public ResponseEntity<DownloadableDO> acknowledgeDownload(String downloadable_uuid) {
        PatientDownloadable pd = null;
        try {
            pd = patientDownloadableDAOImpl.getPatientDownload(downloadable_uuid);
        } catch (EmptyResultDataAccessException e) {
            throw new NotFound404Exception("Cannot find the requested downloadable record");
        } catch (Exception e) {
            throw new InternalServerError500Exception("Internal Server Error "+ e.getLocalizedMessage());
        }
        try {
            if (pd.getDownloadedDate() != null) {
                throw new InternalServerError500Exception("Internal Server Error");
            }
            pd.setDownloadedDate(new Date());
            patientDownloadableDAOImpl.update(pd);
            DownloadableDO d = this.mapperFacade.map(pd, DownloadableDO.class);

            logger.log(PatientDownloadableService.class.getName(), new Date().toString() + " Patient Downloadable item: " + downloadable_uuid + " acknowledged");
            return new ResponseEntity<>(d, HttpStatus.OK);
        } catch (Exception e) {
            throw new InternalServerError500Exception("Internal Server Error");
        }
    }

}
