/*

 Copyright 2014 Health and Social Care Information Centre
 Solution Assurance damian.murphy@hscic.gov.uk

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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import org.warlock.itk.distributionenvelope.AckDistributionEnvelope;
import org.warlock.itk.distributionenvelope.DistributionEnvelope;
import org.warlock.itk.distributionenvelope.DistributionEnvelopeHelper;
import org.warlock.itk.distributionenvelope.Payload;
import org.warlock.spine.connection.ConnectionManager;
import org.warlock.spine.connection.SDSSpineEndpointResolver;
import org.warlock.spine.connection.SDSconnection;
import org.warlock.spine.connection.SdsTransmissionDetails;

/**
 * A DistributionEnvelope handler that :
 *
 * 1. writes the envelope and its content, to a file on disk. This is the
 * default behaviour of the MHS' ITK Trunk handler
 *
 * 2. responds to the requestor with a infrastructure acknowledgement 3. then
 * business ack is also sent (+ve or negative - configurable)
 *
 * @author rrobinson@nhs.net
 */
public class MediPiDistributionEnvelopeHandler extends Thread
        implements DistributionEnvelopeHandler {

    /**
     * System property. Holds the path where received ITK Distribution Envelopes
     * are written. Note that if this property is not set, the system "user.dir"
     * property is used.
     */
    private static final String SAVE_DIRECTORY = "org.warlock.spine.messaging.defaultdistributionenvelopehandler.filesavedirectory";

    /**
     * System property. Set to something beginning with "y" or "Y" to cause the
     * fully-qualified path name of the file, to be written to System.out.
     */
    private static final String REPORT_FILENAME = "org.warlock.spine.messaging.defaultdistributionenvelopehandler.reportfilename";
    private static final String NEGEBXMLACK = "org.warlock.spine.connection.negativeebxmloverride";
    private static final String INFACKREQ = "urn:nhs-itk:ns:201005:infackrequested";

    private File fileSaveDirectory = null;
    private boolean reportFilename = false;

    public MediPiDistributionEnvelopeHandler()
            throws FileNotFoundException, IllegalArgumentException {
        String fsd = System.getProperty(SAVE_DIRECTORY);
        if (fsd == null) {
            fsd = System.getProperty("user.dir");
        }
        fileSaveDirectory = new File(fsd);
        if (!fileSaveDirectory.exists()) {
            throw new FileNotFoundException("Default ITK distribution envelope handler save location does not exist");
        }
        if (!fileSaveDirectory.isDirectory()) {
            throw new IllegalArgumentException("Default ITK distribution envelope handler save location must be a directory");
        }
        if (!fileSaveDirectory.canWrite()) {
            throw new IllegalArgumentException("Default ITK distribution envelope handler save location must be writable");
        }
        String rf = System.getProperty(REPORT_FILENAME);
        reportFilename = ((rf != null) && (rf.toLowerCase().startsWith("y")));
    }

    /**
     * Parses the distribution envelope payloads, and writes the envelope plus
     * contents to disk.
     *
     * @param d the distribution envelope object
     * @throws Exception
     */
    @Override
    public void handle(DistributionEnvelope d)
            throws Exception {
        // The initial parsing for the DistributionEnvlope doesn't extract
        // the payloads (because it is intended to be called by routers that
        // don't need to know about payloads). That will cause an exception
        // to be thrown when we try to write the DE to a string, so call
        // "parsePayloads()" here..
        //
        d.parsePayloads();
        DistributionEnvelopeHelper helper = DistributionEnvelopeHelper.getInstance();
        Payload[] pay = helper.getPayloads(d);
        for (Payload p : pay) {
            System.out.println(p.getContent());
            File outfile = new File(fileSaveDirectory, p.getManifestId() + ".csv");
            try (FileWriter fw = new FileWriter(outfile)) {
                fw.write(p.getContent());

                fw.flush();
            }
        }

        String s = d.getService();
        s = s.replaceAll(":", "_");
        StringBuilder sb = new StringBuilder(s);
        sb.append("_");
        sb.append(getFileSafeMessageID(d.getTrackingId()));
        sb.append(".message");
        File outfile = new File(fileSaveDirectory, sb.toString());
        try (FileWriter fw = new FileWriter(outfile)) {
            fw.write(d.toString());

            fw.flush();
        }

        if (reportFilename) {
            System.out.println(outfile.getCanonicalPath());
        }
        //Send Infrastructure Acknowledgement
        String infackReq = d.getHandlingSpecification("urn:nhs-itk:ns:201005:infackrequested");

        if (infackReq != null && infackReq.toLowerCase().equals("true")) {
            AckDistributionEnvelope ade = new AckDistributionEnvelope(d);
            ade.makeMessage();
            send(d, ade);
            //if a negative ebxml is configured do no further processing
            if (System.getProperty(NEGEBXMLACK).trim().toLowerCase().equals("y")) {
                return;
            }
            MediPiDistributionEnvelopeHandler.sleep(3000);
        }

        //Send Business Acknowledgement
        String busackReq = d.getHandlingSpecification("urn:nhs-itk:ns:201005:ackrequested");

        if (busackReq != null && busackReq.toLowerCase().equals("true")) {
            ApplicationAcknowledgment aa = new ApplicationAcknowledgment(d);
            aa.addHandlingSpecification(INFACKREQ, "true");
            send(d, aa);
        }
    }

    private void send(DistributionEnvelope incoming, DistributionEnvelope outgoingDE) throws Exception {
        //Extract ODS code from sending Entity - assume it's the first entity after "ods:"
        int i = 0;
        String odsCode = null;

        for (String s : incoming.getSender().getParts()) {
            if (s.toLowerCase().equals("ods")) {
                odsCode = incoming.getSender().getParts().get(i + 1);
                break;
            }
            i++;
        }
        if (odsCode == null) {
            throw new Exception("ODS code cannot be derived from the sender address");
        }
        // call SDS to find where/how to send response bus/inf acks
        ConnectionManager cm = ConnectionManager.getInstance();
        cm.loadPersistedMessages();
        SDSconnection sds = cm.getSdsConnection();
        SDSSpineEndpointResolver resolver = new SDSSpineEndpointResolver(sds);
        ArrayList<SdsTransmissionDetails> details = null;
        // The assumption is that there is only instance of an end point in the target ODS.
        // Therefore The SDS resolver will take the only response or first entry in cache
        details = resolver.getTransmissionDetails("urn:nhs:names:services:itk:COPC_IN000001GB01", odsCode, null, null);
        SdsTransmissionDetails std = details.get(0);

        SpineHL7Message msg = new SpineHL7Message(std.getInteractionId(), "");

        ITKDistributionEnvelopeAttachment deattachment = null;
        deattachment = new ITKDistributionEnvelopeAttachment(outgoingDE);

        msg.setMyAsid(std.getAsids().get(0));
        msg.setToAsid(cm.getMyAsid());

// Set author details in msg
        msg.setAuthorRole(System.getProperty("org.warlock.spine.messaging.authorrole"));
        msg.setAuthorUid(System.getProperty("org.warlock.spine.messaging.authoruid"));
        msg.setAuthorUrp(System.getProperty("org.warlock.spine.messaging.authorurp"));
        EbXmlMessage e = new EbXmlMessage(std, msg);
        e.addAttachment(deattachment);
        cm.send(e, std);
    }

    private void substitute(StringBuilder sb, String tag, String value)
            throws Exception {
        int tagPoint = -1;
        int tagLength = tag.length();
        while ((tagPoint = sb.indexOf(tag)) != -1) {
            sb.replace(tagPoint, tagPoint + tagLength, value);
        }
    }

    private String getFileSafeMessageID(String s) {
        if (s == null) {
            return s;
        }
        if (!s.contains(":")) {
            return s;
        }
        StringBuilder sb = new StringBuilder(s);
        int c = -1;
        while ((c = sb.indexOf(":")) != -1) {
            sb.setCharAt(c, '_');
        }
        return sb.toString();
    }

    private StringBuilder readTemplate(String tname)
            throws Exception {
        StringBuilder sb;
        try (InputStream is = getClass().getResourceAsStream(tname)) {
            BufferedReader br = new BufferedReader(new InputStreamReader(is));
            sb = new StringBuilder();
            String line = null;
            while ((line = br.readLine()) != null) {
                sb.append(line);
                sb.append("\r\n");
            }
        }
        return sb;
    }

}
