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
package org.medipi;

import org.medipi.authentication.MediPiWindow;
import org.medipi.devices.Element;
import java.awt.SplashScreen;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.NetworkInterface;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Optional;
import java.util.Properties;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Separator;
import javafx.scene.control.TextArea;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.TilePane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import org.apache.commons.io.output.TeeOutputStream;
import org.medipi.authentication.UnlockConsumer;
import org.medipi.devices.Device;
import org.medipi.devices.Scheduler;
import org.medipi.downloadable.handlers.DownloadableHandlerManager;
import org.medipi.downloadable.handlers.HardwareHandler;
import org.medipi.logging.MediPiLogger;
import org.medipi.messaging.vpn.VPNServiceManager;
import org.medipi.utilities.ConfigurationStringTokeniser;
import org.medipi.utilities.Utilities;

/**
 * This is the main class called at execution. MediPi class requires that a
 * properties file is passed in as a parameter called propertiesFile.
 *
 * The main MediPi class's responsibility is to:
 *
 * 1.Orchestrate the initiation of certain resources: MediPiProperties,
 * MediPiLogger etc
 *
 * 2.Initialise the UI framework/primary stage, providing a tiled dashboard
 * style interface, including a title banner and NHS microbanner
 *
 * 3.to initiate each of the elements of MediPi and expose methods for
 * hiding/unhiding each of the elements' windows
 *
 * 4.return information about its version if asked.
 *
 * CONSIDERATIONS/TODO:
 *
 * Screenwidth has been set at 800x400 as a result of development aimed at
 * raspberry Pi official 7" touchscreen. Dynamic adjustment has not been
 * developed. Implementation of CSS has done very much ad hoc - this needs
 * proper refactoring
 *
 * Debug mode may need to be removed for production - it doesn't affect the
 * functionality but will suppress fault conditions in certain modes
 *
 * @author rick@robinsonhq.com
 */
public class MediPi extends Application implements UnlockConsumer {

    // MediPi version Number
    private static final String MEDIPINAME = "MediPi Telehealth";
    private static final String VERSION = "MediPi_v1.0.15";
    private static final String VERSIONNAME = "PILOT-20171025-1";

    // Set the MediPi Log directory
    private static final String LOG = "medipi.log";

    // Debug mode can take one of 3 values:
    // 	"debug" mode this will report to standard out debug messages
    // 	"errorsuppress" mode this will suppress all error messages to the UI, instead only outputting to standard out
    //	"none" mode will not report any standard output messages
    private static final String DEBUGMODE = "medipi.debugmode";
    // Screensize settings - default is 800x480 if not set
    private static final String SCREENWIDTH = "medipi.screen.width";
    private static final String SCREENHEIGHT = "medipi.screen.height";
    // CSS file location
    private static final String CSS = "medipi.css";
    //List of elements e.g. Oximeter,Scales,Blood Pressure, transmitter, messenger etc to be loaded into MediPi
    private static final String ELEMENTS = "medipi.elementclasstokens";

    // The period of time in seconds to wait before retrying to access the MediPi Concentrator server
    private static final String MEDIPIDOWNLOADPOLLPERIOD = "medipi.downloadable.pollperiod";
    // The period of time in seconds to wait before retrying to access the MediPi Concentrator server
    private static final String MEDIPIWIFIMONITORREFRESHPERIOD = "medipi.wifi.monitorresfreshperiod";
    // Specify action to take when MEdiPi Patient is closed
    private static final String MEDIPISHUTDOWNLINUXOSONCLOSE = "medipi.shutdownlinuxosonclose";
    // System property set in this class when device is authenticated 
    private static final String MEDIPIDEVICECERTNAME = "medipi.device.cert.name";
    // System property set in this class when device is authenticated 
    private static final String MEDIPIDEVICEMACADDRESS = "medipi.device.macaddress";
    // Device certificate JKS used to authenticate that the device and cert match and to encrypt the tramission
    private static final String MEDIPIDEVICECERTLOCATION = "medipi.device.cert.location";
    // ascii character used to separate data elements in transmission using medipi format
    private static final String MEDIPIDATASEPARATOR = "medipi.dataseparator";
    // time sync directory
    private static final String MEDIPITIMESYNCSERVERDIRECTORY = "medipi.timesyncserver.directory";
    // turn on the download functionality to update software on the fly
    private static final String MEDIPIDOWNLOADABLEDOWNLOADUPDATES = "medipi.downloadable.downloadupdates";
    // polling executor service
    public static ScheduledExecutorService POLLSERVICE = Executors.newSingleThreadScheduledExecutor();
    // WIFI monitor executor service
    public static ScheduledExecutorService WIFIMONITORSERVICE = Executors.newSingleThreadScheduledExecutor();

