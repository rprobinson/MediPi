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

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.BufferedReader;
import java.io.FileReader;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.event.ActionEvent;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import org.medipi.utilities.ConfigurationStringTokeniser;
import org.medipi.DashboardTile;
import org.medipi.MediPi;
import org.medipi.MediPiProperties;
import org.medipi.model.DeviceDataDO;
import org.medipi.utilities.Utilities;

/**
 * Class to display and manage a simple yes/no Questionnaire which will follow a
 * ruleset and depending on responses will direct a pathway through the
 * questionnaire.
 *
 * Ultimately the questionnaire ends when an advisory response is returned. The
 * results can transmitted. The transmittable data contains all the questions,
 * answers and advice given in plain text irrespective of ultimate advice
 *
 * A questionnaire is ultimately judged on its outcome: green flag or red flag
 * status
 *
 * @author rick@robinsonhq.com
 */
public class Questionnaire extends Device {

    private static final String PROFILEID = "urn:nhs-en:profile:Questionnaire";
    private static final String NAME = "Questionnaire";
    private static final String MODEL = "NONE";
    private static final String GREEN_FLAG = "GREEN_FLAG";
    private static final String RED_FLAG = "RED_FLAG";
    private final HashMap<String, String[]> responses = new HashMap<>();
    private final HashMap<String, String> questions = new HashMap<>();
    private final HashMap<String, String[]> questionnaire = new HashMap<>();
    private QuestionnaireDO data = new QuestionnaireDO();
    private Instant schedStartTime = null;
    private Instant schedExpireTime = null;
    private final StringProperty resultsSummary = new SimpleStringProperty();
    private Instant dataTime;
    private VBox questionnaireWindow;
    private HBox questionLine = new HBox();
    private Label responseLabel;
    private Button yes;
    private Button no;
    private Button startButton;
    private String firstRuleName = null;
    private String questionSet;
    private Text question = new Text("");
    private String titleName;
    private String questionnaireVersion = null;
    private int questionNo = 0;

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
        titleName = medipi.getProperties().getProperty(MediPi.ELEMENTNAMESPACESTEM + uniqueDeviceName + ".title");
        if (titleName == null || titleName.trim().length() == 0) {
            throw new Exception("The Questionnaire doesn't have a title name");
        }
        //ascertain if this element is to be displayed on the dashboard
        String b = MediPiProperties.getInstance().getProperties().getProperty(MediPi.ELEMENTNAMESPACESTEM + uniqueDeviceName + ".showdashboardtile");
        if (b == null || b.trim().length() == 0) {
            showTile = new SimpleBooleanProperty(true);
        } else {
            showTile = new SimpleBooleanProperty(!b.toLowerCase().startsWith("n"));
        }
        // Scrollable main window
        questionnaireWindow = new VBox();
        questionnaireWindow.setPadding(new Insets(0, 5, 0, 5));
        questionnaireWindow.setSpacing(5);
        questionnaireWindow.setMinHeight(300);
        questionnaireWindow.setMaxHeight(300);
        questionnaireWindow.setMinWidth(800);
        questionnaireWindow.setMaxWidth(800);
        Text guide = new Text("Press \"Start Questionnaire\" button to start the questionnaire. Answer the Yes/No questions until MediPi populates the \"Advice to take\" box");
        guide.setWrappingWidth(600);
        guide.setId("questionnaire-title-label");
        questionLine = new HBox();
        questionLine.setPadding(new Insets(5, 5, 5, 5));
        questionLine.setSpacing(5);
        questionLine.setAlignment(Pos.CENTER);
        questionLine.setId("questionnaire-questionpanel");
        questionLine.setMinHeight(100);
        questionLine.setMaxHeight(100);

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
        startButton = new Button("Start Questionnaire", medipi.utils.getImageView("medipi.images.play", 20, 20));
        startButton.setId("questionnaire-button-start");
        questionnaireWindow.setAlignment(Pos.CENTER_LEFT);
        Label responseTitleLabel = new Label("Action to take:");
        responseTitleLabel.setId("questionnaire-responsepanel");
        questionnaireWindow.getChildren().addAll(
                guide,
                questionLine,
                responseTitleLabel,
                listSP
        );
        listSP.visibleProperty().bind(responseLabel.textProperty().isNotEmpty());
        responseTitleLabel.visibleProperty().bind(responseLabel.textProperty().isNotEmpty());

