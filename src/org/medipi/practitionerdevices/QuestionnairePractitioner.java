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
package org.medipi.practitionerdevices;

import org.medipi.devices.Device;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.regex.Pattern;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Separator;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import org.warlock.itk.distributionenvelope.DistributionEnvelope;
import org.warlock.itk.distributionenvelope.DistributionEnvelopeHelper;
import org.warlock.itk.distributionenvelope.Payload;
import org.medipi.DashboardTile;
import org.medipi.MediPi;
import org.medipi.MediPiMessageBox;
import org.medipi.MediPiProperties;

/**
 * Class to display a simple yes/no Questionnaire taken from an incoming
 * DistributionEnvelope
 *
 * This is a quick implementation of a system that will display results from a
 * incoming file containing a DistributionEnvelope. When combined with a host
 * listening for incoming messages from MediPi and writing then to disk (e.g.
 * TKW) it can be used to demonstrate that the same data which has been sent is
 * being received. This is obviously not a solution for a production receiver! *
 * There is no view mode for this UI.
 *
 * @author rick@robinsonhq.com
 */
public class QuestionnairePractitioner extends Device {

    private static final String MESSAGE_DIR = "medipi.executionmode.practitioner.incomingmessagedir";
    private static final String ADVICE_TO_PATIENT = "ADVICE_TO_PATIENT";
    private static final String PROFILEID = "urn:nhs-en:profile:Questionnaire";
    private static final String NAME = "Questionnaire";
    private VBox questionnaireWindow;
    private VBox questionList;
    private Label responseLabel;
    private Button startButton;
    private String name;

    /**
     * Constructor for Generic Questionnaire
     *
     */
    public QuestionnairePractitioner() {

    }

    @Override
    public String init() throws Exception {

        String uniqueDeviceName = getClassTokenName();
        name = MediPiProperties.getInstance().getProperties().getProperty(MediPi.ELEMENTNAMESPACESTEM + uniqueDeviceName + ".title");
        if (name == null || name.trim().length() == 0) {
            throw new Exception("The Questionnaire doesn't have a title name");
        }
        questionnaireWindow = new VBox();
        questionnaireWindow.setPadding(new Insets(5, 5, 5, 5));
        questionnaireWindow.setSpacing(5);
        questionnaireWindow.setAlignment(Pos.CENTER);
        questionnaireWindow.setMinHeight(400);
        questionnaireWindow.setMaxHeight(400);
        questionList = new VBox();
        questionList.setId("questionnaire-questionpanel");
//        questionList.setPadding(new Insets(5, 5, 5, 5));
//        questionList.setSpacing(5);
//        questionList.setAlignment(Pos.CENTER);
        ScrollPane questionSP = new ScrollPane();
        questionSP.setContent(questionList);
        questionSP.setFitToWidth(true);
        questionSP.setFitToHeight(true);
        questionSP.setPrefHeight(120);
        questionSP.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        questionSP.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        // Make sure that the latest question is always in view
        DoubleProperty wProperty = new SimpleDoubleProperty();
        wProperty.bind(questionList.heightProperty()); // bind to Vbox width chnages
        wProperty.addListener(new ChangeListener() {
            @Override
            public void changed(ObservableValue ov, Object t, Object t1) {
                //when ever Vbox width chnages set ScrollPane Hvalue
                questionSP.setVvalue(questionSP.getVmax());
            }
        });
        ScrollPane listSP = new ScrollPane();
        responseLabel = new Label("");
        responseLabel.setId("questionnaire-responsepanel");
        responseLabel.setWrapText(true);
        listSP.setContent(responseLabel);
        listSP.setFitToWidth(true);
//        listSP.setFitToHeight(true);
        listSP.setPrefHeight(80);
        listSP.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        listSP.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        startButton = new Button("Start Questionnaire");
        startButton.setId("questionnaire-button-start");
        HBox buttonHbox = new HBox();
        buttonHbox.setPadding(new Insets(5, 5, 5, 5));
        buttonHbox.setSpacing(10);
        buttonHbox.setAlignment(Pos.BASELINE_LEFT);
        Label nameLabel = new Label(name);
        nameLabel.setId("questionnaire-title-label");
        buttonHbox.getChildren().addAll(
                nameLabel,
                startButton);
        questionnaireWindow.setAlignment(Pos.TOP_LEFT);
        Label responseTitleLabel = new Label("Action to take:");
        responseTitleLabel.setId("questionnaire-responsepanel");
        questionnaireWindow.getChildren().addAll(
                buttonHbox,
                questionSP,
                new Separator(Orientation.HORIZONTAL),
                responseTitleLabel,
                listSP,
                new Separator(Orientation.HORIZONTAL)
        );

        window.setCenter(questionnaireWindow);

        startButton.setOnAction((ActionEvent t) -> {
            resetDevice();
            execute();
        });

        responseLabel.visibleProperty().bind(responseLabel.textProperty().isNotEmpty());

        return null;
    }

