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
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.UUID;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.CacheHint;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Separator;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.effect.Blend;
import javafx.scene.effect.BlendMode;
import javafx.scene.effect.ColorAdjust;
import javafx.scene.effect.ColorInput;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import org.medipi.utilities.ConfigurationStringTokeniser;
import org.medipi.DashboardTile;
import org.medipi.MediPi;
import org.medipi.MediPiProperties;
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
 * Dependent on the view mode the UI displays either a table of the scheduled
 * lines from the .scheduler file or a graphical representation of each of the
 * elements and the order in which they will be executed.
 *
 * The scheduler will provide data to the transmitter of this schedule only -
 * metadata is added to identify it.
 *
 * After a schedule has been transmitted, new STARTED, MEASURED and TRANSMITTED
 * lines are added to the .scheduler file
 *
 *
 * TODO: This is a new class and some of the UI needs some work - advanced view
 * is out of alignment and certain information is not being shown in the basic
 * view e.g. status
 *
 * @author rick@robinsonhq.com
 */
public class Scheduler extends Device {

    private static final String NAME = "Scheduler";
    private static final String PROFILEID = "urn:nhs-en:profile:Scheduler";
    private static final String MEDIPIIMAGESEXCLAIM = "medipi.images.exclaim";
    private static final String MEDIPIIMAGESARROW = "medipi.images.arrow";
    private static final String MEDIPIIMAGESPASS = "medipi.images.pass";
    private static final String MEDIPIIMAGESFAIL = "medipi.images.fail";
    private VBox schedulerWindow;
    private TableView<Schedule> schedulerList;
    // not sure what the best setting for this thread pool is but 3 seems to work
    private static final int TIMER_THREAD_POOL_SIZE = 3;
    private String schedulerFile;
    private ImageView alertImageView;
    private final ArrayList<Schedule> deviceData = new ArrayList<>();
    // property to indicate whether data has bee recorded for this device
    private final BooleanProperty hasData = new SimpleBooleanProperty(false);

    private ObservableList<Schedule> items;
    private Date nextScheduledEventTime = new Date(0L);
    private int missedReadings = 0;
    private final BooleanProperty alertBooleanProperty = new SimpleBooleanProperty(false);

    private TextArea scheduleStatus;
    private Button left;
    private Schedule lastSchedule = null;

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
        schedulerWindow.setMinSize(800, 350);
        schedulerWindow.setMaxSize(800, 350);
        schedulerWindow.setAlignment(Pos.TOP_CENTER);
        // get details of all images which are required
        String alertImageFile = medipi.getProperties().getProperty(MEDIPIIMAGESEXCLAIM);
        alertImageView = new ImageView("file:///" + alertImageFile);