        // set main Element window
        window.setCenter(questionnaireWindow);

        // load the questionnaire ruleset
        questionSet = medipi.getProperties().getProperty(MediPi.ELEMENTNAMESPACESTEM + uniqueDeviceName + ".questions");
        if (questionSet == null || !questionSet.contains(".questions")) {
            throw new Exception("Cannot find Question Ruleset");
        }
        loadRules(questionSet);
        startButton.setOnAction((ActionEvent t) -> {
            if (startButton.getText().equals("Start Questionnaire")) {
                if (confirmReset()) {
                    resetDevice();
                    startButton.setText("Cancel");
                    startButton.setGraphic(medipi.utils.getImageView("medipi.images.cancel", 20, 20));
                    execute(firstRuleName);
                }
            } else if (confirmReset()) {
                if (isThisElementPartOfAScheduleExecution.get()) {
                    button1.setDisable(false);
                    button3.setDisable(false);
                }
                startButton.setText("Start Questionnaire");
                startButton.setGraphic(medipi.utils.getImageView("medipi.images.play", 20, 20));
                resetDevice();
            }
        });
        setButton2(startButton);

        hasData.bind(responseLabel.textProperty().isNotEmpty());
        // bind the button disable to the time sync indicator
        startButton.disableProperty().bind(medipi.timeSync.not());

