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

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.file.Path;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.util.Callback;
import org.medipi.AlertBanner;
import org.medipi.DashboardTile;
import org.medipi.MediPi;
import org.medipi.MediPiMessageBox;
import org.medipi.MediPiProperties;
import org.medipi.logging.MediPiLogger;
import org.medipi.model.DeviceDataDO;
import org.medipi.utilities.Utilities;

/**
 * Class to display and orchestrate the functionality for the Scheduler.
 *
 * The class is directed by a .scheduler file which contains details of all
 * previously executed schedules. This file consists of line entries relating
 * to:
 *
 * SCHEDULED: The file must contain at least one SCHEDULED line. This records
 * when the schedule was due, its repeat period and what elemets are due to be
 * executed (these are defined by the element class token name). All subsequent
 * scheduled events are calculated from the latest SCHEDULED line
 *
 * STARTED: This records when a schedule was started and what elements were due
 * to be run
 *
 * MEASURED: This records what time a particular element was measured
 *
 * TRANSMITTED: This records at what time, which elements were transmitted
 *
 * The .scheduler file can be dynamically updated but currently the
 * functionality to update the file using outgoing polling from a remote
 * location is not implemented
 *
 * Each of the scheduled elements are executed in turn and a transmitter is
 * called
 *
 * The view shows information about the most recent schedule in words and a list
 * of activity over a configurable period (default period 7 hours)
 *
 * The scheduler will provide data to the transmitter of this schedule only -
 * metadata is added to identify it.
 *
 * After a schedule has been transmitted, new STARTED, MEASURED and TRANSMITTED
 * lines are added to the .scheduler file
 *
 * @author rick@robinsonhq.com
 */
public class Scheduler extends Device {

    private static final String NAME = "Readings";
    private static final String PROFILEID = "urn:nhs-en:profile:Scheduler";
    private static final String MAKE = "NONE";
    private static final String MODEL = "NONE";
    private static final String DISPLAYNAME = "MediPi Readings";
    private static final String MEDIPIIMAGESEXCLAIM = "medipi.images.exclaim";
    private static final String SCHEDTAKEN = "Readings taken";
    // schedule checking executor service
    public static ScheduledExecutorService SCHEDULESERVICE = Executors.newSingleThreadScheduledExecutor();
    private VBox schedulerWindow;
    private TableView<ScheduleItem> schedulerList;
    private String schedulerFile;
    private Image alertImage;
    private final ArrayList<ScheduleItem> deviceData = new ArrayList<>();

    private ObservableList<ScheduleItem> items;
    private Instant currentScheduleStartTime = Instant.EPOCH;
    private Instant currentScheduleExpiryTime = Instant.EPOCH;
    private int missedReadings = 0;
    private final BooleanProperty alertBooleanProperty = new SimpleBooleanProperty(false);
    private final StringProperty resultsSummary = new SimpleStringProperty();

    private final Text schedRepeatText = new Text();
    private final Text schedPressRunText = new Text();
    private final Text schedInfoText = new Text("* You may run and submit readings at any time, but these will only be reviewed by your clinician at the agreed date/time");
    private final Text schedNextText = new Text();
    private Button runScheduleNowButton;
    //This is the most recent schedule
    private ScheduleItem lastScheduleItem = null;
    private int schedulerHistoryPeriod;//(default 7)

    private final BooleanProperty runningSchedule = new SimpleBooleanProperty(false);

    /**
     * Possible state of a line in the .scheduler file
     */
    protected static final String TRANSMITTED = "TRANSMITTED";

    /**
     * Possible state of a line in the .scheduler file
     */
    protected static final String STARTED = "STARTED";

    /**
     * Possible state of a line in the .scheduler file
     */
    protected static final String SCHEDULED = "SCHEDULED";

    /**
     * Possible state of a line in the .scheduler file
     */
    protected static final String MEASURED = "MEASURED";
    private static final String SCHEDULE_DUE_AT = "READINGS DUE AT";
    private static final String MISSING = "MISSING";
    private UUID nextUUID = null;
    private AlertBanner alertBanner = AlertBanner.getInstance();
    private ArrayList<SchedulerCallbacksInterface> schedulerCallbacks = new ArrayList<>();
    private ArrayList<Schedule> schedulesFromFile = null;
    private ObjectMapper mapper = new ObjectMapper();
    private boolean recordExtraMetadata = false;
    private Instant firstScheduledTime = null;
    private int scheduledRepeatPeriod = -1;

