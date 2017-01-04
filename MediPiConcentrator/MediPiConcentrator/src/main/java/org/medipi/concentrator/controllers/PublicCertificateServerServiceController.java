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
import org.medipi.concentrator.logging.MediPiLogger;
import org.medipi.concentrator.services.PublicCertificateService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controller for the Public certificate server. This service is used to request
 * the PEM certificates for systems wishing to send encrypted messages to
 * patients through the MediPi concentrator
 *
 *
 * @author rick@robinsonhq.com
 */
@RestController
@RequestMapping("MediPiConcentrator/webresources/certificate")
public class PublicCertificateServerServiceController {

    @Autowired
    private PublicCertificateService publicCertificateService;

    @Autowired
    private MediPiLogger logger;

    /**
     * Controller for the public certificate service
     *
     * @param patientUuid patientUuid of the requested certificate
     * @return Response to the request
     */
    @RequestMapping(value = "/patient/{patientUuid}", method = RequestMethod.GET)
    @ResponseBody
    public ResponseEntity<byte[]> getPublicCertificate(@PathVariable("patientUuid") String patientUuid) {
        logger.log(PublicCertificateServerServiceController.class.getName(), new Date().toString() + " get Patient Public Certificate for patientUuid: " + patientUuid);
        return this.publicCertificateService.getCertificate(patientUuid);
    }

}
