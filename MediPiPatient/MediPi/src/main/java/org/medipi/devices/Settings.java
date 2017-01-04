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

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Separator;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.util.StringConverter;
import org.medipi.DashboardTile;
import org.medipi.MediPi;
import org.medipi.MediPiMessageBox;
import org.medipi.MediPiProperties;
import org.medipi.PatientDetailsDO;
import org.medipi.PatientDetailsService;
import org.medipi.devices.drivers.service.BluetoothPropertiesDO;
import org.medipi.devices.drivers.service.BluetoothPropertiesService;
import org.medipi.utilities.Utilities;

/**
 * Class to display and handle alteration certain properties to control the
 * application.
 *
 * This element is designed to be used in an admin configuration of MediPi as it
 * gives access to changing patient details and bluetooth device configuration.
 * 
 * TODO:
 * The way in which it manages bluetooth pairing is by calling blueman
 * application on the Xwindows LINUX interface. This means this functionality is
 * platform specific and requires execution on an Xwindows platform with blueman
 * installed.This has been done for expediency and because bluetooth connection
 * and management can be temperamental
 *
 * The code still exists for the credits text panel- these have not been
 * removed as they may be used/refactored elsewhere
 *
 * @author rick@robinsonhq.com
 */
public class Settings extends Element {

    private static final String NAME = "Settings";
    private static final String MAKE = "NONE";
    private static final String MODEL = "NONE";
    private static final String DISPLAYNAME = "MediPi Settings";
    private TextArea creditsView;
    private VBox settingsWindow;
    private final BooleanProperty patientChanged = new SimpleBooleanProperty(false);
    private PatientDetailsDO patient;

    /**
     * Constructor for Messenger
     *
     */
    public Settings() {

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
        settingsWindow = new VBox();
        settingsWindow.setPadding(new Insets(0, 5, 0, 5));
        settingsWindow.setSpacing(5);
        settingsWindow.setMinSize(800, 300);
        settingsWindow.setMaxSize(800, 300);
        // Create the view of the message content - scrollable
        creditsView = new TextArea();
        creditsView.setWrapText(true);
        creditsView.isResizable();
        creditsView.setEditable(false);
        creditsView.setId("settings-creditscontent");
        creditsView.setMaxHeight(90);
        creditsView.setMinHeight(90);
        creditsView.setEditable(false);
        Label versionLabel = new Label();
        versionLabel.setId("settings-creditscontent");
        ScrollPane viewSP = new ScrollPane();
        viewSP.setContent(creditsView);
        viewSP.setFitToWidth(true);
        viewSP.setFitToHeight(true);
        viewSP.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        viewSP.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);

        //ascertain if this element is to be displayed on the dashboard
        String b = MediPiProperties.getInstance().getProperties().getProperty(MediPi.ELEMENTNAMESPACESTEM + uniqueDeviceName + ".showdashboardtile");
        if (b == null || b.trim().length() == 0) {
            showTile = new SimpleBooleanProperty(true);
        } else {
            showTile = new SimpleBooleanProperty(!b.toLowerCase().startsWith("n"));
        }

        // Load patient details from the patient details service class
        try {
            patient = PatientDetailsService.getInstance().getPatientDetails();
        } catch (Exception e) {
            MediPiMessageBox.getInstance().makeErrorMessage("Cannot load patient details: ", e);
        }

        HBox nameHBox = new HBox();
        nameHBox.setSpacing(10);
        nameHBox.setAlignment(Pos.CENTER_LEFT);

        Text forenameLabel = new Text("Forname:");
        forenameLabel.setId("button-closemedipi");
        TextField forenameTF = new TextField();
        forenameTF.setMaxWidth(230);
        forenameTF.setId("button-closemedipi");
        forenameTF.setPromptText("required");
        forenameTF.textProperty().addListener(new ChangeListener<String>() {
            public void changed(final ObservableValue<? extends String> observableValue, final String oldValue,
                    final String newValue) {
                patientChanged.set(true);
            }
        });
        Text surnameLabel = new Text("Surname:");
        surnameLabel.setId("button-closemedipi");
        TextField surnameTF = new TextField();
        surnameTF.setMaxWidth(230);
        surnameTF.setId("button-closemedipi");
        surnameTF.setPromptText("required");
        surnameTF.textProperty().addListener(new ChangeListener<String>() {
            public void changed(final ObservableValue<? extends String> observableValue, final String oldValue,
                    final String newValue) {
                patientChanged.set(true);
            }
        });

        HBox nhsNumberHBox = new HBox();
        nhsNumberHBox.setSpacing(10);
        nhsNumberHBox.setAlignment(Pos.CENTER_LEFT);

