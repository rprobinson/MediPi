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
package org.medipi;

import java.util.HashMap;
import javafx.application.Platform;
import javafx.geometry.Pos;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextArea;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import javafx.util.Callback;
import org.medipi.logging.MediPiLogger;

/**
 * Singleton Class to deliver alert messages to MediPi
 *
 * The alert class keeps track of any messages which are currently open (i.e.
 * not closed down and will not attempt to add any further alerts of the same
 * type to prevent multiple messages if a connection goes down
 *
 * @author rick@robinsonhq.com
 */
public class MediPiMessageBox {

    private MediPi medipi;
    private HashMap<String, Alert> liveMessages = new HashMap<>();

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
     * Method for displaying an error messagebox to the screen.
     *
     * @param message String message
     * @param except exception which this message arose from - null if not
     * applicable
     */
    public void makeErrorMessage(String message, Exception except) {
        try {
            String uniqueString;
            if (except == null) {
                uniqueString = message;
            } else {
                uniqueString = message + except.getLocalizedMessage();
            }
            String debugString = getDebugString(except);

            MediPiLogger.getInstance().log(MediPiMessageBox.class.getName() + ".makeErrorMessage", "MediPi informed the user that: " + message + " " + except);
            if (except != null) {
                except.printStackTrace();
            }

            if (Platform.isFxApplicationThread()) {
                AlertBox ab = new AlertBox();
                ab.showAlertBox(uniqueString, message, debugString, liveMessages, medipi);
            } else {
                Platform.runLater(() -> {
                    AlertBox ab = new AlertBox();
                    ab.showAlertBox(uniqueString, message, debugString, liveMessages, medipi);
                });
            }
        } catch (Exception ex) {
            medipi.makeFatalErrorMessage("Fatal Error - Unable to display error message", ex);
        }
    }

    /**
     * Method to make a general information message
     *
     * @param message String representation of the message to be displayed
     */
    public void makeMessage(String message) {
        try {
            String uniqueString = message;

            MediPiLogger.getInstance().log(MediPiMessageBox.class.getName() + ".makeMessage", "MediPi informed the user that: " + message);
            if (Platform.isFxApplicationThread()) {
                AlertBox ab = new AlertBox();
                ab.showMessageBox(uniqueString, message, liveMessages, medipi);
            } else {
                Platform.runLater(() -> {
                    AlertBox ab = new AlertBox();
                    ab.showMessageBox(uniqueString, message, liveMessages, medipi);
                });
            }
        } catch (Exception ex) {
            medipi.makeFatalErrorMessage("Fatal Error - Unable to display error message", ex);
        }
    }

    private String getDebugString(Exception ex) {
        if (ex == null) {
            return "";
        } else {
            return "\n" + ex.toString();
        }
    }

    private static class MediPiHolder {

        private static final MediPiMessageBox INSTANCE = new MediPiMessageBox();
    }
}

class AlertBox {

    public void showAlertBox(String uniqueString, String message, String debugString, HashMap<String, Alert> liveMessages, MediPi medipi) {
        Alert a = liveMessages.get(uniqueString);
        if (a == null) {
            a = new Alert(AlertType.ERROR);
            a.getDialogPane().getStylesheets().add("file:///" + medipi.getCssfile());
            a.getDialogPane().setId("message-box");
            a.setHeaderText(null);
            a.getDialogPane().setMaxSize(600, 300);
            VBox vb = new VBox();
            a.getDialogPane().setContent(vb);
            TextArea text = new TextArea("Error - " + message + debugString);
            text.setMaxWidth(400);
            text.setWrapText(true);
            text.setEditable(false);
            vb.getChildren().add(text);
            vb.setAlignment(Pos.CENTER);
            a.getDialogPane().setContent(vb);
            a.setTitle("Error dialog");
//            a.setContentText("Error - " + message + debugString);
//            a.getDialogPane().getChildren().stream().filter(node -> node instanceof Label).forEach(node -> ((Label) node).setPrefHeight(300));
            liveMessages.put(uniqueString, a);
            a.setResultConverter(new Callback<ButtonType, ButtonType>() {
                @Override
                public ButtonType call(ButtonType param) {
                    if (param == ButtonType.OK) {
                        liveMessages.remove(uniqueString);
                    }
                    return null;
                }

            });
            a.showAndWait();
        } else {
        }
    }

    public void showMessageBox(String uniqueString, String message, HashMap<String, Alert> liveMessages, MediPi medipi) {
        Alert a = liveMessages.get(uniqueString);
        if (a == null) {
            a = new Alert(AlertType.INFORMATION);
            a.getDialogPane().getStylesheets().add("file:///" + medipi.getCssfile());
            a.getDialogPane().setId("message-box");
            a.setHeaderText(null);
            a.getDialogPane().setMaxSize(600, 300);
            VBox vb = new VBox();
            Text text = new Text(message);
            text.setTextAlignment(TextAlignment.CENTER);
            text.setWrappingWidth(600);
            vb.getChildren().add(text);
            vb.setAlignment(Pos.CENTER);
            a.getDialogPane().setContent(vb);
//           a.getDialogPane().getChildren().stream().filter(node -> node instanceof Label).forEach(node -> ((Label) node).setPrefHeight(300));
            a.setTitle("Message dialog");
            ImageView iw = medipi.utils.getImageView("medipi.images.doctor", 80, 80);
            a.setGraphic(iw);
            liveMessages.put(uniqueString, a);
            a.setResultConverter(new Callback<ButtonType, ButtonType>() {
                @Override
                public ButtonType call(ButtonType param) {
                    if (param == ButtonType.OK) {
                        liveMessages.remove(uniqueString);
                    }
                    return null;
                }

            });
            a.showAndWait();
        } else {
        }
    }
}
