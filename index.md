# MediPi Open Source Remote Patient Monitoring/Telehealth System

MediPi is a clinically lead, community based, open platform development aimed at addressing those factors which have caused telehealth to be economically unattractive.

Distilled to its core, MediPi allows secure transmission of data from one or many satellite systems to a remote sever and can expose it through secure APIs to clinical systems. This is a model which lends itself to many clinical scenarios
![Element image](https://cloud.githubusercontent.com/assets/13271321/26763900/3333ad6a-4954-11e7-8461-d0ebcfb7d48f.jpg)

Both the MediPi Patient and Concentrator software repositories have extensive READMEs with quick start guides

### MediPi Patient Software
The patient software is designed to be used typically in a domestic setting by a patient to measure and transmit data to a remote clinician and to receive alerts/messages in return. 
MediPi Open Source software is written using Java and so is platform independent allowing it to run on Linux, Windows or IOS systems. It a flexible solution enabling interaction with USB, Bluetooth, user input or internet-enabled data streams so it is not tied to any particular models or types of device. Its extensibility means that new devices can be plugged in and configured to work. 
The MediPi Patient software has Element and device classes which allow the measurement of Blood Pressure, Oxygen saturation and weight measurements and take a daily subjective patient health yes/no questionnaire. The questionnaire was created by Heart Failure nurses based on an existing paper flow chart that provides patients with standard instructions as a result of their responses. In this way, clinicians can monitor how patients are feeling subjectively. The MediPi patient unit schedules the taking of these measurements and securely transmits the data to the MediPi Host Concentrator. It can also receive text based alert messages directly from a clinical application through its APIs as a result of transmitted readings. The software or configuration can be remotely updated per device. 

[Link to the MediPi Patient README](https://github.com/rprobinson/MediPi/blob/master/MediPiPatient/README.md)
![Element image](https://cloud.githubusercontent.com/assets/13271321/21643558/db154e44-d280-11e6-926a-a02b39d35cca.JPG)

### MediPi Host Concentrator
The host concentrator stores all the patient data per Trust and exposes APIs for clinical systems to request patient data from. These systems can then send alerts to the patient through the concentrator based on clinically defined thresholds (per patient, per measurement device). 

MediPi Patient and MediPi Concentrator software has been designed from the ground-up to securely pass raw data from front end interfaces and physiological devices to the MediPi Concentrator - specifically not to 'process' or interpret the data in any way. As a result, and after consultation with MHRA (Medicines & Healthcare products Regulatory Agency), we believe that the system software (MediPi Patient and MediPi Concentrator) is classed as a ‘Health IT system’ falling under the clinical risk management standard SCCI 0129,  "not a medical device". However any parties using or modifying the code would need to re-establish this with MHRA. 

[Link to the MediPi Concentrator README](https://github.com/rprobinson/MediPi/blob/master/MediPiConcentrator/README.md)

### MediPi Mock-Clinical System
The mock-clinical system has been developed as part of the pilot to allow clinicians access to their patient's data. It requests data periodically from the concentrator through the concentrator's API. Any new data is tested against configured thresholds and alerts are returned to the patient based upon this calculation. The web based front end is used by clinicians to log on to and presents them with a single screen digest of their cohort of patients. Each patient is displayed with a status indicator reflecting the data they have submitted. Clinicians can access the patient's record which will show graphical history of each submitted measurement and the current status. This allows them to review and update thresholds for each device. The advantage of implementing a mock clinical system is that we maintian end-to-end control of the software for the pilot, however the ultimate aim is that thrid party systems will use the concentrator's APIs to perform this function. As the MediPi mock-clinical system makes caluculations based upon the measurement thresholds, it has been submitted and approved as a medical device with the MHRA.

[Link to the MediPi Clinical README](https://github.com/rprobinson/MediPi/blob/master/Clinician/README.md)
![Element image](https://cloud.githubusercontent.com/assets/13271321/26763948/18472116-4955-11e7-8cec-1907cf66233e.png)

### MediPi Transport Tools/Security
MediPi Patient and Concentrator exchange data using secure 2-way SSL/ Mutually Authenticated messaging and the concentrator exposes APIs to Clinical systems using the same. Additionally, data is exchanged using data objects which have been encrypted and signed using JSON Web encryption objects and JSON Web Signing objects. We have published a common library opf tools for this purpose.