        Text nhsNumberLabel = new Text("NHS Number:");
        nhsNumberLabel.setId("button-closemedipi");
        TextField nhsNumberTF = new TextField();
        nhsNumberTF.setMaxWidth(180);
        nhsNumberTF.setId("button-closemedipi");
        nhsNumberTF.setPromptText("required");
        nhsNumberTF.textProperty().addListener(new ChangeListener<String>() {
            public void changed(final ObservableValue<? extends String> observableValue, final String oldValue,
                    final String newValue) {
                if (nhsNumberTF.getText().length() > 10) {
                    String s = nhsNumberTF.getText().substring(0, 10);
                    nhsNumberTF.setText(s);
                }
                patientChanged.set(true);
            }
        });

        HBox dobHBox = new HBox();
        dobHBox.setSpacing(10);
        dobHBox.setAlignment(Pos.CENTER_LEFT);

        Text dobLabel = new Text("Date of Birth (YYYY/MM/DD):");
        dobLabel.setId("button-closemedipi");
        TextField yyyyTF = new TextField();
        yyyyTF.setMaxWidth(90);
        yyyyTF.setId("button-closemedipi");
        yyyyTF.setPromptText("YYYY");
        yyyyTF.textProperty().addListener(new ChangeListener<String>() {
            public void changed(final ObservableValue<? extends String> observableValue, final String oldValue,
                    final String newValue) {
                if (yyyyTF.getText().length() > 4) {
                    String s = yyyyTF.getText().substring(0, 4);
                    yyyyTF.setText(s);
                }
                patientChanged.set(true);
            }
        });
        TextField mmTF = new TextField();
        mmTF.setMaxWidth(60);
        mmTF.setId("button-closemedipi");
        mmTF.setPromptText("MM");
        mmTF.textProperty().addListener(new ChangeListener<String>() {
            public void changed(final ObservableValue<? extends String> observableValue, final String oldValue,
                    final String newValue) {
                if (mmTF.getText().length() > 2) {
                    String s = mmTF.getText().substring(0, 2);
                    mmTF.setText(s);
                }
                patientChanged.set(true);
            }
        });
        TextField ddTF = new TextField();
        ddTF.setMaxWidth(60);
        ddTF.setId("button-closemedipi");
        ddTF.setPromptText("DD");
        ddTF.textProperty().addListener(new ChangeListener<String>() {
            public void changed(final ObservableValue<? extends String> observableValue, final String oldValue,
                    final String newValue) {
                if (ddTF.getText().length() > 2) {
                    String s = ddTF.getText().substring(0, 2);
                    ddTF.setText(s);
                }
                patientChanged.set(true);
            }
        });
        Button patientDetailsSave = new Button("Save Patient");
        patientDetailsSave.setId("button-closemedipi");
        patientDetailsSave.disableProperty().bind(patientChanged.not());
        patientDetailsSave.setOnAction((ActionEvent t) -> {
            //save 
            try {
                patient.setForename(forenameTF.getText());
                patient.setSurname(surnameTF.getText());
                patient.setNhsNumber(nhsNumberTF.getText());
                yyyyTF.setText(String.format("%04d", Integer.valueOf(yyyyTF.getText())));
                mmTF.setText(String.format("%02d", Integer.valueOf(mmTF.getText())));
                ddTF.setText(String.format("%02d", Integer.valueOf(ddTF.getText())));

                patient.setDob(yyyyTF.getText() + mmTF.getText() + ddTF.getText());
                patient.checkValidity();
                PatientDetailsService pds = PatientDetailsService.getInstance();
                pds.savePatientDetails(patient);
                medipi.setPatientMicroBanner(patient);
                patientChanged.set(false);
            } catch (Exception e) {
                MediPiMessageBox.getInstance().makeErrorMessage("Patient Details Data format Issues: ", e);
            }
        });

        nameHBox.getChildren().addAll(
                forenameLabel,
                forenameTF,
                surnameLabel,
                surnameTF
        );
        nhsNumberHBox.getChildren().addAll(
                nhsNumberLabel,
                nhsNumberTF
        );
        dobHBox.getChildren().addAll(
                dobLabel,
                yyyyTF,
                new Label("/"),
                mmTF,
                new Label("/"),
                ddTF,
                patientDetailsSave
        );

        //add data to patient details
        forenameTF.setText(patient.getForename());
        surnameTF.setText(patient.getSurname().toUpperCase());
        nhsNumberTF.setText(patient.getNhsNumber());
        try {
            LocalDate dobld = LocalDate.parse(patient.getDob(), DateTimeFormatter.BASIC_ISO_DATE);
            yyyyTF.setText(String.format("%04d", dobld.getYear()));
            mmTF.setText(String.format("%02d", dobld.getMonthValue()));
            ddTF.setText(String.format("%02d", dobld.getDayOfMonth()));
        } catch (DateTimeParseException e) {
            throw new Exception(" Patient Date of Birth (" + patient.getDob() + ") in wrong format.");
        }
        patientChanged.set(false);

        // Bluetooth maintainance code
        HBox btHBox = new HBox();
        btHBox.setAlignment(Pos.CENTER_LEFT);
        btHBox.setSpacing(10);
        Button bluetoothPairingButton = new Button("Bluetooth Pairing");
        bluetoothPairingButton.setId("button-closemedipi");

