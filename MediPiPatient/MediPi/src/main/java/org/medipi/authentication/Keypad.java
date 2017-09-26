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
package org.medipi.authentication;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.Optional;
import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.TilePane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import org.medipi.MediPiProperties;

/**
 * This is a class to establish authentication for using MediPi.
 *
 * Patient Mode Authentication: The interface is a keypad which takes a
 * configurable number of digits. Use the passcode to unlock the jks. The jks
 * password is made from the digits of the passcode alternately padded with the
 * complement of the number inputted. e.g a passcode of 1234 would become a
 * password on the jks of 18273645. This has been done in order to allow
 * passcodes which are less than 6 digits. The minimum length of a jks password
 * is 6.
 *
 * Admin Mode Authentication: This password is also used to unlock a jdk but as
 * the admin jdk is expected to be universal the password is expected to be
 * longer and therfore is not padded as before. This is a separate login mode
 * designed to be used for admin purposes: update/input of patient details,
 * bluetooth pairing, bluetooth MAC address. It closes MediPi Patient with a
 * specific exit code which can then be acted upon by the calling script e.g. to
 * call MediPi Patient with an Admin configuration
 *
 * @author rick@robinsonhq.com
 */
public class Keypad implements AuthenticationInterface {

    private static final String MEDIPIPATIENTCERTLOCATION = "medipi.patient.cert.location";
    private static final String MEDIPIADMINCERTLOCATION = "medipi.admin.cert.location";
    private static final int ADMINEXITCODE = 0; // dunno what number this should be?? Does it matter?
    VBox mainWindow = new VBox();
    String[] buttonList = new String[]{"1", "2", "3", "4", "5", "6", "7", "8", "9", "Admin", "0", "Cancel"};
    ArrayList<Label> output = new ArrayList<>();
    int keypadWidth = 200;
    final int passcodeLength = 4;
    int currentInputDigit = 0;
    final int adminPasscodeLength = 10;
    Integer[] passDigits = new Integer[adminPasscodeLength];
    boolean configMode = false;
    private final byte[] patientJDKFile;
    private final byte[] adminJDKFile;
    private BooleanProperty disableDigits = new SimpleBooleanProperty(false);
    private TilePane display = new TilePane();

    /**
     * Constructor creating the keypad interface. The MediPiwindow is passed in
     * to allow Keypad to unlock the window.
     *
     * TODO: lock out or delay after x attempts?
     *
     * @param mediPiWindow
     * @throws java.lang.Exception
     */
    public Keypad(MediPiWindow mediPiWindow) throws Exception {
        String patientKSF = MediPiProperties.getInstance().getProperties().getProperty(MEDIPIPATIENTCERTLOCATION);

        patientJDKFile = Files.readAllBytes(Paths.get(patientKSF));
        String adminKSF = MediPiProperties.getInstance().getProperties().getProperty(MEDIPIADMINCERTLOCATION);

        adminJDKFile = Files.readAllBytes(Paths.get(adminKSF));
        mainWindow.setPrefSize(800, 420);
        mainWindow.setId("background-colour");
        mainWindow.setAlignment(Pos.TOP_CENTER);
        GridPane keypad = new GridPane();
        keypad.setId("keypad-keypad");
        keypad.setAlignment(Pos.CENTER);
        createNewOutputRow(passcodeLength);
        configMode = false;

        int x = 0;
        int y = 1;
        // Create the number pad
        for (String s : buttonList) {

            Button numBut = new Button(s);
            try {
                Integer.parseInt(numBut.getText());
                numBut.setId("keypad-button-large");
            } catch (NumberFormatException nfe) {
                numBut.setId("keypad-button");
            }
            numBut.setPrefSize(keypadWidth / 3, keypadWidth / 3);
            numBut.setOnMousePressed((MouseEvent event) -> {
                if (!numBut.getText().toLowerCase().startsWith("cancel")
                        && !numBut.getText().toLowerCase().startsWith("admin")
                        && !numBut.getText().toLowerCase().startsWith("patient")) {
                    Label l = output.get(currentInputDigit);
                    l.setText("*");
                }
            });
            numBut.setOnMouseClicked((MouseEvent event) -> {
                disableDigits.set(true);
                try {
                    int num = Integer.parseInt(numBut.getText());
                    passDigits[currentInputDigit] = num;
                    if (!configMode) {
                        if (currentInputDigit == passcodeLength - 1) {
                            disableDigits.set(true);
                            if (loadPatientJKS(passDigits)) {
                                mediPiWindow.unlock();
                                clearDisplay();
                            } else {
                                for (Label l : output) {
                                    l.setStyle("-fx-background-color: lightpink;");
                                }
                            }
                            return;
                        }
                    } else if (currentInputDigit == adminPasscodeLength - 1) {
                        if (loadAdminJKS(passDigits)) {
                            Alert alert = new Alert(AlertType.CONFIRMATION);
                            alert.setTitle("Configuration Mode");
                            alert.setHeaderText(null);
                            Text text = new Text("Would you like to reboot into Administration Mode?\nACCESS TO THE ADMINISTRATION MODE IS FOR AUTHORISED USERS ONLY.\nAny unauthorised user must click the 'CANCEL' button");
                            text.setWrappingWidth(600);
                            alert.getDialogPane().getStylesheets().add("file:///" + MediPiProperties.getInstance().getProperties().getProperty("medipi.css"));
                            alert.getDialogPane().setId("message-box");
                            alert.getDialogPane().setContent(text);
                            Optional<ButtonType> result = alert.showAndWait();
                            if (result.get() == ButtonType.OK) {
                                System.out.println("Config Unlock");
                                System.exit(ADMINEXITCODE);
                            } else {
                                clearDisplay();
                            }
                        } else {
                            for (Label l : output) {
                                l.setStyle("-fx-background-color: lightpink;");
                            }
                        }
                        return;
                    }
                    disableDigits.set(false);
                    currentInputDigit++;
                } catch (NumberFormatException nfe) {
                    if (numBut.getText().equals("Cancel")) {
                        clearDisplay();
                        for (Label l : output) {
                            l.setStyle("-fx-background-color: white;");
                        }
                    } else if (numBut.getText().equals("Admin")) {
                        configMode = true;
                        createNewOutputRow(adminPasscodeLength);
                        for (Node n : keypad.getChildren()) {
                            Button b = (Button) n;
                            if (b.getText().equals("Admin")) {
                                b.setText("Patient");
                                clearDisplay();
                            }
                        }
                    } else if (numBut.getText().equals("Patient")) {
                        configMode = false;
                        createNewOutputRow(passcodeLength);
                        for (Node n : keypad.getChildren()) {
                            Button b = (Button) n;
                            if (b.getText().equals("Patient")) {
                                b.setText("Admin");
                                clearDisplay();
                            }
                        }
                    }

                }
            });
            keypad.add(numBut, x, y);
            if (x < 2) {
                x++;
            } else {
                x = 0;
                y++;
            }
            try {
                int num = Integer.parseInt(numBut.getText());
                numBut.disableProperty().bind(disableDigits);
            } catch (NumberFormatException nfe) {
                //do nothing to the non digit buttons
            }

        }
        mainWindow.getChildren().addAll(
                display,
                keypad
        );
    }

