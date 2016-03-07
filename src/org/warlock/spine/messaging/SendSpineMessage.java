/*
 Copyright 2016  Richard Robinson @ HSCIC <rrobinson@hscic.gov.uk, rrobinson@nhs.net>

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
package org.warlock.spine.messaging;

import java.io.File;
import java.util.ArrayList;
import java.util.Properties;
import org.warlock.itk.distributionenvelope.DistributionEnvelope;
import org.warlock.itk.distributionenvelope.DistributionEnvelopeHelper;
import org.warlock.spine.connection.ConnectionManager;
import org.warlock.spine.connection.SDSSpineEndpointResolver;
import org.warlock.spine.connection.SdsTransmissionDetails;
import org.warlock.spine.logging.MediPiLogger;
import org.warlock.spine.logging.SpineToolsLogger;
import org.medipi.MediPiProperties;

/**
 * A singleton class to configure SpineTools,start it as an MHS, send requests
 * to Spine and handle the responses. Also will handle retires and any other
 * exception functions
 *
 * @author RIRO
 */
public class SendSpineMessage {

    private static final String CACHEDIR = "tks.spinetools.sds.cachedir";
    private static final String MYASID = "tks.spinetools.sds.myasid";
    private static final String MYPARTYKEY = "tks.spinetools.sds.mypartykey";
    private static final String URLRESOLVERDIR = "tks.spinetools.sds.urlresolver";
    private static final String URL = "tks.spinetools.sds.url";
    private static final String RETRY = "tks.spinetools.messaging.retrytimerperiod";
    private static final String MYIP = "tks.spinetools.connection.myip";
    private static final String MESSAGESPOOLDIR = "tks.spinetools.messaging.messagedirectory";
    private static final String EXPIREDMESSAGEDIR = "tks.spinetools.messaging.expireddirectory";
    private static final String PERSISTDURATIONDIR = "tks.spinetools.messaging.persistdurations";
    private static final String EBXMLDIR = "tks.spinetools.messaging.defaultebxmlhandler.filesavedirectory";
    private static final String DEDIR = "tks.spinetools.messaging.defaultdistributionenvelopehandler.filesavedirectory";
    private static final String SYNCDIR = "tks.spinetools.messaging.defaultsynchronousresponsehandler.filesavedirectory";
    private static final String ASYNCWAIT = "tks.spinetools.messaging.asynchronouswaitperiod";
    private static final String CLEAR = "tkw.spine-test.cleartext";
    private static final String ASYNCLISTENPORT = "tks.SpineToolsTransport.asynclistenport";
    private static final String LOGDIR = "spineTools.log";
    private static final String SPINECERT = "tkw.http.spine.certs";
    private static final String SPINESSLCONTEXTPASS = "tkw.http.spine.sslcontextpass";
    private static final String SPINETRUST = "tkw.http.spine.trust";
    private static final String SPINETRUSTPASS = "tkw.http.spine.trustpass";
    private static final String PROXYHOST = "tks.spinetools.proxyhost";
    private static final String PROXYPORT = "tks.spinetools.proxyport";
    private static final String MAXWAIT = "tks.spinetools.syncresponsewait";
    private static final String PSISEVENTMAXWAIT = "tks.scrapi.event.syncresponsewait";
    private static final String PSISDOCUMENTMAXWAIT = "tks.scrapi.document.syncresponsewait";
    private static final String AUTHROLE = "tks.spinetools.messaging.authorrole";
    private static final String AUTHUID = "tks.spinetools.messaging.authoruid";
    private static final String AUTHURP = "tks.spinetools.messaging.authorurp";
    private static final String PAYLOADATTACH = "tks.spinetools.messaging.payloadasattachment";
    private static final String SVCIA = "tks.spinetools.transmit.svcia";
    private static final String ODS = "tks.spinetools.transmit.ods";
    private static final String ASID = "tks.spinetools.transmit.asid";
    private static final String PARTYKEY = "tks.spinetools.transmit.partykey";
    private static final String AUDIT_ID_PROPERTY = "tks.spinetools.desender.auditidentity";
    private static final String SENDER_PROPERTY = "tks.spinetools.desender.senderaddress";
    private static final String CORRESPONDENCE_CLIENT = "tks.spinetools.correspondence.client";
    private static final String RECIPIENTADDRESS = "medipi.recipientaddress";
    private static final String OXIMETERPYTHON = "medipi.oximeter.python";
    private static final int DEFAULTLISTENPORT = 4848;

    private static final int DEFAULTMAXWAIT = 5000;
    private int maxwait = 0;

    private File logDirectory = null;

