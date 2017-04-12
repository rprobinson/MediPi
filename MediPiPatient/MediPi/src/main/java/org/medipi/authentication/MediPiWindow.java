/*
 *  Copyright 2016  Richard Robinson @ NHS Digital <rrobinson@nhs.net>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.medipi.authentication;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.List;
import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.scene.Node;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import org.medipi.MediPi;
import org.medipi.MediPiProperties;

/**
 * This class extends the Node Pane to allow MediPiWindow to swap between the
 * MediPi UI Node and a pluggable authentication UI Node.
 *
 * A timer is started to check to see if the authentication has to be
 * re-established. All parameters are configurable from the medipi.properties
 * file. The Unlock method allows the switch between authentication UI and
 * standard Medipi UI
 *
 * @author rick@robinsonhq.com
 */
public class MediPiWindow extends Pane {

    private static final String MEDIPIAUTHENTICATIONLIFESPAN = "medipi.authentication.lifespan";
    private static final String MEDIPIAUTHENTICATIONCLASS = "medipi.authentication.class";
    Node medipiWindow;
    Node authenticationWindow;
    private long expireTime;
    private boolean stopTimer;
    private int expirePeriod = 0;
    private Thread thread;
    private List<UnlockConsumer> unlockConsumerList = new ArrayList<>();
    BooleanProperty lockedstatus = new SimpleBooleanProperty(true);
    /**
     * Constructor to establish basic parameters and start the timer. The mediPi
     * Node is passed in and the class defaults to authentication window. A
     * lifespan of 0 sets the authentication off
     *
     * @param child UI Node
     * @param m main medipi window
     * @throws Exception
     */
    public MediPiWindow(Node child) throws Exception {
        medipiWindow = child;
        // instantiate the authentication class
        String authClass = MediPiProperties.getInstance().getProperties().getProperty(MEDIPIAUTHENTICATIONCLASS);
        try {
            Class<?> cl = Class.forName(authClass);
            Constructor<?> cons = cl.getConstructor(MediPiWindow.class);
            Object o = cons.newInstance(this);
            AuthenticationInterface ai = (AuthenticationInterface) o;
            authenticationWindow = ai.getWindow();
        } catch (Exception ex) {
            throw new Exception("The authentication class could not be found");
        }
        // Set how long the authentication is valid for
        String s = MediPiProperties.getInstance().getProperties().getProperty(MEDIPIAUTHENTICATIONLIFESPAN);
        if (s == null) {
            throw new Exception("The authentication lifespan has not  been set");
        } else {
            expirePeriod = Integer.parseInt(s) * 1000;
        }
        if (expirePeriod < 0) {
            throw new Exception("The authentication lifespan is not a positive integer: " + expirePeriod);
        }
        super.getChildren().add(authenticationWindow);
        expireTime = System.currentTimeMillis();
        lockedstatus.addListener(new ChangeListener<Boolean>() {

            @Override
            public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
                for (UnlockConsumer uc : unlockConsumerList) {
                    if(newValue){
                        uc.locked();
                    }else{
                        uc.unlocked();
                    }
                }
            }
        });
        startTimer();
    }

    private void startTimer() {
        thread = new Thread(() -> {
            medipiWindow.setOnMouseMoved((MouseEvent event) -> {
                resetTimer();
            });
            while (true) {
                try {
                    // While the is in time
                    if (expireTime > System.currentTimeMillis() || expirePeriod == 0) {
                        Platform.runLater(() -> {
                            this.getChildren().set(0, medipiWindow);
                            lockedstatus.set(false);
                        });
                    } else {
                        // while the authentication is expired
                        Platform.runLater(() -> {
                            this.getChildren().set(0, authenticationWindow);
                            lockedstatus.set(true);
                        });
                    }
                    Thread.sleep(1000);
                } catch (InterruptedException ex) {
                    //no action as this is how unlock immediately opens the medipi window
                }
            }
        });

        thread.start();
    }

    /**
     * A method to reset the timer to the original timeout period as defined by
     * the timeout configuration
     */
    protected void unlock() {
        resetTimer();
        thread.interrupt();
    }

    private void resetTimer() {
        expireTime = System.currentTimeMillis() + expirePeriod;
    }
    // public lock method
    public void lock() {
        expireTime = 0;
    }

    public void registerForAuthenticationCallback(UnlockConsumer unlockConsumer) {
        unlockConsumerList.add(unlockConsumer);
    }
}
