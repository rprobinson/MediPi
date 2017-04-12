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

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.NetworkInterface;
import java.util.Enumeration;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import org.medipi.AlertBanner;
import org.medipi.MediPi;
import org.medipi.MediPiProperties;

/**
 * Class to Manage the basic VPN connection through to the MediPi Concentrator
 *
 * This class uses the commandline interface to bring the vpn connection up and
 * down
 *
 * @author damian
 */
public class VPNConnectionManager {

    private static final String OPENVPNCOMMAND = "medipi.vpn.openvpncommand";
    private static final String OPENVPNKILLER = "medipi.vpn.openvpnkiller";
    private static final String CLIENTNAME = "medipi.vpn.clientname";
    private String openVpnCommand = null;
    private String openVpnKiller = null;
    private Process openVpnProcess = null;
    private String connectionName = null;
    private String clientName = null;
    private static String OS = null;
    private static IntegerProperty hasTunnelProperty = new SimpleIntegerProperty(0);

    public VPNConnectionManager(String name) throws Exception {

        connectionName = name;
        clientName = MediPiProperties.getInstance().getProperties().getProperty(CLIENTNAME);
        //Search for different connection names depending on the OS
        String os = System.getProperty("os.name").toLowerCase();
        if (os.contains("win")) {
            OS = "WIN";
        } else if (os.contains("nix") || os.contains("nux") || os.contains("aix")) {
            OS = "UNIX";
            if (connectionName == null) {
                throw new Exception("No Open VPN configuration file is set");
            }
            if (clientName == null) {
                throw new Exception("The open VPN client name has not been set");
            }
        } else {
            throw new Exception("The MediPi VPN Manager does not currently support " + os);
        }

        openVpnCommand = MediPiProperties.getInstance().getProperties().getProperty(OPENVPNCOMMAND);
        if (openVpnCommand == null) {
            throw new Exception("The open VPN command has not been set");
        }
        openVpnKiller = MediPiProperties.getInstance().getProperties().getProperty(OPENVPNKILLER);
        if (openVpnKiller == null) {
            throw new Exception("The VPN killer command has not been set");
        }

        if (hasTunnel()) {
            hasTunnelProperty.set(MediPi.VPNCONNECTED);
        } else {
            hasTunnelProperty.set(MediPi.VPNNOTCONNECTED);
        }
    }

    @Override
    public void finalize() throws Throwable {
        try {
            down();
            super.finalize();
        } catch (Exception e) {
            System.err.println("Exception in finalizer: " + e.toString());
        }

    }

    public synchronized boolean up() throws Exception {

        if (VPNConnectionManager.hasTunnel()) {
            hasTunnelProperty.set(MediPi.VPNCONNECTED);
            return false;
        }
        hasTunnelProperty.set(MediPi.VPNCONNECTING);
        if (OS.equals("WIN")) {
            openVpnProcess = Runtime.getRuntime().exec(openVpnCommand);
        } else if (OS.equals("UNIX")) {
            openVpnProcess = Runtime.getRuntime().exec(openVpnCommand + " " + connectionName);
        }

        InputStreamReader in = new InputStreamReader(openVpnProcess.getInputStream());
        String line = null;
        BufferedReader br = new BufferedReader(in);
        StringBuilder sb = new StringBuilder();
        while ((line = br.readLine()) != null) {
            System.out.println(line);
            sb.append(line);
            sb.append("\n");
            if (line.contains("Complete")) {
                hasTunnelProperty.set(MediPi.VPNCONNECTED);
                return true;

            }
            if (line.contains("Network is unreachable")) {
                hasTunnelProperty.set(MediPi.VPNFAILED);
                System.out.println("internet down");
                throw new Exception("Network is unreachable");
            }
            if (line.contains("Connection timed out")) {
                hasTunnelProperty.set(MediPi.VPNFAILED);
                System.out.println("VPN Connection timed out");
                continue;
            }            
            if (line.contains("Connection reset")) {
                hasTunnelProperty.set(MediPi.VPNRESTARTING);
                continue;
            }

            //
            // Look for error reports... but filter out cases such as VPN route
            // addition having failed because there is one already there (the
            // VPN route will still work as long as the tun interface is there
            // as the network on the other end will still be the same.
            //
            if (line.contains("error")) {
                if (!line.contains("route add command") && !VPNConnectionManager.hasTunnel()) {
                    hasTunnelProperty.set(MediPi.VPNFAILED);
                    throw new Exception("Connection failed: " + sb.toString());
                }
            }
        }
        hasTunnelProperty.set(MediPi.VPNFAILED);
        throw new Exception("Did not create tunnel: " + sb.toString());
    }

    public synchronized boolean down() throws Exception {
        //Check there is an open VPN connection to close
        if (!VPNConnectionManager.hasTunnel()) {
            hasTunnelProperty.set(MediPi.VPNNOTCONNECTED);
            return false;
        }
        try {
            externalKill();
            return true;
//            if (openVpnProcess == null) {
//                externalKill();
//                return false;
//            } else {
//                int i = openVpnProcess.destroyForcibly().waitFor();
//                System.out.println("forcibly destroy output = " + i);
//                // Other things to consider are an external call to make sure that
//                // if we think we've killed the VPN process then when we did so the
//                // action did actually remove the VPN routing table entries... 
//                // ... because that doesn't always happen cleanly.
//                //
//                return true;
//            }
        } finally {
            Thread.sleep(1000);
            if (VPNConnectionManager.hasTunnel()) {
                hasTunnelProperty.set(MediPi.VPNCONNECTED);
            } else {
                hasTunnelProperty.set(MediPi.VPNNOTCONNECTED);
            }
        }

    }

    private void externalKill() throws Exception {
        // This relies on a system specific and deeply horrible hack.
        // Call out to something configurable that will in a system-specific
        // way find and kill the openvpn process that we don't now controln/

        System.out.println("start kill attempt");
        int i = Runtime.getRuntime().exec(new String[]{openVpnKiller}).waitFor();
        System.out.println("external kill output = " + i);
//        p.waitFor();
    }

    public static boolean hasTunnel() throws Exception {
        return !(getTunnel() == null);
    }

    public static NetworkInterface getTunnel() throws Exception {
        Enumeration<NetworkInterface> ifs = NetworkInterface.getNetworkInterfaces();
        while (ifs.hasMoreElements()) {
            NetworkInterface n = ifs.nextElement();

            //Search for different connection names depending on the OS
            if (OS.equals("WIN")) {
                if (n.getDisplayName().contains("TAP-Win32") && n.isUp()) {
                    return n;
                }
            } else if (OS.equals("UNIX")) {
                if (n.getName().contains("tun") || n.getName().contains("tap")) {
                    return n;
                }

            }

        }
        return null;
    }

    protected IntegerProperty getVPNTunnelProperty() {
        return hasTunnelProperty;
    }
}
