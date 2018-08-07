# MediPi Software Testing

## Functional Testing
Functional testing of the end to end functionality of the MediPi remote patient monitoring system was carried out by the NHS Digital Compliance testing team. This included, but was not limited to:
* MediPi Patient Device
   *	Login
   *	Taking measurements from all physiological devices
   *	Inputting a range of data into the forms
   *	Use of Scheduler
   *	Clinician’s messages to patients
   *	Transmission of data to Clinical System
   *	Time Synchronisation and connectivity
   *	Exploratory testing of the MediPi Patient Device     
* Clinical Web Page
   *	Access – logon
   *	Caseload View Screen per Clinician
   *	Patient detailed view screen including threshold management

#### Test Evidence
[MediPi Test Report Executive](https://github.com/rprobinson/MediPi/tree/master/SoftwareTesting/FunctionalTesting/MediPiTestReportv0.5.doc)  
[MediPi Full Test Schedule and Evidence](https://github.com/rprobinson/MediPi/tree/master/SoftwareTesting/FunctionalTesting/MediPi-NHSDigitalFunctionalTestScheduleandEvidence.docx)

## Non-Functional Testing
Some basic non-functional testing was carried out using Apache JMeter 3.1.
The tests were carried out by injecting 50 concurrent threads to both:
* MediPi Concentrator - Patient Upload interface: [JMeter script](https://github.com/rprobinson/MediPi/tree/master/SoftwareTesting/NonFunctionalTestingScripts/MediPi_Concentrator_Patient_Upload.jmx)
* MediPi Clinical Server - Web Front End: [JMeter script](https://github.com/rprobinson/MediPi/tree/master/SoftwareTesting/NonFunctionalTestingScripts/MediPi_Front_End.jmx)

#### Test Evidence
 [MediPi Concentrator test evidence](https://github.com/rprobinson/MediPi/tree/master/SoftwareTesting/NonFunctionalTestingScripts/MediPiConcentratorPatientLoadTest.xml)  
[MediPi Clinical Web Server test evidence](https://github.com/rprobinson/MediPi/tree/master/SoftwareTesting/NonFunctionalTestingScripts/MediPiFrontEndLoadTest.xml)