    @Override
    public String getProfileId() {
        return PROFILEID;
    }

    @Override
    public String getType() {
        return NAME;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public void resetDevice() {
        responseLabel.setText("");
        questionList.getChildren().clear();
    }

    private void execute() {
        try {
            String itkTrunkDir = MediPiProperties.getInstance().getProperties().getProperty(MESSAGE_DIR);
            File file = lastFileModified(itkTrunkDir);
            FileInputStream fis = new FileInputStream(file);
            StringBuilder builder = new StringBuilder();
            int ch;
            while ((ch = fis.read()) != -1) {
                builder.append((char) ch);
            }
            DistributionEnvelopeHelper helper = DistributionEnvelopeHelper.getInstance();
            DistributionEnvelope d = helper.getDistributionEnvelope(builder.toString());
            Payload[] p = helper.getPayloads(d);
            for (Payload pay : p) {
                if (medipi.getDebugMode() == MediPi.DEBUG) {
                    System.out.println(pay.getContent());
                }
                if (pay.getProfileId().equals(PROFILEID)) {
                    BufferedReader br = new BufferedReader(new InputStreamReader(new ByteArrayInputStream(pay.getContent().getBytes())));
                    String line;
                    while ((line = br.readLine()) != null) {
                        if (line.startsWith("metadata:")) {
                            continue;
                        }
                        String incomingData[] = line.split(Pattern.quote(medipi.getDataSeparator()));
                        if (incomingData.length != 2) {
                            throw new Exception("incoming data separtaor has not been recognised or the seprataor has been included as part of the data");
                        }
                        if (!incomingData[1].equals(ADVICE_TO_PATIENT)) {
                            Button dataButton = new Button(incomingData[1]);
                            dataButton.setDisable(true);
                            HBox questionLine = new HBox();
                            questionLine.setPadding(new Insets(5, 5, 5, 5));
                            questionLine.setSpacing(5);
                            questionLine.setAlignment(Pos.CENTER);
                            questionLine.setId("questionnaire-questionpanel");
                            questionLine.getChildren().addAll(
                                    new Label(incomingData[0]),
                                    dataButton
                            );
                            questionList.getChildren().add(questionLine);
                        } else {
                            responseLabel.setText(incomingData[0]);
                        }

                    }
                }
            }
        } catch (Exception ex) {
            MediPiMessageBox.getInstance().makeErrorMessage("Cannot read the Questionnaire data", ex, Thread.currentThread());
        }
    }

    private static File lastFileModified(String dir) {
        File fl = new File(dir);
        File[] files = fl.listFiles(File::isFile);
        long lastMod = Long.MIN_VALUE;
        File choice = null;
        for (File file : files) {
            if (file.lastModified() > lastMod) {
                choice = file;
                lastMod = file.lastModified();
            }
        }
        return choice;
    }

    @Override
    public BorderPane getDashboardTile() throws Exception {
        DashboardTile dashComponent = new DashboardTile(this);
        dashComponent.addTitle(getName());
        return dashComponent.getTile();
    }

    @Override
    public BooleanProperty hasDataProperty() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public String getData() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

}
