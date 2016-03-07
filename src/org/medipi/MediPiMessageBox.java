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
package org.medipi;

import javafx.application.Platform;
import jfx.messagebox.MessageBox;
import org.warlock.spine.logging.MediPiLogger;

/**
 * Singleton Class to deliver alert messages to MediPi This class is intended to
 * be phased out in favour of alerting the user using a window integrated within
 * the application window.
 *
 * This class uses the JFXMessageBox library - licensed under a LGPL/EPL/ASL
 * triple license, allowing use of the files under the terms of any one of the
 * GNU Lesser General Public License, the Eclipse Public License, or the Apache
 * License.
 *
 * However since this was developed Medipi now uses the OpenJFX which supports
 * alert boxes natively so an intermediate solution may be to introduce this.
 * 
 * These message box only display errors
 *
 * @author rick@robinsonhq.com
 */
public class MediPiMessageBox {

    private MediPi medipi;

    private MediPiMessageBox() {
    }

    /**
     * returns the one and only instance of the singleton
     *
     * @return singleton instance
     */
    public static MediPiMessageBox getInstance() {
        return MediPiHolder.INSTANCE;
    }

    /**
     * Sets a reference to the main MediPi class for callbacks and access
     *
     * @param m medipi reference to the main class
     */
    public void setMediPi(MediPi m) {
        medipi = m;
    }

    /**
     * Method for displaying an error messagebox to the screen. As it takes a
     * thread argument this displays messages from either the current thread or
     * the display thread
     *
     * @param message String message
     * @param except exception which this message arose from - null if not
     * applicable
     * @param thisThread Thread from which the message was created
     */
    public void makeErrorMessage(String message, Exception except, Thread thisThread) {
        try {
            String debugString = getDebugString(except);
            MediPiLogger.getInstance().log(MediPiMessageBox.class.getName() + ".makeErrorMessage", "MediPi informed the user that: " + message + " " + except);
            if (medipi.getDebugMode() == MediPi.DEBUG) {
                if (except != null) {
                    except.printStackTrace();
                }
            }
            if (medipi.getDebugMode() == MediPi.ERRORSUPPRESSING) {
                System.out.println("Error - " + message + debugString);
            } else if (thisThread != Thread.currentThread()) {
                Platform.runLater(() -> {
                    MessageBox.show(medipi.getStage(),
                            "Error - " + message + debugString,
                            "Error dialog",
                            MessageBox.ICON_ERROR | MessageBox.OK);
                });
            } else {
                MessageBox.show(medipi.getStage(),
                        "Error - " + message + debugString,
                        "Error dialog",
                        MessageBox.ICON_ERROR | MessageBox.OK);
            }
        } catch (Exception ex) {
            medipi.makeFatalErrorMessage("Fatal Error - Unable to display error message", ex);
        }
    }

    private String getDebugString(Exception ex) {
        if (ex == null) {
            return "";
        } else if (medipi.getDebugMode() == MediPi.DEBUG) {
            return "\n" + ex.toString();

        } else {
            return "";

        }
    }

    private static class MediPiHolder {

        private static final MediPiMessageBox INSTANCE = new MediPiMessageBox();
    }
}