        // Find location of scheduler file
        schedulerFile = medipi.getProperties().getProperty(MediPi.ELEMENTNAMESPACESTEM + uniqueDeviceName + ".scheduler");
        if (schedulerFile == null || schedulerFile.trim().length() == 0) {
            return "Scheduler Directory parameter not configured";
        }

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
        TableColumn uuidTC = new TableColumn("UUID");
        uuidTC.setMinWidth(50);
        uuidTC.setCellValueFactory(
                new PropertyValueFactory<>("eventIdDisp"));
        TableColumn eventTypeTC = new TableColumn("Type");
        eventTypeTC.setMinWidth(150);
        eventTypeTC.setCellValueFactory(
                new PropertyValueFactory<>("eventTypeDisp"));
        TableColumn scheduleTimeTC = new TableColumn("Time");
        scheduleTimeTC.setMinWidth(150);
        scheduleTimeTC.setCellValueFactory(
                new PropertyValueFactory<>("timeDisp"));
        TableColumn repeatTC = new TableColumn("Repeat");
        repeatTC.setMinWidth(70);
        repeatTC.setCellValueFactory(
                new PropertyValueFactory<>("repeatDisp"));
        TableColumn schedDevicesTC = new TableColumn("Devices");
        schedDevicesTC.setMinWidth(400);
        schedDevicesTC.setCellValueFactory(
                new PropertyValueFactory<>("deviceSchedDisp"));
        schedulerList.setMinHeight(180);
        schedulerList.setMaxHeight(180);
        schedulerList.setItems(items);
        schedulerList.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        schedulerList.getColumns().addAll(uuidTC, eventTypeTC, scheduleTimeTC, repeatTC, schedDevicesTC);
        ScrollPane listSP = new ScrollPane();
        listSP.setContent(schedulerList);
        listSP.setFitToWidth(true);
        listSP.setFitToHeight(true);
        listSP.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        listSP.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        schedulerList.getSelectionModel().select(0);
        // text area to contain the status of the schedule event
        scheduleStatus = new TextArea();
        scheduleStatus.setWrapText(true);
        scheduleStatus.isResizable();
        scheduleStatus.setEditable(false);
        scheduleStatus.setId("messenger-messagecontent");
        scheduleStatus.setMinHeight(70);
        scheduleStatus.setMaxHeight(70);
        ScrollPane viewStatus = new ScrollPane();
        viewStatus.setContent(scheduleStatus);
        viewStatus.setFitToWidth(true);
        viewStatus.setFitToHeight(true);
        viewStatus.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        viewStatus.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        left = new Button("Run Schedule Now", medipi.utils.getImageView("medipi.images.play", 20, 20));
        left.setId("button-runschednow");

        Label title = new Label("List of schedules");
        title.setId("schedule-title");
        Label status = new Label("Status");
        status.setId("schedule-title");

        //Decide whether to show basic or advanced view
        if (!medipi.isBasicDataView()) {
            schedulerWindow.getChildren().addAll(
                    title,
                    schedulerList,
                    status,
                    scheduleStatus,
                    new Separator(Orientation.HORIZONTAL)
            );
        }