    /**
     * Constructor for Messenger
     *
     */
    public Scheduler() {

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
        schedulerWindow = new VBox();
        schedulerWindow.setPadding(new Insets(0, 5, 0, 5));
        schedulerWindow.setSpacing(10);
        schedulerWindow.setMinSize(800, 300);
        schedulerWindow.setMaxSize(800, 300);
        schedulerWindow.setAlignment(Pos.TOP_CENTER);
        // get details of all images which are required
        String alertImageFile = medipi.getProperties().getProperty(MEDIPIIMAGESEXCLAIM);
        alertImage = new Image("file:///" + alertImageFile);
        //ascertain if this element is to be displayed on the dashboard
        String b = MediPiProperties.getInstance().getProperties().getProperty(MediPi.ELEMENTNAMESPACESTEM + uniqueDeviceName + ".showdashboardtile");
        if (b == null || b.trim().length() == 0) {
            showTile = new SimpleBooleanProperty(true);
        } else {
            showTile = new SimpleBooleanProperty(!b.toLowerCase().startsWith("n"));
        }
        //Record extra Metadata for STARTED and MEASURED in the scheduler.json file
        String extraMeta = MediPiProperties.getInstance().getProperties().getProperty(MediPi.ELEMENTNAMESPACESTEM + uniqueDeviceName + ".recordextrametadatatofile");
        if (extraMeta == null || extraMeta.trim().length() == 0) {
            recordExtraMetadata = false;
        } else {
            recordExtraMetadata = extraMeta.toLowerCase().startsWith("y");
        }
        // Find location of scheduler file
        schedulerFile = medipi.getProperties().getProperty(MediPi.ELEMENTNAMESPACESTEM + uniqueDeviceName + ".scheduler");
        if (schedulerFile == null || schedulerFile.trim().length() == 0) {
            return "Readings Directory parameter not configured";
        }
        // get the parameter for number of past readings to display
        try {
            String time = medipi.getProperties().getProperty(MediPi.ELEMENTNAMESPACESTEM + uniqueDeviceName + ".schedulerhistoryperiod");
            if (time == null || time.trim().length() == 0) {
                time = "7";
            }
            schedulerHistoryPeriod = Integer.parseInt(time);
        } catch (NumberFormatException e) {
            throw new Exception("Unable to set the period of history to display in readings - make sure that " + MediPi.ELEMENTNAMESPACESTEM + uniqueDeviceName + ".schedulerhistoryperiod property is set correctly");
        }
        mapper.findAndRegisterModules();
        //set up watch on the schedule.schedule file
        File f = new File(schedulerFile);
        String file = f.getName();
        Path dir = f.getParentFile().toPath();

        // Call the ScheduleWatcher class which will update the message list if 
        // a new txt file appears in the configured incoming message directory
        try {
            ScheduleWatcher mw = new ScheduleWatcher(dir, file, this);
        } catch (Exception e) {
            return "Schedule Watcher failed to initialise" + e.getMessage();
        }

        //Create the table of the .scheduler items
        schedulerList = new TableView<>();
        schedulerList.setId("scheduler-schedulelist");
        items = FXCollections.observableArrayList();
        TableColumn scheduleTimeTC = new TableColumn("Time");
        scheduleTimeTC.setMinWidth(150);
        scheduleTimeTC.setCellValueFactory(
                new Callback<TableColumn.CellDataFeatures<ScheduleItem, String>, ObservableValue<String>>() {
            @Override
            public ObservableValue<String> call(TableColumn.CellDataFeatures<ScheduleItem, String> schedule) {
                SimpleStringProperty property = new SimpleStringProperty();
                Instant t = Instant.ofEpochMilli(schedule.getValue().getTime());
                property.setValue(Utilities.DISPLAY_SCHEDULE_FORMAT_LOCALTIME.format(t));

                return property;
            }
        });
        TableColumn eventTypeTC = new TableColumn("Status");
        eventTypeTC.setMinWidth(150);
        eventTypeTC.setCellValueFactory(new PropertyValueFactory<>("eventTypeDisp"));
        eventTypeTC.setCellFactory(column -> {
            return new TableCell<ScheduleItem, String>() {
                @Override
                protected void updateItem(String item, boolean empty) {
                    super.updateItem(item, empty);

                    setText(empty ? "" : getItem().toString());
                    setGraphic(null);

                    TableRow<ScheduleItem> currentRow = getTableRow();

                    if (!isEmpty()) {

                        if (item.equals(MISSING)) {
                            currentRow.setStyle("-fx-background-color:lightcoral");
                        } else if (item.equals(SCHEDULE_DUE_AT)) {
                            currentRow.setStyle("-fx-background-color:yellow");
                        } else if (item.equals(TRANSMITTED)) {
                            currentRow.setStyle("-fx-background-color:lightgreen");
                        }
                    }
                }
            };
        });
        schedulerList.setItems(items);

        schedulerList.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        schedulerList.getColumns().addAll(scheduleTimeTC, eventTypeTC);
        ScrollPane listSP = new ScrollPane();
        listSP.setContent(schedulerList);
        listSP.setFitToWidth(true);
        listSP.setFitToHeight(true);
        listSP.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        listSP.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        schedulerList.getSelectionModel().select(0);
        runScheduleNowButton = new Button("Take Readings Now", medipi.utils.getImageView("medipi.images.play", 20, 20));
        runScheduleNowButton.setId("button-runschednow");
        schedRepeatText.setId("schedule-text");
        schedRepeatText.setWrappingWidth(340);
        schedNextText.setId("schedule-text");
        schedNextText.setWrappingWidth(340);
        schedPressRunText.setId("schedule-text");
        schedPressRunText.setWrappingWidth(340);
        schedInfoText.setId("schedule-text");
        schedInfoText.setWrappingWidth(340);
        Text schedListTitle = new Text("Readings History List");
        schedListTitle.setId("schedule-text");
        VBox schedRHS = new VBox();
        schedRHS.setPadding(new Insets(20, 5, 20, 5));

        schedRHS.getChildren().addAll(
                schedListTitle,
                schedulerList
        );
        VBox schedLHS = new VBox();
        schedLHS.setPadding(new Insets(20, 5, 20, 5));
        schedLHS.setSpacing(20);
        schedLHS.getChildren().addAll(
                schedRepeatText,
                schedNextText,
                schedPressRunText,
                schedInfoText
        );

        GridPane schedGrid = new GridPane();
        ColumnConstraints col1 = new ColumnConstraints();
        col1.setPrefWidth(400);
        col1.setHalignment(HPos.CENTER);
        ColumnConstraints col2 = new ColumnConstraints();
        col2.setPrefWidth(400);
        col2.setHalignment(HPos.CENTER);
        schedGrid.getColumnConstraints().addAll(col1, col2);
        schedGrid.setId("scheduler-grid-border");
        schedGrid.setPadding(new Insets(5, 10, 10, 10));
        schedGrid.setMinSize(800, 300);
        schedGrid.setMaxSize(800, 300);
        schedGrid.add(schedLHS, 0, 0);
        schedGrid.add(schedRHS, 1, 0);

        schedulerWindow.getChildren().addAll(
                schedGrid
        );

        // set main Element window
        window.setCenter(schedulerWindow);
        setButton2(runScheduleNowButton);
        refreshSchedule();
        //set the scheduler on MediPi for access by all devices(elements)
        medipi.setScheduler(this);

        // refresh the schedule every time the Scheduler window is called
        window.visibleProperty().addListener((ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) -> {
            if (newValue) {
                refreshSchedule();
            }
        });

        // Start the scheduler timer. This wakes up every definable period (default set to 10s) 
        // and refreshes the schedule to see if the scheduler is up to date
        try {
            String time = medipi.getProperties().getProperty(MediPi.ELEMENTNAMESPACESTEM + uniqueDeviceName + ".pollscheduletimer");
            if (time == null || time.trim().length() == 0) {
                time = "10";
            }
            Integer scheduleCheckPeriod = Integer.parseInt(time);
            // Create a scheduled exec service to keep checking to see if there is a scheduled job to execute. 
            // This means that any scheduled activity will be up to x seconds late where x= the pollscheduletimer from the mediPi properties
            SCHEDULESERVICE.scheduleAtFixedRate(() -> {
                System.out.println("Schedule Timer run at: " + Instant.now());
                // when an old schedule expires and a new one is due
                if (currentScheduleExpiryTime.isBefore(Instant.now()) && lastScheduleItem != null) {
                    if (!alertBooleanProperty.get()) {
                        alertBooleanProperty.set(true);
                    }
                    // If a schedule is currently underway, inform the user, reset and restart
                    if (runningSchedule.get()) {
                        MediPiMessageBox.getInstance().makeMessage("The schedule period has just elapsed during the execution of this schedule and a new schedule must be taken. All data taken in the expired schedule has been deleted and must be retaken.");
                        Platform.runLater(() -> {
                            medipi.resetAllDevices();
                            this.callDeviceWindow();
                        });
                        runningProperty().set(false);
                    }
                    refreshSchedule();
                    // Callback any interfaces which have registered with Scheduler
                    for (SchedulerCallbacksInterface scbi : schedulerCallbacks) {
                        scbi.ScheduleExpired();
                    }
                }
            }, 0L, (long) scheduleCheckPeriod, TimeUnit.SECONDS);
        } catch (NumberFormatException e) {
            throw new Exception("Unable to get the poll period to schedule - make sure that " + MediPi.ELEMENTNAMESPACESTEM + uniqueDeviceName + ".pollscheduletimer property is set correctly");
        }
        runScheduleNowButton.setOnAction((ActionEvent t) -> {
            runSchedule();
        });
        // bind the running and the has data properties
        hasData.bind(runningSchedule);

        // At the start of running a schedule clear the data
        runningSchedule.addListener((ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) -> {
            if (newValue) {
                deviceData.clear();
            }
        });

        // successful initiation of the this class results in a null return
        return null;
    }

