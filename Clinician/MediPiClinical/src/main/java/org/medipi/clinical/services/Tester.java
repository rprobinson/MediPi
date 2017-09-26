/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.medipi.clinical.services;

import java.util.List;
import org.medipi.model.DirectPatientMessage;

/**
 *
 * @author riro
 */
public interface Tester {

    public boolean isEnabled();

    public String getDirectPatientMessageResourcePath();

    public boolean updateDirectPatientMessageTableWithSuccess(DirectPatientMessage directPatientMessage) throws Exception;

    //Increment the retyr attempt column +1 with each failure
    public void updateDirectPatientMessageTableWithFail(DirectPatientMessage directPatientMessage);
    
    //perminantly fail the direct message attempt with update of -1
    public void failDirectPatientMessageTable(DirectPatientMessage directPatientMessage);

    public List<DirectPatientMessage> findDirectPatientMessagesToResend();
    
}
