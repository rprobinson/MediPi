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
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import javafx.beans.binding.Bindings;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Separator;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.util.Pair;
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
import org.medipi.devices.drivers.domain.DeviceTimestampUpdateInterface;
import org.medipi.devices.drivers.domain.DeviceModeUpdateInterface;

/**
 * Class to display and handle alteration certain properties to control the
 * application.
 *
 * This element is designed to be used in an admin configuration of MediPi as it
 * gives access to changing patient details and bluetooth device configuration.
 *
 * TODO: The way in which it manages bluetooth pairing is by calling blueman
 * application on the Xwindows LINUX interface. This means this functionality is
 * platform specific and requires execution on an Xwindows platform with blueman
 * installed.This has been done for expediency and because bluetooth connection
 * and management can be temperamental
 *
 * The code still exists for the credits text panel- these have not been removed
 * as they may be used/refactored elsewhere
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
    private final BooleanProperty scheduleChanged = new SimpleBooleanProperty(false);
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
        VBox settingsDataBox;
        settingsDataBox = new VBox();
        settingsDataBox.setId("transmitter-text");
        settingsDataBox.setSpacing(5);
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

// ----------- PATIENT SETTINGS CODE ------------------ 
        Text patientTitleLabel = new Text("Patient Demographic Settings");
        patientTitleLabel.setId("mainwindow-dashboard-component-title");
        Text forenameLabel = new Text("Forename:");
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
                if (yyyyTF.getText().isEmpty() || mmTF.getText().isEmpty() || ddTF.getText().isEmpty()) {
                    MediPiMessageBox.getInstance().makeErrorMessage("The date contains empty values", null);
                    return;
                }

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

// ----------- Time Sync code ---------------
        Text timesetTitleLabel = new Text("Set Internal Physiological Device Time");
        timesetTitleLabel.setId("mainwindow-dashboard-component-title");
        VBox timesetVBox = new VBox();
        timesetVBox.setSpacing(5);
        timesetVBox.getChildren().add(timesetTitleLabel);

        for (Element e : medipi.getElements()) {
            Label deviceTimestampName = new Label(e.getSpecificDeviceDisplayName());
            deviceTimestampName.setId("button-closemedipi");
            HBox lineHBox = new HBox();
            lineHBox.setAlignment(Pos.CENTER_LEFT);
            lineHBox.setSpacing(10);
            lineHBox.getChildren().add(
                    deviceTimestampName
            );
            if (DeviceModeUpdateInterface.class.isAssignableFrom(e.getClass())) {
                System.out.println(e.getClassTokenName());
                Label deviceModeName = new Label(e.getSpecificDeviceDisplayName());
                deviceModeName.setId("button-closemedipi");
                Button deviceModeButton = new Button("Update Mode");
                deviceModeButton.setId("button-closemedipi");
                deviceModeButton.setOnAction((ActionEvent t) -> {
                    DeviceModeUpdateInterface tvi = (DeviceModeUpdateInterface) e;
                    Node guide = tvi.getDeviceModeUpateMessageBoxContent();
                    // Create the custom dialog.
                    Dialog<Pair<String, String>> dialog = new Dialog<>();
                    dialog.getDialogPane().getStylesheets().add("file:///" + medipi.getCssfile());
                    dialog.getDialogPane().setId("message-window");
                    dialog.setTitle("Mode Update");
                    dialog.setHeaderText(e.getSpecificDeviceDisplayName() + " requires update to correct Mode");

                    // Set the button types.
                    ButtonType okButton = new ButtonType("OK", ButtonBar.ButtonData.OK_DONE);
                    dialog.getDialogPane().getButtonTypes().addAll(okButton);

                    // Create the username and password labels and fields.
                    GridPane grid = new GridPane();
                    grid.setHgap(10);
                    grid.setVgap(10);
                    grid.setPadding(new Insets(10, 10, 10, 10));

                    grid.add(guide, 0, 0);

                    dialog.getDialogPane().setContent(grid);
                    dialog.setResultConverter(dialogButton -> {
                        if (dialogButton == okButton) {
                            return null;
                        }
                        return null;
                    });

                    dialog.showAndWait();

                });
                lineHBox.getChildren().add(
                        deviceModeButton
                );

            }
            if (DeviceTimestampUpdateInterface.class.isAssignableFrom(e.getClass())) {
                System.out.println(e.getClassTokenName());
                Button deviceTimestampButton = new Button("Update Time");
                deviceTimestampButton.setId("button-closemedipi");
                deviceTimestampButton.setOnAction((ActionEvent t) -> {
                    DeviceTimestampUpdateInterface tvi = (DeviceTimestampUpdateInterface) e;
                    Node guide = tvi.getDeviceTimestampUpdateMessageBoxContent();
                    // Create the custom dialog.
                    Dialog<Pair<String, String>> dialog = new Dialog<>();
                    dialog.getDialogPane().getStylesheets().add("file:///" + medipi.getCssfile());
                    dialog.getDialogPane().setId("message-window");
                    dialog.setTitle("Time Synchronisation");
                    dialog.setHeaderText(e.getSpecificDeviceDisplayName() + " requires time synchronisation");

                    // Set the button types.
                    ButtonType okButton = new ButtonType("OK", ButtonBar.ButtonData.OK_DONE);
                    dialog.getDialogPane().getButtonTypes().addAll(okButton);

                    // Create the username and password labels and fields.
                    GridPane grid = new GridPane();
                    grid.setHgap(10);
                    grid.setVgap(10);
                    grid.setPadding(new Insets(10, 10, 10, 10));

                    grid.add(guide, 0, 0);

                    dialog.getDialogPane().setContent(grid);
                    dialog.setResultConverter(dialogButton -> {
                        if (dialogButton == okButton) {
                            return null;
                        }
                        return null;
                    });

                    dialog.showAndWait();

                });
                lineHBox.getChildren().add(
                        deviceTimestampButton
                );
            }
            if (lineHBox.getChildren().size() > 1) {
                timesetVBox.getChildren().add(lineHBox);
            }
        }

// ----------- Bluetooth maintainance code ---------------
        Text bluetoothTitleLabel = new Text("Bluetooth Settings");
        bluetoothTitleLabel.setId("mainwindow-dashboard-component-title");
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
                boolean isHex = value.matches("[0-9A-Fa-f]+");
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
            String friendlyName = e.getSpecificDeviceDisplayName();
            String protocolId = "0x1101";
            String url = bps.getUrlFromMac(tf.getText().replace("-", ""));
            bps.addPropertyDO(device, friendlyName, protocolId, url);
            save.setDisable(true);
        });

// ----------- SCHEDULE SETTINGS CODE ------------------ 
        Text scheduleTitleLabel = new Text("Schedule Settings");
        scheduleTitleLabel.setId("mainwindow-dashboard-component-title");
        VBox scheduleDataBox;
        scheduleDataBox = new VBox();
        scheduleDataBox.setId("transmitter-text");
        scheduleDataBox.setSpacing(5);
        Map<String, Boolean> schedDevices = new LinkedHashMap<>();
        HBox schedTimeHBox = new HBox();
        schedTimeHBox.setSpacing(5);
        schedTimeHBox.setAlignment(Pos.CENTER_LEFT);
        HBox schedRepeatPeriodHBox = new HBox();
        schedRepeatPeriodHBox.setSpacing(10);
        schedRepeatPeriodHBox.setAlignment(Pos.CENTER_LEFT);
        Text previousSchedTimeLabel = new Text();
        previousSchedTimeLabel.setId("button-closemedipi");

        Scheduler scheduler = medipi.getScheduler();
        if (scheduler != null) {
            // loop through all loaded elements and add checkboxes and images to the window
            for (Element e : medipi.getElements()) {
                if (Device.class.isAssignableFrom(e.getClass())) {
                    Device d = (Device) e;
                    // Dont want the scheduler in the list as it's not a schedulable device
                    if (e.getGenericDeviceDisplayName().equals("Readings")) {
                        continue;
                    }
                } else if (e.getGenericDeviceDisplayName().equals("Transmitter")) {
                } else {
                    continue;
                }
                schedDevices.put(e.getClassTokenName(), false);
                String classTokenName = e.getClassTokenName();
                ImageView image = e.getImage();
                image.setFitHeight(25);
                image.setFitWidth(25);
                BorderPane imagePane = new BorderPane(image);
                imagePane.setMinSize(30, 30);
                imagePane.setId("transmitter-component");
                CheckBox tcb = new CheckBox();
                tcb.setMinWidth(600);
                tcb.setId("transmitter-text");
                // Get device data summary if present
                tcb.setText(e.getSpecificDeviceDisplayName());
                if (e.getGenericDeviceDisplayName().equals("Transmitter")) {
                    tcb.setSelected(true);
                    tcb.setDisable(true);
                    schedDevices.replace(e.getClassTokenName(), true);
                }
                tcb.selectedProperty().addListener(new ChangeListener<Boolean>() {
                    @Override
                    public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
                        if (newValue) {
                            schedDevices.replace(e.getClassTokenName(), true);
                        } else {
                            schedDevices.replace(e.getClassTokenName(), false);
                        }
                        scheduleChanged.set(true);
                    }
                });
                //loop through the currently scheduled devices and tick boxes
                if (scheduler.getScheduledElements() != null) {
                    for (Element schedElem : scheduler.getScheduledElements()) {
                        if (schedElem.equals(e)) {
                            tcb.setSelected(true);
                        }
                    }
                }
                HBox hb = new HBox();
                hb.setAlignment(Pos.CENTER_LEFT);
                hb.getChildren().addAll(
                        tcb,
                        imagePane
                );
                hb.setPadding(new Insets(0, 0, 0, 50));
                scheduleDataBox.getChildren().add(hb);

            }

            Instant scheduledFirstScheduleTime = medipi.getScheduler().getScheduledFirstScheduleTime();
            if (scheduledFirstScheduleTime != null) {
                previousSchedTimeLabel = new Text("Previous Schedule's Initialisation Time: " + scheduledFirstScheduleTime.toString());
            }

            Text schedTimeLabel = new Text("Schedule Initialisation Time:");
            schedTimeLabel.setId("button-closemedipi");
            TextField yyyySTTF = new TextField();
            yyyySTTF.setMaxWidth(90);
            yyyySTTF.setId("button-closemedipi");
            yyyySTTF.setPromptText("YYYY");
            yyyySTTF.textProperty().addListener(new ChangeListener<String>() {
                public void changed(final ObservableValue<? extends String> observableValue, final String oldValue,
                        final String newValue) {
                    if (yyyySTTF.getText().length() > 4) {
                        String s = yyyySTTF.getText().substring(0, 4);
                        yyyySTTF.setText(s);
                    }
                    scheduleChanged.set(true);
                }
            });
            TextField mmSTTF = new TextField();
            mmSTTF.setMaxWidth(60);
            mmSTTF.setId("button-closemedipi");
            mmSTTF.setPromptText("MM");
            mmSTTF.textProperty().addListener(new ChangeListener<String>() {
                public void changed(final ObservableValue<? extends String> observableValue, final String oldValue,
                        final String newValue) {
                    if (mmSTTF.getText().length() > 2) {
                        String s = mmSTTF.getText().substring(0, 2);
                        mmSTTF.setText(s);
                    }
                    scheduleChanged.set(true);
                }
            });
            TextField ddSTTF = new TextField();
            ddSTTF.setMaxWidth(60);
            ddSTTF.setId("button-closemedipi");
            ddSTTF.setPromptText("DD");
            ddSTTF.textProperty().addListener(new ChangeListener<String>() {
                public void changed(final ObservableValue<? extends String> observableValue, final String oldValue,
                        final String newValue) {
                    if (ddSTTF.getText().length() > 2) {
                        String s = ddSTTF.getText().substring(0, 2);
                        ddSTTF.setText(s);
                    }
                    scheduleChanged.set(true);
                }
            });
            TextField hourSTTF = new TextField();
            hourSTTF.setMaxWidth(60);
            hourSTTF.setId("button-closemedipi");
            hourSTTF.setPromptText("hh");
            hourSTTF.textProperty().addListener(new ChangeListener<String>() {
                public void changed(final ObservableValue<? extends String> observableValue, final String oldValue,
                        final String newValue) {
                    if (hourSTTF.getText().length() > 2) {
                        String s = hourSTTF.getText().substring(0, 2);
                        hourSTTF.setText(s);
                    }
                    scheduleChanged.set(true);
                }
            });
            TextField secSTTF = new TextField();
            secSTTF.setMaxWidth(60);
            secSTTF.setId("button-closemedipi");
            secSTTF.setPromptText("ss");
            secSTTF.textProperty().addListener(new ChangeListener<String>() {
                public void changed(final ObservableValue<? extends String> observableValue, final String oldValue,
                        final String newValue) {
                    if (secSTTF.getText().length() > 2) {
                        String s = secSTTF.getText().substring(0, 2);
                        secSTTF.setText(s);
                    }
                    scheduleChanged.set(true);
                }
            });
            TextField minSTTF = new TextField();
            minSTTF.setMaxWidth(60);
            minSTTF.setId("button-closemedipi");
            minSTTF.setPromptText("mm");
            minSTTF.textProperty().addListener(new ChangeListener<String>() {
                public void changed(final ObservableValue<? extends String> observableValue, final String oldValue,
                        final String newValue) {
                    if (minSTTF.getText().length() > 2) {
                        String s = minSTTF.getText().substring(0, 2);
                        minSTTF.setText(s);
                    }
                    scheduleChanged.set(true);
                }
            });
            schedTimeHBox.getChildren().addAll(
                    schedTimeLabel,
                    yyyySTTF,
                    new Label("/"),
                    mmSTTF,
                    new Label("/"),
                    ddSTTF,
                    new Label(" "),
                    hourSTTF,
                    new Label(":"),
                    minSTTF,
                    new Label(":"),
                    secSTTF
            );

            Text schedRepeatPeriodLabel = new Text("Schedule Repeat Time (minutes):");
            schedRepeatPeriodLabel.setId("button-closemedipi");
            TextField schedRepeatPeriodTF = new TextField();
            schedRepeatPeriodTF.setMaxWidth(180);
            schedRepeatPeriodTF.setId("button-closemedipi");
            schedRepeatPeriodTF.setPromptText("required");
            if (scheduler.getScheduledRepeatPeriod() != -1) {
                schedRepeatPeriodTF.setText(String.valueOf(scheduler.getScheduledRepeatPeriod()));
            }
            schedRepeatPeriodTF.textProperty().addListener(new ChangeListener<String>() {
                public void changed(final ObservableValue<? extends String> observableValue, final String oldValue,
                        final String newValue) {
                    scheduleChanged.set(true);
                }
            });

            //load the current data from the Scheduler into the UI
            if (scheduledFirstScheduleTime != null) {
                try {
                    LocalDateTime schedTimeld = LocalDateTime.ofInstant(scheduledFirstScheduleTime, ZoneId.systemDefault());
                    yyyySTTF.setText(String.format("%04d", schedTimeld.getYear()));
                    mmSTTF.setText(String.format("%02d", schedTimeld.getMonthValue()));
                    ddSTTF.setText(String.format("%02d", schedTimeld.getDayOfMonth()));
                    hourSTTF.setText(String.format("%02d", schedTimeld.getHour()));
                    minSTTF.setText(String.format("%02d", schedTimeld.getMinute()));
                    secSTTF.setText(String.format("%02d", schedTimeld.getSecond()));
                } catch (DateTimeParseException e) {
                    throw new Exception(" Schedule  (" + scheduledFirstScheduleTime + ") in wrong format.");
                }
            }
            scheduleChanged.set(false);

            Button schedulerSave = new Button("Save new Schedule");
            schedulerSave.setId("button-closemedipi");
            schedulerSave.disableProperty().bind(scheduleChanged.not());
            schedulerSave.setOnAction((ActionEvent t) -> {
                //save 
                try {
                    boolean isPostiveInteger = schedRepeatPeriodTF.getText().matches("^[1-9]\\d*$");
                    if (!isPostiveInteger) {
                        MediPiMessageBox.getInstance().makeErrorMessage("The schedule repeat period must be a positive integer", null);
                        return;
                    }
                    int repeatPeriod = Integer.valueOf(schedRepeatPeriodTF.getText());
                    ArrayList<String> devicesArray = new ArrayList<>();

                    for (Map.Entry<String, Boolean> entry : schedDevices.entrySet()) {
                        if (entry.getValue()) {
                            devicesArray.add(entry.getKey());
                        }
                    }

                    if (devicesArray.isEmpty()) {
                        MediPiMessageBox.getInstance().makeErrorMessage("There must be at least one device selected", null);
                        return;
                    }
                    String date = yyyySTTF.getText() + "-" + mmSTTF.getText() + "-" + ddSTTF.getText() + "T" + hourSTTF.getText() + ":" + minSTTF.getText() + ":" + secSTTF.getText() + ".00Z";
                    Instant schedInstant = Instant.parse(date);
                    if (scheduler.getCurrentScheduleStartTime().isBefore(Instant.now()) && schedInstant.isBefore(scheduler.getCurrentScheduleStartTime())) {
                        MediPiMessageBox.getInstance().makeErrorMessage("Schedule date cannot be before the current schedule start date", null);
                        return;
                    }
                    scheduler.addScheduleData(UUID.randomUUID(), "SCHEDULED", schedInstant, repeatPeriod, devicesArray);
                    MediPiMessageBox.getInstance().makeMessage("New Schedule added:\nStart Time:" + schedInstant + "\nRepeat Period: " + repeatPeriod + " mins\nDevices: " + String.join(", ", devicesArray));
                    scheduleChanged.set(false);
                } catch (DateTimeParseException e) {
                    MediPiMessageBox.getInstance().makeErrorMessage("Schedule date is incorrect: ", null);
                } catch (Exception e) {
                    MediPiMessageBox.getInstance().makeErrorMessage("Schedule format Issues: ", e);
                }
            });
            schedRepeatPeriodHBox.getChildren().addAll(
                    schedRepeatPeriodLabel,
                    schedRepeatPeriodTF,
                    schedulerSave
            );

        }
// ----------- VERSION CODE ------------------ 
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
        settingsDataBox.getChildren().addAll(
                new Separator(Orientation.HORIZONTAL),
                patientTitleLabel,
                nameHBox,
                nhsNumberHBox,
                dobHBox,
                new Separator(Orientation.HORIZONTAL),
                bluetoothTitleLabel,
                //                bluetoothPairingButton,
                btHBox,
                btSerialHBox,
                new Separator(Orientation.HORIZONTAL),
                timesetVBox,
                new Separator(Orientation.HORIZONTAL),
                scheduleTitleLabel,
                scheduleDataBox,
                previousSchedTimeLabel,
                schedTimeHBox,
                schedRepeatPeriodHBox,
                new Separator(Orientation.HORIZONTAL),
                versionLabel
        );
        ScrollPane settingsDataBoxSc = new ScrollPane();
        settingsDataBoxSc.setContent(settingsDataBox);
        settingsDataBoxSc.setFitToWidth(true);
        settingsDataBoxSc.setFitToHeight(true);
        settingsDataBoxSc.setMinHeight(medipi.getScreenheight() - 130);
        settingsDataBoxSc.setMaxHeight(medipi.getScreenheight() - 130);
        settingsDataBoxSc.setMinWidth(800);
        settingsDataBoxSc.setId("mainwindow-dashboard-scroll");
        settingsDataBoxSc.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        settingsDataBoxSc.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);

        settingsWindow.getChildren().addAll(
                settingsDataBoxSc
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
    public String getSpecificDeviceDisplayName() {
        return DISPLAYNAME;
    }

    @Override
    public String getGenericDeviceDisplayName() {
        return NAME;
    }

    /**
     * method to return the component to the dashboard
     *
     * @return @throws Exception
     */
    @Override
    public BorderPane getDashboardTile() throws Exception {
        DashboardTile dashComponent = new DashboardTile(this, showTile);
        dashComponent.addTitle(getSpecificDeviceDisplayName());
        return dashComponent.getTile();
    }

    class ChoiceBoxElementLabel extends StringConverter<Element> {

        public Element fromString(String string) {
            // convert from a string to a myClass instance
            return null;
        }

        public String toString(Element myClassinstance) {
            // convert a myClass instance to the text displayed in the choice box
            return myClassinstance.getSpecificDeviceDisplayName();
        }
    }

}