    /**
     * Method to return the time at which the current schedule started
     *
     * @return Date representation of the time at which the current schedule
     * started
     */
    public Instant getCurrentScheduleStartTime() {
        return currentScheduleStartTime;
    }

    /**
     * Method to return the time at which the next schedule will start
     *
     * @return Date representation of the time at which the next schedule will
     * start
     */
    public Instant getCurrentScheduleExpiryTime() {
        return currentScheduleExpiryTime;
    }

    @Override
    public String getProfileId() {
        return PROFILEID;
    }

    @Override
    public String getGenericDeviceDisplayName() {
        return NAME;
    }

    /**
     * property to inform other classes as to whether a schedule is currently
     * being run
     *
     * @return Boolean property describing run status
     */
    public BooleanProperty runningProperty() {
        return runningSchedule;
    }

    /**
     * Method to refresh the screen with respect to the .scheduler file contents
     * and current time
     */
    protected synchronized void refreshSchedule() {
        System.out.println("Schedule file changed @" + Instant.now().toString());
        // Dont update schedule if part way through recording a Scheduled event
        if (isThisElementPartOfAScheduleExecution.getValue()) {
            return;
        }
        //If scheduler file has changed or it's the first time this loop has executed load everything
        //clear the table
        items.clear();
        // load all items from file and return the chronologially latest "SCHEDULED" entry
        // a scheduler file MUST have at least one "SCHEDULED" entry line in it

        try {
            schedulesFromFile = mapper.readValue(new File(schedulerFile), new TypeReference<ArrayList<Schedule>>() {
            });

            ScheduleItem latestSchedItem = null;
            ScheduleItem latestTransItem = null;
            Instant latestSchedDate = Instant.EPOCH;
            Instant latestTransDate = Instant.EPOCH;
            Instant futureScheduleStartTime = null;
            Instant futureScheduleExpiryTime = null;
            boolean currentScheduleFound = false;
            for (Schedule s : schedulesFromFile) {
                // find the latest scheduled time and save the data
                if (s.getTime().isAfter(latestSchedDate) && s.getTime().isBefore(Instant.now()) && s.getEventType().equals(SCHEDULED)) {
                    firstScheduledTime = s.getTime();
                    scheduledRepeatPeriod = s.getRepeat();
                    latestSchedDate = s.getTime();
                    latestSchedItem = new ScheduleItem(s.getUuid(), s.getEventType(), s.getTime(), s.getRepeat(), s.getDeviceSched());
                    currentScheduleFound = true;
                } else if (s.getTime().isAfter(Instant.now()) && s.getEventType().equals(SCHEDULED)) {
                    // This is for situations where there is a schedule but only in the future
                    futureScheduleStartTime = s.getTime();
                    futureScheduleExpiryTime = s.getTime().plus(s.getRepeat(), ChronoUnit.MINUTES);
                }
                // find the latest transmitted time and save the data
                if (s.getTime().isAfter(latestTransDate) && s.getEventType().equals(TRANSMITTED)) {
                    latestTransDate = s.getTime();
                    latestTransItem = new ScheduleItem(s.getUuid(), s.getEventType(), s.getTime(), s.getRepeat(), s.getDeviceSched());
                }

                Instant historicalStart = Instant.now().minus(schedulerHistoryPeriod, ChronoUnit.DAYS);
                if (s.getTime().isAfter(historicalStart) && s.getEventType().equals(TRANSMITTED)) {
                    items.add(new ScheduleItem(s.getUuid(), s.getEventType(), s.getTime(), s.getRepeat(), s.getDeviceSched()));
                }
            }
            //Empty schedule file or no entries with type SCHEDULED or latest date in the future
            if (!currentScheduleFound) {
                if (futureScheduleStartTime != null && futureScheduleExpiryTime != null) {
                    final Instant futureDate = futureScheduleStartTime;
                    currentScheduleStartTime = futureScheduleStartTime;
                    currentScheduleExpiryTime = futureScheduleExpiryTime;
                    runScheduleNowButton.setDisable(true);
                    schedNextText.setText("* Readings due next from " + Utilities.DISPLAY_SCHEDULE_FORMAT_LOCALTIME.format(currentScheduleStartTime));
                    Platform.runLater(() -> {
                        MediPiMessageBox.getInstance().makeErrorMessage("The first Reading Schedule currently loaded in is in the future: " + futureDate.toString() + " - Schedules cannot be taken until this time", null);
                    });
                } else {
                    Platform.runLater(() -> {
                        MediPiMessageBox.getInstance().makeErrorMessage("There are no Reading Schedules - use the Settings app to create any futher Schedules", null);
                        runScheduleNowButton.setDisable(true);
                    });
                }
                return;
            } else {
                lastScheduleItem = latestSchedItem;
                //check to see if the latest entry is valid
                Instant lastTime = Instant.ofEpochMilli(lastScheduleItem.getTime());
                runScheduleNowButton.setDisable(false);
                // find next scheduled measurements
                findNextSchedule(lastScheduleItem, latestTransItem);
                // No missed readings = due to be run now
                if (missedReadings == 0) {
                    alertBooleanProperty.set(false);
                    Platform.runLater(() -> {
                        alertBanner.removeAlert(getClassTokenName());
                        schedNextText.setText("* Readings due next from " + Utilities.DISPLAY_SCHEDULE_FORMAT_LOCALTIME.format(currentScheduleExpiryTime));
                        schedPressRunText.setText("");
                    });
                } else {
                    // more than 1 missed reading
                    alertBooleanProperty.set(true);
                    Platform.runLater(new Runnable() {
                        @Override
                        public void run() {
                            alertBanner.addAlert(getClassTokenName(), "You have new Readings to take");
                            schedNextText.setText("* Your latest Readings were due on " + Utilities.DISPLAY_SCHEDULE_FORMAT_LOCALTIME.format(currentScheduleStartTime));
                            schedPressRunText.setText("* Please press 'Take Readings Now'");
                        }
                    });
                }
            }
            //now add the latest schedule to the item list
            String schedType;
            if (missedReadings == 0) {
                items.add(new ScheduleItem(
                        lastScheduleItem.getUUID(),
                        SCHEDULE_DUE_AT,
                        currentScheduleExpiryTime,
                        lastScheduleItem.getRepeat(),
                        lastScheduleItem.getDeviceSched()
                ));
            }
            items.sort(Comparator.comparing(ScheduleItem::getTime).reversed());

            int hours = lastScheduleItem.getRepeat() / 60;
            schedRepeatText.setText("* Take your Readings every " + hours + " hours");

            // Callback any interfaces which have registered with Scheduler
            for (SchedulerCallbacksInterface scbi : schedulerCallbacks) {
                scbi.ScheduleRefreshed();
            }

        } catch (IOException ex) {
            Platform.runLater(() -> {
                MediPiMessageBox.getInstance().makeErrorMessage("Loading the Readings schedule file has encountered problems. It is empty, contains no scheduled events or corrupt", ex);
                runScheduleNowButton.setDisable(true);
            });
        } catch (Exception e) {
            // any exception here returns null to report that an error has occured loading scheduler file
            Platform.runLater(() -> {
                MediPiMessageBox.getInstance().makeErrorMessage("Parsing the Readings schedule file entries has encountered problems. It may be corrupt", e);
                runScheduleNowButton.setDisable(true);
                // This code is put in place to capture a non reproducable persistant error of null pointer
                StringWriter sw = new StringWriter();
                PrintWriter pw = new PrintWriter(sw);
                e.printStackTrace(pw);
                MediPiLogger.getInstance().log(Scheduler.class.getName() + ".error", "Stack Trace: " + sw.toString());
               
            });
        }

    }

