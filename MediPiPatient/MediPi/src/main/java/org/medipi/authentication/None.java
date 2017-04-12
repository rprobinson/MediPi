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

import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.VBox;

/**
 * This is a class to allow acces to MediPi without any authorisation
 *
 * The purpose of this calss is for the Administration mode when deployed as
 * part of the Hertfordshire pilot. On a raspberry pi device, MediPi is called
 * from the command line without calling startx, requires authorisation with a 4
 * digit pin and also allows clinicians to log in as admin for maintaining
 * bluetooth pairing, Patient deomgraphics, WIFI etc. When the admin mode is
 * called as a result of the previous authorisation, startx is called which
 * boots raspberry pi into the desktop environment. Here we want MediPi to open
 * with only settings showing and with authentication
 *
 * @author rick@robinsonhq.com
 */
public class None implements AuthenticationInterface {

    VBox mainWindow = new VBox();

    /**
     * Constructor creating the keypad interface. The MediPiwindow is passed in
     * to allow Keypad to unlock the window.
     *
     * TODO: lock out or delay after x attempts?
     *
     * @param mediPiWindow
     * @throws java.lang.Exception
     */
    public None(MediPiWindow mediPiWindow) throws Exception {
        mainWindow.setPrefSize(800, 420);
        mainWindow.setId("background-colour");
        mainWindow.setAlignment(Pos.CENTER);
        Button unlock = new Button("Administration Mode");
        unlock.setId("button-next");
        unlock.setOnMousePressed((MouseEvent event) -> {
            mediPiWindow.unlock();
        });
        mainWindow.getChildren().addAll(
                unlock
        );

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
}