    private void createNewOutputRow(int passLength) {
        display.getChildren().clear();
        display.setId("keypad-display");
        display.setAlignment(Pos.CENTER);
        display.setPrefWidth(keypadWidth);
        display.setPrefColumns(passLength);
        display.setPrefRows(1);
        // Create the display of the input numbers masked with *
        for (int i = 0; i < passLength; i++) {
            createNewOutputCell(i);
        }
    }

    private void createNewOutputCell(int i) {
        Label passDigitLabel = new Label("-");
        passDigitLabel.setId("keypad-output");
        passDigitLabel.setAlignment(Pos.CENTER);
        passDigitLabel.setPrefSize(keypadWidth / passcodeLength, keypadWidth / passcodeLength);
        display.getChildren().add(i, passDigitLabel);
        output.add(passDigitLabel);
    }

    private void clearDisplay() {
        disableDigits.set(false);
        Arrays.fill(passDigits, null);
        currentInputDigit = 0;
        output.clear();
        if (configMode) {
            createNewOutputRow(adminPasscodeLength);
        } else {
            createNewOutputRow(passcodeLength);
        }
        for (Label l : output) {
            l.setText("-");
        }
    }

    /**
     * Get the window for the authentication interface
     *
     * @return Node
     */
    @Override
    public Node getWindow() {
        return mainWindow;
    }

    // Use the passcode to unlock the patient jks. The patient jks password is made from the
    // digits of the passcode alternately padded with the complement of the number inputted
    private boolean loadPatientJKS(Integer[] passDigits) {
        char[] pass = new char[passcodeLength * 2];
        try {
            int loop = 0;
            for (Integer i : passDigits) {
                pass[loop] = Character.forDigit(i, 10);
                loop++;
                pass[loop] = Character.forDigit(9 - i, 10);
                loop++;
                if (loop == pass.length) {
                    break;
                }
            }

            KeyStore keyStore = KeyStore.getInstance("jks");
            InputStream jdkInputStream = new ByteArrayInputStream(patientJDKFile);
            keyStore.load(jdkInputStream, pass);
            // use a system property to save the certicicate name
            Enumeration<String> aliases = keyStore.aliases();
            // the keystore will only ever contain one key -so take the 1st one
            System.setProperty("medipi.patient.cert.name", aliases.nextElement());
            // Not sure if this is kosher and may changing in the future but store password in a system property in order that the message can later be signed
            System.setProperty("medipi.patient.cert.password", new String(pass));
        } catch (KeyStoreException | IOException | NoSuchAlgorithmException | CertificateException e) {
            System.err.println(e.toString());
            Arrays.fill(pass, (char) 0);
            return false;
        }
        Arrays.fill(pass, (char) 0);
        return true;
    }

    private boolean loadAdminJKS(Integer[] passDigits) {
        char[] pass = new char[adminPasscodeLength];
        try {
            int loop = 0;
            for (Integer i : passDigits) {
                pass[loop] = Character.forDigit(i, 10);
                loop++;
                if (loop == pass.length) {
                    break;
                }
            }

            KeyStore keyStore = KeyStore.getInstance("jks");
            InputStream jdkInputStream = new ByteArrayInputStream(adminJDKFile);
            keyStore.load(jdkInputStream, pass);
        } catch (KeyStoreException | IOException | NoSuchAlgorithmException | CertificateException e) {
            System.err.println(e.toString());
            Arrays.fill(pass, (char) 0);
            return false;
        }
        Arrays.fill(pass, (char) 0);
        return true;
    }

}
