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
package org.medipi.authentication;

import java.io.FileInputStream;
import java.io.IOException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.TilePane;
import javafx.scene.layout.VBox;
import org.medipi.MediPiProperties;

/**
 * This is a class to establish authentication for using MediPi.
 *
 * The interface is a keypad which takes a configurable number of digits. Use
 * the passcode to unlock the jks. The jks password is made from the digits of
 * the passcode alternately padded with the complement of the number inputted.
 * e.g a passcode of 1234 would become a password on the jks of 18273645. This
 * has been done in order to allow passcodes which are less than 6 digits. The
 * minimum length of a jks password is 6
 *
 * @author riro
 */
public class Keypad implements AuthenticationInterface {

    VBox mainWindow = new VBox();
    String[] buttonList = new String[]{"1", "2", "3", "4", "5", "6", "7", "8", "9", "Cancel", "0"};
    ArrayList<Label> output = new ArrayList<>();
    int keypadWidth = 200;
    final int passcodeLength = 4;
    int currentInputDigit = 0;
    Integer[] passDigits = new Integer[passcodeLength];

    /**
     * Constructor creating the keypad interface. The MediPiwindow is passed in
     * to allow Keypad to unlock the window.
     *
     * TODO: lock out or delay after x attempts. Add a close Medipi button? Add
     * a lock facility to the Settings tile?
     *
     * @param mw
     */
    public Keypad(MediPiWindow mw) {
        mainWindow.setPrefSize(800, 420);
        mainWindow.setId("keypad-mainwindow");
        mainWindow.setAlignment(Pos.TOP_CENTER);
        GridPane keypad = new GridPane();
        keypad.setId("keypad-keypad");
        keypad.setAlignment(Pos.CENTER);
        TilePane display = new TilePane();
        display.setId("keypad-display");
        display.setAlignment(Pos.CENTER);
        display.setPrefWidth(keypadWidth);
        display.setPrefColumns(passcodeLength);
        display.setPrefRows(1);
        // Create the display of the input numbers masked with *
        for (int i = 0; i < passcodeLength; i++) {
            Label passDigitLabel = new Label("-");
            passDigitLabel.setId("keypad-output");
            passDigitLabel.setAlignment(Pos.CENTER);
            passDigitLabel.setPrefSize(keypadWidth / passcodeLength, keypadWidth / passcodeLength);
            display.getChildren().add(i, passDigitLabel);
            output.add(passDigitLabel);
        }

        int x = 0;
        int y = 1;
        // Create the number pad
        for (String s : buttonList) {

            Button numBut = new Button(s);
            numBut.setId("keypad-button");
            numBut.setPrefSize(keypadWidth / 3, keypadWidth / 3);
            numBut.setOnMousePressed((MouseEvent event) -> {
                if (!numBut.getText().toLowerCase().startsWith("cancel")) {
                    Label l = output.get(currentInputDigit);
                    l.setText("*");
                }
            });
            numBut.setOnMouseClicked((MouseEvent t) -> {
                try {
                    int num = Integer.parseInt(numBut.getText());
                    passDigits[currentInputDigit] = num;
                    currentInputDigit++;
                    if (currentInputDigit >= passcodeLength) {
                        if (loadJKS(passDigits)) {
                            mw.unlock();
                        }
                        clearDisplay();
                    }
                } catch (NumberFormatException nfe) {
                    if (numBut.getText().equals("Cancel")) {
                        clearDisplay();
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
        }
        mainWindow.getChildren().addAll(
                display,
                keypad
        );
    }

    private void clearDisplay() {
        Arrays.fill(passDigits, null);
        currentInputDigit = 0;
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

    // Use the passcode to unlock the jks. The jks password is made from the
    // digits of the passcode alternately padded with the complement of the number inputted
    private boolean loadJKS(Integer[] passDigits) {
        char[] pass = new char[passcodeLength * 2];
        /*try {
            int loop = 0;
            for (Integer i : passDigits) {
                pass[loop] = Character.forDigit(i, 10);
                loop++;
                pass[loop] = Character.forDigit(9 - i, 10);
                loop++;

            }
            String ksf = MediPiProperties.getInstance().getProperties().getProperty("medipi.patient.cert.location");

            KeyStore keyStore = KeyStore.getInstance("jks");
            try (FileInputStream fis = new FileInputStream(ksf)) {
                keyStore.load(fis, pass);
                // use a system property to save the certicicate name
                Enumeration<String> aliases = keyStore.aliases();
                // the keystore will only ever contain one key -so take the 1st one
                System.setProperty("medipi.patient.cert.name", aliases.nextElement());
                // Not sure if this is kosher and may changing in the future but store password in a system property in order that the message can later be signed
                System.setProperty("medipi.patient.cert.password", new String(pass));
            }
        } catch (KeyStoreException | IOException | NoSuchAlgorithmException | CertificateException e) {
            System.err.println(e.toString());
            Arrays.fill(pass, (char) 0);
            return false;
        }*/
        System.setProperty("medipi.patient.cert.name", "patient.cert");
        Arrays.fill(pass, (char) 0);
        return true;
    }
}
