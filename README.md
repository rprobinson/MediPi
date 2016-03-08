# MediPi Telehealth System

##Software
This is a simple implementation of a Telehealth patient/client system. It has been developed to be flexible and extensible.

This project started as a demonstration of a general telehealth system but with clinical involvement from a Trust in the South of England, it has been developed into a Heart Failure implementation.
The project is written in Java using JavaFX but includes two python classes. The python scripts are used for convenience to interface with their respective USB medical devices, but the functionality could be incorporated into Java using an open source Java USB library.

Functionality:

* Records data from:
  * Finger Oximeter
  * Diagnostic Scales
  * Blood Pressure Meter (upper arm cuff)
  * Patient Yes/No Daily Questionnaire
* Flexible Scheduler
*  Direct text based message from clinician
* Secure Flexible Transmitter
* Remotely configurable

This is a list of functionality to date which has been created under guidance from clinicians. It is not an exhaustive list and is intended to be expanded.

See the MediPi Summary Document: https://github.com/rprobinson/MediPi/blob/master/MediPi_handout-v1.2.pdf

##Architecture
###MediPi Class
The application is managed by the main MediPi class. This orchestrates the initiation of certain resources, initialises the UI framework/primary stage, initiates each of the elements of MediPi and can return information about its version if asked.

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
2. Downloaded - All data on the medical device is downloaded and passed to the Generic Device Class. The 2 instances of this (Beurer BF480 and BM55) call python scripts to pull the data down - however this functionality could be incorporated into Java
This class does not provide any UI nodes - just data

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

	The transmitter element shows a list of all the elements loaded into MediPi with checkboxes which are enabled if data is present/ready to be transmitted. Transmitter will take all available and selected data and will use the Distribution Envelope format to contain the data. The available data from each of the elements is taken, compressed if applicable and can be individually encrypted. The data from each of the Devices has it's own metadata tags. Each payload is added to a Distribution Envelope structure with their own profile Id. The Distribution Envelope is then transmitted using the transport method defined

	While SpineTools is the current transport for MediPi this is historic and due to be changed as the particular spine message choice is asynchronous and the async responses are currently ignored by MediPi. The current configuration however does “work” in so far as it transmits the payload correctly but being necessarily a synchronous entity (for sercurity purposes), MediPi does not open any ports for async response. The Transmitter element will be split into a transmitting UI and a transport specific subclass and will transmit payloads within a DistributionEnvelope using a restful interface.

* ####Practitioner classes
Included with the MediPi classes are several “practitioner” classes within the package:org.medipi.practitionerdevices. It is possible, using the medipi.properties file to create a “practitioner” version of the application. This version will display incoming information from flat files and also has the functionality to send simple text based messages from the practitioner version to the main patient version of MediPi. These demonstration classes have been developed solely for the purpose of displaying data after it has been transmitted in order to show equivalence at either end. They have been used when demonstrating the code to peers. It is **not** any attempt at creating a receiving system. 


###CSS Implementation
MediPi uses JavaFX which can be controlled using CSS. The implementation of this is sub-optimal and has been done on a node by node basis. Whilst individual elements can be controlled, a refactoring exercise is required to properly implement it and take full advantage of the technology.

###MediPi.properties and configuration files
MediPi.properties file defines the properties for the configuration of MediPi. The Spine tools configuration has been left in the MediPi properties file but the source and the binaries will have to be build by the contributor - see Github link below in the software dependencies section. 

Medipi has been designed to be flexible and extensible and uses dynamically initialised Element classes from the properties file. Elements are defined in the properties file and only those Elements which appear in the medipi.elementclasstokens list will be initialised or displayed.

The patient details are defined in the properties file.

###Software Dependencies:
MediPi depends on the following libraries:

* **SpineTools:** https://github.com/DamianJMurphy/SpineTools-Java
* **DistributionEnvelopeTools:** https://github.com/DamianJMurphy/DistributionEnvelopeTools-Java
* **PyUSB:** https://github.com/walac/pyusb
* **Java RXTX libraries for serial devices:** https://github.com/rxtx/rxtx

### Configuration files:
 The configuration files can be downloaded as a zip from here:https://github.com/rprobinson/MediPi/files/162967/reference_Config_v1.0.0-PILOT-20160308.zip

###USB Medical Device Interfaces:
3 particular devices have been used but others can be developed.

* Contec CMS50D+ Finger Pulse Oximeter - The interface is a Java port of the streamed serial interface developed here: https://github.com/atbrask/CMS50Dplus. The device can store up to 24 hours of data but this function has not implemented.
* Beurer BF480 Diagnostic Scales - The Python script is based upon https://usb2me.wordpress.com/2013/02/03/beurer-bg64/ but the BF480 is a cheaper scale and has a different data structure
* Beurer BG55 Upper Arm Blood Pressure Monitor: This python script was reverse engineered based upon the experience gained with the previous two devices


##Hardware:
As MediPi is written in Java (other than the 2 Python scripts outlined above), it can be run on any system which has an appropriate JRE and thus is cross platform. This Heart Failure implementation of MediPi has been executed using:

* Raspberry Pi 2 Model B or Raspberry Pi 3: https://www.raspberrypi.org/products/raspberry-pi-2-model-b/
* Raspberry Pi Touch Display: https://www.raspberrypi.org/products/raspberry-pi-touch-display/
* Contec CMS50D+ Finger Pulse Oximeter: http://www.contecmed.com/index.php?page=shop.product_details&flypage=flypage.tpl&product_id=126&category_id=10&option=com_virtuemart&Itemid=595
* Beurer BF480 Diagnostic Scales: https://www.beurer.com/web/uk/products/Beurer-Connect/HealthManager-Products/BF-480-USB
* Beurer BG55 Upper Arm Blood Pressure Monitor: https://www.beurer.com/web/en/products/bloodpressure/upper_arm/BM-55

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

6. Install PyUSB: https://github.com/walac/pyusb

7. Install Java RXTX libraries for serial devices

    ```
    sudo apt-get install librxtx-java

    ```
8. Configure the pi so that USB ports can be used without needing su 

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
9. Create a directory named "MediPi" in an appropriate location and unzip the reference configuration files to it.

10. Update the following files with location details - globally replace `"__ROOT__"` (double underscore) with the location of the MediPi directory created in the previous step:
	* /MediPi.properties
	* /guides/beurerbf480.guide
	* /guides/beurerbm55.guide
    * /guides/contecCMS50DPlus.guide

11. Build MediPi.jar and its dependencies from the source and paste it and the lib directory into MediPi directory

12. Execute MediPi using:

        java -Djava.library.path=/usr/lib/jni -jar MediPi/MediPi.jar --propertiesFile=/home/riro/MediPi/MediPi.properties
    