    // Method to find the time the next schedule should start based upon the last recorded SCHEDULED line in .scheduler
    private void findNextSchedule(ScheduleItem latestSchedItem, ScheduleItem latestTransItem) throws Exception {
        Instant transTime;
        //if there is no transmitter time previously recorded then set as Epoch time
        if (latestTransItem == null) {
            transTime = Instant.EPOCH;
        } else {
            transTime = Instant.ofEpochMilli(latestTransItem.getTime());
        }
        Instant schedTime = Instant.ofEpochMilli(latestSchedItem.getTime());
        int repeat = latestSchedItem.getRepeat();
        Instant schedEnd = schedTime.plus(repeat, ChronoUnit.MINUTES);
        //knowing that the time is in the past get next scheduled time before we test if it has been missed
        missedReadings = 0;
        while (true) {
            Instant schedStart = schedEnd.minus(repeat, ChronoUnit.MINUTES);

            if (schedStart.isAfter(Instant.now().minus(schedulerHistoryPeriod, ChronoUnit.DAYS))) {
                boolean found = false;
                for (ScheduleItem s : items) {
                    if (s.getEventTypeDisp().equals(TRANSMITTED)) {
                        Instant i = Instant.ofEpochMilli(s.getTime());
                        if (schedStart.isBefore(i) && schedEnd.isAfter(i)) {
                            //We have found a transmission for this schedCal iteration
                            found = true;
                            break;
                        }
                    }
                }
                if (!found) {
                    items.add(new ScheduleItem(
                            UUID.randomUUID(), MISSING,
                            schedStart,
                            0,
                            lastScheduleItem.getDeviceSched()
                    ));
                }
            }

            if (schedEnd.isAfter(Instant.now())) {
                currentScheduleExpiryTime = schedEnd;
                currentScheduleStartTime = schedStart;
                break;
            }
            if (transTime.isBefore(schedEnd)) {
                missedReadings++;
            }

            schedEnd = schedEnd.plus(repeat, ChronoUnit.MINUTES);
        }
    }