    private Stage primaryStage;
    private final ArrayList<Element> elements = new ArrayList<>();
    private final ScrollPane dashTileSc = new ScrollPane();
    private VBox subWindow;
    // Fatal error flag to stop use of MediPi - one way there is no set back to false
    private boolean fatalError = false;
    private final StringBuilder fatalErrorLog = new StringBuilder("There has been a fatal error: \n");
    private String versionIdent;
    private String dataSeparator;
    private Properties properties;
    private MediPiWindow mediPiWindow;
    private final IntegerProperty vpnConnectionIndicatorProperty = new SimpleIntegerProperty(0);
    private IntegerProperty wifiConnectionIndicatorProperty = new SimpleIntegerProperty(0);
    public static final int VPNFAILED = -1;
    public static final int VPNNOTCONNECTED = 0;
    public static final int VPNCONNECTING = 1;
    public static final int VPNRESTARTING = 2;
    public static final int VPNCONNECTED = 3;
    public static final int WIFINOTCONNECTED = 0;
    public static final int WIFICONNECTED = 1;
    private Integer screenwidth = 800;
    private Integer screenheight = 480;
    private boolean closeLinuxOS = false;
    private final Label patientForename = new Label();
    private final Label patientSurname = new Label();
    private final Label nhsNumber = new Label();
    private final Label dob = new Label();
    private Scheduler scheduler = null;
    private String cssfile = null;
    private BooleanProperty unlocked = new SimpleBooleanProperty(false);
    private PollDownloads pim;
    private Integer incomingMessageCheckPeriod;
    /**
     * When set on the debug mode will send all std and err output to the
     * version screen accessed by tapping the MediPi Telehealth banner label
     *
     */
    private boolean debugMode = false;

    // Instantiation of the download handler 
    private DownloadableHandlerManager dhm = new DownloadableHandlerManager();

    /**
     * allows access to scene to allow the cursor to be set
     */
    public Scene scene;

    // "ELEMENTNAMESPACESTEM" a single variable to contain element stem
    public static final String ELEMENTNAMESPACESTEM = "medipi.element.";

    /**
     * Accessible utilities class
     */
    public Utilities utils;

    /**
     * Property encapsulating time server sync status used by all elements to
     * determine whether data should be taken or not
     */
    public BooleanProperty timeSync = new SimpleBooleanProperty(false);

    /**
     * Property encapsulating wifi connectivity status used by transmitter and
     * polling to determine if connection is possible
     */
    public BooleanProperty wifiSync = new SimpleBooleanProperty(false);

    /**
     * Properties from the main properties file
     *
     * @return
     */
    public Properties getProperties() {
        return properties;
    }

    /**
     *
     * @return an integerProperty relating to the status of the VPN Connection
     */
    public IntegerProperty getVPNConnectionIndicator() {
        return vpnConnectionIndicatorProperty;
    }

    /**
     * Method to get the DownloadableHandlerManager in order to set any extra
     * handlers for any element
     *
     * @return DownloadableHandlerManager
     */
    public DownloadableHandlerManager getDownloadableHandlerManager() {
        return dhm;
    }

    /**
     * Method to get the MediPiwindow - this is the one which sets whether the
     * authenticated screen is shown
     *
     * @return MediPiWindow
     */
    public MediPiWindow getMediPiWindow() {
        return mediPiWindow;
    }

    public Scheduler getScheduler() {
        return scheduler;
    }

    public void setScheduler(Scheduler s) {
        scheduler = s;
    }

    public Integer getScreenheight() {
        return screenheight;
    }

    public String getCssfile() {
        return cssfile;
    }

