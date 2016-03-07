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
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleBooleanProperty;
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
import org.medipi.utilities.ConfigurationStringTokeniser;
import org.medipi.DashboardTile;
import org.medipi.MediPi;
import org.medipi.MediPiProperties;

/**
 * Class to display and manage a simple yes/no Questionnaire which will follow a
 * ruleset and depending on responses will direct a pathway through the
 * questionnaire.
 *
 * Ultimately the questionnaire ends when an advisory response is returned. The
 * results can transmitted. The transmittable data contains all the questions,
 * answers and advice given in plain text irrespective of ultimate advice
 *
 * There is no view mode for this UI.
 *
 * TODO does an alert message need to be sent only when particular outcomes are
 * reached? should this be automatic i.e. outside the normal transmit? what
 * should the message contain?
 *
 * @author rick@robinsonhq.com
 */
public class Questionnaire extends Device {

    private static final String PROFILEID = "urn:nhs-en:profile:Questionnaire";
    private static final String NAME = "Questionnaire";
    private static final String ADVICE_TO_PATIENT = "ADVICE_TO_PATIENT";
    private final HashMap<String, String> responses = new HashMap<>();
    private final HashMap<String, String> questions = new HashMap<>();
    private final HashMap<String, String[]> questionnaire = new HashMap<>();
    private final ArrayList<String[]> data = new ArrayList<>();
    // property to indicate whether data has bee recorded for this device
    private final BooleanProperty hasData = new SimpleBooleanProperty(false);
    private VBox questionnaireWindow;
    private VBox questionList;
    private Label responseLabel;
    private Button yes;
    private Button no;
    private Button startButton;
    private String firstRuleName = null;
    private String questionSet;
    private Label question = new Label("");
    private String name;
    private Date downloadTimestamp = null;
    private String questionnaireVersion = null;

    /**
     * Constructor for Generic Questionnaire
     *
     */
    public Questionnaire() {

    }

    /**
     * Initiation method called for this Element.
     *
     * Successful initiation of the this class results in a null return. Any
     * other response indicates a failure with the returned content being a
     * reason for the failure
     *
     * @return populated or null for whether the initiation was successful
     * @throws java.lang.Exception
     */
    @Override
    public String init() throws Exception {

        String uniqueDeviceName = getClassTokenName();
        // The device is dynamically named from the configuration file as there 
        // are different questionnaire rulsets which could be applied
        name = MediPiProperties.getInstance().getProperties().getProperty(MediPi.ELEMENTNAMESPACESTEM + uniqueDeviceName + ".title");
        if (name == null || name.trim().length() == 0) {
            throw new Exception("The Questionnaire doesn't have a title name");
        }
        // Scrollable main window
        questionnaireWindow = new VBox();
        questionnaireWindow.setPadding(new Insets(0, 5, 0, 5));
        questionnaireWindow.setSpacing(5);
        questionnaireWindow.setMinHeight(350);
        questionnaireWindow.setMaxHeight(350);
        questionList = new VBox();
        questionList.setId("questionnaire-questionpanel");
        ScrollPane questionSP = new ScrollPane();
        questionSP.setContent(questionList);
        questionSP.setFitToWidth(true);
        questionSP.setFitToHeight(true);
        questionSP.setMinHeight(170);
        questionSP.setMaxHeight(170);
        questionSP.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        questionSP.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        // Make sure that the latest question is always in view
        DoubleProperty wProperty = new SimpleDoubleProperty();
        // bind to Vbox width chnages
        wProperty.bind(questionList.heightProperty());
        wProperty.addListener(new ChangeListener() {
            @Override
            public void changed(ObservableValue ov, Object t, Object t1) {
                //when ever Vbox width chnages set ScrollPane Hvalue
                questionSP.setVvalue(questionSP.getVmax());
            }
        });

        // Scrollable result window
        ScrollPane listSP = new ScrollPane();
        responseLabel = new Label("");
        responseLabel.setId("questionnaire-responsepanel");
        responseLabel.setWrapText(true);
        listSP.setContent(responseLabel);
        listSP.setFitToWidth(true);
        listSP.setMinHeight(60);
        listSP.setMaxHeight(60);
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

        // set main Element window
        window.setCenter(questionnaireWindow);

        // load the questionnaire ruleset
        questionSet = MediPiProperties.getInstance().getProperties().getProperty(MediPi.ELEMENTNAMESPACESTEM + uniqueDeviceName + ".questions");
        if (questionSet == null || !questionSet.contains(".questions")) {
            throw new Exception("Cannot find Question Ruleset");
        }
        loadRules(questionSet);
        startButton.setOnAction((ActionEvent t) -> {
            resetDevice();
            execute(firstRuleName);
        });

        hasData.bind(responseLabel.textProperty().isNotEmpty());

        // successful initiation of the this class results in a null return
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
        question.setText("");
        responseLabel.setText("");
        questionList.getChildren().clear();
        downloadTimestamp = null;
        data.clear();
    }