    // Method to execute the chain of elements in a schedule. A new STARTED line
    // is prepared and data in each of the elements is reset
    private void runSchedule() {
        if (!runningSchedule.get()) {
            runningSchedule.set(true);
            //encapsulate all the scheduler within a try catch so that the running scheduler boolean cannot get out of sync
            try {
                System.out.println("Scheduled event @" + currentScheduleExpiryTime + " - now!" + Instant.now());
                //Record the start of the run
                nextUUID = UUID.randomUUID();
                addScheduleData("STARTED", Instant.now(), lastScheduleItem.getDeviceSched());
                // Reset all the devices to be taken
                for (String s : lastScheduleItem.getDeviceSched()) {
                    Element e = medipi.getElement(s);
                    if (e == null) {
                        MediPiMessageBox.getInstance().makeErrorMessage("Readings Scheduler is expecting a device called '" + s + "' but cannot find it in the schedule.schedule file", null);
                        throw new Exception("Scheduler is expecting a device called '" + s + "' but cannot find it in the schedule.schedule file", null);
                    }
                    if (Device.class.isAssignableFrom(e.getClass())) {
                        Device d = (Device) e;
                        if (d.confirmReset()) {
                            d.resetDevice();
                        } else {
                            runningSchedule.set(false);
                            return;
                        }
                    }

                }

                //Call the first device in the list and pass the remaining ones into the recursive Element.callDeviceWindow() 
                ArrayList<String> d = lastScheduleItem.getDeviceSched();
                String firstDevice = d.get(0);
                ArrayList<String> remainingDevices = new ArrayList(d.subList(1, d.size()));
                medipi.hideAllWindows();
                Element e = medipi.getElement(firstDevice);
                e.callDeviceWindow(new ArrayList<>(), remainingDevices);
            } catch (Exception ex) {
                runningSchedule.set(false);
            }
        }
    }