        // successful initiation of the this class results in a null return
        return null;
    }

    @Override
    public String getProfileId() {
        return PROFILEID;
    }

    @Override
    public String getGenericDeviceDisplayName() {
        return titleName;
    }

    @Override
    public void resetDevice() {
        question.setText("");
        responseLabel.setText("");
        questionLine.getChildren().clear();
        data = new QuestionnaireDO();
        resultsSummary.setValue("");
        questionNo = 0;
        schedStartTime = null;
        schedExpireTime = null;
    }

    // This is a recursive method used for each line of the questionnaire
    // - presenting the UI with a yes/no choice and following a separate 
    // question path depending upon the answers given
    private void execute(String ruleName) {
        final int QUESTION = 0;
        final int TRUE_RESPONSE = 1;
        final int FALSE_RESPONSE = 2;
        // handle buttons for when being run as part of a schedule 
        if (isThisElementPartOfAScheduleExecution.get()) {
            button1.setDisable(true);
            button3.setDisable(true);
        }
        String[] rule = questionnaire.get(ruleName);
        yes = new Button("Yes", medipi.utils.getImageView("medipi.images.yes", 20, 20));
        yes.setId("questionnaire-button-yes");
        no = new Button("No", medipi.utils.getImageView("medipi.images.no", 20, 20));
        no.setId("questionnaire-button-no");

        //actions for clicking "yes"
        yes.setOnAction((ActionEvent t) -> {
            // add data to data arraylist for transmission later
            String[] conv = {questions.get(rule[QUESTION]), yes.getText()};
            data.addConversation(conv);
            yes.setDisable(true);
            no.setDisable(true);
            no.setVisible(false);
            String[] response = responses.get(rule[TRUE_RESPONSE]);
            if (response != null) {
                executeLastStep(response);
            } else {
                // if there is no utimate advice to be given as a result of
                // this question recursively execute the subsequent question(s)
                execute(rule[TRUE_RESPONSE]);
            }
        });

        //actions for clicking "no"
        no.setOnAction((ActionEvent t) -> {
            // add data to data arraylist for transmission later
            String[] conv = {questions.get(rule[QUESTION]), no.getText()};
            data.addConversation(conv);
            yes.setDisable(true);
            yes.setVisible(false);
            no.setDisable(true);
            String[] response = responses.get(rule[FALSE_RESPONSE]);
            if (response != null) {
                executeLastStep(response);
            } else {
                // if there is no utimate advice to be given as a result of
                // this question recursively execute the subsequent question(s)
                execute(rule[FALSE_RESPONSE]);
            }
        });
        questionNo++;
        question = new Text("Q" + questionNo + ". " + questions.get(rule[QUESTION]));
        question.setWrappingWidth(580);
        questionLine.getChildren().clear();
        questionLine.getChildren().addAll(
                question,
                yes,
                no
        );

    }

    private void executeLastStep(String[] response) {
        // add data to data arraylist for transmission later
        // n.b. 1 millisecond is added to the response to differentiate it and maintain unique timestamps
        data.setAdvice(response[1]);
        boolean redFlag = Boolean.valueOf(response[0]);
        if (redFlag) {
            data.setStatus(RED_FLAG);
        } else {
            data.setStatus(GREEN_FLAG);
        }
        resultsSummary.setValue(getSpecificDeviceDisplayName() + " completed");
        dataTime = Instant.now();
        responseLabel.setText(response[1]);
        Scheduler scheduler = null;
        if ((scheduler = medipi.getScheduler()) != null) {
            schedStartTime = scheduler.getCurrentScheduleStartTime();
            schedExpireTime = scheduler.getCurrentScheduleExpiryTime();
        }
        if (isThisElementPartOfAScheduleExecution.get()) {
            button1.setDisable(false);
            button3.setDisable(false);
        }
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
                throw new Exception(titleName + " Ruleset has no version number - unsafe to continue");
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
        if (st.countTokens() <= 3) {
            throw new Exception("Syntax error in " + questionSet + " defining response: " + line);
        }
        String ruleName = st.nextToken();
        String redFlag = st.nextToken();
        StringBuilder text = new StringBuilder();
        while (st.hasMoreTokens()) {
            text.append(st.nextToken());
            if (st.hasMoreTokens()) {
                text.append(" ");
            }
        }
        responses.put(ruleName, new String[]{redFlag, text.toString()});
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
     * Gets a DevicedataDO containing the payload
     *
     *
     * @return DevicedataDO containing the payload
     * @throws java.lang.Exception
     */
    @Override
    public DeviceDataDO getData() throws Exception {
        DeviceDataDO payload = new DeviceDataDO(UUID.randomUUID().toString());
        StringBuilder sb = new StringBuilder();
        //Add MetaData
        sb.append("metadata->persist->medipiversion->").append(medipi.getVersion()).append("\n");
        sb.append("metadata->persist->questionnaireversion->").append(titleName).append("\n");
        sb.append("metadata->make->").append(getMake()).append("\n");
        sb.append("metadata->model->").append(getModel()).append("\n");
        sb.append("metadata->displayname->").append(getSpecificDeviceDisplayName()).append("\n");
        sb.append("metadata->datadelimiter->").append(medipi.getDataSeparator()).append("\n");
        if (medipi.getScheduler() != null) {
            sb.append("metadata->scheduleeffectivedate->").append(Utilities.ISO8601FORMATDATEMILLI_UTC.format(schedStartTime)).append("\n");
            sb.append("metadata->scheduleexpirydate->").append(Utilities.ISO8601FORMATDATEMILLI_UTC.format(schedExpireTime)).append("\n");
        }
        sb.append("metadata->columns->")
                .append("iso8601time").append(medipi.getDataSeparator())
                .append("outcome").append("\n");
        sb.append("metadata->format->")
                .append("DATE").append(medipi.getDataSeparator())
                .append("STRING").append("\n");
        sb.append("metadata->units->")
                .append("NONE").append(medipi.getDataSeparator())
                .append("NONE").append("\n");
        // Add Downloaded data
        sb.append(dataTime.toString());
        sb.append(medipi.getDataSeparator());
        sb.append(getJSON(data));
        sb.append("\n");
        payload.setProfileId(PROFILEID);
        payload.setPayload(sb.toString());
        return payload;
    }

    @Override
    public BorderPane getDashboardTile() throws Exception {
        DashboardTile dashComponent = new DashboardTile(this, showTile);
        dashComponent.addTitle(getSpecificDeviceDisplayName());
        dashComponent.addOverlay(Color.LIGHTGREEN, hasDataProperty());
        return dashComponent.getTile();
    }

    /**
     * method to get the Make of the device
     *
     * @return make and model of device
     */
    @Override
    public String getMake() {
        return titleName;
    }

    /**
     * method to get the Model of the device
     *
     * @return model of device
     */
    @Override
    public String getModel() {
        return MODEL;
    }

    /**
     * method to get the Display Name of the device
     *
     * @return displayName of device
     */
    @Override
    public String getSpecificDeviceDisplayName() {
        return titleName;
    }

    @Override
    public StringProperty getResultsSummary() {
        return resultsSummary;
    }

    @Override
    public void setData(ArrayList<ArrayList<String>> deviceData) {
        throw new UnsupportedOperationException("This method is not used as the class has no extensions");
    }

    private String getJSON(QuestionnaireDO data) throws JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper();
        return mapper.writeValueAsString(data);

    }

}