    private long asyncWait = 0;
    private int asyncPort = 0;
    private String authorRole = null;
    private String authorUid = null;
    private String authorUrp = null;
    private boolean payloadAttach = false;
    private SDSSpineEndpointResolver resolver = null;
    private ConnectionManager cm = null;
    private String action = null;
    private String ods = null;
    private String asid = null;
    private String partyKey = null;

    /**
     * Method to initialise the spineTools library and open an async listener
     * 
     * @throws Exception
     */
    public SendSpineMessage() throws Exception {

        try {
            Properties p = MediPiProperties.getInstance().getProperties();
            String l = p.getProperty(CORRESPONDENCE_CLIENT);
            if (l != null && l.toLowerCase().trim().startsWith("y")) {
                System.setProperty("org.warlock.spine.correspondence.client", l);
                l = p.getProperty(SENDER_PROPERTY);
                if ((l != null) && (l.trim().length() != 0)) {
                    System.setProperty("org.warlock.itk.router.senderaddress", l);
                } else {
                    throw new Exception("Distribution Envelope Sender Address not set for ack response");
                }
                l = p.getProperty(AUDIT_ID_PROPERTY);
                if ((l != null) && (l.trim().length() != 0)) {
                    System.setProperty("org.warlock.itk.router.auditidentity", l);
                } else {
                    throw new Exception("Distribution Envelope Audit ID not set for ack response");
                }
            }
            l = p.getProperty(OXIMETERPYTHON);
            if ((l != null) && (l.trim().length() != 0)) {
                System.setProperty("medipi.oximeter.python", l);
            }
            l = p.getProperty(RECIPIENTADDRESS);
            if ((l != null) && (l.trim().length() != 0)) {
                System.setProperty("medipi.recipientaddress", l);
            }
            l = p.getProperty(AUTHROLE);
            if ((l != null) && (l.trim().length() != 0)) {
                authorRole = l;
                System.setProperty("org.warlock.spine.messaging.authorrole", l);
            }
            l = p.getProperty(AUTHUID);
            if ((l != null) && (l.trim().length() != 0)) {
                authorUid = l;
                System.setProperty("org.warlock.spine.messaging.authoruid", l);
            }
            l = p.getProperty(AUTHURP);
            if ((l != null) && (l.trim().length() != 0)) {
                authorUrp = l;
                System.setProperty("org.warlock.spine.messaging.authorurp", l);
            }
            l = p.getProperty(PAYLOADATTACH);
            payloadAttach = (l != null) && (l.trim().toUpperCase().startsWith("Y"));
            l = p.getProperty(CLEAR);
            if ((l != null) && (l.trim().length() != 0)) {
                System.setProperty("tkw.spine-test.cleartext", l);
            }
            l = p.getProperty(SPINECERT);
            if ((l != null) && (l.trim().length() != 0)) {
                System.setProperty("org.warlock.http.spine.certs", l);
            }
            l = p.getProperty(SPINESSLCONTEXTPASS);
            if ((l != null) && (l.trim().length() != 0)) {
                System.setProperty("org.warlock.http.spine.sslcontextpass", l);
            }
            l = p.getProperty(SPINETRUST);
            if ((l != null) && (l.trim().length() != 0)) {
                System.setProperty("org.warlock.http.spine.trust", l);
            }
            l = p.getProperty(SPINETRUSTPASS);
            if ((l != null) && (l.trim().length() != 0)) {
                System.setProperty("org.warlock.http.spine.trustpass", l);
            }
            l = p.getProperty(CACHEDIR);
            if ((l != null) && (l.trim().length() != 0)) {
                System.setProperty("org.warlock.spine.sds.cachedir", l);
            } else {
                System.err.println("SDS cache directory not set");
            }
            l = p.getProperty(MYASID);
            if ((l != null) && (l.trim().length() != 0)) {
                System.setProperty("org.warlock.spine.sds.myasid", l);
            } else {
                System.err.println("SDS sending ASID  not set");
            }
            l = p.getProperty(MYPARTYKEY);
            if ((l != null) && (l.trim().length() != 0)) {
                System.setProperty("org.warlock.spine.sds.mypartykey", l);
            } else {
                System.err.println("SDS sending Party Key  not set");
            }
            l = p.getProperty(URLRESOLVERDIR);
            if ((l != null) && (l.trim().length() != 0)) {
                System.setProperty("org.warlock.spine.sds.urlresolver", l);
            }
            l = p.getProperty(URL);
            if ((l != null) && (l.trim().length() != 0)) {
                System.setProperty("org.warlock.spine.sds.url", l);
            } else {
                System.err.println("SDS URL  not set");
            }
            l = p.getProperty(RETRY);
            if ((l != null) && (l.trim().length() != 0)) {
                System.setProperty("org.warlock.spine.messaging.retrytimerperiod", l);
            }
            l = p.getProperty(MYIP);
            if ((l != null) && (l.trim().length() != 0)) {
                System.setProperty("org.warlock.spine.connection.myip", l);
            } else {
                System.err.println("Connection sending IP not set");
            }
            l = p.getProperty(MESSAGESPOOLDIR);
            if ((l != null) && (l.trim().length() != 0)) {
                System.setProperty("org.warlock.spine.messaging.messagedirectory", l);
            } else {
                System.err.println("Message Pool Directory not set");
            }
            l = p.getProperty(EXPIREDMESSAGEDIR);
            if ((l != null) && (l.trim().length() != 0)) {
                System.setProperty("org.warlock.spine.messaging.expireddirectory", l);
            } else {
                System.err.println("Expired message directory not set");
            }
            l = p.getProperty(PERSISTDURATIONDIR);
            if ((l != null) && (l.trim().length() != 0)) {
                System.setProperty("org.warlock.spine.messaging.persistdurations", l);
            }
            l = p.getProperty(EBXMLDIR);
            if ((l != null) && (l.trim().length() != 0)) {
                System.setProperty("org.warlock.spine.messaging.defaultebxmlhandler.filesavedirectory", l);
            } else {
                System.err.println("EbXML saved directory not set");
            }
            l = p.getProperty(DEDIR);
            if ((l != null) && (l.trim().length() != 0)) {
                System.setProperty("org.warlock.spine.messaging.defaultdistributionenvelopehandler.filesavedirectory", l);
            } else {
                System.err.println("Distribution Envelope Saved directory not set");
            }
            l = p.getProperty(SYNCDIR);
            if ((l != null) && (l.trim().length() != 0)) {
                System.setProperty("org.warlock.spine.messaging.defaultsynchronousresponsehandler.filesavedirectory", l);
            } else {
                System.err.println("Synchronous message saved directory not set");
            }
            l = p.getProperty(ASYNCWAIT);
            if ((l != null) && (l.trim().length() != 0)) {
                try {
                    asyncWait = Long.parseLong(l) * 1000;
                } catch (NumberFormatException e) {
                    System.err.println("Asynchronous wait period not a valid integer - " + e.toString());
                }
            } else {
                System.err.println("Asynchronous wait period not set");
            }
            try {
                asyncPort = Integer.parseInt(p.getProperty(ASYNCLISTENPORT));
            } catch (NumberFormatException numberFormatException) {
                asyncPort = DEFAULTLISTENPORT;
            }
            l = p.getProperty(LOGDIR);
            if ((l == null) || (l.trim().length() == 0)) {
                throw new Exception("SpineTools log configuration location: null or empty directory " + LOGDIR);
            }
            logDirectory = new File(l);
            if (!logDirectory.canRead()) {
                throw new Exception("SpineTools log configuration location: Unable to read destination directory " + l);
            }
            SpineToolsLogger stl = SpineToolsLogger.getInstance();
            stl.setAppName("MEDIPI-SPINETOOLS", l);

            l = p.getProperty(PROXYHOST);
            if ((l != null) && (l.trim().length() != 0)) {
                System.setProperty("org.warlock.spine.proxyhost", l);
            }
            l = p.getProperty(PROXYPORT);
            if ((l != null) && (l.trim().length() != 0)) {
                System.setProperty("org.warlock.spine.proxyport", l);
            }
            try {
                maxwait = Integer.parseInt(p.getProperty(MAXWAIT));
                if (maxwait < 250) {
                    throw new NumberFormatException();
                }
            } catch (NumberFormatException numberFormatException) {
                maxwait = DEFAULTMAXWAIT;
            }
            l = p.getProperty(PSISDOCUMENTMAXWAIT);
            if ((l != null) && (l.trim().length() != 0)) {
                System.setProperty("org.warlock.scrapi.document.syncresponsewait", l);
            }
            l = p.getProperty(PSISEVENTMAXWAIT);
            if ((l != null) && (l.trim().length() != 0)) {
                System.setProperty("org.warlock.scrapi.event.syncresponsewait", l);
            }
            l = p.getProperty(SVCIA);
            if ((l == null) || (l.trim().length() == 0)) {
                throw new Exception("SpineTools Transmitter: SvcIA - service-qualified interaction id cannot be null: " + SVCIA);
            }
            action = l;
            l = p.getProperty(ODS);
            if ((l == null) || (l.trim().length() == 0)) {
                throw new Exception("SpineTools Transmitter: ODS code, cannot be null: " + ODS);
            }
            ods = l;
            l = p.getProperty(ASID);
            if ((l == null) || l.equals("null") || (l.trim().length() == 0)) {
                l = null;
            }
            asid = l;
            l = p.getProperty(PARTYKEY);
            if ((l == null) || l.equals("null") || (l.trim().length() == 0)) {
                l = null;
            }
            partyKey = l;
        } catch (Exception e) {
            MediPiLogger.getInstance().log(SendSpineMessage.class.getName(), "A problem occurred whilst loading Spine Tools properties" + e);
            throw new Exception("Error Starting the SpineTools Listener " + e);
        }

        try {
            cm = ConnectionManager.getInstance();
            cm.listen(asyncPort);
        } catch (Exception e) {
            MediPiLogger.getInstance().log(SendSpineMessage.class.getName(), "Error Starting the SpineTools Listener " + e);
            throw new Exception("Error Starting the SpineTools Listener " + e);
        }

        try {
//            SDSconnection sds = cm.getSdsConnection();
            resolver = new SDSSpineEndpointResolver();
        } catch (Exception ex) {
            MediPiLogger.getInstance().log(SendSpineMessage.class.getName(), "Unable to initialise SDSConnection - " + ex.getMessage());
            throw new Exception("Unable to initialise SDSConnection " + ex);
        }

    }