    /**
     * Main javaFX start method
     *
     * @param stg Primary Stage for the JavaFX program
     * @throws Exception
     */
    @Override
    public void start(Stage stg) throws Exception {
        try {
            primaryStage = stg;

            // Code to capture all stdout and errout and display on screen for debugging purposes
            TextArea stdoutText = new TextArea();
            stdoutText.setPrefWidth(800);
            stdoutText.setPrefHeight(300);
            stdoutText.setWrapText(true);
            StdOutConsole console = new StdOutConsole(stdoutText);
            PrintStream cps = new PrintStream(console, true);
            TeeOutputStream myOut = new TeeOutputStream(cps, System.out);
            PrintStream ps = new PrintStream(myOut);

            // set versioning and print to standard output
            versionIdent = MEDIPINAME + " " + VERSION + "-" + VERSIONNAME + " starting at " + Instant.now().toString();
            System.out.println(versionIdent);

            // call to the singletons which provide properties and pop-up messages
            MediPiProperties mpp = MediPiProperties.getInstance();
            if (!mpp.setProperties(getParameters().getNamed().get("propertiesFile"))) {
                makeFatalErrorMessage("Properties file failed to load", null);
                return;
            }
            properties = MediPiProperties.getInstance().getProperties();
            // initialise utilities which is accessible to all classes which have reference to MediPi.class
            utils = new Utilities(properties);
            // configure the screensize - the default is 800x480 - see considerations/todo in main text above
            try {
                String sw = mpp.getProperties().getProperty(SCREENWIDTH);
                screenwidth = Integer.parseInt(sw);
                sw = mpp.getProperties().getProperty(SCREENHEIGHT);
                screenheight = Integer.parseInt(sw);
            } catch (Exception e) {
                screenwidth = 800;
                screenheight = 480;
                makeFatalErrorMessage(SCREENWIDTH + " and " + SCREENHEIGHT + " - The configured screen sizes are incorrect", e);
                return;
            }
            StackPane root = new StackPane();
            scene = new Scene(root, screenwidth, screenheight);

            // what should MediPi do when exitting?
            String shut = properties.getProperty(MEDIPISHUTDOWNLINUXOSONCLOSE);
            if (shut == null || shut.trim().length() == 0 || shut.toLowerCase().startsWith("n")) {
                closeLinuxOS = false;
            } else {
                closeLinuxOS = true;
            }
            //set debug mode
            String dbm = properties.getProperty(DEBUGMODE);
            if (dbm == null || dbm.trim().length() == 0 || !dbm.toLowerCase().trim().startsWith("y")) {
                debugMode = false;
            } else {
                debugMode = true;
            }
            //Instantiate Message Box Class
            MediPiMessageBox message = MediPiMessageBox.getInstance();
            message.setMediPi(this);
            //Set up logging
            String log = properties.getProperty(LOG);
            if (log == null || log.trim().equals("")) {
                makeFatalErrorMessage("MediPi log directory is not set", null);
                return;
            } else if (new File(log).isDirectory()) {
                MediPiLogger.getInstance().setAppName("MEDIPI", log);
                MediPiLogger.getInstance().log(MediPi.class.getName() + "startup", versionIdent);
            } else {
                makeFatalErrorMessage(log + " - MediPi log directory is not a directory", null);
                return;
            }
            //set the data separator - default = ^
            dataSeparator = properties.getProperty(MEDIPIDATASEPARATOR);
            if (dataSeparator == null || dataSeparator.trim().equals("")) {
                dataSeparator = "^";
            }

            // It has been difficult to access all the Mac address/IP Addresses (whether they 
            // are up or not) using NetworkInterface on Linux machines. This method does work on Linux
            // I'm not sure if the alternative method (i.e. the non Linux method) works for all 
            // other OS MACAddresses/IPs whether they are up or not
            if (System.getProperty("os.name").equals("Linux")) {

                // Read all available device names
                List<String> devices = new ArrayList<>();
                Pattern pattern = Pattern.compile("^ *(.*):");
                try (FileReader reader = new FileReader("/proc/net/dev")) {
                    BufferedReader in = new BufferedReader(reader);
                    String line = null;
                    while ((line = in.readLine()) != null) {
                        Matcher m = pattern.matcher(line);
                        if (m.find()) {
                            devices.add(m.group(1));
                        }
                    }
                } catch (IOException e) {
                    makeFatalErrorMessage("Device certificate is not correct for this device", null);
                    return;
                }

                String ksf = MediPiProperties.getInstance().getProperties().getProperty(MEDIPIDEVICECERTLOCATION);
                // read the hardware address for each device
                for (String device : devices) {
                    try (FileReader reader = new FileReader("/sys/class/net/" + device + "/address")) {
                        BufferedReader in = new BufferedReader(reader);
                        String addr = in.readLine();
                        try {

                            KeyStore keyStore = KeyStore.getInstance("jks");
                            try (FileInputStream fis = new FileInputStream(ksf)) {
                                //the MAC address used to unlock the JKS must be lowercase
                                keyStore.load(fis, addr.toCharArray());
                                // use a system property to save the certicicate name
                                Enumeration<String> aliases = keyStore.aliases();
                                // the keystore will only ever contain one key -so take the 1st one
                                System.setProperty("medipi.device.cert.name", aliases.nextElement());
                                System.setProperty("medipi.device.macaddress", addr);
                            }
                        } catch (KeyStoreException | IOException | NoSuchAlgorithmException | CertificateException e) {
                        }

                        System.out.println(String.format("%5s: %s", device, addr));
                    } catch (IOException e) {
                        makeFatalErrorMessage("Device certificate is not correct for this device", null);
                        return;
                    }
                }
                if (System.getProperty(MEDIPIDEVICECERTNAME) == null || System.getProperty(MEDIPIDEVICEMACADDRESS) == null) {
                    makeFatalErrorMessage("Device certificate is not correct for this device", null);
                    return;
                }

            } else {
                // for all other non Linux OS systems 
                String ip;
                try {
                    // try to find the MAC address
                    String macAddress = "";
                    Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
                    while (interfaces.hasMoreElements()) {
                        StringBuilder macAdd = new StringBuilder();
                        NetworkInterface iface = interfaces.nextElement();
                        // filters out 127.0.0.1 and inactive interfaces
                        if (iface.isLoopback()) {
                            continue;
                        }
                        byte[] mac = iface.getHardwareAddress();
                        if (mac == null) {
                            continue;
                        }
                        System.out.print("Current MAC address : ");
                        for (int i = 0; i < mac.length; i++) {
                            macAdd.append(String.format("%02X%s", mac[i], (i < mac.length - 1) ? ":" : ""));
                        }
                        macAddress = macAdd.toString().toLowerCase();
                        System.setProperty("medipi.device.macaddress", macAddress);
                        System.out.print(macAddress);
                    }
                    // Using the Mac address unlock the JKS keystore for the device
                    try {
                        String ksf = MediPiProperties.getInstance().getProperties().getProperty(MEDIPIDEVICECERTLOCATION);

                        KeyStore keyStore = KeyStore.getInstance("jks");
                        try (FileInputStream fis = new FileInputStream(ksf)) {
                            //the MAC address used to unlock the JKS must be lowercase
                            keyStore.load(fis, macAddress.toCharArray());
                            // use a system property to save the certicicate name
                            Enumeration<String> aliases = keyStore.aliases();
                            // the keystore will only ever contain one key -so take the 1st one
                            System.setProperty("medipi.device.cert.name", aliases.nextElement());
                        }
                    } catch (KeyStoreException | IOException | NoSuchAlgorithmException | CertificateException e) {
                        makeFatalErrorMessage("Device certificate is not correct for this device", null);
                        return;
                    }

                } catch (Exception e) {
                    makeFatalErrorMessage("Can't find Mac Address for the machine, therefore unable to check device certificate", null);
                    return;
                }
            }

            // Patient Demographics data is discovered for the patient Microbanner -
            try {
                PatientDetailsDO patient = PatientDetailsService.getInstance().getPatientDetails();
                patientSurname.setId("mainwindow-title-microbannerupper");
                patientForename.setId("mainwindow-title-microbannerupper");
                nhsNumber.setId("mainwindow-title-microbannerlower");
                dob.setId("mainwindow-title-microbannerlower");

                setPatientMicroBanner(patient);

            } catch (Exception e) {
                makeFatalErrorMessage("Patient Name issue: ", e);
                return;
            }

            // Create Patient Banner
            VBox microPatientBannerVBox = new VBox();
            microPatientBannerVBox.setAlignment(Pos.CENTER);
            HBox patientNameHBox = new HBox(patientSurname, new Label(","), patientForename);
            patientNameHBox.setAlignment(Pos.CENTER);
            HBox patientOtherHBox = new HBox(new Label("D.O.B "), dob, new Label("  NHS No."), nhsNumber);
            patientOtherHBox.setAlignment(Pos.CENTER);
            microPatientBannerVBox.getChildren().addAll(
                    patientNameHBox,
                    new Separator(Orientation.HORIZONTAL),
                    patientOtherHBox
            );

            patientOtherHBox.visibleProperty().bind(unlocked);
            // Start to create the screen
            Label title = new Label("MediPi Telehealth");
            title.setId("mainwindow-title");
            title.setWrapText(true);
            title.setAlignment(Pos.CENTER);
            title.setOnMouseClicked((MouseEvent event) -> {

                Alert alert = new Alert(Alert.AlertType.CONFIRMATION, "", ButtonType.YES, ButtonType.NO);
                alert.setTitle(getVersion());
                alert.setHeaderText("Version: " + getVersion());
                if (debugMode) {
                    alert.getDialogPane().setContent(stdoutText);
                } else {
                    alert.getDialogPane().setContentText("Would you like to lock MediPi?");
                }
                alert.getDialogPane().getStylesheets().add("file:///" + getCssfile());
                alert.getDialogPane().setId("message-box");
                ImageView iw = utils.getImageView("medipi.images.doctor", 80, 80);
                alert.setGraphic(iw);
                Optional<ButtonType> result = alert.showAndWait();
                if (result.get() == ButtonType.YES) {
                    mediPiWindow.lock();
                }
            });
            // add the NHS logo
            ImageView iw = new ImageView("/org/medipi/logo.png");
            ImageView off = new ImageView("/org/medipi/onoff.png");
            off.setOnMouseClicked((MouseEvent event) -> {
                exit();
            });
            VBox mainWindow = new VBox();
            mainWindow.setId("mainwindow");
            mainWindow.setPadding(new Insets(0, 5, 0, 5));
            mainWindow.setAlignment(Pos.TOP_CENTER);
            GridPane titleBP = new GridPane();
            ColumnConstraints col1 = new ColumnConstraints();
            col1.setPercentWidth(28);
            ColumnConstraints col2 = new ColumnConstraints();
            col2.setPercentWidth(20);
            col2.setHalignment(HPos.CENTER);
            ColumnConstraints col3 = new ColumnConstraints();
            col3.setPercentWidth(45);
            col3.setHalignment(HPos.CENTER);
            ColumnConstraints col4 = new ColumnConstraints();
            col4.setPercentWidth(7);
            col4.setHalignment(HPos.RIGHT);
            titleBP.getColumnConstraints().addAll(col1, col2, col3, col4);
            titleBP.setPadding(new Insets(0, 5, 0, 5));
            titleBP.setMinSize(800, 60);
            titleBP.setMaxSize(800, 60);
            BorderPane.setAlignment(iw, Pos.CENTER);
            titleBP.add(iw, 0, 0);
            titleBP.add(title, 1, 0);
            titleBP.add(microPatientBannerVBox, 2, 0);
            titleBP.add(off, 3, 0);
            VBox connectionLEDs = new VBox();
            connectionLEDs.setAlignment(Pos.CENTER_RIGHT);
            HBox vpnConn = new HBox();
            //Create elements and  structure for the lower banner
            LED vpnled = new LED();
            vpnConn.setAlignment(Pos.CENTER_RIGHT);
            vpnConn.setSpacing(10);
            vpnConn.setId("lowerbannerled");
            vpnConn.getChildren().addAll(
                    new Label("VPN Connection"),
                    vpnled
            );
            HBox wifiConn = new HBox();
            //Create elements and  structure for the lower banner
            LED wifiled = new LED();
            wifiConn.setAlignment(Pos.CENTER_RIGHT);
            wifiConn.setSpacing(10);
            wifiConn.setId("lowerbannerled");
            wifiConn.getChildren().addAll(
                    new Label("WIFI Connection"),
                    wifiled
            );

            BorderPane lowerBanner = new BorderPane();
            lowerBanner.setPadding(new Insets(0, 5, 0, 5));
            lowerBanner.setMinSize(800, 40);
            lowerBanner.setMaxSize(800, 40);
            DigitalClock dc = new DigitalClock();
            dc.setMinHeight(40);
            dc.setMaxHeight(40);
            dc.setId("lowerbanner");
            lowerBanner.setLeft(dc);

            lowerBanner.setCenter(AlertBanner.getInstance().getAlertBanner());

            // Check Synchronisation status of MediPi time server
            String messageDir = properties.getProperty(MEDIPITIMESYNCSERVERDIRECTORY);
            if (messageDir == null || messageDir.trim().length() == 0) {
                makeFatalErrorMessage("Time server directory parameter not configured", null);
                return;
            }
            Path dir = Paths.get(messageDir);
            TimeServerWatcher tsw = new TimeServerWatcher(dir, this);
            // a shell script which calls the MediPi Patient application also currently 
            // calls the synchronisation of the time server. The output of which is saved
            // to file and monitored by the TimeServerWatcher class

            //instnatiate the VPN Manager            
            VPNServiceManager vpnm = VPNServiceManager.getInstance();
            if (vpnm.isEnabled()) {
                vpnConnectionIndicatorProperty.addListener(new ChangeListener<Number>() {
                    @Override
                    public void changed(ObservableValue<? extends Number> observableValue, Number oldValue,
                            Number newValue) {
                        switch (newValue.intValue()) {
                            case VPNFAILED:
                                vpnled.blink(Color.RED, Color.GREY, 1000);
                                break;
                            case VPNNOTCONNECTED:
                                vpnled.ledOn(Color.GREY);
                                break;
                            case VPNCONNECTING:
                                vpnled.blink(Color.GREEN, Color.GREY, 250);
                                break;
                            case VPNRESTARTING:
                                vpnled.blink(Color.ORANGE, Color.GREY, 250);
                                break;
                            case VPNCONNECTED:
                                vpnled.ledOn(Color.GREEN);
                                break;
                            default:
                                break;
                        }
                    }
                });
                vpnm.setVPNConnectionIndicator(vpnConnectionIndicatorProperty);
                connectionLEDs.getChildren().add(
                        vpnConn
                );

            }
            //WIFI connection indicator
            try {
                String time = getProperties().getProperty(MEDIPIWIFIMONITORREFRESHPERIOD);
                if (time == null || time.trim().length() == 0) {
                    time = "10";
                }
                Integer wifiRefresh = Integer.parseInt(time);
                WIFIConnectionMonitor wifiConnectionMonitor = new WIFIConnectionMonitor(this);
                wifiConnectionIndicatorProperty = wifiConnectionMonitor.getWIFIProperty();
                wifiConnectionIndicatorProperty.addListener(new ChangeListener<Number>() {
                    @Override
                    public void changed(ObservableValue<? extends Number> observableValue, Number oldValue,
                            Number newValue) {
                        switch (newValue.intValue()) {
                            case WIFINOTCONNECTED:
                                wifiled.blink(Color.RED, Color.GREY, 1000);
                                break;
                            case WIFICONNECTED:
                                wifiled.ledOn(Color.GREEN);
                                break;
                            default:
                                break;
                        }
                    }
                });
                connectionLEDs.getChildren().add(
                        wifiConn
                );
                WIFIMONITORSERVICE.scheduleAtFixedRate(wifiConnectionMonitor, (long) 0, (long) wifiRefresh, TimeUnit.SECONDS);
            } catch (Exception nfe) {
                makeFatalErrorMessage("Unable to start the wifi monitor service: " + nfe.getLocalizedMessage(), null);
                return;
            }

            lowerBanner.setRight(connectionLEDs);
            // Set up the Dashboard view
            TilePane dashTile;
            dashTile = new TilePane();
            dashTile.setMinWidth(800);
            dashTile.setId("mainwindow-dashboard");
            dashTileSc.setContent(dashTile);
            dashTileSc.setFitToWidth(true);
            dashTileSc.setFitToHeight(true);
            dashTileSc.setMinHeight(380);
            dashTileSc.setMaxHeight(380);
            dashTileSc.setMinWidth(800);
            dashTileSc.setId("mainwindow-dashboard-scroll");
            dashTileSc.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
            dashTileSc.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
            //bind the visibility property so that when not visible the panel doesnt take any space
            dashTileSc.managedProperty().bind(dashTileSc.visibleProperty());
            subWindow = new VBox();
            subWindow.setId("subwindow");
            subWindow.setAlignment(Pos.TOP_CENTER);
            subWindow.getChildren().addAll(
                    dashTileSc
            );
            try {
                mediPiWindow = new MediPiWindow(subWindow);
                mainWindow.getChildren().addAll(titleBP,
                        new Separator(Orientation.HORIZONTAL),
                        mediPiWindow,
                        new Separator(Orientation.HORIZONTAL),
                        lowerBanner
                );
            } catch (Exception e) {
                makeFatalErrorMessage("Authentication cannot be loaded - " + e.getMessage(), null);
                return;
            }

            root.getChildren().add(mainWindow);

            // Load CSS properties - see considerations/todo in main text above
            cssfile = properties.getProperty(CSS);
            if (cssfile == null || cssfile.trim().length() == 0) {
                makeFatalErrorMessage("No CSS file defined in " + CSS, null);
                return;
            } else {
                scene.getStylesheets().add("file:///" + cssfile);
            }

            primaryStage.setTitle(MEDIPINAME + " " + VERSION + "-" + VERSIONNAME);
            //show the screen
            if (fatalError) {
                Label l = new Label(fatalErrorLog.toString());
                l.setStyle("-fx-background-color: lightblue;");
                scene = new Scene(l);
            }
            primaryStage.setScene(scene);
            primaryStage.setMinWidth(screenwidth);
            primaryStage.setMinHeight(screenheight);
            primaryStage.show();

            // register this class as an implementor of the locked/unlocked interface 
            // in order to hide the NHS NUmber and DOB when not authenticated
            mediPiWindow.registerForAuthenticationCallback(this);
            // Basic structure is now created and ready to display any errors that
            // occur when sub elements are called
            // loop through all the element class tokens defined in the properties file and instantiate
            // Add dashboard Tiles to the initial GUI structure
            String e = properties.getProperty(ELEMENTS);
            if (e != null && e.trim().length() != 0) {
                ConfigurationStringTokeniser cst = new ConfigurationStringTokeniser(e);
                while (cst.hasMoreTokens()) {
                    String classToken = cst.nextToken();
                    String elementClass = properties.getProperty(ELEMENTNAMESPACESTEM + classToken + ".class");
                    try {
                        Element elem = (Element) Class.forName(elementClass).newInstance();
                        elem.setMediPi(this);
                        elem.setClassToken(classToken);
                        String initError = elem.init();
                        elem.setElementTitle();
                        if (initError == null) {
                            dashTile.getChildren().add(elem.getDashboardTile());
                            elements.add(elem);
                            subWindow.getChildren().add(elem.getWindowComponent());
                        } else {
                            MediPiMessageBox.getInstance().makeErrorMessage("Cannot instantiate an element: \n" + classToken + " - " + elementClass + " - " + initError, null);
                        }
                    } catch (Exception ex) {
                        MediPiMessageBox.getInstance().makeErrorMessage("Cannot instantiate an element: \n" + classToken + " - " + elementClass, ex);
                    }
                }
            } else {
                makeFatalErrorMessage("No Elements have been defined", null);
                return;
            }

            //show the tiled dashboard view
            callDashboard();
            // functionality which closes the window when the x is pressed
            primaryStage.setOnHiding((WindowEvent event) -> {
                Platform.runLater(() -> {
                    System.exit(0);
                });
            });
            boolean downloadUpdates = false;
            // what should MediPi do when exitting?
            String downloadUpdatesString = properties.getProperty(MEDIPIDOWNLOADABLEDOWNLOADUPDATES);
            if (downloadUpdatesString == null || downloadUpdatesString.trim().length() == 0 || downloadUpdatesString.toLowerCase().startsWith("n")) {
                downloadUpdates = false;
            } else {
                downloadUpdates = true;
            }

            if (downloadUpdates) {
                // add a ahndler for harware updates from MediPI Concentrator
                dhm.addHandler("HARDWAREUPDATE", new HardwareHandler(properties));
            }

            if (dhm.hasHandlers()) {
                // Start the downloadable timer. This wakes up every definable period (default set to 30s) 
                // and performs functions to send restful messages to retreive the downloadable entities - Hardware and Patient Messages
                try {
                    String time = getProperties().getProperty(MEDIPIDOWNLOADPOLLPERIOD);
                    if (time == null || time.trim().length() == 0) {
                        time = "30";
                    }
                    incomingMessageCheckPeriod = Integer.parseInt(time);
                    pim = new PollDownloads(this);
                } catch (Exception nfe) {
                    makeFatalErrorMessage("Unable to start the download service - make sure that " + MEDIPIDOWNLOADPOLLPERIOD + " property is set correctly", null);
                    return;
                }
            }
            //set debug mode
            if (debugMode) {
                System.setOut(ps);
                System.setErr(ps);

            }
            // splash screen
            final SplashScreen splash = SplashScreen.getSplashScreen();

            //Close splashscreen
            if (splash != null) {
                System.out.println("Closing splashscreen...");
                splash.close();
            }
        } catch (Exception e) {
            makeFatalErrorMessage("A fatal and fundamental error occurred at bootup", e);
            return;
        }
    }

