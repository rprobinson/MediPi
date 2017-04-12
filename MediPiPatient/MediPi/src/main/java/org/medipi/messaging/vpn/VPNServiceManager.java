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
package org.medipi.messaging.vpn;

import java.time.Instant;
import java.util.HashMap;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import javafx.beans.property.IntegerProperty;
import javafx.concurrent.Task;
import org.medipi.MediPiProperties;

/**
 * Service singleton class to manage the connection to the VPN which allows
 * secure access to the MediPi Concentrator
 *
 * As well as allowing the VPN connection to be managed on demand by the calling
 * program - this can also be disabled with the expectation that the VPN
 * connection is already up, managed by another entity. The period after which
 * connection is successfully made can also be configured.
 *
 * @author rick@robinsonhq.com
 */
public class VPNServiceManager {

    // Connection commands
    public static final int OPEN = 1;
    public static final int CLOSE = 0;

    HashMap<UUID, Instant> currentConnections = new HashMap<>();
    private static final String VPNKEEPALIVEPERIOD = "medipi.vpn.keepaliveperiod";
    VPNConnectionManager manager = null;
    private static final String CONFIGFILELOCATION = "medipi.vpn.configlocation";
    private static final String MEDIPIVPNENABLE = "medipi.vpn.enable";
    private boolean enableVPN = true;
    private long expireTime;
    private boolean stopTimer;
    private int expirePeriod = 10;
    private static Exception bootException = null;
    private Task<String> task = null;

    private VPNServiceManager() {
        try {
            // is the VPN enabled
            String b = MediPiProperties.getInstance().getProperties().getProperty(MEDIPIVPNENABLE);
            if (b == null || b.trim().length() == 0) {
                enableVPN = false;
            } else {
                // If not y then disable
                enableVPN = b.toLowerCase().startsWith("y");
            }
            String config = MediPiProperties.getInstance().getProperties().getProperty(CONFIGFILELOCATION);
            manager = new VPNConnectionManager(config);
            String s = MediPiProperties.getInstance().getProperties().getProperty(VPNKEEPALIVEPERIOD);
            if (s != null) {
                expirePeriod = Integer.parseInt(s) * 1000;
                if (expirePeriod < 0) {
                    bootException = new Exception("The VPN keep alive period is not a positive integer");
                }
            }
        } catch (Exception e) {
            bootException = new Exception("An unexpected issue has occured when loading the VPN configurations" + e);
        }
        stopTimer = false;

    }

    public static VPNServiceManager getInstance() throws Exception {
        if (bootException != null) {
            throw bootException;
        }
        return VPNManagerHolder.INSTANCE;
    }

    public synchronized void VPNConnection(int command, UUID uuid) throws Exception {
        if (command == OPEN) {
            openConnection(uuid);
        } else if (command == CLOSE) {
            closeConnection(uuid);
        }

    }

    private void closeConnection(UUID uuid) {
        try {
            //if there is still > 1 connections open dont close 
            if (currentConnections.size() == 1) {
                try {
                    manager.down();
                    System.out.println("VPN connection shutdown");
                    currentConnections.remove(uuid);
                    System.out.println("removed: " + uuid + " size = " + currentConnections.size());
                } catch (Exception ex) {
                    System.out.println("Manager failed to close VPN connection: " + ex.getLocalizedMessage());
                }
            } else {
                currentConnections.remove(uuid);
                System.out.println("removed: " + uuid + " size = " + currentConnections.size());
            }
        } catch (Exception ex) {
            //do nothing as there is no vpn to close
        }
    }

    private void openConnection(UUID uuid) throws Exception {
        // when makes a new connection up method returns true
        manager.up();
        // any failures in "up" method" throw exception
        currentConnections.put(uuid, Instant.now());
        System.out.println("added: " + uuid + " size = " + currentConnections.size());
    }

//    private void keepAliveClock() {
//        task = new Task<String>() {
//            @Override
//            protected String call() throws Exception {
//                String operationStatus = "Unknown error";
//                while (expireTime > System.currentTimeMillis() || expirePeriod == 0) {
//                    if (stopTimer) {
//                        break;
//                    }
//                    try {
//                        System.out.println("sleep");
//                        Thread.sleep(1000);
//                    } catch (InterruptedException ex) {
//                        System.out.println("VPN Timer Sleep interrupted " + ex.getLocalizedMessage());
//                    }
//                }
//                stopTimer = false;
//                try {
//                    System.out.println("shutdown");
//                    manager.down();
//                } catch (Exception ex) {
//                    System.out.println("Manager failed to close VPN connection: " + ex.getLocalizedMessage());
//                }
//                return operationStatus;
//            }
//
//            // the measure of completion and success is returning "SUCCESS"
//            // all other outcomes indicate failure and pipe the failure
//            // reason given from the device to the error message box
//            @Override
//            protected void succeeded() {
//                super.succeeded();
//            }
//
//            @Override
//            protected void scheduled() {
//                super.scheduled();
//            }
//
//            @Override
//            protected void failed() {
//                super.failed();
//            }
//
//            @Override
//            protected void cancelled() {
//                super.failed();
//            }
//        };
//
//        new Thread(task).start();
//    }
    /**
     * A method to reset the timer to the original timeout period as defined by
     * the timeout configuration
     */
//    public void reset() {
//        expireTime = System.currentTimeMillis() + expirePeriod;
//    }
    /**
     * A method to stop the timer without closing the connection as this is
     * expected to be handled by the Pipelining queue which has the
     * responsibility in normal operation
     */
    public void stopTimer() {
        stopTimer = true;
    }

    public void setVPNConnectionIndicator(IntegerProperty connectionIndicator) {
        IntegerProperty hasTunnel = manager.getVPNTunnelProperty();
        connectionIndicator.bind(hasTunnel);
    }

    public boolean isEnabled() {
        return enableVPN;
    }

    private static class VPNManagerHolder {

        private static final VPNServiceManager INSTANCE = new VPNServiceManager();
    }

}
