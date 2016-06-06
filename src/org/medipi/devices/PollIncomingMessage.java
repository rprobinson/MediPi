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
package org.medipi.devices;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.file.Path;
import java.util.Date;
import org.medipi.MediPiMessageBox;
import org.medipi.logging.MediPiLogger;

/**
 * Class to poll a secure remote server directory and bring back into the local
 * incoming mail directory.
 *
 * This uses a simple wget message to mirror the remote server file
 *
 * @author rick@robinsonhq.com
 */
public class PollIncomingMessage
        implements Runnable {

    private final Path dir;
    private final String userName;
    private final String url;
    private final String wget = "wget -P %%PATH%% --mirror -r -l1 --no-parent -nd -A.txt %%URL%%/%%USERNAME%%/";

    /**
     * Constructor for PollIncomingMessage class
     *
     * @param d path in local machine the mail directory
     * @param u username on the remote server
     * @param url of the remote server of the mail directory
     */
    public PollIncomingMessage(Path d, String u, String url) {
        dir = d;
        userName = u;
        this.url = url;
    }

    @Override
    public void run() {
        try {
            String command = wget.replace("%%PATH%%", dir.toString());
            command = command.replace("%%USERNAME%%", userName);
            command = command.replace("%%URL%%", url);
            System.out.println(command);
            Process p = Runtime.getRuntime().exec(command);
            BufferedReader stdInput = new BufferedReader(new InputStreamReader(p.getInputStream()));
            String response = "";
            String temp = "";
            while ((temp = stdInput.readLine()) != null) {
                response += temp;
            }
            MediPiLogger.getInstance().log(PollIncomingMessage.class.getName(), "retreival for incomingmessage: " + new Date() + response);
        } catch (Exception e) {
            MediPiMessageBox.getInstance().makeErrorMessage("MediPi is unable to retreive incoming clinician's messages", e, Thread.currentThread());
        }
    }
}
