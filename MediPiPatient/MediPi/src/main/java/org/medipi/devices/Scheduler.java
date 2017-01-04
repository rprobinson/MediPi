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
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.Collator;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Comparator;
import java.util.UUID;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.ObservableMap;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.event.ActionEvent;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.CacheHint;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Separator;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.effect.Blend;
import javafx.scene.effect.BlendMode;
import javafx.scene.effect.ColorAdjust;
import javafx.scene.effect.ColorInput;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.util.Callback;
import org.medipi.utilities.ConfigurationStringTokeniser;
import org.medipi.DashboardTile;
import org.medipi.MediPi;
import org.medipi.MediPiMessageBox;
import org.medipi.MediPiProperties;
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
 * of activity over a configurable period (default period 7 days)
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

    private static final String NAME = "Scheduler";
    private static final String PROFILEID = "urn:nhs-en:profile:Scheduler";
    private static final String MAKE = "NONE";
    private static final String MODEL = "NONE";
    private static final String DISPLAYNAME = "MediPi Scheduler";
    private static final String MEDIPIIMAGESEXCLAIM = "medipi.images.exclaim";
    private static final String SCHEDTAKEN = "Schedule taken";
    private VBox schedulerWindow;
    private TableView<Schedule> schedulerList;
    // not sure what the best setting for this thread pool is but 3 seems to work
    private static final int TIMER_THREAD_POOL_SIZE = 3;
    private String schedulerFile;
    private ImageView alertImageView;
    private final ArrayList<Schedule> deviceData = new ArrayList<>();

    private ObservableList<Schedule> items;
    private Instant currentScheduledEventTime = Instant.EPOCH;
    private Instant nextScheduledEventTime = Instant.EPOCH;
    private int missedReadings = 0;
    private final BooleanProperty alertBooleanProperty = new SimpleBooleanProperty(false);
    private ObservableMap alertBanner;
    private final StringProperty resultsSummary = new SimpleStringProperty();

    private final Text schedRepeatText = new Text();
    private final Text schedPressRunText = new Text();
    private final Text schedInfoText = new Text("* You may run and submit a schedule at any time");
    private final Text schedNextText = new Text();
    private Button runScheduleNowButton;
    //This is the most recent schedule
    private Schedule lastSchedule = null;
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
    private UUID nextUUID = null;

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
        alertImageView = new ImageView("file:///" + alertImageFile);
        //ascertain if this element is to be displayed on the dashboard
        String b = MediPiProperties.getInstance().getProperties().getProperty(MediPi.ELEMENTNAMESPACESTEM + uniqueDeviceName + ".showdashboardtile");
        if (b == null || b.trim().length() == 0) {
            showTile = new SimpleBooleanProperty(true);
        } else {
            showTile = new SimpleBooleanProperty(!b.toLowerCase().startsWith("n"));
        }
        // Find location of scheduler file
        schedulerFile = medipi.getProperties().getProperty(MediPi.ELEMENTNAMESPACESTEM + uniqueDeviceName + ".scheduler");
        if (schedulerFile == null || schedulerFile.trim().length() == 0) {
            return "Scheduler Directory parameter not configured";
        }
        // get the parameter for number of past readings to display
        try {
            String time = medipi.getProperties().getProperty(MediPi.ELEMENTNAMESPACESTEM + uniqueDeviceName + ".schedulerhistoryperiod");
            if (time == null || time.trim().length() == 0) {
                time = "7";
            }
            schedulerHistoryPeriod = Integer.parseInt(time);
        } catch (NumberFormatException e) {
            throw new Exception("Unable to set the period of history to display in scheduler - make sure that " + MediPi.ELEMENTNAMESPACESTEM + uniqueDeviceName + ".schedulerhistoryperiod property is set correctly");
        }
        //set up watch on the schedule.schedule file
        File f = new File(schedulerFile);
        String file = f.getName();
        Path dir = f.getParentFile().toPath();

        // Call the MessageWatcher class which will update the message list if 
        // a new txt file appears in the configured incoming message directory
        try {
            ScheduleWatcher mw = new ScheduleWatcher(dir, file, this);
        } catch (Exception e) {
            return "Message Watcher failed to initialise" + e.getMessage();
        }

        //Create the table of the .scheduler items
        schedulerList = new TableView<>();
        schedulerList.setId("scheduler-schedulelist");
        items = FXCollections.observableArrayList();
        TableColumn scheduleTimeTC = new TableColumn("Time");
        scheduleTimeTC.setMinWidth(150);
        scheduleTimeTC.setCellValueFactory(
                new Callback<TableColumn.CellDataFeatures<Schedule, String>, ObservableValue<String>>() {
            @Override
            public ObservableValue<String> call(TableColumn.CellDataFeatures<Schedule, String> schedule) {
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
            return new TableCell<Schedule, String>() {
                @Override
                protected void updateItem(String item, boolean empty) {
                    super.updateItem(item, empty);

                    setText(empty ? "" : getItem().toString());
                    setGraphic(null);

                    TableRow<Schedule> currentRow = getTableRow();

                    if (!isEmpty()) {

                        if (item.equals("MISSING")) {
                            currentRow.setStyle("-fx-background-color:lightcoral");
                        } else if (item.equals("SCHEDULE NOW DUE")) {
                            currentRow.setStyle("-fx-background-color:lightcoral");
                        } else if (item.equals("SCHEDULE DUE AT")) {
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
        runScheduleNowButton = new Button("Run Schedule Now", medipi.utils.getImageView("medipi.images.play", 20, 20));
        runScheduleNowButton.setId("button-runschednow");
        schedRepeatText.setId("schedule-text");
        schedRepeatText.setWrappingWidth(340);
        schedNextText.setId("schedule-text");
        schedNextText.setWrappingWidth(340);
        schedPressRunText.setId("schedule-text");
        schedPressRunText.setWrappingWidth(340);
        schedInfoText.setId("schedule-text");
        schedInfoText.setWrappingWidth(340);
        Text schedListTitle = new Text("Schedule List");
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
        col1.setPercentWidth(350);
        col1.setHalignment(HPos.CENTER);
        ColumnConstraints col2 = new ColumnConstraints();
        col2.setPercentWidth(300);
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

        alertBanner = medipi.getLowerBannerAlert();
        // set main Element window
        window.setCenter(schedulerWindow);
        setButton2(runScheduleNowButton);
        refreshSchedule();
        //set the scheduler on all devices(elements)
        // This relies on the fact that the scheduler is called AFTER all the measurement devices
        for (String s : lastSchedule.getDeviceSched()) {
            Element e = medipi.getElement(s);
            if (e != null) {
                // set this Schedule in each of the devices to be run for callbacks
                e.setScheduler(this);
            }
        }
        setScheduler(this);

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
            // Create a scheduled Thread Pool exec to keep checking to see if there is a scheduled job to execute. 
            // This means that any scheduled activity will be up to x seconds late where x= the pollscheduletimer from the mediPi properties
            ScheduledThreadPoolExecutor timer = new ScheduledThreadPoolExecutor(TIMER_THREAD_POOL_SIZE);
            timer.scheduleWithFixedDelay(() -> {
                if (nextScheduledEventTime.isBefore(Instant.now()) && lastSchedule != null) {
                    if (!alertBooleanProperty.get()) {
                        alertBooleanProperty.set(true);
                        refreshSchedule();
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
    public Instant getCurrentScheduledEventTime() {
        return currentScheduledEventTime;
    }

    /**
     * Method to return the time at which the next schedule will start
     *
     * @return Date representation of the time at which the next schedule will
     * start
     */
    public Instant getNextScheduledEventTime() {
        return nextScheduledEventTime;
    }

    @Override
    public String getProfileId() {
        return PROFILEID;
    }

    @Override
    public String getType() {
        return NAME;
    }

    /**
     * property to inform other classes as to whether a schedule is currently being run
     * @return Boolean property describing run status
     */
    public BooleanProperty runningProperty() {
        return runningSchedule;
    }

    /**
     * Method to refresh the screen with respect to the .scheduler file contents
     * and current time
     */
    protected void refreshSchedule() {
        if (medipi.getDebugMode() == MediPi.DEBUG) {
            System.out.println("Schedule file changed @" + Instant.now().toString());
        }
        // Dont update schedule if part way through recording a Scheduled event
        if (isSchedule.getValue()) {
            return;
        }
        //If scheduler file has changed or it's the first time this loop has executed load everything
        //clear the table
        items.clear();
        // load all items from file and return the chronologially latest "SCHEDULED" entry
        // a scheduler file MUST have at least one "SCHEDULED" entry line in it
        Schedule latestSched = null;
        Schedule latestTrans = null;
        try (InputStream is = Files.newInputStream(Paths.get(schedulerFile));
                BufferedReader br = new BufferedReader(new InputStreamReader(is))) {
            String line = null;
            Instant latestSchedDate = Instant.EPOCH;
            Instant latestTransDate = Instant.EPOCH;
            while ((line = br.readLine()) != null) {
                if (line.trim().startsWith("#")) {
                    continue;
                }
                if (line.trim().isEmpty()) {
                    continue;
                }
                ConfigurationStringTokeniser st = new ConfigurationStringTokeniser(line);
                if (st.countTokens() < 4) {
                    throw new Exception("The schdule file has become corrupted");
                }
                //schedule number
                UUID uuid = UUID.fromString(st.nextToken());
                //schedule status
                String status = st.nextToken().toUpperCase();
                //schedule time
                String d = st.nextToken();
                Instant time = Instant.parse(d);

                //repeat time in mins
                int repeat = Integer.parseInt(st.nextToken());
                //Subsequent Tokens  = devices to be called
                ArrayList<String> deviceList = new ArrayList<>();
                while (st.hasMoreTokens()) {
                    String s = st.nextToken();
                    deviceList.add(s);
                }
                // find the latest scheduled time and save the data
                if (time.isAfter(latestSchedDate) && status.equals(SCHEDULED)) {
                    latestSchedDate = time;
                    latestSched = new Schedule(uuid, status, time, repeat, deviceList);
                }
                // find the latest transmitted time and save the data
                if (time.isAfter(latestTransDate) && status.equals(TRANSMITTED)) {
                    latestTransDate = time;
                    latestTrans = new Schedule(uuid, status, time, repeat, deviceList);
                }

                Instant historicalStart = Instant.now().minus(schedulerHistoryPeriod, ChronoUnit.DAYS);
                if (time.isAfter(historicalStart) && status.equals(TRANSMITTED)) {
                    items.add(new Schedule(uuid, status, time, repeat, deviceList));
                }
            }
            //Empty schedule file or no entries with type SCHEDULED or latest date in the future
            if (latestSched == null || latestSchedDate.isAfter(Instant.now())) {
                Platform.runLater(() -> {
                    MediPiMessageBox.getInstance().makeErrorMessage("Loading the schedule file has encountered problems. It is empty, contains no scheduled events or corrupt", null);
                    runScheduleNowButton.setDisable(true);
                });
            } else {
                lastSchedule = latestSched;
                //check to see if the latest entry is valid
                Instant lastTime = Instant.ofEpochMilli(lastSchedule.getTime());
                runScheduleNowButton.setDisable(false);
                // find next scheduled measurements
                findNextSchedule(lastSchedule, latestTrans);
                // No missed readings = due to be run now
                if (missedReadings == 0) {
                    alertBooleanProperty.set(false);
                    Platform.runLater(() -> {
                        alertBanner.remove(getClassTokenName());
                        schedNextText.setText("* Your next schedule is due at " + Utilities.DISPLAY_SCHEDULE_FORMAT_LOCALTIME.format(nextScheduledEventTime));
                        schedPressRunText.setText("");
                    });
                } else {
                    // more than 1 missed reading
                    alertBooleanProperty.set(true);
                    Platform.runLater(new Runnable() {
                        @Override
                        public void run() {
                            alertBanner.put(getClassTokenName(), "You have measurements to take");
                            schedNextText.setText("* Your next schedule was due at " + Utilities.DISPLAY_SCHEDULE_FORMAT_LOCALTIME.format(nextScheduledEventTime));
                            schedPressRunText.setText("* Please press 'Run Schedule Now'");
                        }
                    });
                }
            }
            //now add the latest schedule to the item list
            String schedType;
            if (missedReadings > 0) {
                schedType = "SCHEDULE NOW DUE";
            } else {
                schedType = "SCHEDULE DUE AT";
            }
            items.add(new Schedule(
                    lastSchedule.getUUID(),
                    schedType,
                    nextScheduledEventTime,
                    lastSchedule.getRepeat(),
                    lastSchedule.getDeviceSched()
            ));
            items.sort(Comparator.comparing(Schedule::getTime).reversed());

            double days = lastSchedule.getRepeat() / 1440;
            schedRepeatText.setText("* Your schedule occurs every " + days + " day(s)");

        } catch (Exception e) {
            // any exception here returns null to report that an error has occured loading scheduler file
            Platform.runLater(() -> {
                MediPiMessageBox.getInstance().makeErrorMessage("Loading the schedule file has encountered problems. It is empty, contains no scheduled events or corrupt", e);
                runScheduleNowButton.setDisable(true);
            });
        }

    }

    // Method to find the time the next schedule should start based upon the last recorded SCHEDULED line in .scheduler
    private void findNextSchedule(Schedule latestSched, Schedule latestTrans) throws Exception {
        Instant transTime;
        //if there is no transmitter time previously recorded then set as Epoch time
        if (latestTrans == null) {
            transTime = Instant.EPOCH;
        } else {
            transTime = Instant.ofEpochMilli(latestTrans.getTime());
        }
        Instant schedTime = Instant.ofEpochMilli(latestSched.getTime());
        int repeat = latestSched.getRepeat();
        Instant schedEnd = schedTime.plus(repeat, ChronoUnit.MINUTES);
        //knowing that the time is in the past get next scheduled time before we test if it has been missed
        missedReadings = 0;
        while (true) {
            Instant schedStart = schedEnd.minus(repeat, ChronoUnit.MINUTES);

            if (schedStart.isAfter(Instant.now().minus(schedulerHistoryPeriod, ChronoUnit.DAYS))) {
                boolean found = false;
                for (Schedule s : items) {
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
                    items.add(new Schedule(
                            UUID.randomUUID(),
                            "MISSING",
                            schedStart,
                            0,
                            lastSchedule.getDeviceSched()
                    ));
                }
            }

            if (schedEnd.isAfter(Instant.now())) {
                nextScheduledEventTime = schedEnd;
                currentScheduledEventTime = schedStart;
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
                if (medipi.getDebugMode() == MediPi.DEBUG) {
                    System.out.println("Scheduled event @" + nextScheduledEventTime + " - now!" + Instant.now());
                }
                //Record the start of the run
                nextUUID = UUID.randomUUID();
                Schedule started = new Schedule(nextUUID, "STARTED", Instant.now(), lastSchedule.getRepeat(), lastSchedule.getDeviceSched());
                deviceData.add(started);
                // Reset all the devices to be taken
                for (String s : lastSchedule.getDeviceSched()) {
                    Element e = medipi.getElement(s);
                    if (e == null) {
                        MediPiMessageBox.getInstance().makeErrorMessage("Scheduler is expecting a device called '" + s + "' but cannot find it in the schedule.schedule file", null);
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
                ArrayList<String> d = lastSchedule.getDeviceSched();
                String firstDevice = d.get(0);
                ArrayList<String> remainingDevices = new ArrayList(d.subList(1, d.size()));
                medipi.hideAllWindows();
                Element e = medipi.getElement(firstDevice);
                e.callDeviceWindow(remainingDevices);
            } catch (Exception ex) {
                runningSchedule.set(false);
            }
        }
    }

    // Method to write all the newly added .scheduler lines to the .scheduler 
    // file when the transmission has been sucessful
    private boolean writeNewScheduleLineToFile(ArrayList<Schedule> s) {

        try {
            Writer output;
            output = new BufferedWriter(new FileWriter(schedulerFile, true));
            for (Schedule sched : s) {
                output.append(System.getProperty("line.separator"));
                output.append(sched.getUUIDDisp());
                output.append(" ");
                output.append(sched.getEventTypeDisp());
                output.append(" ");
                output.append(Instant.ofEpochMilli(sched.getTime()).toString());
                output.append(" ");
                output.append(sched.getRepeatDisp());
                output.append(" ");
                output.append(sched.getDeviceSchedDisp());
            }
            output.flush();
            output.close();
            return true;
        } catch (IOException ex) {
            Logger.getLogger(Scheduler.class.getName()).log(Level.SEVERE, null, ex);
            return false;
        }
    }

    @Override
    public BorderPane getDashboardTile() throws Exception {
        DashboardTile dashComponent = new DashboardTile(this, showTile);
        dashComponent.addTitle(NAME);
        dashComponent.addOverlay(alertImageView, alertBooleanProperty);
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
        sb.append("metadata->displayname->").append(getDisplayName()).append("\n");
        sb.append("metadata->datadelimiter->").append(medipi.getDataSeparator()).append("\n");
        sb.append("metadata->scheduleeffectivedate->").append(Utilities.ISO8601FORMATDATEMILLI_UTC.format(scheduler.getCurrentScheduledEventTime())).append("\n");
        sb.append("metadata->scheduleexpirydate->").append(Utilities.ISO8601FORMATDATEMILLI_UTC.format(scheduler.getNextScheduledEventTime())).append("\n");
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
        for (Schedule sched : deviceData) {
            sb.append(Instant.ofEpochMilli(sched.getTime()).toString());
            sb.append(separator);
            sb.append(sched.getUUIDDisp());
            sb.append(separator);
            sb.append(sched.getEventTypeDisp());
            sb.append(separator);
            sb.append(sched.getRepeatDisp());
            sb.append(separator);
            sb.append(sched.getDeviceSchedDisp());
            sb.append("\n");
        }
        payload.setProfileId(PROFILEID);
        payload.setPayload(sb.toString());
        return payload;
    }

    @Override
    public void resetDevice() {
        nextUUID = null;
        lastSchedule = null;
        items.clear();
        refreshSchedule();
        resultsSummary.setValue("");
    }

    // method to add new .scheduler lines ready to be written to the .scheduler file
    void addScheduleData(String type, Instant date, ArrayList<String> devices) {
        Schedule newSched = new Schedule(nextUUID, type, date, 0, devices);
        deviceData.add(newSched);
        if (type.equals(TRANSMITTED)) {
            writeNewScheduleLineToFile(deviceData);
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
    public String getDisplayName() {
        return DISPLAYNAME;
    }

    @Override
    public StringProperty getResultsSummary() {
        return resultsSummary;
    }

    @Override
    public void setData(ArrayList<ArrayList<String>> deviceData) {
        throw new UnsupportedOperationException("This method is not used as the class has no extensions");
    }

}