    /**
     * Display Patient details in the Patient Banner
     *
     * @param patient details data object to be displayed in the patient banner
     * @throws Exception
     */
    public void setPatientMicroBanner(PatientDetailsDO patient) throws Exception {
        patientSurname.setText(patient.getSurname().toUpperCase());
        patientForename.setText(patient.getForename());
        nhsNumber.setText(patient.formatNHSNumber(patient.getNhsNumber()));
        dob.setText(patient.formatDOB(patient.getDob()));
    }

    /**
     * Method to close the JavaFX application
     */
    public void exit() {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION, "", ButtonType.YES, ButtonType.NO);
        alert.setTitle("MediPi Exit");
        alert.setHeaderText(null);
        alert.getDialogPane().getStylesheets().add("file:///" + getCssfile());
        alert.getDialogPane().setMaxSize(400, 200);
        alert.getDialogPane().setId("message-box");
        VBox vb = new VBox();
        Text text = new Text("Are you sure you want to close MediPi?");
        text.setTextAlignment(TextAlignment.CENTER);
        text.setWrappingWidth(400);
        vb.getChildren().add(text);
        vb.setAlignment(Pos.CENTER);
        ImageView iw = utils.getImageView("medipi.images.doctor", 80, 80);
        alert.setGraphic(iw);
        alert.getDialogPane().setContent(vb);

