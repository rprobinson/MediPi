# MediPi Open Source Telehealth System

MediPi is a clinically lead, community based, open platform development aimed at addressing those factors which have caused telehealth to be economically unattractive.

Distilled to its core, MediPi allows secure transmission of data from one or many satellite systems to a remote sever and can expose it through secure APIs to clinical systems. This is a model which lends itself to many clinical scenarios

Both the MediPi Patient and Concentrator software repositories have extensive READMEs with quick start guides

### MediPi Patient Software
The patient software is designed to be used typically in a domestic setting by a patient to measure and transmit data to a remote clinician and to receive alerts/messages in return. 
MediPi Open Source software is written using Java and so is platform independent allowing it to run on Linux, Windows, Android or IOS systems. It a flexible solution enabling interaction with USB, Bluetooth, user input or internet-enabled data streams so it is not tied to any particular models or types of device. Its extensibility means that new devices can be plugged in and configured to work. 
The MediPi Patient software has Element and device classes which allow the measurement of Blood Pressure, Oxygen saturation and weight measurements and take a daily subjective patient health yes/no questionnaire. The questionnaire was created by Heart Failure nurses based on an existing paper flow chart that provides patients with standard instructions as a result of their responses. In this way, clinicians can monitor how patients are feeling subjectively. The MediPi patient unit schedules the taking of these measurements and securely transmits the data to the MediPi Host Concentrator. It can also receive text based alert messages directly from a clinical application through its APIs as a result of transmitted readings. The software or configuration can be remotely updated per device. 

[Link to the MediPi Patient README](https://github.com/rprobinson/MediPi/blob/master/MediPiPatient/README.md)

### MediPi Host Concentrator
The host concentrator stores all the patient data per Trust and exposes APIs for clinical systems to request patient data from. These systems can then send alerts to the patient through the concentrator based on clinically defined thresholds (per patient, per measurement device). 

[Link to the MediPi Concentrator README](https://github.com/rprobinson/MediPi/blob/master/MediPiConcentrator/README.md)

### Security
MediPi Patient and Concentrator exchange data using secure 2-way SSL/ Mutually Authenticated messaging and the concentrator exposes APIs to Clinical systems using the same. Additionally, data is exchanged using data objects which have been encrypted and signed using JSON Web encryption objects and JSON Web Signing objects.

![Element image](https://cloud.githubusercontent.com/assets/13271321/18472733/fe3ba2e8-79b0-11e6-8097-8ebc0ed732dc.jpg)