    // Method to write all the newly added .scheduler lines to the .scheduler 
    // file when the transmission has been successful
    private synchronized boolean writeAllSchedulesToFile() {

        if (schedulesFromFile == null) {
            return false;
        }
        try {
            for (ScheduleItem si : deviceData) {
                // dependent on a preoperties flag record entries for MEASURED and STARTED        
                if (!recordExtraMetadata) {
                    if (si.getEventTypeDisp().equals(STARTED) || si.getEventTypeDisp().equals(MEASURED)) {
                        continue;
                    }
                }
                schedulesFromFile.add(new Schedule(si));
            }

            FileOutputStream output;
            output = new FileOutputStream(schedulerFile);
            mapper.writeValue(output, schedulesFromFile);
            output.flush();
            output.close();

            return true;
        } catch (Exception ex) {
            Logger.getLogger(Scheduler.class.getName()).log(Level.SEVERE, null, ex);
            return false;
        }
    }

    @Override
    public BorderPane getDashboardTile() throws Exception {
        DashboardTile dashComponent = new DashboardTile(this, showTile);
        dashComponent.addTitle(NAME);
        dashComponent.addOverlay(new SimpleObjectProperty<>(alertImage), alertBooleanProperty);
        dashComponent.addOverlay(Color.LIGHTPINK, alertBooleanProperty);
        return dashComponent.getTile();
    }

