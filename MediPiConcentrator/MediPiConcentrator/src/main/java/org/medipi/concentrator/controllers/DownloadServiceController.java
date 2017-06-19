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
package org.medipi.concentrator.controllers;

import java.util.Date;
import java.util.List;
import org.medipi.concentrator.logging.MediPiLogger;
import org.medipi.concentrator.model.DownloadableDO;
import org.medipi.concentrator.services.DownloadableListService;
import org.medipi.concentrator.services.HardwareDownloadableService;
import org.medipi.concentrator.services.PatientDownloadableService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

/**
 * Class for controlling the download service for the MediPi patient units .
 * This exposes interfaces for: Listing all available downloads for a patient
 * device, Downloading AllHardware, Hardware and Patient Messaging
 *
 * @author rick@robinsonhq.com
 */
@RestController
@RequestMapping("MediPiConcentrator/webresources/download")
public class DownloadServiceController {

    @Autowired
    private DownloadableListService downloadableListService;

    @Autowired
    private PatientDownloadableService patientDownloadableService;

    @Autowired
    private HardwareDownloadableService hardwareDownloadableService;

    @Autowired
    private MediPiLogger logger;

    /**
     * Controller for downloading a list of available updates to a patient
     * device. This method passes the incoming message to the service layer for
     * processing
     *
     * @param hardwareName incoming deviceId parameter from RESTful message
     * @param patientUuid incoming patientUuid parameter from RESTful message
     * @return Response to the request
     */
    @RequestMapping(value = "/{hardwareName}/{patientUuid}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public ResponseEntity<List<DownloadableDO>> getDownloadableList(@PathVariable("hardwareName") String hardwareName, @PathVariable("patientUuid") String patientUuid) {
//Removed to Reduce Logs size        logger.log(DownloadServiceController.class.getName(), new Date().toString() + " get DownloadableList called by patientUuid: " + patientUuid + " using hardwareName: " + hardwareName);
        return this.downloadableListService.getDownloadableList(hardwareName, patientUuid);
    }

    /**
     * Controller for downloading a patient message to a patient device. This
     * method passes the incoming request to the service layer for processing
     *
     * @param downloadableUuid downloadable UUID of the patient downloadable
     * item
     * @return Response to the request
     */
    @RequestMapping(value = "/patient/{downloadableUuid}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public ResponseEntity<FileSystemResource> getPatientDownloadable(@PathVariable("downloadableUuid") String downloadableUuid) {
        logger.log(DownloadServiceController.class.getName(), new Date().toString() + " get PatientDownloadable for downloadableUuid: " + downloadableUuid);
        return this.patientDownloadableService.getDownload(downloadableUuid);
    }

    /**
     * Controller for downloading a hardware update script to a patient device.
     * This method passes the incoming request to the service layer for
     * processing
     *
     * @param downloadableUuid downloadable UUID of the hardware downloadable
     * item
     * @return Response to the request
     */
    @RequestMapping(value = "/hardware/{downloadableUuid}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public ResponseEntity<FileSystemResource> getHardwareDownloadable(@PathVariable("downloadableUuid") String downloadableUuid) {
        logger.log(DownloadServiceController.class.getName(), new Date().toString() + " get HardwareDownloadable for downloadableUuid: " + downloadableUuid);
        return this.hardwareDownloadableService.getDownload(downloadableUuid);
    }

    /**
     * Controller for downloading a hardware update script (which is available
     * to all patient devices) to a patient device. This method passes the
     * incoming request to the service layer for processing
     *
     * @param downloadableUuid downloadable UUID of the all hardware
     * downloadable item
     * @param hardwareName hardware name uuid of requesting system
     * @return Response to the request
     */
    @RequestMapping(value = "/hardware/{downloadableUuid}/{hardwareName}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public ResponseEntity<FileSystemResource> getAllHardwareDownloadable(@PathVariable("downloadableUuid") String downloadableUuid, @PathVariable("hardwareName") String hardwareName) {
        logger.log(DownloadServiceController.class.getName(), new Date().toString() + " get AllHardwareDownloadable for downloadableUuid: " + downloadableUuid + " and hardwareName: " + hardwareName);
        return this.hardwareDownloadableService.getAllDownload(downloadableUuid, hardwareName);
    }

    /**
     * Controller for acknowledging that the patient message has been received
     * by the patient device. This method passes the incoming request to the
     * service layer for processing
     *
     * @param downloadableUuid downloadable UUID of the patient downloadable
     * item
     * @return Response to the request
     */
    @RequestMapping(value = "/patient/{downloadableUuid}", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public ResponseEntity<DownloadableDO> acknowledgePatientDownload(@PathVariable("downloadableUuid") String downloadableUuid) {
        logger.log(DownloadServiceController.class.getName(), new Date().toString() + " acknowledge download of patient DownloadableId: " + downloadableUuid);
        return this.patientDownloadableService.acknowledgeDownload(downloadableUuid);
    }

    /**
     * Controller for acknowledging that the hardware update script has been
     * received by the patient device. This method passes the incoming request
     * to the service layer for processing
     *
     * @param downloadableUuid downloadable UUID of the hardware downloadable
     * item
     * @return Response to the request
     */
    @RequestMapping(value = "/hardware/{downloadableUuid}", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public ResponseEntity<DownloadableDO> acknowledgeHardwareDownload(@PathVariable("downloadableUuid") String downloadableUuid) {
        logger.log(DownloadServiceController.class.getName(), new Date().toString() + " acknowledge download of hardware DownloadableId: " + downloadableUuid);
        return this.hardwareDownloadableService.acknowledgeDownload(downloadableUuid);
    }

    /**
     * Controller for acknowledging that the all hardware update script (which
     * is available to all patient devices)has been received by the patient
     * device. This method passes the incoming request to the service layer for
     * processing
     *
     * @param downloadableUuid downloadable UUID of the all hardware
     * downloadable item
     * @param hardwareName hardware name uuid of requesting system
     * @return Response to the request
     */
    @RequestMapping(value = "/hardware/{downloadableUuid}/{hardwareName}", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public ResponseEntity<DownloadableDO> acknowledgeAllHardwareDownload(@PathVariable("downloadableUuid") String downloadableUuid, @PathVariable("hardwareName") String hardwareName) {
        logger.log(DownloadServiceController.class.getName(), new Date().toString() + " acknowledge download of hardware DownloadableId: " + downloadableUuid + " and hardwareName: " + hardwareName);
        return this.hardwareDownloadableService.acknowledgeAllDownload(downloadableUuid, hardwareName);
    }
}
