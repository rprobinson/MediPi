# MediPi Patient Telehealth System

![Element image](https://cloud.githubusercontent.com/assets/13271321/18472733/fe3ba2e8-79b0-11e6-8097-8ebc0ed732dc.jpg)

##Software
This is an implementation of a Telehealth patient/client system (It is intended to be used in with the MediPi Concentrator server implementation which is published alongside this GitHub account). It has been developed to be flexible and extensible.

This project started as a demonstration of a general telehealth system but with clinical involvement from a Hertfordshire Community NHS Trust, it has been developed into a Heart Failure implementation.
The project is written in Java using JavaFX which communicates with the USB medical devices using javax-usb libraries.

It is intended to be used in with the MediPi Concentrator server implementation which is published alongside this GitHub account

Functionality:

* Records data from:
  * Finger Oximeter
  * Diagnostic Scales
  * Blood Pressure Meter (upper arm cuff)
  * Patient Yes/No Daily Questionnaire
* Flexible Scheduler
* Direct per patient text based messages from clinician
* Alerts based upon programmable measurement thresholds transmitted from clinical system
* Secure Flexible Transmitter - 2way SSL/TLS mutual authenticated messaging in transit and encryption and signing of the data at rest
* Remotely configurable

This is a list of functionality to date which has been created under guidance from clinicians. It is not an exhaustive list and is intended to be expanded.

See the MediPi Summary Document: https://github.com/rprobinson/MediPi/blob/master/MediPi_Summary-v1.3.pdf

##Architecture
###MediPi Class
The application is managed by the main MediPi class. This orchestrates the initiation of certain resources (Authentication interface, validation of device MAC address against the device certificate) initialises the UI framework/primary stage, initiates each of the elements of MediPi and can return information about its version if asked.

###Element Class
All main functions of MediPi are encapsulated as Elements. These are visually represented on the main screen as tiles (governed by dashboardTiles.class). Elements may be used to capture data from the patient (e.g. USB enabled devices or user interface for capturing responses to yes/no questions) or may be used for other functions (e.g. transmitting data or displaying incoming messages from the clinician).

This class provides container window nodes for the UI

![Element image](https://cloud.githubusercontent.com/assets/13271321/13568138/3856b57a-e457-11e5-947c-299967dc2d2a.png)


####Device Class
Elements whose purpose is to record data from the patient are classified as Devices. This is a specific subclass of Element which provides methods for notifying when data is present, retrieving data and resetting the device.

####Generic Device Class
Generic Device classes are subclasses of Device class. The class controls the UI for the device. Through configuration it can run in a simple or advanced mode. In the simple mode a digest of the data in large text and a guide to using the device is displayed and in "advanced" mode graphs or tables of the data taken are displayed. This display mode is governed by the properties file. Generic device classes require a concrete driver class to provide the data.

#####Guide Class
This is a class to encapsulate the Guide for any Element. This class reads a flat-text ruleset and constructs a guide object from it using referenced images and text with forward and backward buttons. Each guide window has one image and some accompanying text. The specific content of the guides provided has not been verified or certified.

####Driver Classes
These are the concrete classes which retrieve data from the specific USB enabled device. They are make and model specific. There are 2 ways which data can be taken:

1. Serially/continuously - data is taken for the duration of the device's use, passed to the generic device class and averages are shown on the interface.
2. Downloaded - All data on the medical device is downloaded and passed to the Generic Device Class.

####Other Concrete Classes
* ####Questionnaire Device Class
A Generic Device Class to display and manage a simple yes/no Questionnaire which will follow a ruleset and depending on responses will direct a pathway through the questionnaire. Ultimately the questionnaire ends when an advisory response is returned. The results can be transmitted. The transmittable data contains all the questions, answers and advice given in plain text irrespective of ultimate advice. The questionnaire rulesets are versioned and this information is transmitted

* ####Scheduler Device Class
A Device Class to schedule data recording and orchestrate the collection and transmission of the data from the devices. The class is directed by a .scheduler file which contains details of all previously executed schedules. This file consists of line entries of:

	* SCHEDULED: The file must contain at least one SCHEDULED line. This records when the schedule was due, its repeat period and what elements are due to be executed (these are defined by the element class token name). All subsequent scheduled events are calculated from the latest SCHEDULED line
	* STARTED: This records when a schedule was started and what elements were due to be run
	* MEASURED: This records what time a particular element was measured
	* TRANSMITTED: This records at what time, which elements were transmitted
	
	The scheduler class has been designed to allow the .scheduler file to be remotely updated but currently the functionality to update the file using outgoing polling from a remote location is not implemented. 

	Each of the scheduled elements are executed in turn and the transmitter is called

* ####Transmitter Element Class
Class to display and handle the functionality for transmitting the data collected by other Elements to a known endpoint. 

	The transmitter element shows a list of all the elements loaded into MediPi with checkboxes which are enabled if data is present/ready to be transmitted. Transmitter will take all available and selected data and will place it in a json structure. This payload is encrypted and then signed using the patient certificate. The resulting json JWT/JWE is passed in a json message - this uses Nimbus JOSE + JWT libraries. The message is sent to a restful interface on the MediPi Concentrator encrypted in transit using 2-way SSL/TLS mutual authenticated messaging.

###CSS Implementation
MediPi uses JavaFX which can be controlled using CSS. The implementation of this is sub-optimal and has been done on a node by node basis. Whilst individual elements can be controlled, a refactoring exercise is required to properly implement it and take full advantage of the technology.

###MediPi.properties and configuration files
MediPi.properties file defines the properties for the configuration of MediPi.

Medipi has been designed to be flexible and extensible and uses dynamically initialised Element classes from the properties file. Elements are defined in the properties file and only those Elements which appear in the medipi.elementclasstokens list will be initialised or displayed.

The patient details are defined in the properties file, however these are only used for display on the patient device and are not used in any communication or attached to the device data in transit. All MediPi communication uses a generated UUID to identify the patient. This UUID is used to cross reference against the patient details in the clinical system, but this is outside the scope of this project

####Instructions to update configuration files
1. Copy config directory to an external location e.g. C:\MediPi\ (for windows machine) or /home/{user}/MediPi (Linux based)
2. Open command prompt which is capable of executing .sh file. (Git bash if you are on windows. Terminal on linux installation is capable of executing sh files)
3. Go to config directory location on command prompt e.g. C:\MediPi\config or /home/{user}/MediPi/config
4. Execute setup-all-configurations.sh "{config-directory-location}" as './set-all-configurations.sh "C:/config"' or './set-all-configurations.sh "/home/{user}/config"'. This will replace all the relative paths in properties and guides files of the configuration.

###Software Dependencies:
MediPi depends on the following libraries:

* **nimbus-jose-jwt** - [http://connect2id.com/products/nimbus-jose-jwt](http://connect2id.com/products/nimbus-jose-jwt) for json encryption and signing - Apache 2.0
* **json-smart** Apache 2.0 licence
* **Jackson core/annotations/bind** - Apache 2.0
* **libUSB4Java** for USB control [https://github.com/usb4java/libusb4java](https://github.com/usb4java/libusb4java)
* **Java RXTX libraries for serial devices:** [https://github.com/rxtx/rxtx](https://github.com/rxtx/rxtx)
* **extFX** - JavaFX has no implementation for a Date axis in its graphs so the extFX library has been used (Published under the MIT OSS licence. ([https://bitbucket.org/sco0ter/extfx](https://bitbucket.org/sco0ter/extfx))

###USB Medical Device Interfaces:
3 particular devices have been used but others can be developed.

* Contec CMS50D+ Finger Pulse Oximeter - The interface is a Java port of the streamed serial interface developed here: https://github.com/atbrask/CMS50Dplus. The device can store up to 24 hours of data but this function has not implemented.
* Beurer BF480 Diagnostic Scales - The Java code is based upon [https://usb2me.wordpress.com/2013/02/03/beurer-bg64/](https://usb2me.wordpress.com/2013/02/03/beurer-bg64/) but the BF480 is a cheaper scale and has a different data structure
* Beurer BG55 Upper Arm Blood Pressure Monitor: This Java code was reverse engineered based upon the experience gained with the previous two devices


##Hardware:
The MediPi project is a software project but is dependent on hardware for use in a home setting. As MediPi is written in Java, it can be run on any system which has an appropriate JRE and thus is cross platform. This Heart Failure implementation of MediPi has been executed using:

####MediPi Patient Module:
* Raspberry Pi 3: [https://www.raspberrypi.org/products/raspberry-pi-2-model-b/](https://www.raspberrypi.org/products/raspberry-pi-2-model-b/)
* Raspberry Pi Touch Display: [https://www.raspberrypi.org/products/raspberry-pi-touch-display/](https://www.raspberrypi.org/products/raspberry-pi-touch-display/)
* MultiComp Enclosure [http://uk.farnell.com/multicomp/cbrpp-ts-blk-wht/raspberry-pi-touchscreen-enclosure/dp/2494691?MER=bn_search_2TP_Echo_4](http://uk.farnell.com/multicomp/cbrpp-ts-blk-wht/raspberry-pi-touchscreen-enclosure/dp/2494691?MER=bn_search_2TP_Echo_4)
* Sontrinics 2.5A PSU [http://uk.farnell.com/stontronics/t6090dv/psu-raspberry-pi-5v-2-5a-uk-euro/dp/2520786](http://uk.farnell.com/stontronics/t6090dv/psu-raspberry-pi-5v-2-5a-uk-euro/dp/2520786)
* 8Gb microSD card class 10

####Physiological Measurement Devices:
* Contec CMS50D+ Finger Pulse Oximeter: [http://www.contecmed.com/index.php?page=shop.product_details&flypage=flypage.tpl&product_id=126&category_id=10&option=com_virtuemart&Itemid=595](http://www.contecmed.com/index.php?page=shop.product_details&flypage=flypage.tpl&product_id=126&category_id=10&option=com_virtuemart&Itemid=595)
* Beurer BF480 Diagnostic Scales: [https://www.beurer.com/web/uk/products/Beurer-Connect/HealthManager-Products/BF-480-USB](https://www.beurer.com/web/uk/products/Beurer-Connect/HealthManager-Products/BF-480-USB)
* Beurer BG55 Upper Arm Blood Pressure Monitor: [https://www.beurer.com/web/en/products/bloodpressure/upper_arm/BM-55](https://www.beurer.com/web/en/products/bloodpressure/upper_arm/BM-55)

## Certificates and PKI
The Patient device requires 2 certificates:
####- Patient Certificate - The JKS password controls the authentication of the patient device. The cert is used to encrypt and sign the patient measurement data in the EncryptedAndSignedUploadDO data object.
####- Device Certificate - The JKS is unlocked using the MAC address of the host computer at start up and will not allow operation unless the MAC address of the system unlocks the device certificate. The provided test certificate will not work on your system, however for test purposes the following line can be amended in org.medpi.MediPi class to allow it to work:

For the device cert 9b636f94-e1c2-4773-a5ca-3858ba176e9c.jks 

Linux:

```
	350 String addr = "b8:27:eb:27:09:93";
```

non-Linux:

```
	397 macAddress = "b8:27:eb:27:09:93";
```

The Device Certificate is also used for 2-Way SSl/TLSMA encryption on the data in transit.
	
The certs for MediPi Patient software are published here (and are intended to work out-of-the-box) as java key stores and should allow testing of the MediPiPatient with the MediPi Concentrator. The authentication PIN is 2222
**The certs are for testing purposes and not suitable for use in any other circumstance**

##Licence

 Licensed under the Apache License, Version 2.0 (the "License");
 you may not use this code except in compliance with the License.
 You may obtain a copy of the License at

 http://www.apache.org/licenses/LICENSE-2.0

 Unless required by applicable law or agreed to in writing, software
 distributed under the License is distributed on an "AS IS" BASIS,
 WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 See the License for the specific language governing permissions and
 limitations under the License.

##Warranty 
Under construction... from the Apache 2 licence:

Unless required by applicable law or agreed to in writing, Licensor provides the Work (and each Contributor provides its Contributions) on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied, including, without limitation, any warranties or conditions of TITLE, NON-INFRINGEMENT, MERCHANTABILITY, or FITNESS FOR A PARTICULAR PURPOSE. You are solely responsible for determining the appropriateness of using or redistributing the Work and assume any risks associated with Your exercise of permissions under this License.

##Liability
Under construction... from the Apache 2 licence:

In no event and under no legal theory, whether in tort (including negligence), contract, or otherwise, unless required by applicable law (such as deliberate and grossly negligent acts) or agreed to in writing, shall any Contributor be liable to You for damages, including any direct, indirect, special, incidental, or consequential damages of any character arising as a result of this License or out of the use or inability to use the Work (including but not limited to damages for loss of goodwill, work stoppage, computer failure or malfunction, or any and all other commercial damages or losses), even if such Contributor has been advised of the possibility of such damages.

#Quick Start Guide
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

#MediPi V1.0.8 .img file
This downloadable image file of MediPi Patient v1.0.8 was built on the latest version of Raspbian (Jessie) and has been compressed.
Uncompress and write to a microSD card. 

[Compressed MediPi Image File](https://www.dropbox.com/s/y7q508cpatgu5h7/reduced_Medipi_v1.0.8.img.zip?dl=0)

[Raspberry Pi Guide to writing an image to microSD](https://www.raspberrypi.org/documentation/installation/installing-images/)

This has been tested using the Raspberry Pi 3 with the Raspberry Pi official 7" touchsceen.
The image once written to the microSD card will boot to the Raspbian desktop and execute MediPi Patient in full screen. To get back to the desktop just close MediPi in "Settings". The authentication PIN is 2222
