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
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Date;
import java.util.UUID;
import org.medipi.utilities.Utilities;
import org.warlock.itk.distributionenvelope.Address;
import org.warlock.itk.distributionenvelope.DistributionEnvelope;
import org.warlock.itk.distributionenvelope.Identity;
import org.warlock.itk.distributionenvelope.Payload;

import org.warlock.itk.util.ITKException;
/**
 * Method to create a Business Acknowledgement
 * 
 * @author DAMU2
 */
public class ApplicationAcknowledgment 
    extends DistributionEnvelope
{    
    private static final String BIZACKTEMPLATE = "business-ack-payload-template.txt";
    private static final String BIZNACKTEMPLATE = "business-nack-payload-template.txt";
        
    private static final String AUDIT_ID_PROPERTY = "org.warlock.itk.router.auditidentity";
    private static final String SENDER_PROPERTY = "org.warlock.itk.router.senderaddress";
    
    public static final String SERVICE = "urn:nhs-itk:services:201005:SendBusinessAck-v1-0"; 
    public static final String INTERACTION = "urn:nhs-itk:interaction:ITKBusinessAcknowledgement-v1-0"; 
    public static final String PROFILEID = "urn:nhs-en:profile:ITKBusinessAcknowledgement-v1-0";
    
    public static final String RESPONSETYPE = "org.warlock.spine.messaging.acknowledgements.positiveresponsetype";

    public static final String NACKERRORCODE = "org.warlock.spine.messaging.acknowledgements.nack.errorcode";    
    public static final String NACKERRORTEXT = "org.warlock.spine.messaging.acknowledgements.nack.errortext";
    public static final String NACKDIAGTEXT = "org.warlock.spine.messaging.acknowledgements.nack.diagnostictext";
    
    private static String ackTemplate = null;
    private static String nackTemplate = null;
    //private static String responseEnvelope = null;
    
    protected String serviceRef = null;
    
    protected DistributionEnvelope original = null;
    
    public ApplicationAcknowledgment(DistributionEnvelope d)
            throws Exception
    {         
        synchronized (BIZACKTEMPLATE) {
            if (ackTemplate == null) {
                ackTemplate = loadTemplate(getClass().getResourceAsStream(BIZACKTEMPLATE));
                nackTemplate = loadTemplate(getClass().getResourceAsStream(BIZNACKTEMPLATE));
      //          responseEnvelope = loadTemplate(getClass().getResourceAsStream(RESPDETEMPLATE));
            }
        }
        original = d;
        setTrackingId(UUID.randomUUID().toString().toUpperCase());
        Address[] a = new Address[1];
        a[0] = d.getSender();
        setTo(a);
        String id = null;
        String snd = null;
        try {
            id = System.getProperty(AUDIT_ID_PROPERTY);
            snd = System.getProperty(SENDER_PROPERTY);
        }
        catch (Exception e) {
            throw new ITKException("SYST-0000", "Configuration manager exception", e.getMessage());
        }
        Address sndr = new Address(snd);
        Identity[] auditId = new Identity[1];
        auditId[0] = new Identity(id);
        setAudit(auditId);
        setSender(sndr);
        setService(SERVICE);
        serviceRef = d.getService();
        addHandlingSpecification(INTERACTIONID, INTERACTION);
        makeResponse();
        
    }
    
    protected final void makeResponse() 
            throws Exception
    {        
       String rtype = System.getProperty(RESPONSETYPE);
       StringBuilder ack = null;
       boolean isack = ((rtype == null) || rtype.toLowerCase().startsWith("y"));
       if (isack) {
           ack = new StringBuilder(ackTemplate);
           doAckSubstitutions(ack);
       } else {
           ack = new StringBuilder(nackTemplate);
           doAckSubstitutions(ack);
           doNackSubstitutions(ack);
       }
       
       Payload p = new Payload("text/xml");
       p.setBody(ack.toString(), false);
       p.setProfileId(PROFILEID);
       super.addPayload(p);
    }
    
    private void doAckSubstitutions(StringBuilder sb) 
            throws Exception
    {
        // Substitutions common to acks and nacks
        substitute(sb, "__MESSAGE_ID__", UUID.randomUUID().toString().toUpperCase());
        substitute(sb, "__HL7_CREATION_DATE__", Utilities.INTERNAL_FORMAT.format(new Date()));
        substitute(sb, "__INTERACTION_ID__", original.getInteractionId());
        substitute(sb, "__INBOUND_TRANSMISSIONID__", original.getTrackingId());
        substitute(sb, "__RESPONDER_RECEIVER_ADDRESS_OID__", original.getSender().getOID());
        substitute(sb, "__RESPONDER_RECEIVER_ADDRESS__", original.getSender().getUri());        
        substitute(sb, "__RESPONDER_SENDER_ADDRESS_OID__", sender.getOID());
        substitute(sb, "__RESPONDER_SENDER_ADDRESS__", sender.getUri());
        substitute(sb, "__INBOUND_PAYLOAD_ID__", original.getPayloadId(0));
    }
    
    private void doNackSubstitutions(StringBuilder sb) 
            throws Exception
    {
        // Substitutions for nacks only
        // __ERROR_CODE__ __ERROR_TEXT__ and __DIAGNOSTIC_TEXT__ need to be picked up from
        // somewhere... Properties for now... Needs review.
        
        String errorCode = System.getProperty(NACKERRORCODE);
        String errorText = System.getProperty(NACKERRORTEXT);
        String diagText = System.getProperty(NACKDIAGTEXT);
        if (errorCode == null) { errorCode = "1000"; }
        if (errorText == null) { errorText = "Example error"; }
        if (diagText == null) { diagText = "Example diagnostic text"; }
        substitute(sb, "__ERROR_CODE__", errorCode);
        substitute(sb, "__ERROR_TEXT__", errorText);
        substitute(sb, "__DIAGNOSTIC_TEXT__", diagText);
    }
    
    protected final String loadTemplate(InputStream is)
            throws Exception
    {
        BufferedReader br = new BufferedReader(new InputStreamReader(is));
        return loadTemplate(br);
    }
    
    protected String loadTemplate(BufferedReader br)
            throws Exception
    {
        StringBuilder sb = new StringBuilder();
        String line = null;
        while ((line = br.readLine()) != null) {
            sb.append(line);
            sb.append("\n");
        }
        br.close();
        return sb.toString();        
    }    

    private void substitute(StringBuilder sb, String tag, String value)
            throws Exception
    {
        int tagPoint = -1;
        int tagLength = tag.length();
        while ((tagPoint = sb.indexOf(tag)) != -1 ) {
            sb.replace(tagPoint, tagPoint + tagLength, value);
        }
    }

}