    // This is a recursive method used for each line of the questionnaire
    // - presenting the UI with a yes/no choice and following a separate 
    // question path depending upon the answers given
    private void execute(String ruleName) {
        final int QUESTION = 0;
        final int TRUE_RESPONSE = 1;
        final int FALSE_RESPONSE = 2;
        // handle buttons for when being run as part of a schedule 
        if (isSchedule.get()) {
            centreButton.setDisable(true);
            leftButton.setDisable(true);
        }
        String[] rule = questionnaire.get(ruleName);
        yes = new Button("Yes");
        yes.setId("questionnaire-button-yes");
        no = new Button("No");
        no.setId("questionnaire-button-no");

        //actions for clicking "yes"
        yes.setOnAction((ActionEvent t) -> {
            // add data to data arraylist for transmission later
            data.add(new String[]{questions.get(rule[QUESTION]), yes.getText()});
            yes.setDisable(true);
            no.setDisable(true);
            no.setVisible(false);
            String response = responses.get(rule[TRUE_RESPONSE]);
            if (response != null) {
                // add data to data arraylist for transmission later
                data.add(new String[]{response, ADVICE_TO_PATIENT});
                responseLabel.setText(response);
                if (isSchedule.get()) {
                    centreButton.setDisable(false);
                    leftButton.setDisable(false);
                }
                // take the time of downloading the data
                downloadTimestamp = new Date();
            } else {
                // if there is no utimate advice to be given as a result of
                // this question recursively execute the subsequent question(s)
                execute(rule[TRUE_RESPONSE]);
            }
        });

        //actions for clicking "no"
        no.setOnAction((ActionEvent t) -> {
            // add data to data arraylist for transmission later
            data.add(new String[]{questions.get(rule[QUESTION]), no.getText()});
            yes.setDisable(true);
            yes.setVisible(false);
            no.setDisable(true);
            String response = responses.get(rule[FALSE_RESPONSE]);
            if (response != null) {
                // add data to data arraylist for transmission later
                data.add(new String[]{response, ADVICE_TO_PATIENT});
                responseLabel.setText(response);
                if (isSchedule.get()) {
                    centreButton.setDisable(false);
                    leftButton.setDisable(false);
                }
                // take the time of downloading the data
                downloadTimestamp = new Date();
            } else {
                // if there is no utimate advice to be given as a result of
                // this question recursively execute the subsequent question(s)
                execute(rule[FALSE_RESPONSE]);
            }
        });
        question = new Label(questions.get(rule[QUESTION]));
        HBox questionLine = new HBox();
        questionLine.setPadding(new Insets(5, 5, 5, 5));
        questionLine.setSpacing(5);
        questionLine.setAlignment(Pos.CENTER);
        questionLine.setId("questionnaire-questionpanel");
        questionLine.getChildren().addAll(
                question,
                yes,
                no
        );
        questionList.getChildren().add(questionLine);

    }

    // Read ruleset line by line and depending on type of rule call appropriate methods
    private void loadRules(String filename) throws Exception {
        final int NOTHING = -1;
        final int VERSION = 0;
        final int QUESTIONS = 1;
        final int RESPONSES = 2;
        final int QUESTIONNAIRE = 3;
        try (BufferedReader br = new BufferedReader(new FileReader(filename))) {
            int readingWhat = -1;
            String line = null;
            while ((line = br.readLine()) != null) {
                if (line.trim().startsWith("#")) {
                    continue;
                }
                switch (readingWhat) {
                    // reads version of the questionnaire ruleset
                    case VERSION:
                        if (line.contentEquals("END VERSION")) {
                            readingWhat = NOTHING;
                        } else {
                            questionnaireVersion = line;
                        }
                        break;
                    // defines each yes/no question to be asked. Consists of a questionId and question text
                    case QUESTIONS:
                        if (line.contentEquals("END QUESTIONS")) {
                            readingWhat = NOTHING;
                        } else {
                            addQuestion(line);
                        }
                        break;
                    // defines each possible ultimate response. Consists of a responseId and response text
                    case RESPONSES:
                        if (line.contentEquals("END RESPONSES")) {
                            readingWhat = NOTHING;
                        } else {
                            addResponse(line);
                        }
                        break;
                    // rules that define how the QUESTIONS and RESPONSES relate to each other. 
                    // Consists of a ruleId, an IF statement, a responseId if TRUE and a responseId if FALSE
                    case QUESTIONNAIRE:
                        if (line.contentEquals("END QUESTIONNAIRE")) {
                            readingWhat = NOTHING;
                        } else {
                            addQuestionnaire(line);
                        }
                        break;

                    case NOTHING:
                    default:
                        if (line.contentEquals("BEGIN VERSION")) {
                            readingWhat = VERSION;
                            continue;
                        }
                        if (line.contentEquals("BEGIN QUESTIONS")) {
                            readingWhat = QUESTIONS;
                            continue;
                        }
                        if (line.contentEquals("BEGIN RESPONSES")) {
                            readingWhat = RESPONSES;
                            continue;
                        }
                        if (line.contentEquals("BEGIN QUESTIONNAIRE")) {
                            readingWhat = QUESTIONNAIRE;
                            continue;
                        }
                        break;

                }
            }
            if (questionnaireVersion == null) {
                throw new Exception(name + " Ruleset has no version number - unsafe to continue");
            }
        }
    }