    @Override
    public DeviceDataDO getData() {
        DeviceDataDO payload = new DeviceDataDO(UUID.randomUUID().toString());
        String separator = medipi.getDataSeparator();
        StringBuilder sb = new StringBuilder();
        //Add MetaData
        sb.append("metadata->persist->medipiversion->").append(medipi.getVersion()).append("\n");
        sb.append("metadata->make->").append(getMake()).append("\n");
        sb.append("metadata->model->").append(getModel()).append("\n");
        sb.append("metadata->displayname->").append(getSpecificDeviceDisplayName()).append("\n");
        sb.append("metadata->datadelimiter->").append(medipi.getDataSeparator()).append("\n");
        sb.append("metadata->scheduleeffectivedate->").append(Utilities.ISO8601FORMATDATEMILLI_UTC.format(getCurrentScheduleStartTime())).append("\n");
        sb.append("metadata->scheduleexpirydate->").append(Utilities.ISO8601FORMATDATEMILLI_UTC.format(getCurrentScheduleExpiryTime())).append("\n");
        sb.append("metadata->columns->")
                .append("iso8601time").append(medipi.getDataSeparator())
                .append("id").append(medipi.getDataSeparator())
                .append("type").append(medipi.getDataSeparator())
                .append("repeat").append(medipi.getDataSeparator())
                .append("devices").append("\n");
        sb.append("metadata->format->")
                .append("DATE").append(medipi.getDataSeparator())
                .append("STRING").append(medipi.getDataSeparator())
                .append("STRING").append(medipi.getDataSeparator())
                .append("INTEGER").append(medipi.getDataSeparator())
                .append("STRING").append("\n");
        sb.append("metadata->units->")
                .append("NONE").append(medipi.getDataSeparator())
                .append("NONE").append(medipi.getDataSeparator())
                .append("NONE").append(medipi.getDataSeparator())
                .append("NONE").append(medipi.getDataSeparator())
                .append("NONE").append("\n");
        for (ScheduleItem sched : deviceData) {
            sb.append(Instant.ofEpochMilli(sched.getTime()).toString());
            sb.append(separator);
            sb.append(sched.getUUIDDisp());
            sb.append(separator);
            sb.append(sched.getEventTypeDisp());
            sb.append(separator);
            sb.append(sched.getRepeatDisp());
            sb.append(separator);
            StringBuilder devices = new StringBuilder();
            for (String dev : sched.getDeviceSched()) {
                devices.append(dev);
                devices.append(" ");
            }
            sb.append(devices.toString().trim());
            sb.append("\n");
        }
        payload.setProfileId(PROFILEID);
        payload.setPayload(sb.toString());
        return payload;
    }