        // Setup download button action to run in its own thread
        bluetoothPairingButton.setOnAction((ActionEvent t) -> {
            medipi.executeCommand("blueman-manager");
        });
        Text cbLabel = new Text("Bluetooth Serial Device:");
        cbLabel.setId("button-closemedipi");

        HBox btSerialHBox = new HBox();
        btSerialHBox.setSpacing(10);
        btSerialHBox.setAlignment(Pos.CENTER_LEFT);

        Text serialLabel = new Text("Bluetooth Serial Port MAC:");
        serialLabel.setId("button-closemedipi");
        Button save = new Button("Save BT Serial MAC");
        save.setId("button-closemedipi");
        save.setDisable(true);
        TextField tf = new TextField();
        tf.setMaxWidth(230);
        tf.setId("button-closemedipi");
        tf.setPromptText("no value");
        tf.textProperty().addListener(new ChangeListener<String>() {
            public void changed(final ObservableValue<? extends String> observableValue, final String oldValue,
                    final String newValue) {
                String value = newValue.replace("-", "");
                boolean isHex = value.matches("[0-9A-F]+");
                if (value.length() == 12 && isHex) {
                    save.setDisable(false);
                } else {
                    save.setDisable(true);
                }
            }
        });
        BluetoothPropertiesService bps = BluetoothPropertiesService.getInstance();
        ChoiceBox cb = new ChoiceBox();
        cb.setId("button-closemedipi");
        cb.setConverter(new ChoiceBoxElementLabel());
        cb.setItems(FXCollections.observableArrayList(bps.getRegisteredElements()));

        cb.getSelectionModel().selectedIndexProperty().addListener(new ChangeListener<Number>() {
            @Override
            public void changed(ObservableValue<? extends Number> observableValue, Number number, Number number2) {
                Element e = ((Element) cb.getItems().get((Integer) number2));
                BluetoothPropertiesDO bpdo = bps.getBluetoothPropertyDOByMedipiDeviceName(e.getClassTokenName());
                if (bpdo == null) {
                    tf.setText("");
                } else {
                    tf.setText(bps.getMACFromUrl(bpdo.getUrl()));
                }
                save.setDisable(true);
            }
        });
        if (!cb.getItems().isEmpty()) {
            cb.getSelectionModel().selectFirst();
        } else {
            cb.setDisable(true);
            tf.setDisable(true);
        }
        save.setOnAction((ActionEvent t) -> {
            Element e = (Element) cb.getSelectionModel().getSelectedItem();
            String device = e.getClassTokenName();
            String friendlyName = e.getDisplayName();
            String protocolId = "0x1101";
            String url = bps.getUrlFromMac(tf.getText().replace("-", ""));
            bps.addPropertyDO(device, friendlyName, protocolId, url);
            save.setDisable(true);
        });


        versionLabel.setText("MediPi Version: " + medipi.getVersion());

//        // location of the credits file
//        String creditsDir = medipi.getProperties().getProperty(MediPi.ELEMENTNAMESPACESTEM + uniqueDeviceName + ".credits");
//        if (creditsDir == null || creditsDir.trim().length() == 0) {
//            throw new Exception("credits file location parameter not configured");
//        }
//
//        creditsView.setText(readFile(creditsDir, StandardCharsets.UTF_8));
//
//        Label creditsLabel = new Label("Credits");
//        creditsLabel.setId("settings-text");


        btHBox.getChildren().addAll(
                cbLabel,
                cb
        );
        btSerialHBox.getChildren().addAll(
                serialLabel,
                tf,
                save
        );
        settingsWindow.getChildren().addAll(
                nameHBox,
                nhsNumberHBox,
                dobHBox,
                new Separator(Orientation.HORIZONTAL),
                bluetoothPairingButton,
                btHBox,
                btSerialHBox,
                new Separator(Orientation.HORIZONTAL),
                versionLabel
        );

        // set main Element window
        window.setCenter(settingsWindow);

        // successful initiation of the this class results in a null return
        return null;
    }

    private static String readFile(String path, Charset encoding)
            throws IOException {
        byte[] encoded = Files.readAllBytes(Paths.get(path));
        return new String(encoded, encoding);
    }

    /**
     * method to get the Display Name of the device
     *
     * @return displayName of device
     */
    @Override
    public String getDisplayName() {
        return DISPLAYNAME;
    }

    /**
     * method to return the component to the dashboard
     *
     * @return @throws Exception
     */
    @Override
    public BorderPane getDashboardTile() throws Exception {
        DashboardTile dashComponent = new DashboardTile(this, showTile);
        dashComponent.addTitle(getDisplayName());
        return dashComponent.getTile();
    }

    class ChoiceBoxElementLabel extends StringConverter<Element> {

        public Element fromString(String string) {
            // convert from a string to a myClass instance
            return null;
        }

        public String toString(Element myClassinstance) {
            // convert a myClass instance to the text displayed in the choice box
            return myClassinstance.getDisplayName();
        }
    }

}