        Optional<ButtonType> result = alert.showAndWait();
        if (result.get() == ButtonType.YES) {
            POLLSERVICE.shutdownNow();
            WIFIMONITORSERVICE.shutdownNow();
            if (closeLinuxOS) {
                executeCommand("sudo shutdown -h now");
            } else {
                Platform.runLater(() -> {
                    System.exit(0);
                });
            }
        }
    }

    /**
     * This method executes strings on the command line and can shut the
     * raspberry pi down
     *
     * @param command
     * @return
     */
    public String executeCommand(String command) {

        StringBuffer output = new StringBuffer();

        Process p;
        try {
            p = Runtime.getRuntime().exec(command);
            p.waitFor();
            BufferedReader reader
                    = new BufferedReader(new InputStreamReader(p.getInputStream()));

            String line = "";
            while ((line = reader.readLine()) != null) {
                output.append(line + "\n");
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return output.toString();

    }

    /**
     * Method to return the primary stage of MediPi - useful for adding
     * messageboxes etc
     *
     * @return The primary stage of the MediPi program
     */
    protected Stage getStage() {
        return primaryStage;
    }

    /**
     * Return the version of the code when asked using -version argument
     *
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        if (args[0].toLowerCase().trim().equals("-version")) {
            System.out.println(MEDIPINAME + " " + VERSION + "-" + VERSIONNAME);
            System.exit(0);
        } else {
            launch(args);
        }
    }

    /**
     * Method to call the mainwindow back to the dashboard
     *
     */
    public void callDashboard() {
        hideAllWindows();
        dashTileSc.setVisible(true);
    }

    /**
     * Method to hide all the element windows from the MediPi mainwindow
     */
    public void hideAllWindows() {
        dashTileSc.setVisible(false);
        for (Element e : elements) {
            e.hideDeviceWindow();
        }
    }

    /**
     * Return the MediPi version ident
     *
     * @return the version of MediPi
     */
    public String getVersion() {
        return VERSION + "-" + VERSIONNAME;
    }

    /**
     * Supplies the dataSeparator used to delimit data
     *
     * @return String of the separator
     */
    public String getDataSeparator() {
        return dataSeparator;
    }

    /**
     * Method to return an ArrayList of all the currently loaded elements
     *
     * @return ArrayList of elements loaded into MediPi
     */
    public ArrayList<Element> getElements() {
        return elements;
    }

    /**
     * Method to return an instance of an element using its element class token
     *
     * @param elementClassToken Element Class Token of the element to be
     * returned
     * @return The Element requested using the element class token string. Null
     * is returned if not found
     */
    public Element getElement(String elementClassToken) {
        //loop round all the presentation elements until the required one is found
        for (Element e : elements) {
            if (e.getClassTokenName().equals(elementClassToken)) {
                return e;
            }
        }
        return null;
    }

    /**
     * Method to force a fatal error message to the user, leaving them only the
     * option to close the application
     *
     * @param errorMessage message to be displayed
     * @param except exception which caused the Fatal Error - null if not
     * applicable
     */
    public void makeFatalErrorMessage(String errorMessage, Exception except) {
        fatalError = true;
        String exString = "";
        if (except != null) {
            exString = except.getMessage();
        }
        fatalErrorLog.append(errorMessage).append("\n").append(exString);
        MediPiLogger
                .getInstance().log(MediPi.class
                        .getName() + ".initialisation", "Fatal Error:" + errorMessage + exString);
        primaryStage.setTitle(VERSION);
        HBox hbox = new HBox();
        hbox.setStyle("-fx-background-color: lightblue;");
        Label l = new Label(fatalErrorLog.toString());
        Button exit = new Button("exit");
        exit.setOnAction((ActionEvent t) -> {
            exit();
        });
        hbox.getChildren().addAll(
                exit,
                l
        );
        scene = new Scene(hbox, screenwidth, screenheight);
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    /**
     * Method to reset all the data on all the devices loaded onto MediPi
     *
     */
    public void resetAllDevices() {
        for (Element e : getElements()) {
            if (Device.class
                    .isAssignableFrom(e.getClass())) {
                Device d = (Device) e;
                d.resetDevice();
            }
        }
    }

    @Override
    public void unlocked() {
        unlocked.set(true);
        if (dhm.hasHandlers()) {
            // Start the downloadable timer. This wakes up every definable period (default set to 30s) 
            // and performs functions to send restful messages to retreive the downloadable entities - Hardware and Patient Messages
            try {
                POLLSERVICE = Executors.newSingleThreadScheduledExecutor();
                POLLSERVICE.scheduleAtFixedRate(pim, (long) 1, (long) incomingMessageCheckPeriod, TimeUnit.SECONDS);
            } catch (Exception nfe) {
                makeFatalErrorMessage("Unable to start the download service - make sure that " + MEDIPIDOWNLOADPOLLPERIOD + " property is set correctly", null);
                return;
            }
        }
    }

    @Override
    public void locked() {
        unlocked.set(false);
        POLLSERVICE.shutdownNow();
    }

}