    // Parses the QUESTIONNAIRE part of the ruleset. Each QUESTIONNAIRE consists
    // of a ruleId, an IF statement, a responseId if TRUE and a responseId if FALSE
    private void addQuestionnaire(String line) throws Exception {
        ConfigurationStringTokeniser st = new ConfigurationStringTokeniser(line);
        if (st.countTokens() != 7) {
            throw new Exception("Syntax error in " + questionSet + " defining questionnaire: " + line);
        }
        String ruleName = st.nextToken();
        if (firstRuleName == null) {
            firstRuleName = ruleName;
        }
        String[] rule = new String[3];
        if (st.nextToken().equals("IF")) {
            rule[0] = st.nextToken();
            if (st.nextToken().equals("TRUE")) {
                rule[1] = st.nextToken();
                if (st.nextToken().equals("FALSE")) {
                    rule[2] = st.nextToken();
                } else {
                    throw new Exception("Syntax error in " + questionSet + " next element should start with FALSE: " + line);
                }

            } else {
                throw new Exception("Syntax error in " + questionSet + " next element should start with TRUE: " + line);
            }
        } else {
            throw new Exception("Syntax error in " + questionSet + " should start with IF: " + line);
        }
        questionnaire.put(ruleName, rule);
    }

    // Parses the RESPONSES part of the ruleset. 
    // Each RESPONSES consists of a responseId and response text
    private void addResponse(String line)
            throws Exception {
        ConfigurationStringTokeniser st = new ConfigurationStringTokeniser(line);
        if (st.countTokens() <= 2) {
            throw new Exception("Syntax error in " + questionSet + " defining response: " + line);
        }
        String ruleName = st.nextToken();
        StringBuilder text = new StringBuilder();
        while (st.hasMoreTokens()) {
            text.append(st.nextToken());
            if (st.hasMoreTokens()) {
                text.append(" ");
            }
        }
        responses.put(ruleName, text.toString());
    }

    // Parses the QUESTIONS part of the ruleset. 
    // Each QUESTIONS consists of a questionId and question text
    private void addQuestion(String line)
            throws Exception {
        ConfigurationStringTokeniser st = new ConfigurationStringTokeniser(line);
        if (st.countTokens() <= 2) {
            throw new Exception("Syntax error in " + questionSet + " defining question: " + line);
        }
        String ruleName = st.nextToken();
        StringBuilder text = new StringBuilder();
        while (st.hasMoreTokens()) {
            text.append(st.nextToken());
            if (st.hasMoreTokens()) {
                text.append(" ");
            }
        }
        questions.put(ruleName, text.toString());
    }

    /**
     * Method which returns a booleanProperty which UI elements can be bound to,
     * to discover whether there is data to be downloaded
     *
     * @return BooleanProperty signalling the presence of downloaded data
     */
    @Override
    public BooleanProperty hasDataProperty() {
        return hasData;
    }

    /**
     * Gets a csv representation of the data
     *
     * @return csv string of each value set of data points
     */
    @Override
    public String getData() {
        StringBuilder sb = new StringBuilder();
        //Add MetaData
        sb.append("metadata:medipiversion:").append(medipi.getVersion()).append("\n");
        sb.append("metadata:patientname:").append(medipi.getPatientLastName()).append(",").append(medipi.getPatientFirstName()).append("\n");
        sb.append("metadata:patientdob:").append(medipi.getPatientDOB()).append("\n");
        sb.append("metadata:patientnhsnumber:").append(medipi.getPatientNHSNumber()).append("\n");
        sb.append("metadata:timedownloaded:").append(downloadTimestamp).append("\n");
        sb.append("metadata:device:").append(getName()).append("\n");
        sb.append("metadata:format:").append("question").append(medipi.getDataSeparator()).append("answer").append("\n");
        sb.append("metadata:questionnaireversion:").append(name).append("\n");
        // Add Downloaded data
        for (String[] s : data) {
            sb.append(s[0]);
            sb.append(medipi.getDataSeparator());
            sb.append(s[1]);
            sb.append("\n");
        }
        return sb.toString();
    }

    @Override
    public BorderPane getDashboardTile() throws Exception {
        DashboardTile dashComponent = new DashboardTile(this);
        dashComponent.addTitle(getName());
        return dashComponent.getTile();
    }

}
