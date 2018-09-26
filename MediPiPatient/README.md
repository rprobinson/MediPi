# MediPi Patient Unit Software and Hardware

![2017-02-07-220414_800x480_scrot](https://cloud.githubusercontent.com/assets/13271321/26764192/608f2504-495a-11e7-983c-ca7cf0ad8f09.png)

## Software
This is an implementation of a Telehealth patient/client system (It is intended to be used in with the MediPi Concentrator server implementation which is published alongside this GitHub account). It has been developed to be flexible and extensible.

This project has been developed as a demonstration of a general telehealth system with clinical involvement from a Hertfordshire Community NHS Trust, it currently has three main clinical applications: Heart Failure, COPD and Diabetes.
The project is written in Java using JavaFX which communicates with the USB medical devices using javax-usb libraries and Bluetooth devices, but input can be taken from any source with extention of the current flexible interfaces.

It is intended to be used in with the MediPi Concentrator server implementation which is published in this GitHub account

Functionality:

* Included drivers record data from USB and Bluetooth physiological devices:
  * Finger Oximeter
  * Diagnostic Scales
  * Blood Pressure Meter (upper arm cuff)
  * Manual entry Thermometer
  * Patient Yes/No Daily Questionnaire
* Flexible Scheduler
* Secure Flexible Transmitter - 2way SSL/TLS mutual authenticated messaging in transit and encryption and signing of the data at rest
	* Direct per patient text based messages from clinician
	* Alerts based upon programmable measurement thresholds transmitted from clinical system
* Remotely configurable - this funtionality is part implemented (change scripts can be remotely and individually uploaded to the MediPi Patient Unit but there is currently no functionality to execute them on the device)

This is a list of functionality to date which has been created under guidance from clinicians. It is not an exhaustive list and is intended to be expanded.

See the MediPi Summary Document: https://github.com/rprobinson/MediPi/blob/master/MediPi_Summary-v1.3.pdf

## Architecture
### MediPi Class
The application is managed by the main MediPi class. This orchestrates the initiation of certain resources (Authentication interface, validation of device MAC address against the device certificate) initialises the UI framework/primary stage, initiates each of the elements of MediPi and can return information about its version if asked.

### Element Class
All main functions of MediPi are encapsulated as Elements. These are visually represented on the main screen as tiles (governed by dashboardTiles.class). Elements may be used to capture data from the patient (e.g. Bluetooth devices, USB enabled devices or user interface for capturing responses to yes/no questions) or may be used for other functions (e.g. transmitting data, scheduler, settings, responses or displaying incoming messages from the clinician).

This class provides container window nodes for the UI

![Element image](https://cloud.githubusercontent.com/assets/13271321/13568138/3856b57a-e457-11e5-947c-299967dc2d2a.png)


#### Device Class
Elements whose purpose is to record data from the patient are classified as Devices. This is a specific abstract subclass of Element which provides methods for discovery of device make and model, notifying when data is present, retrieving data and resetting the device.

#### Generic Device Class
Generic Device classes are subclasses of Device class which group functionality for a particular type of device which records data e.g. Oximeter, Blood Pressure, Scale, Questionnaire. The class controls the UI for the device. As generic device classes are abstract they require a device specific concrete driver class to provide the data e.g. Nonin9560.

#### Driver Classes
These are the concrete classes which retrieve data from the specific physiological device. They are make and model specific. There are currently 2 ways which data can be taken:

1. Serially/continuously - data is taken for the duration of the device's use, passed to the generic device class and averages are shown on the interface.
2. Downloaded - All available data on the medical device is downloaded and passed to the Generic Device Class.

#### Other Concrete Classes
* #### Questionnaire Device Class
A Generic Device Class to display and manage a simple yes/no Questionnaire which will follow a ruleset and depending on responses will direct a pathway through the questionnaire. Ultimately the questionnaire ends when an advisory response is returned. The results can be transmitted. The transmittable data contains all the questions, answers and advice given in plain text. The questionnaire rulesets are versioned and this information is transmitted. Current questionnaires included in the project are for Heart Failure, COPD and Diabetes and have been devised by Hertfordshire Community NHS Trust. They represent a digital replacement of an existing paper-based flowchart questionnaire.

* #### Scheduler Device Class (called Readings in the tiled dashboard)
A Device Class to schedule data recording and orchestrate the collection and transmission of the data from the devices. It reminds patients when it is time to take measurements based upon its programmable schedule: This groups funtionality from the devices (both physiological and patient input) into a seemless process ending in the transmission of the data to the clinician. The class is directed by a scheduler.json file which contains a schedule and details of all previously executed schedules. This file consists of json entries:

	* SCHEDULED: The file must contain at least one SCHEDULED line. This records when the schedule was due, its repeat period and what elements are due to be executed (these are defined by the element class token name). All subsequent scheduled events are calculated from the latest SCHEDULED line
	* STARTED: This records when a schedule was started and what elements were due to be run (this can configurably be omitted)
	* MEASURED: This records what time a particular element was measured (this can configurably be omitted)
	* TRANSMITTED: This records at what time, which elements were transmitted

	The scheduler class has been designed to allow the scheduler.json file to be remotely updated but currently only part implemented - see above.

	Each of the scheduled elements defined in the most recent SCHEDULED element are executed in turn and the transmitter is called

	The devices which are to be executed as part of the schedule can be modified and maintained by the Settings element

* #### Transmitter Element Class
	Class to handle the functionality for transmitting the data collected by other Device elements to a known endpoint. The class is currently extented with a RESTful transmitter class allowing a payload particular to the MediPi Concentrator to be RESTfully passed in the correct format and manner, however a different transport method and format could be implemented by extention here.

	The transmitter element shows a list of all the elements in the schedule with checkboxes allowing choice of data to be sent. These are enabled if data is present/ready to be transmitted and each line has a summary of the data results. Transmitter will take all available and selected data and will place it in a json structure. This payload is encrypted and then signed using the patient certificate. The resulting json JWT/JWE is passed in a json message - this uses Nimbus JOSE + JWT libraries. The encryption at rest is performed using the MediPi Transport Tools common Library - this is published also in this GitHub repository.The message is sent to a restful interface on the MediPi Concentrator encrypted in transit using 2-way SSL/TLS mutual authenticated messaging.

* #### Settings Element Class
	The Settings class allows certain configurations to be changed. These configurations should allow general setup and maintainence of the MediPi Patient Device in a production environment. This interface isn't usually enabled in the patient mode but is available in the clinician/admin mode. Currently the available configurations are:
	* Patient Demographic Settings: validated fields for updating patient forename, surname, Date of Birth and NHS Number
	* Bluetooth Settings: validated fields for setting MAC addresses for serial bluetooth devices. Only devices requiring this setting are shown
	* Synchronisation procedures (automatic and manual) for specific devices where it is necessary to maintain correct time on the physiological device.
	* Schedule Settings: A checkbox is displayed for each device configured on the MediPi Patient Device allowing a patient specific choice of scheduled physiological devices to be made. Validated input fields for first schedule time and repeat time.

* #### Responses Element Class
	 The MediPi Clinical system allows clinicians to set and maintain thresholds for each device a patient submits data from. The responses element allows patients to see the results of their current scheduled measurement submissions when compared and calculated against the clincian's thresholds. The responses class receives automatically created encrypted and signed messages from the Clinical system and displays each of them in a list coloured by their threshold status. The overall status of the current schedule period is displayed on the element's tile as a green smiley face, a red frowny face, an exclaimation mark or question mark dependent on the responses.


#### Guide Class
This is a class to encapsulate the Guide for any Device. This class reads a flat-text ruleset and constructs a guide object from it using referenced images and text with forward and backward buttons. It is designed to help patients with descriptions and imagres take readings from a physiological device

### VPN Connection
MediPi Patient software includes configuration to connect to the MediPi Concentrator through a Virtual Private Network (VPN) when necessary and drops the connection after the communication has been sent. The example does not include a vpn file (it has been tested using OpenVPN) but the configuration should allow for any VPN connection provided that it can be brought up and down via a command line script. Alternatively the vpn can be initialised as part of the MediPi Patient Unit's calling script.

### Time Server Synchronisation
MediPi Patient software relies on the internal clock being correct. Some physiological devices take their data value timestamps directly from the internal clock and other devices use the internal clock as a reference for granular based checking to see if the physiological device's clock has drifted outside of an allowable threshold. Correct timestamp is vital for the scheduler and scheduling functionality. To this end, synchronisation is vital before any data can be recorded. As part of the calling script which MediPi Patient uses when executed, it calls the timesync.sh script which attempts to contact pool.ntp.org. It will repeatedly continue to do this until a sucessful synchronisation has occurred. When this happens MediPi patient software will enable all data downloading functions with the devices.

### Authentication
MediPi Patient software includes an authentication interface with an implementation of an n-digit PIN keypad authentication class. Other methods of authentication could be developed and plugged into this interface (When in the Admin mode using the Raspberry Pi as authentication has already been input to boot into the admin environment a interface of NONE is used which allows access without authentication). The Keypad allows access to MediPi Patient functionality when a 4 digit PIN is entered - technically this unlocks the patient certificate Java Keystore. The keypad will automatically require reauthentication and reappear after a configurable period of inactivity. The Keypad also has an "Admin" logon facility which takes a 10 digit PIN known to the Administrative user for access to configure certain parameters: bluetooth connections and displayable patient demographics.

### CSS Implementation
MediPi uses JavaFX which can be skinned/controlled using CSS. Whilst individual elements can be controlled, a refactoring exercise is required to properly implement it and take full advantage of the technology.

### MediPi.properties and configuration files
MediPi.properties file defines the properties for the configuration of MediPi.

Medipi has been designed to be flexible and extensible and uses dynamically initialised Element classes from the properties file. Elements are defined in the properties file and only those Elements which appear in the medipi.elementclasstokens list will be initialised.

MediPi.admin.properties file defines the properties for the MediPi when it is booted in admin mode. This mode is designed for the configuration of the Raspberry Pi MediPi patient unit: updating the patient name, NHS Number, DOB and managing the bluetooth pairing and access.
The admin mode is accessed by clicking on the "admin" button on the authentication screen and inputting a 10 digit admin code. Successful input of this code reboots MediPi Patient into the admin mode controlled by the MediPi.admin.properties file.

The patient details are defined in a separate json file, however these are only used for display on the patient device and are not used in any communication or attached to the device data in transit. All MediPi communication uses a generated pseudonymised UUID to identify the patient. This UUID is used to cross reference against the patient details in the clinical system, so no patient identifiable data is kept in the MediPi Concentrator database. The patient DOB and NHS Number are hidden when the unit is not authenticated.

#### Instructions to update configuration files
1. Copy config directory to an external location e.g. C:\MediPi\ (for windows machine) or /home/{user}/MediPi (Linux based)
2. Open command prompt which is capable of executing .sh file. (Git bash if you are on windows. Terminal on linux installation is capable of executing sh files)
3. Go to config directory location on command prompt e.g. C:\MediPi\config or /home/{user}/MediPi/config
4. Execute setup-all-configurations.sh "{config-directory-location}" as './set-all-configurations.sh "C:/config"' or './set-all-configurations.sh "/home/{user}/config"'. This will replace all the relative paths in properties and guides files of the configuration.

### Software Dependencies:
MediPi depends on the following libraries:

* **nimbus-jose-jwt** - [http://connect2id.com/products/nimbus-jose-jwt](http://connect2id.com/products/nimbus-jose-jwt) for json encryption and signing - Apache 2.0
* **Jackson core/annotations/bind** - Apache 2.0
* **libUSB4Java** for USB control [https://github.com/usb4java/libusb4java](https://github.com/usb4java/libusb4java)
* **Java RXTX libraries for serial devices:** [https://github.com/rxtx/rxtx](https://github.com/rxtx/rxtx)
* **Bluecove** for communication over bluetooth - Apache 2.0 [http://www.bluecove.org/](http://www.bluecove.org/)

### Bluetooth Medical Device Interfaces:
3 particular devices have been created but others can be developed.

* Nonin 9560 Finger Pulse Oximeter - There are 2 implementations of this device - one which uses the Serial Port Protocol and one which uses the Continua Health Device Protocol (HDP). This uses code from the signove open source library [https://github.com/signove/hdpy](https://github.com/signove/hdpy) - HDPy is a pure Python implementation of HDP profile and MCAP protocol [http://oss.signove.com/](http://oss.signove.com/) and the device driver uses python scripts to retreive the data (this version has been used in the pilot as it is reliable).
* Marsden M430 Scales - The device driver uses Marsden's freely available Serial Post Protocol
* Omron708BT Upper Arm Blood Pressure Monitor: The device driver uses the Continua Health Device Protocol (HDP). This uses code from the signove open source library [https://github.com/signove/hdpy](https://github.com/signove/hdpy) - HDPy is a pure Python implementation of HDP profile and MCAP protocol [http://oss.signove.com/](http://oss.signove.com/) and the device driver uses python scripts to retreive the data.

### USB Medical Device Interfaces:
3 particular devices have been used but others can be developed.

* Contec CMS50D+ Finger Pulse Oximeter - The interface is a Java port of the streamed serial interface developed here: https://github.com/atbrask/CMS50Dplus. The device can store up to 24 hours of data but this function has not implemented.
* Beurer BF480 Diagnostic Scales - The Java code is based upon [https://usb2me.wordpress.com/2013/02/03/beurer-bg64/](https://usb2me.wordpress.com/2013/02/03/beurer-bg64/) but the BF480 is a cheaper scale and has a different data structure
* Beurer BG55 Upper Arm Blood Pressure Monitor: This Java code was reverse engineered based upon the experience gained with the previous two devices


## Hardware:
The MediPi project is a software project but is dependent on hardware for use in a patient's home setting. As MediPi is written in Java, it can be run on any system which has an appropriate JRE and thus is cross platform. This implementation of MediPi has been executed using the following hardware components:

#### MediPi Patient Module:
* Raspberry Pi 3: [https://www.raspberrypi.org/products/raspberry-pi-2-model-b/](https://www.raspberrypi.org/products/raspberry-pi-2-model-b/)
* Raspberry Pi Touch Display: [https://www.raspberrypi.org/products/raspberry-pi-touch-display/](https://www.raspberrypi.org/products/raspberry-pi-touch-display/)
* MultiComp Enclosure [http://uk.farnell.com/multicomp/cbrpp-ts-blk-wht/raspberry-pi-touchscreen-enclosure/dp/2494691?MER=bn_search_2TP_Echo_4](http://uk.farnell.com/multicomp/cbrpp-ts-blk-wht/raspberry-pi-touchscreen-enclosure/dp/2494691?MER=bn_search_2TP_Echo_4)
* Sontrinics 2.5A PSU [http://uk.farnell.com/stontronics/t6090dv/psu-raspberry-pi-5v-2-5a-uk-euro/dp/2520786](http://uk.farnell.com/stontronics/t6090dv/psu-raspberry-pi-5v-2-5a-uk-euro/dp/2520786)
* 16Gb microSD card class 10

#### Physiological Measurement Devices:
* Nonin Pulse Oximeter Onyx II Model 9560 with Bluetooth [http://www.nonin.com/Onyx9560](http://www.nonin.com/Onyx9560)
* Marsden M-430 Bluetooth Floor Scale [http://www.marsden-weighing.co.uk/index.php/marsden-m-430.html](http://www.marsden-weighing.co.uk/index.php/marsden-m-430.html)
* Omron 708BT Bluetooth Upper Arm Blood Pressure Device [http://www.healthcare.omron.co.jp/bt/english/](http://www.healthcare.omron.co.jp/bt/english/)
* Contec CMS50D+ Finger Pulse Oximeter: [http://www.contecmed.com/index.php?page=shop.product_details&flypage=flypage.tpl&product_id=126&category_id=10&option=com_virtuemart&Itemid=595](http://www.contecmed.com/index.php?page=shop.product_details&flypage=flypage.tpl&product_id=126&category_id=10&option=com_virtuemart&Itemid=595)
* Beurer BF480 Diagnostic Scales: [https://www.beurer.com/web/uk/products/Beurer-Connect/HealthManager-Products/BF-480-USB](https://www.beurer.com/web/uk/products/Beurer-Connect/HealthManager-Products/BF-480-USB)
* Beurer BG55 Upper Arm Blood Pressure Monitor: [https://www.beurer.com/web/en/products/bloodpressure/upper_arm/BM-55](https://www.beurer.com/web/en/products/bloodpressure/upper_arm/BM-55)

## Certificates and PKI
The Patient device requires 2 certificates:
#### - Patient Certificate - The JKS password controls the authentication of the patient device. The cert is used to encrypt and sign the patient measurement data in the EncryptedAndSignedUploadDO data object.
#### - Device Certificate - The JKS is unlocked using the MAC address of the host computer at start up and will not allow operation unless the MAC address of the system unlocks the device certificate JKS. The provided test certificate will not work on your system as the MAC address will not match your system, however for test purposes the following line can be amended in org.medpi.MediPi class to allow it to work:

For the device cert 9b636f94-e1c2-4773-a5ca-3858ba176e9c.jks

Linux:

change the line:

```
	String addr = in.readLine();
```
to be:

```
	String addr = "b8:27:eb:27:09:93";
```

non-Linux:

```
	macAddress = macAdd.toString().toLowerCase();
```
to be:

```
	macAddress = "b8:27:eb:27:09:93";
```

The Device Certificate is also used for 2-Way SSl/TLSMA encryption on the data in transit.

The certs for MediPi Patient software are published here (and are intended to work out-of-the-box) as java key stores and should allow testing of the MediPiPatient with the MediPi Concentrator.

The patient authentication PIN is 2222

The admin authentication PIN is 9999999999

**The certs and the PKI is for testing purposes and not suitable for use in any other circumstance**

## Licence

 Licensed under the Apache License, Version 2.0 (the "License");
 you may not use this code except in compliance with the License.
 You may obtain a copy of the License at

 http://www.apache.org/licenses/LICENSE-2.0

 Unless required by applicable law or agreed to in writing, software
 distributed under the License is distributed on an "AS IS" BASIS,
 WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 See the License for the specific language governing permissions and
 limitations under the License.

## Warranty
Under construction... from the Apache 2 licence:

Unless required by applicable law or agreed to in writing, Licensor provides the Work (and each Contributor provides its Contributions) on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied, including, without limitation, any warranties or conditions of TITLE, NON-INFRINGEMENT, MERCHANTABILITY, or FITNESS FOR A PARTICULAR PURPOSE. You are solely responsible for determining the appropriateness of using or redistributing the Work and assume any risks associated with Your exercise of permissions under this License.

## Liability
Under construction... from the Apache 2 licence:

In no event and under no legal theory, whether in tort (including negligence), contract, or otherwise, unless required by applicable law (such as deliberate and grossly negligent acts) or agreed to in writing, shall any Contributor be liable to You for damages, including any direct, indirect, special, incidental, or consequential damages of any character arising as a result of this License or out of the use or inability to use the Work (including but not limited to damages for loss of goodwill, work stoppage, computer failure or malfunction, or any and all other commercial damages or losses), even if such Contributor has been advised of the possibility of such damages.

# Quick Start Guide
Assuming the use of a Raspberry Pi 2 or 3:

1. Flash the latest Raspbian Jessie image to a C10 microSD card (at least 8Gb)
2. update and upgrade the Raspbian OS:

    ```
    sudo apt-get update
    sudo apt-get upgrade
    ```
3. Using the raspbian configs increase the GPU Memory to at least 256Mb

4. Depending on the enclosure used the screen may need rotating:

    ```
    sudo nano /boot/config.txt
    ```
then add a line:

    ```
    lcd_rotate=2

    ```

5. Install OpenJFX - Since java 1.8.0_33 Java for ARM hardfloat has not shipped with JavaFX.
Guide for building OpenJFX: https://wiki.openjdk.java.net/display/OpenJFX/Building+OpenJFX

6. Install Java RXTX libraries for serial devices

    ```
    sudo apt-get install librxtx-java

    ```
7. Configure the pi so that USB ports can be used without needing su

    ```
    sudo adduser pi plugdev
    sudo udevadm control --reload
    sudo udevadm trigger
    sudo nano /etc/udev/rules.d/50-MediPi-v3.rules

    ```
    then add the lines:

    ```
    ACTION=="add", SUBSYSTEMS=="usb", ATTRS{idVendor}=="04d9", 	ATTRS{idProduct}=="8010", MODE="660", GROUP="plugdev"
    ACTION=="add", SUBSYSTEMS=="usb", ATTRS{idVendor}=="0c45", 	ATTRS{idProduct}=="7406", MODE="660", GROUP="plugdev"

    ```
8. Refer the section `Instructions to update configuration files` in this document above to update the configurations files.

9. Build MediPi.jar using maven build. Navigate to MediPi/MediPiPatient in the cloned repository and execute `mvn clean install`

10. Copy the `{medipi-repo-directory}/MediPi/MediPiPatient/target/MediPi.jar` file to /home/{user}/MediPi/ directory

11. Upgrade the Java Cryptography Extention. Download from http://www.oracle.com/technetwork/java/javase/downloads/jce8-download-2133166.html and follow the README.txt instructions included in the package. The certs included for demonstration purposes require greater strength binaries in the JRE than are present by default.
12. Execute MediPi using:

        java -Djava.library.path=/usr/lib/jni -jar /home/{user}/MediPi/MediPi.jar --propertiesFile=/home/{user}/MediPi/config/MediPi.properties

# Even Quicker Start - MediPi V1.0.15.img Pilot image file
If you wish to try out the software using a raspberry pi (with or without a touchscreen) here is a microSD card image which you can use. Uncompress and flash the image to a microSD card (8Gb or larger) and insert into a raspberry pi 3. This downloadable image file of MediPi Patient v1.0.15 was built on the latest version of Raspbian (Jessie) and has been compressed. This is the image which is being used in the pilot.

[Compressed MediPi Image File](https://www.dropbox.com/s/i1bvwtja3up5vcg/MediPiImage_v1.0.15_PILOT-20171025-1_sanitised.img.zip?dl=0)

[Raspberry Pi Guide to writing an image to microSD](https://www.raspberrypi.org/documentation/installation/installing-images/)

This has been tested using the Raspberry Pi 3 with the Raspberry Pi official 7" touchsceen.

The patient authentication PIN is 2222

The admin authentication PIN is 9999999999
