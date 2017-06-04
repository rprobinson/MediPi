# MediPi Mock Clinical System


## Software
The mock-clinical system has been developed as part of the pilot to allow clinicians access to their patient's data. 

* **MediPiClinical** requests data periodically from the concentrator through the concentrator's API. Any new data is tested against configured thresholds and alerts are returned to the patient based upon this calculation. 
* **medipi-clinician-service & medipi-clinician-web-application** constitute the web based front end which is used by clinicians to log on to and presents them with a single screen digest of their cohort of patients. Each patient is displayed with a status indicator reflecting the data they have submitted. Clinicians can access the patient's record which will show graphical history of each submitted measurement and the current status. This allows them to review and update thresholds for each device. The advantage of implementing a mock clinical system is that we maintian end-to-end control of the software for the pilot, however the ultimate aim is that thrid party systems will use the concentrator's APIs to perform this function. As the MediPi mock-clinical system makes calculations based upon the measurement thresholds, it has been submitted and approved as a medical device with the MHRA.


The project is written using Java Spring Boot, Hibernate, Postgres Database

It is intended to be used with the MediPi Concentrator which is published in this GitHub repoisitory

Functionality:

* Accesses Clinical Application side APIs on MediPi Concentrator
	* Request Patient Data: requests patient data periodically (every 10 seconds by default) by patient groups and date
	* Request Patient Certificate: Requests PEMs for patients in order to encrypt direct messages/alerts for them
	

## Postgres DB
The mock clinical system uses an instance of Postgres (v9.4.8). The data is stored in an extensible manner, meaning that individual datapoints are stored in the recording_device_data table with their timestamp against their attributes in recording_device_attributes table. Many devices will record more than one datatype per reading (finger oximeters typically record heartrate and SpO2 levels) and this would result in 2 records on the recording_device_data table against 2 separate attributes on the recording_device_attribute table. The 2 data records are linkable via their timestamps. The design of the database in this manner means that any measurement device with any number of datatypes which it is capable of recording can be accomodated without any structural DB changes. The mock clinical database data holds data in the same structures as the MediPi concentrator database.

The published SQL dump of the database gives the structure and some data examples. Note this is a pg_dump file and will require to be restored from the command line using pg_restore

[ClinicalSchemaDiagram.pdf](https://github.com/rprobinson/MediPi/files/1050716/ClinicalSchemaDiagram.pdf)

## Certificates and PKI
The certs for the clicical system are published here (and are intended to work out-of-the-box) as java key stores and should allow testing of the clinical system and patient software. **The certs are for testing purposes and not suitable for use in any other circumstance.**

#### Instructions to update configuration files
1. Copy config directory to an external location e.g. C:\MediPiClinical\ (for windows machine) or /home/{user}/MediPiClinical (Linux based)
2. Open command prompt which is capable of executing .sh file. (Git bash if you are on windows. Terminal on linux installation is capable of executing sh files)
3. Go to config directory location on command prompt e.g. C:\MediPiClinical\config or /home/{user}/MediPiClinical/config
4. Execute setup-all-configurations.sh "{config-directory-location}" as './set-all-configurations.sh "C:/config"' or './set-all-configurations.sh "/home/{user}/config"'. This will replace all the relative paths in properties and guides files of the configuration.

The user name and password will need to be udated for the postgres DB.

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

1. Refer the section `Instructions to update configuration files` in this document above to update the configurations files.

2. Build MediPiClinical.jar using maven build. Navigate to MediPi/MediPiClinical in the cloned repository and execute `mvn clean install`

3. Copy the `{medipi-repo-directory}/MediPi/MediPiClinical/MediPiClinical/target/MediPiClinical.jar` file to /home/{user}/MediPiClinical/ directory

4. Upgrade the Java Cryptography Extention. Download from http://www.oracle.com/technetwork/java/javase/downloads/jce8-download-2133166.html and follow the README.txt instructions included in the package. The certs included for demonstration purposes require greater strength binaries in the JRE than are present by default.

5. Install postgres database - the version tested is v9.4.8

6. Create a database 

7. Import pg_dump file

8. Execute MediPiClinical using:
        
        java -jar /home/{user}/MediPi/MediPiClinical.jar /home/{user}/MediPiClinical/config/MediPiClinical.properties --spring.config.location=/home/{user}/MediPiClinical/config/application.properties
