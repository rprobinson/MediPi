/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.medipi.devices;

import javafx.beans.property.BooleanProperty;
import javafx.collections.ObservableList;

/**
 *
 * @author riro
 */
public interface MessageReceiver {

    public void callFailure(String failureMessage, Exception e);
   
    public BooleanProperty getAlertBooleanProperty();

    public void setMessageList(ObservableList<Message> items);
}