    /**
     * Method to send a message to/via the spine 
     *
     * @param payload to be wrapped correctly by spineTools
     * @return boolean pass/fail on whether it was successful
     * @throws Exception
     */
    public boolean sendSpineMessage(String payload) throws Exception {
        try {
            MediPiLogger.getInstance().log(SendSpineMessage.class.getName(), "Using SpineTools: svcIA - " + action + " ODS - " + ods + " ASID - " + asid + " Party Key - " + partyKey);

            ArrayList<SdsTransmissionDetails> details;
            details = resolver.getTransmissionDetails(action, ods, asid, partyKey);
            if (details == null || details.isEmpty()) {
                MediPiLogger.getInstance().log(SendSpineMessage.class.getName(), "SDS failed to resolve details for the spine request message " + action);
                return false;
            }
// test for null and throw an exception saying that SDS cannot be acessesd
            SdsTransmissionDetails pds = details.get(0);
            SpineHL7Message msg;
            ITKDistributionEnvelopeAttachment deattachment = null;
            // Is the payload a Distribution Envelope requiring attachment to a separate ebXML MIME part?
            if (!payloadAttach) {
                msg = new SpineHL7Message(pds.getInteractionId(), payload);
            } else {
                msg = new SpineHL7Message(pds.getInteractionId(), "");
                // Extract XMLEncrypted DE
//                if (xmlEncryption) {
//                    ToolkitService svc = ServiceManager.getInstance().getService("XMLEncryptionAdapter");
//                    ServiceResponse sr = svc.execute(payload, XMLEncryptionAdapter.WRITEMODE);
//                    payload = sr.getResponse();
//                }

                DistributionEnvelope d = DistributionEnvelopeHelper.getInstance().getDistributionEnvelope(payload);
                deattachment = new ITKDistributionEnvelopeAttachment(d);

            }
            msg.setMyAsid(cm.getMyAsid());
            msg.setToAsid(pds.getAsids().get(0));

            try {
// Set author details in msg
                msg.setAuthorRole(authorRole);
                msg.setAuthorUid(authorUid);
                msg.setAuthorUrp(authorUrp);
            } catch (Exception e) {
                MediPiLogger.getInstance().log(SendSpineMessage.class.getName(), "Error in message preparation " + e.getMessage());
                return false;
            }
            Sendable sendable;
            if (pds.isSynchronous()) {
                sendable = new SpineSOAPRequest(pds, msg);
//                sr.setMessageId(sendable.getMessageId());
                cm.send(sendable, pds);
                try {
                    // pause until the synchronous request has completed
                    for (int i = 0; i < (int) (maxwait / 250); i++) {
                        Thread.sleep(250);
                        if (sendable.getSynchronousResponse() != null) {
//                            sr.getSpineItem().setSpineResponse(sendable.getSynchronousResponse());
//                            sr.setSpineRequest(sendable.getOnTheWireRequest());
                            break;
                        }
                    }
                } catch (Exception ie) {
                    MediPiLogger.getInstance().log(SendSpineMessage.class.getName(), "Asynchronous wait interrupted " + ie.getMessage());
                }

            } else {
                EbXmlMessage e = new EbXmlMessage(pds, msg);
                if (payloadAttach) {
                    e.addAttachment(deattachment);
                }
                sendable = e;
                cm.send(sendable, pds);
                try {
                    Thread.sleep(asyncWait);
                } catch (InterruptedException ie) {
                    MediPiLogger.getInstance().log(SendSpineMessage.class.getName(), "Asynchronous wait interrupted " + ie.getMessage());
                }

                cm.stopListening();
                cm.stopRetryProcessor();
            }

        } catch (Exception ex) {
            MediPiLogger.getInstance().log(SendSpineMessage.class.getName(), "Error in message transmission to Spine - check SpineTools Logs for details" + ex.getMessage());
            return false;
        }
        return true;
    }

}