        // set main Element window
        window.setCenter(schedulerWindow);
        setButton2(left);
        refreshSchedule();
        // refresh the schedule every time the Schedule window is called
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
                if (nextScheduledEventTime.before(new Date()) && lastSchedule != null) {
                    if (!alertBooleanProperty.get()) {
                        alertBooleanProperty.set(true);
                        refreshSchedule();
                    }
                }
            }, 0L, (long) scheduleCheckPeriod, TimeUnit.SECONDS);
        } catch (NumberFormatException e) {
            throw new Exception("Unable to start the incoming message service - make sure that " + MediPi.ELEMENTNAMESPACESTEM + uniqueDeviceName + ".pollincomingmsgperiod property is set correctly");
        }
        left.setOnAction((ActionEvent t) -> {
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
        return NAME;
    }

    /**
     *
     * @return
     */
    public BooleanProperty runningProperty() {
        return runningSchedule;
    }

    // Method to create/update the basic view of the scheduler 
    private void updateBasicSchedView(Schedule sched, Schedule trans) {
        HBox scheduled = new HBox();
        scheduled.setAlignment(Pos.CENTER);
        HBox transmitted = new HBox();
        transmitted.setAlignment(Pos.CENTER);
        ArrayList<String> transmittedDevices;

        // defining details of the last transmitted schedule - if present
        Label lastTransmittedLabel;
        if (trans == null) {
            transmittedDevices = new ArrayList<>(Arrays.asList(""));
            lastTransmittedLabel = getLabel("No previous transmitted schedules", "schedule-text");
        } else {
            transmittedDevices = trans.getDeviceSched();
            lastTransmittedLabel = getLabel("Last Recorded Schedule: " + trans.getTimeDisp(), "schedule-text");

        }

        // Defining details of the Status of the schedule
        Label status;
        if (missedReadings == 0) {
            status = getLabel("No schedules are due", "schedule-title");
            status.setTextFill(Color.GREEN);
        } else {
            status = getLabel("Please 'Run Schedule Now'. You have missed " + missedReadings + " measurements", "schedule-title");
            status.setTextFill(Color.RED);
        }

        // create flow diagrams of the images of elements to be run in future schedules
        // (painted red or green depending on whether they are overdue or on time)
        // and those that have been run in the last schedule executed 
        // (with a pass or fail badge superimposed).
        for (String s : sched.getDeviceSched()) {
            Element elem = medipi.getElement(s);

            Transmitter st = null;
            if (Transmitter.class.isAssignableFrom(elem.getClass())) {
                st = (Transmitter) elem;
            }
            if (st == null) {
                boolean found = false;
                for (String t : transmittedDevices) {
                    // if transmitted element found in list of scheduled elements then pass badge
                    if (s.equals(t)) {
                        found = true;
                        transmitted.getChildren().add(getImage(elem, true));
                        transmitted.getChildren().add(medipi.utils.getImageView(MEDIPIIMAGESARROW, 40, 40));
                    }
                }
                // if transmitted element not found in list of scheduled elements then fail badge
                if (!found) {
                    transmitted.getChildren().add(getImage(elem, false));
                    transmitted.getChildren().add(medipi.utils.getImageView(MEDIPIIMAGESARROW, 40, 40));
                }
            }
            scheduled.getChildren().add(getImage(elem, null));
            scheduled.getChildren().add(medipi.utils.getImageView(MEDIPIIMAGESARROW, 40, 40));
        }
        // remove the last element which will be an arrow
        scheduled.getChildren().remove(scheduled.getChildren().size() - 1);
        transmitted.getChildren().remove(transmitted.getChildren().size() - 1);

        Platform.runLater(() -> {
            // update the scheduler window
            if (schedulerWindow.getChildren().size() > 0) {
                schedulerWindow.getChildren().clear();
            }
            schedulerWindow.getChildren().addAll(
                    status,
                    new Separator(Orientation.HORIZONTAL),
                    lastTransmittedLabel,
                    transmitted,
                    new Separator(Orientation.HORIZONTAL),
                    getLabel("Next Scheduled Event: " + Utilities.DISPLAY_SCHEDULE_FORMAT.format(nextScheduledEventTime), "schedule-text"),
                    scheduled,
                    new Separator(Orientation.HORIZONTAL)
            );
        });
    }

    // private method for adding the CSS id to a Label - should be subsumed into Utilities generally 
    private Label getLabel(String text, String id) {
        Label l = new Label(text);
        l.setId(id);
        return l;
    }

    // private method for returning a node containing and image for the element passed
    // depending on the state of the Measured Boolean: 
    //      null => colours red or green depening if its overdue
    //      true/false => pass/fail badge superimposed
    private BorderPane getImage(Element e, Boolean measured) {
        ImageView image = e.getImage();
        image.setFitHeight(80);
        image.setFitWidth(80);

        StackPane imageSP = new StackPane(image);
        BorderPane imageBP = new BorderPane(imageSP);
        if (measured == null) {
            ColorAdjust monochrome = new ColorAdjust();
            monochrome.setSaturation(-1.0);

            ColorInput ci;
            if (missedReadings == 0) {
                ci = new ColorInput(0, 0, 80, 80, Color.LIGHTGREEN);
                imageBP.setStyle("-fx-background-color: lightgreen;");
            } else {
                ci = new ColorInput(0, 0, 80, 80, Color.LIGHTPINK);
                imageBP.setStyle("-fx-background-color: lightpink;");
            }

            Blend blush = new Blend(
                    BlendMode.MULTIPLY,
                    monochrome,
                    ci
            );
            image.setEffect(blush);
            image.setCache(true);
            image.setCacheHint(CacheHint.SPEED);
        } else if (measured) {
            imageSP.getChildren().add(medipi.utils.getImageView(MEDIPIIMAGESPASS, 70, 70));
        } else {
            imageSP.getChildren().add(medipi.utils.getImageView(MEDIPIIMAGESFAIL, 70, 70));
        }
        imageBP.setMinSize(90, 90);
        imageBP.setId("schedule-component");
        return imageBP;
    }

    /**
     * Method to refresh the screen with respect to the .scheduler file contents
     * and current time
     */
    protected void refreshSchedule() {
        if (medipi.getDebugMode() == MediPi.DEBUG) {
            System.out.println("Schedule file changed @" + new Date());
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
            Date latestSchedDate = new Date(0L);
            Date latestTransDate = new Date(0L);
            while ((line = br.readLine()) != null) {
                if (line.trim().startsWith("#")) {
                    continue;
                }
                ConfigurationStringTokeniser st = new ConfigurationStringTokeniser(line);
                if (st.countTokens() < 4) {
                    throw new Exception();
                    // any exception here returns null to report that an error has occured loading scheduler file
                }
                //schedule number
                UUID uuid = UUID.fromString(st.nextToken());
                //schedule status
                String status = st.nextToken().toUpperCase();
                //schedule time
                String d = st.nextToken();
                Date time = Utilities.ISO8601FORMATDATESECONDS.parse(d);

                //repeat time in mins
                int repeat = Integer.parseInt(st.nextToken());
                //Subsequent Tokens  = devices to be called
                ArrayList<String> deviceList = new ArrayList<>();
                while (st.hasMoreTokens()) {
                    String s = st.nextToken();
                    deviceList.add(s);
                }
                // find the latest scheduled time and save the data
                if (time.after(latestSchedDate) && status.equals(SCHEDULED)) {
                    latestSchedDate = time;
                    latestSched = new Schedule(uuid, status, time, repeat, deviceList);
                }
                // find the latest transmitted time and save the data
                if (time.after(latestTransDate) && status.equals(TRANSMITTED)) {
                    latestTransDate = time;
                    latestTrans = new Schedule(uuid, status, time, repeat, deviceList);
                }

                items.add(new Schedule(uuid, status, time, repeat, deviceList));
            }
            //Empty schedule file or no entries with type SCHEDULED or latest date in the future
            if (latestSched == null || latestSchedDate.after(new Date())) {
                Platform.runLater(() -> {
                    scheduleStatus.setText("Loading the schedule file has encountered problems. It is empty, contains no scheduled events or corrupt");
                    left.setDisable(true);
                });
            } else {
                lastSchedule = latestSched;
                //check to see if the latest entry is valid
                Date lastTime = new Date(lastSchedule.getTime());
                left.setDisable(false);
                // find next scheduled measurements
                nextScheduledEventTime = findNextSchedule(lastSchedule, latestTrans);
                // No missed readings = due to be run now
                if (missedReadings == 0) {
                    alertBooleanProperty.set(false);
                    Platform.runLater(() -> {
                        scheduleStatus.setText("You are up to date with your scheduled measurements. The next scheduled measurement is due at " + Utilities.DISPLAY_SCHEDULE_FORMAT.format(nextScheduledEventTime) + ". If you would like to perform measurements now press the run measurements now button");
                    });
                } else {
                    // more than 1 missed reading
                    alertBooleanProperty.set(true);
                    Platform.runLater(new Runnable() {
                        @Override
                        public void run() {
                            scheduleStatus.setText("You have missed " + missedReadings + " scheduled measurements. The next scheduled measurement is due at " + Utilities.DISPLAY_SCHEDULE_FORMAT.format(nextScheduledEventTime) + ". If you would like to perform measurements now press the run measurements now button");
                        }
                    });
                }
                if (medipi.isBasicDataView()) {
                    updateBasicSchedView(lastSchedule, latestTrans);
                }
            }
        } catch (Exception e) {
            // any exception here returns null to report that an error has occured loading scheduler file
            Platform.runLater(() -> {
                scheduleStatus.setText("Loading the schedule file has encountered problems. It is empty, contains no scheduled events or corrupt");
                left.setDisable(true);
            });
        }

    }

    // Method to find the time the next schedule should start based upon the last recorded SCHEDULED line in .scheduler
    private Date findNextSchedule(Schedule latestSched, Schedule latestTrans) throws Exception {
        Date transTime;
        //if there is no transmitter time previously recorded then set as Epoch time
        if (latestTrans == null) {
            transTime = new Date(0L);
        } else {
            transTime = new Date(latestTrans.getTime());
        }
        Date schedTime = new Date(latestSched.getTime());
        int repeat = latestSched.getRepeat();
        Calendar schedCal = Calendar.getInstance();
        schedCal.setTime(schedTime);
        //knowing that the time is in the past get next scheduled time before we test if it has been missed
        schedCal.add(Calendar.MINUTE, repeat);
        missedReadings = 0;
        while (true) {
            if (schedCal.getTime().after(new Date())) {
                break;
            }
            if (transTime.before(schedCal.getTime())) {
                missedReadings++;
            }
            schedCal.add(Calendar.MINUTE, repeat);
        }
        return schedCal.getTime();
    }

    // Method to execute the chain of elements in a schedule. A new STARTED line
    // is prepared and data in each of the elements is reset
    private void runSchedule() {
        if (!runningSchedule.get()) {
            runningSchedule.set(true);
            //encapsulate all the scheduler within a try catch so that the running scheduler boolean cannot get out of sync
            try {
                if (medipi.getDebugMode() == MediPi.DEBUG) {
                    System.out.println("Scheduled event @" + nextScheduledEventTime + " - now!" + new Date());
                }
                //Record the start of the run
                nextUUID = UUID.randomUUID();
                Schedule started = new Schedule(nextUUID, "STARTED", new Date(), lastSchedule.getRepeat(), lastSchedule.getDeviceSched());
                deviceData.add(started);
                // Reset all the devices to be taken
                for (String s : lastSchedule.getDeviceSched()) {
                    Element e = medipi.getElement(s);
                    // set this Schedule in each of the devices to be run for callbacks
                    e.setScheduler(this);
                    if (Device.class.isAssignableFrom(e.getClass())) {
                        Device d = (Device) e;
                        d.resetDevice();
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
                Date time = new Date(sched.getTime());
                output.append(Utilities.ISO8601FORMATDATESECONDS.format(time));
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
        DashboardTile dashComponent = new DashboardTile(this);
        dashComponent.addTitle(getName());
        dashComponent.addOverlay(alertImageView, alertBooleanProperty);
        return dashComponent.getTile();
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

    @Override
    public String getData() {
        String separator = medipi.getDataSeparator();
        StringBuilder sb = new StringBuilder();
        //Add MetaData
        sb.append("metadata->persist->medipiversion->").append(medipi.getVersion()).append("\n");
        sb.append("metadata->timedownloaded->").append(Utilities.ISO8601FORMATDATEMILLI.format(new Date())).append("\n");
        sb.append("metadata->subtype->").append(getName()).append("\n");
        sb.append("metadata->datadelimiter->").append(medipi.getDataSeparator()).append("\n");
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
        for (Schedule sched : deviceData) {
            Date time = new Date(sched.getTime());
            sb.append(Utilities.ISO8601FORMATDATESECONDS.format(time));
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
        return sb.toString();
    }

    @Override
    public void resetDevice() {
        refreshSchedule();
        nextUUID = null;
        lastSchedule = null;
        items.clear();
    }

    // method to add new .scheduler lines ready to be written to the .scheduler file
    void addScheduleData(String type, Date date, ArrayList<String> devices) {
        Schedule newSched = new Schedule(nextUUID, type, date, 0, devices);
        deviceData.add(newSched);
        if (type.equals(TRANSMITTED)) {
            writeNewScheduleLineToFile(deviceData);
        }
    }
}