    @Override
    public synchronized void resetDevice() {
        nextUUID = null;
        lastScheduleItem = null;
        items.clear();
        refreshSchedule();
        resultsSummary.setValue("");
    }

    // method to add new .scheduler lines ready to be written to the .scheduler file for this schedule without repeat period
    void addScheduleData(String type, Instant date, ArrayList<String> devices) {

        addScheduleData(nextUUID, type, date, 0, devices);
    }

    // method to add new .scheduler lines ready to be written to the .scheduler file
    void addScheduleData(UUID uuid, String type, Instant date, int repeat, ArrayList<String> devices) {
        // protect against adding MEASURED entries more than once
        if (type.equals(MEASURED)) {
            for (ScheduleItem si : deviceData) {
                if (si.getEventTypeDisp().equals(MEASURED)) {
                    if (si.getDeviceSched().get(0).equals(devices.get(0))) {
                        if (si.getUUID().equals(uuid)) {
                            return;
                        }
                    }
                }
            }
        }
        ScheduleItem newSched = new ScheduleItem(uuid, type, date, repeat, devices);
        deviceData.add(newSched);
        if (type.equals(TRANSMITTED) || type.equals(SCHEDULED)) {
            writeAllSchedulesToFile();
            if (type.equals(SCHEDULED)) {
                refreshSchedule();
            }
        }
        resultsSummary.setValue(SCHEDTAKEN);

    }

    /**
     * method to get the Make of the device
     *
     * @return make and model of device
     */
    @Override
    public String getMake() {
        return MAKE;
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
        return DISPLAYNAME;
    }

    @Override
    public StringProperty getResultsSummary() {
        return resultsSummary;
    }

    @Override
    public void setData(ArrayList<ArrayList<String>> deviceData
    ) {
        throw new UnsupportedOperationException("This method is not used as the class has no extensions");
    }

    public ArrayList<Element> getScheduledElements() throws Exception {
        ArrayList<Element> schedElements = new ArrayList<>();
        if (lastScheduleItem != null) {
            for (String s : lastScheduleItem.getDeviceSched()) {
                Element e = medipi.getElement(s);
                if (e == null) {
                    MediPiMessageBox.getInstance().makeErrorMessage("Readings Scheduler is expecting a device called '" + s + "' but cannot find it in the schedule.schedule file", null);
                    throw new Exception("Scheduler is expecting a device called '" + s + "' but cannot find it in the schedule.schedule file", null);

                }
                schedElements.add(e);
            }
        }
        return schedElements;

    }
    // Method to return any elements of the schedule which have not had data taken against them

    public ArrayList<Element> getMissingDataElements() throws Exception {
        ArrayList<Element> missing = new ArrayList<>();
        for (Element e : getScheduledElements()) {
            if (Device.class.isAssignableFrom(e.getClass())) {
                boolean found = false;
                for (ScheduleItem sch : deviceData) {
                    if (sch.getEventTypeDisp().equals(MEASURED)) {
                        if (sch.getDeviceSched().size() != 1) {
                            MediPiMessageBox.getInstance().makeErrorMessage("Readings Scheduler records are corrupt: MEASURED schedule object has > 1 device", null);
                            break;
                        } else if (e.getClassTokenName().equals(sch.getDeviceSched().get(0))) {
                            found = true;
                            break;
                        }

                    }
                }
                if (!found) {
                    missing.add(e);
                }
            }
        }
        return missing;
    }

    public void registerScheduleCallbacks(SchedulerCallbacksInterface callbacksInterface) {
        schedulerCallbacks.add(callbacksInterface);
    }

    public Instant getScheduledFirstScheduleTime() {
        return firstScheduledTime;
    }

    public int getScheduledRepeatPeriod() {
        return scheduledRepeatPeriod;
    }
}
