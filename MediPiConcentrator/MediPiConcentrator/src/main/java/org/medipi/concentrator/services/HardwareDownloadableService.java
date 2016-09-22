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
import java.io.IOException;
import java.util.Date;
import ma.glasnost.orika.MapperFacade;
import org.medipi.concentrator.dao.AllHardwareDownloadableDAOImpl;
import org.medipi.concentrator.dao.AllHardwareDownloadedDAOImpl;
import org.medipi.concentrator.dao.HardwareDAOImpl;
import org.medipi.concentrator.dao.HardwareDownloadableDAOImpl;
import org.medipi.concentrator.entities.AllHardwareDownloadable;
import org.medipi.concentrator.entities.AllHardwareDownloaded;
import org.medipi.concentrator.entities.HardwareDownloadable;
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
 * The "all hardware" updates are global updates intended for all the MediPi
 * Patient devices connected to the concentrator
 *
 * @author rick@robinsonhq.com
 */
@Service
public class HardwareDownloadableService {

    @Autowired
    private MediPiLogger logger;

    @Autowired
    private HardwareDownloadableDAOImpl hardwareDownloadableDAOImpl;

    @Autowired
    private AllHardwareDownloadableDAOImpl allHardwareDownloadableDAOImpl;

    @Autowired
    private AllHardwareDownloadedDAOImpl allHardwareDownloadedDAOImpl;

    @Autowired
    private HardwareDAOImpl hardwareDAOImpl;

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
        String fileName = null;
        try {
            HardwareDownloadable hd = hardwareDownloadableDAOImpl.getHardwareDownload(downloadable_uuid);
            fileName = hd.getScriptLocation();
        } catch (EmptyResultDataAccessException e) {
            throw new NotFound404Exception("Cannot find the requested hardware downloadable record UUID: " + downloadable_uuid);
        } catch (Exception e) {
            throw new InternalServerError500Exception("Internal Server Error");
        }
        try {

            ResponseEntity<FileSystemResource> response = retreiveDownloadableBinary(fileName, downloadable_uuid);
            return response;
        } catch (Exception e) {
            throw new NotFound404Exception("Cannot find the resource quested download");
        }
    }

    /**
     * Method to enable download of "all hardware" global update files from
     * Concentrator
     *
     * @param downloadable_uuid of the download file item
     * @param hardwareName
     * @return File System Resource Response
     */
    @Transactional(rollbackFor = RuntimeException.class)
    public ResponseEntity<FileSystemResource> getAllDownload(String downloadable_uuid, String hardwareName) {
        String fileName = null;
        try {
            AllHardwareDownloadable ahd = allHardwareDownloadableDAOImpl.getHardwareDownload(downloadable_uuid);
            fileName = ahd.getScriptLocation();
        } catch (EmptyResultDataAccessException e) {
            throw new NotFound404Exception("Cannot find the requested all hardware downloadable record UUID: "+downloadable_uuid);
        } catch (Exception e) {
            throw new InternalServerError500Exception("Internal Server Error " + e.getLocalizedMessage());
        }
        try {

            ResponseEntity<FileSystemResource> response = retreiveDownloadableBinary(fileName, downloadable_uuid);
            return response;
        } catch (IOException | NotFound404Exception e) {
            throw new NotFound404Exception("Cannot find the requested download " + e.getLocalizedMessage());
        }
    }

    private ResponseEntity<FileSystemResource> retreiveDownloadableBinary(String fileName, String downloadable_uuid) throws IOException, NotFound404Exception {
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
        logger.log(HardwareDownloadableService.class.getName(), new Date().toString() + " Hardware Downloadable item: " + downloadable_uuid + " downloaded");
        ResponseEntity<FileSystemResource> response = new ResponseEntity<>(fsr, header, HttpStatus.OK);
        return response;
    }

    /**
     * Method to enable acknowledgement of the hardware update files from
     * Concentrator
     *
     * @param downloadable_uuid of the download file item
     * @return updated DownloadableDO Response
     */
    @Transactional(rollbackFor = RuntimeException.class)
    public ResponseEntity<DownloadableDO> acknowledgeDownload(String downloadable_uuid) {
        HardwareDownloadable hd = null;
        try {
            //retreive the hardwareDownloadable element to update
            hd = hardwareDownloadableDAOImpl.getHardwareDownload(downloadable_uuid);
        } catch (EmptyResultDataAccessException e) {
            throw new NotFound404Exception("Cannot find the requested downloadable record");
        } catch (Exception e) {
            throw new InternalServerError500Exception("Internal Server Error "+ e.getLocalizedMessage());
        }
        if (hd != null) {
            try {
                if (hd.getDownloadedDate() != null) {
                    throw new InternalServerError500Exception("Internal Server Error");
                }
                hd.setDownloadedDate(new Date());
                hardwareDownloadableDAOImpl.update(hd);
                DownloadableDO d = this.mapperFacade.map(hd, DownloadableDO.class);

                logger.log(HardwareDownloadableService.class.getName(), new Date().toString() + " Patient Downloadable item: " + downloadable_uuid + " acknowledged");
                return new ResponseEntity<>(d, HttpStatus.OK);
            } catch (Exception e) {
                throw new InternalServerError500Exception("Internal Server Error");
            }
        } else {
            throw new InternalServerError500Exception("Internal Server Error");
        }
    }

    /**
     * Method to enable acknowledgement of the all hardware global update files
     * from Concentrator
     *
     * @param downloadable_uuid of the download file item
     * @param hardwareName
     * @return updated DownloadableDO Response
     */
    @Transactional(rollbackFor = RuntimeException.class)
    public ResponseEntity<DownloadableDO> acknowledgeAllDownload(String downloadable_uuid, String hardwareName) {
        try {
            // check to see that the download hasnt already beend acked
            AllHardwareDownloaded ahdd = allHardwareDownloadedDAOImpl.hasBeenDownloaded(downloadable_uuid, hardwareName);
            if (ahdd != null) {
                // The download has an entry already so must have been acked previously
                throw new InternalServerError500Exception("Internal Server Error");
            }
        } catch (EmptyResultDataAccessException e) {
            // Expected outcome 
        } catch (Exception e) {
            throw new InternalServerError500Exception("Internal Server Error"+ e.getLocalizedMessage());
        }
        try {
            AllHardwareDownloaded ahd = new AllHardwareDownloaded();
            AllHardwareDownloadable ahde = allHardwareDownloadableDAOImpl.findByPrimaryKey(downloadable_uuid);
            ahd.setDownloadableUuid(ahde);
            ahd.setHardwareName(hardwareDAOImpl.findByPrimaryKey(hardwareName));
            ahd.setDownloadedDate(new Date());
            allHardwareDownloadedDAOImpl.save(ahd);
            DownloadableDO d = this.mapperFacade.map(ahde, DownloadableDO.class);
            d.setDownloadedDate(ahd.getDownloadedDate());
            logger.log(HardwareDownloadableService.class.getName(), new Date().toString() + " Patient Downloadable item: " + downloadable_uuid + " acknowledged");
            return new ResponseEntity<>(d, HttpStatus.OK);
        } catch (Exception e) {
            throw new InternalServerError500Exception("Internal Server Error" + e.getLocalizedMessage());
        }

    }

}
