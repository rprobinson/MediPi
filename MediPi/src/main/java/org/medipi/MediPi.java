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
package org.medipi;

import java.awt.SplashScreen;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.NetworkInterface;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.Enumeration;
import java.util.Properties;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.TilePane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

import org.medipi.authentication.MediPiWindow;
import org.medipi.devices.Device;
import org.medipi.devices.Element;
import org.medipi.logging.MediPiLogger;
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
 * The patient details are currently taken from the configuration properties
 * file but proper consideration of authentication must be included
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
public class MediPi extends Application {

    // MediPi version Number
    private static final String MEDIPINAME = "MediPi Telehealth";
    private static final String VERSION = "MediPi_v1.0.7";
    private static final String VERSIONNAME = "PILOT-20160613-1";

    // Set the MediPi Log directory
    private static final String LOG = "medipi.log";

    // Debug mode can take one of 3 values:
    // 	"debug" mode this will report to standard out debug messages
    // 	"errorsuppress" mode this will suppress all error messages to the UI, instead only outputting to standard out
    //	"none" mode will not report any standard output messages
    private static final String DEBUGMODE = "medipi.debugmode";
    // Switch to put Patient MediPi into a basic view where no graphs or tables of data are shown
    // to the patient eventhough they are still recorded and transmitted. Instead a guide is displayed
    // which directs patients on how to use the device
    private static final String DATAVIEWBASIC = "medipi.dataview.basic";
    // Patient Microbanner info - patient firstname
    private static final String PATIENTFIRSTNAME = "medipi.patient.firstname";
    // Patient Microbanner info - patient surname
    private static final String MEDIPIPATIENTLASTNAME = "medipi.patient.lastname";
    // Patient Microbanner info - patient NHS Number
    private static final String PATIENTNHSNUMBER = "medipi.patient.nhsnumber";
    // Patient Microbanner info - patient DOB
    private static final String PATIENTDOB = "medipi.patient.dob";
    // Screensize settings - default is 800x480 if not set
    private static final String SCREENWIDTH = "medipi.screen.width";
    private static final String SCREENHEIGHT = "medipi.screen.height";
    // CSS file location
    private static final String CSS = "medipi.css";
    //List of elements e.g. Oximeter,Scales,Blood Pressure, transmitter, messenger etc to be loaded into MediPi
    private static final String ELEMENTS = "medipi.elementclasstokens";

    private Stage primaryStage;
    private final ArrayList<Element> elements = new ArrayList<>();
    private TilePane dashTile;
    private VBox subWindow;
    // Fatal error flag to stop use of MediPi - one way there is no set back to false
    private boolean fatalError = false;
    private final StringBuilder fatalErrorLog = new StringBuilder("There has been a fatal error: \n");
    private boolean basicDataView = true;
    private String patientNHSNumber;
    private String formatDOB;
    private String patientFirstName;
    private String patientLastName;
    private String versionIdent;
    private String dataSeparator;
    private Properties properties;
    /**
     * Debug mode governing standard output and error reporting. Debug mode can
     * take one of 3 values: "debug" mode this will report to standard out debug
     * messages. "errorsuppress" mode this will suppress all error messages to
     * the UI, instead outputting to standard out. "none" mode will not report
     * any standard output messages
     *
     */
    private int debugMode = NONE;

    /**
     * allows access to scene to allow the cursor to be set
     */
    public Scene scene;
    //DEBUG states
    /**
     * "NONE" mode will not report any standard output messages
     */
    public static final int NONE = 0;

    /**
     * "DEBUG" mode this will report to standard out debug messages
     */
    public static final int DEBUG = 1;

    /**
     * "ERRORSUPPRESS" mode this will suppress all error messages to the UI,
     * instead outputting to standard out
     */
    public static final int ERRORSUPPRESSING = 2;

    /**
     * "ELEMENTNAMESPACESTEM" a single variable to contain element stem
     */
    public static final String ELEMENTNAMESPACESTEM = "medipi.element.";

    /**
     * Accessible utilities class
     */
    public Utilities utils;

    /**
     * Properties from the main properties file
     */
    public Properties getProperties() {
        return properties;
    }

    /**
     * Boolean to toggle basic dataview where a guide is shown or a graph of
     * recorded data
     *
     * @return basic data view value
     */
    public boolean isBasicDataView() {
        return basicDataView;
    }

