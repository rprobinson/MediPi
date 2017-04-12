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
package org.medipi.devices;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;
import javafx.event.ActionEvent;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import org.medipi.utilities.ConfigurationStringTokeniser;
import org.medipi.MediPiProperties;

/**
 * This is a class to encapsulate the Guide for any Element. This class reads a
 * flat-text ruleset and constructs a guide object from it using referenced
 * images and text with forward and backward buttons. Each "screen" in the guide
 * has one image and some accompanying text.
 *
 *
 * The guide ruleset is of the format:
 *
 * file_location_of_image text_for_use_with_image
 *
 * @author rick@robinsonhq.com
 */
public class Guide {

    public static final int RESET = 0;
    public static final int INCREMENT = 1;
    public static final int DECREMENT = -1;
    private final String guideSet;
    private final ArrayList<Node[]> instructions = new ArrayList<>();
    private int instructionNo;
    private Button forward;
    private Button back;
    private HBox instructionHBox;

    /**
     * Constructor to configure the guide ruleset
     *
     * @param guideNamespace element class token - defines which ruleset to be
     * called
     * @throws Exception which is ultimately passed to the instantiation routine
     * and reported in MediPi class
     */
    public Guide(String guideNamespace) throws Exception {
        guideSet = MediPiProperties.getInstance().getProperties().getProperty(guideNamespace);
        if (guideSet == null) {
            throw new Exception("Cannot find Guide Ruleset: " + guideNamespace);
        }
        loadRules(guideSet);
    }

    // Method to load from the ruleset file location the ruleset defining the guide
    private void loadRules(String filename)
            throws Exception {
        try (BufferedReader br = new BufferedReader(new FileReader(filename))) {
            String line = null;
            while ((line = br.readLine()) != null) {
                if (line.trim().startsWith("#")) {
                    continue;
                }
                // any line with fewer than 2 tokens is invalid
                ConfigurationStringTokeniser st = new ConfigurationStringTokeniser(line);
                if (st.countTokens() < 2) {
                    throw new Exception("Syntax error in " + guideSet + " defining Instruction: " + line);
                }
                //Each Guide node has 2 elements - an image and a text display
                Node n[] = new Node[2];
                String imageFile = st.nextToken();
                ImageView image;
                try {
                    image = new ImageView("file:///" + imageFile);
                    image.setFitHeight(250);
                    image.setFitWidth(230);
                    image.setId("guide-image");
                    n[0] = image;
                } catch (Exception e) {
                    throw new Exception("Cant find image for guide element in: " + imageFile);
                }
                StringBuilder text = new StringBuilder();
                while (st.hasMoreTokens()) {
                    text.append(st.nextToken());
                    if (st.hasMoreTokens()) {
                        text.append(" ");
                    }
                }
                Label l = new Label(text.toString());
                l.setId("guide-text");
                l.setWrapText(true);
                l.setPrefWidth(250);
                l.setMinHeight(248);
                ScrollPane dashTileSc = new ScrollPane();
                dashTileSc.setContent(l);
                dashTileSc.setFitToWidth(true);
                dashTileSc.setMinHeight(250);
                dashTileSc.setMaxHeight(500);
                dashTileSc.setId("guide-scrollpane");
                dashTileSc.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
                dashTileSc.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
                n[1] = dashTileSc;
                instructions.add(n);

            }
        }
    }

    /**
     * Method to return a VBox node containing the guide constructed from the
     * guide ruleset
     *
     * @return VBox node to be inserted into the Element window
     */
    public VBox getGuide() {
        VBox guideVBox = new VBox();
        guideVBox.setPadding(new Insets(0, 5, 0, 5));
        guideVBox.setAlignment(Pos.CENTER_LEFT);
        guideVBox.setMinHeight(300);
        HBox guideHBox = new HBox();
        guideHBox.setPadding(new Insets(0, 5, 0, 5));
        guideHBox.setAlignment(Pos.CENTER);
        instructionNo = 0;
        Node n[] = instructions.get(instructionNo);
        instructionHBox = new HBox();
        instructionHBox.setPadding(new Insets(0, 5, 0, 5));
        instructionHBox.setSpacing(10);
        instructionHBox.setMinHeight(250);
        instructionHBox.setAlignment(Pos.CENTER);
        instructionHBox.getChildren().addAll(
                n[0],
                n[1]
        );
        //add the forward and back buttons
        back = new Button("<");
        back.setMinHeight(250);
        back.setMinWidth(50);
        back.setId("guide-button-back");
        forward = new Button(">");
        forward.setMinHeight(250);
        forward.setMinWidth(50);
        forward.setId("guide-button-forward");
        back.setDisable(true);
        guideHBox.getChildren().addAll(
                back,
                instructionHBox,
                forward
        );
        guideVBox.getChildren().addAll(
                guideHBox
        );

        if (instructions.size() == 1) {
            forward.setDisable(true);
            back.setDisable(true);
        }
        //Add functionality to forward button
        forward.setOnAction((ActionEvent t) -> {
            changeInstruction(INCREMENT);
        });
        //Add functionality to back button
        back.setOnAction((ActionEvent t) -> {
            changeInstruction(DECREMENT);
        });
        return guideVBox;
    }

    private void changeInstruction(int change) {
        if (instructionHBox == null) {
            return;
        }
        switch (change) {
            case -1:
                instructionNo--;
                break;
            case 0:
                instructionNo = 0;
                break;
            case 1:
                instructionNo++;
                break;
            default:
                return;
        }

        instructionHBox.getChildren().clear();
        Node[] n1 = instructions.get(instructionNo);
        instructionHBox.getChildren().addAll(n1[0], n1[1]);
        if (instructionNo == instructions.size() - 1) {
            forward.setDisable(true);
        } else {
            forward.setDisable(false);
        }
        if (instructionNo == 0) {
            back.setDisable(true);
        } else {
            back.setDisable(false);
        }
    }

    public void reset() {
        changeInstruction(RESET);
    }

}