    /**
     * patient NHS Number string made accessible for messaging and adding to
     * metadata
     *
     * @return the patient's NHS number
     */
    public String getPatientNHSNumber() {
        return patientNHSNumber;
    }

    /**
     * patient Last Name string made accessible for messaging and adding to
     * metadata
     *
     * @return The patient's Last name
     */
    public String getPatientLastName() {
        return patientLastName;
    }

    /**
     * patient First Name string made accessible for messaging and adding to
     * metadata
     *
     * @return The patient's first name
     */
    public String getPatientFirstName() {
        return patientFirstName;
    }

    /**
     * patient DOB string made accessible for messaging and adding to metadata
     *
     * @return the patient's formatted date of birth (dd-MMM-yyyy)
     */
    public String getPatientDOB() {
        return formatDOB;
    }

    /**
     * Method to get the debug mode
     *
     * @return int representation of debug mode
     */
    public int getDebugMode() {
        return debugMode;
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

            // set versioning and print to standard output
            versionIdent = MEDIPINAME + " " + VERSION + "-" + VERSIONNAME + " starting at " + new Date();
            System.out.println(versionIdent);

            // call to the singletons which provide properties and pop-up messages
            MediPiProperties mpp = MediPiProperties.getInstance();
            if (!mpp.setProperties(getParameters().getNamed().get("propertiesFile"))) {
                makeFatalErrorMessage("Properties file failed to load", null);
            }
            properties = MediPiProperties.getInstance().getProperties();
            // initialise utilities which is accessible to all classes which have reference to MediPi.class
            utils = new Utilities(properties);
            MediPiMessageBox message = MediPiMessageBox.getInstance();
            message.setMediPi(this);
            //Set up logging
            String log = properties.getProperty(LOG);
            if (log == null || log.trim().equals("")) {
                makeFatalErrorMessage(log + " - MediPi log directory is not set", null);
            } else if (new File(log).isDirectory()) {
                MediPiLogger.getInstance().setAppName("MEDIPI", log);
                MediPiLogger.getInstance().log(MediPi.class.getName() + "startup", versionIdent);
            } else {
                makeFatalErrorMessage(log + " - MediPi log directory is not a directory", null);
            }
            //set the data separator - default = ^
            dataSeparator = properties.getProperty("medipi.dataseparator");
            if (dataSeparator == null || dataSeparator.trim().equals("")) {
                dataSeparator = "^";
            }
            //set debug mode - this may need to be removed for production
            String dbm = properties.getProperty(DEBUGMODE);
            if (dbm == null || dbm.trim().length() == 0 || dbm.toLowerCase().trim().equals("none")) {
                debugMode = NONE;
            } else if (dbm.toLowerCase().trim().equals("debug")) {
                debugMode = DEBUG;
            } else if (dbm.toLowerCase().trim().equals("errorsuppressing")) {
                debugMode = ERRORSUPPRESSING;
            }

            // find the Device Name
            /*String ip;
            try {
                // try to find the MAC address
                StringBuilder macAdd = new StringBuilder();
                Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
                while (interfaces.hasMoreElements()) {
                    NetworkInterface iface = interfaces.nextElement();
                    // filters out 127.0.0.1 and inactive interfaces
                    if (iface.isLoopback() || !iface.isUp()) {
                        continue;
                    }
                    byte[] mac = iface.getHardwareAddress();
                    if(mac==null){
                        continue;
                    }
                    System.out.print("Current MAC address : ");
                    for (int i = 0; i < mac.length; i++) {
                        macAdd.append(String.format("%02X%s", mac[i], (i < mac.length - 1) ? ":" : ""));
                    }
                    System.out.print(macAdd.toString().toLowerCase());
                }
                // Using the Mac address unlock the JKS keystore for the device
                try {
                    String ksf = MediPiProperties.getInstance().getProperties().getProperty("medipi.device.cert.location");

                    KeyStore keyStore = KeyStore.getInstance("jks");
                    try (FileInputStream fis = new FileInputStream(ksf)) {
                        //the MAC address used to unlock the JKS must be lowercase
                        keyStore.load(fis, macAdd.toString().toLowerCase().toCharArray());
                        // use a system property to save the certicicate name
                        Enumeration<String> aliases = keyStore.aliases();
                        // the keystore will only ever contain one key -so take the 1st one
                        System.setProperty("medipi.device.cert.name", aliases.nextElement());
                    }
                } catch (KeyStoreException | IOException | NoSuchAlgorithmException | CertificateException e) {
                    makeFatalErrorMessage("Device certificate is not correct for this device", null);
                }

            } catch (Exception e) {
                makeFatalErrorMessage("Can't find Mac Address for the machine, therefore unable to check device certificate", null);
            }*/
            System.setProperty("medipi.device.cert.name", "device.cert");
            // fundamental UI decisions made from the properties
            String b = properties.getProperty(DATAVIEWBASIC);
            if (b == null || b.trim().length() == 0) {
                basicDataView = true;
            } else {
                // If not set then  start in basic view mode
                basicDataView = !b.toLowerCase().startsWith("n");
            }
            // Patient Demographics data is discovered for the patient Microbanner -
            // currently taken directly from the properties but may need interaction with PDS in the future
            Label patientName = null;
            Label nhsNumber = null;
            Label dob = null;
            patientFirstName = properties.getProperty(PATIENTFIRSTNAME);
            if (patientFirstName == null || patientFirstName.trim().equals("")) {
                makeFatalErrorMessage(PATIENTFIRSTNAME + " - Patient First Name is not set", null);
            }
            patientLastName = properties.getProperty(MEDIPIPATIENTLASTNAME);
            if (patientLastName == null || patientLastName.trim().equals("")) {
                makeFatalErrorMessage(MEDIPIPATIENTLASTNAME + " - Patient Last Name is not set", null);
            } else {
                patientName = new Label(patientLastName.toUpperCase() + ", " + patientFirstName);
                patientName.setId("mainwindow-title-microbannerupper");
            }
            patientNHSNumber = properties.getProperty(PATIENTNHSNUMBER);
            if (patientNHSNumber == null || patientNHSNumber.trim().equals("")) {
                makeFatalErrorMessage(PATIENTNHSNUMBER + " - Patient NHS Number is not set", null);
            } else {
                // Check the NHS Number is valid
                //LENGTH CHECK
                if (patientNHSNumber.length() != 10) {
                    makeFatalErrorMessage(PATIENTNHSNUMBER + " - Patient NHS Number (" + patientNHSNumber + ") is not the correct length", null);
                }

                // NUMERIC CHECK
                Long i;
                try {
                    i = Long.parseLong(patientNHSNumber);
                } catch (NumberFormatException e) {
                    makeFatalErrorMessage(PATIENTNHSNUMBER + " - Patient NHS Number (" + patientNHSNumber + ") contains non numeric content", e);
                }
                // MOD11 CHECK
                int len = patientNHSNumber.length();
                int sum = 0;
                for (int k = 1; k <= len; k++) // compute weighted sum
                {
                    sum += (11 - k) * Character.getNumericValue(patientNHSNumber.charAt(k - 1));
                }
                if ((sum % 11) != 0) {
                    makeFatalErrorMessage(PATIENTNHSNUMBER + " - Patient NHS Number (" + patientNHSNumber + ") checksum is not correct", null);
                }
                nhsNumber = new Label(patientNHSNumber);
                nhsNumber.setId("mainwindow-title-microbannerlower");
            }
            String patientDOB = properties.getProperty(PATIENTDOB);
            formatDOB = null;
            if (patientDOB == null || patientDOB.trim().equals("")) {
                makeFatalErrorMessage(PATIENTDOB + " - Patient Date of Birth is not set", null);
            } else {
                try {
                    Date d = Utilities.INTERNAL_DOB_FORMAT.parse(patientDOB);
                    formatDOB = Utilities.DISPLAY_DOB_FORMAT.format(d);
                } catch (ParseException e) {
                    makeFatalErrorMessage(PATIENTDOB + " - Patient Date of Birth (" + patientDOB + ") in wrong format", null);
                }
                dob = new Label(formatDOB);
                dob.setId("mainwindow-title-microbannerlower");
            }
            VBox microPatientBannerVBox = new VBox();
            microPatientBannerVBox.setAlignment(Pos.CENTER);
            microPatientBannerVBox.getChildren().addAll(
                    patientName,
                    new Separator(Orientation.HORIZONTAL),
                    new HBox(new Label("Born "), dob, new Label("  NHS No."), nhsNumber
                    )
            );

            // Start to create the screen
            Label title = new Label(MEDIPINAME);
            title.setId("mainwindow-title");
            title.setAlignment(Pos.CENTER);
            // add the NHS logo
            ImageView iw = new ImageView("/org/medipi/nhs.jpg");
            VBox mainWindow = new VBox();
            mainWindow.setId("mainwindow");
            mainWindow.setPadding(new Insets(0, 5, 0, 5));
            mainWindow.setAlignment(Pos.TOP_CENTER);
            BorderPane titleBP = new BorderPane();
            titleBP.setPadding(new Insets(0, 15, 0, 15));
            titleBP.setMinSize(800, 80);
            titleBP.setMaxSize(800, 80);
            BorderPane.setAlignment(iw, Pos.CENTER);
            titleBP.setLeft(iw);
            titleBP.setCenter(title);
            titleBP.setRight(microPatientBannerVBox);

            // Set up the Dashboard view
            dashTile = new TilePane();
            dashTile.setMinSize(800, 420);
            dashTile.setId("mainwindow-dashboard");
            //bind the visibility property so that when not visible the panel doesnt take any space
            dashTile.managedProperty().bind(dashTile.visibleProperty());
            subWindow = new VBox();
            subWindow.setId("subwindow");
            subWindow.setAlignment(Pos.TOP_CENTER);
            subWindow.getChildren().addAll(
                    dashTile
            );
            try {
                MediPiWindow mw = new MediPiWindow(subWindow);
                mainWindow.getChildren().addAll(
                        titleBP,
                        new Separator(Orientation.HORIZONTAL),
                        mw
                );
            } catch (Exception e) {
                makeFatalErrorMessage("Authentication cannot be loaded - " + e.getMessage(), null);
            }

            StackPane root = new StackPane();
            root.getChildren().add(mainWindow);

            // configure the screensize - the default is 800x480 - see considerations/todo in main text above
            Integer screenwidth = 800;
            Integer screenheight = 480;
            try {
                String sw = mpp.getProperties().getProperty(SCREENWIDTH);
                screenwidth = Integer.parseInt(sw);
                sw = mpp.getProperties().getProperty(SCREENHEIGHT);
                screenheight = Integer.parseInt(sw);
            } catch (Exception e) {
                makeFatalErrorMessage(SCREENWIDTH + " and " + SCREENHEIGHT + " - The configured screen sizes are incorrect: width:" + screenwidth + " height:" + screenheight, e);
            }
            scene = new Scene(root, screenwidth, screenheight);
            // Load CSS properties - see considerations/todo in main text above
            String cssfile = properties.getProperty(CSS);
            if (cssfile == null || cssfile.trim().length() == 0) {
                makeFatalErrorMessage("No CSS file defined in " + CSS, null);
            } else {
                scene.getStylesheets().add("file:///" + cssfile);
            }

            primaryStage.setTitle(VERSION);
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
                        if (initError == null) {
                            dashTile.getChildren().add(elem.getDashboardTile());
                            elements.add(elem);
                            subWindow.getChildren().add(elem.getWindowComponent());
                        } else {
                            MediPiMessageBox.getInstance().makeErrorMessage("Cannot instantiate an element: \n" + classToken + " - " + elementClass + " - " + initError, null, Thread.currentThread());
                        }
                    } catch (Exception ex) {
                        MediPiMessageBox.getInstance().makeErrorMessage("Cannot instantiate an element: \n" + classToken + " - " + elementClass, ex, Thread.currentThread());
                    }
                }
            } else {
                makeFatalErrorMessage("No Elements have been defined", null);
            }

            //show the tiled dashboard view
            callDashboard();
            // functionality which closes the window when the x is pressed
            primaryStage.setOnHiding((WindowEvent event) -> {
                exit();
            });

            // splash screen
            final SplashScreen splash = SplashScreen.getSplashScreen();

            //Close splashscreen
            if (splash != null) {
                if (getDebugMode() == MediPi.DEBUG) {
                    System.out.println("Closing splashscreen...");
                }
                splash.close();
            }
        } catch (Exception e) {
            makeFatalErrorMessage("A fatal and fundamental error occurred at bootup", e);
        }
    }

    /**
     * Method to close the JavaFX application
     */
    public void exit() {
        Platform.runLater(() -> {
            System.exit(0);
        });
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
        dashTile.setVisible(true);
    }

    /**
     * Method to hide all the element windows from the MediPi mainwindow
     */
    public void hideAllWindows() {
        dashTile.setVisible(false);
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
        Label l = new Label(fatalErrorLog.toString());
        l.setStyle("-fx-background-color: lightblue;");
        scene = new Scene(l);
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
}
