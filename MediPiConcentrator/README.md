# MediPi Concentrator Telehealth System


##Software
This is an implementation of a Telehealth host/concentrator system (It is intended to be used in with the MediPi Patient implementation which is published alongside this GitHub account). It has been developed to be flexible and extensible.

This project started as a demonstration of a general telehealth system but with clinical involvement from a Hertfordshire Community NHS Trust, it has been developed into a Heart Failure implementation.
The project is written using Java Spring Boot, Hibernate, Postgres Database

It is intended to be used in with the MediPi Patient implementation which is published alongside this GitHub account

Functionality:

* Exposes MediPi Patient side APIs:
	* Data upload from MediPi Patient Devices: This accepts measurement data from the MediPi Patient device
	* Data Downloadable List request: This returns a list of patient and hardware updates
	* Patient Downloadable entities: This returns signed patient messges/Alerts from the clinical system available to the MediPi patient software
	* Hardware Downloadable entites (for future dev): This allows MediPi Patient Software to download scripts for execution on the MediPi Patient Device to upload softweare entities. The scripts are either for the specific MediPi Patient device or for all Devices. Needs to be renamed to "software downloads"
* Exposes Clinical Application side APIs
	* Request Patient Data: Allows Clinical systems to request patient data by patient groups and date
	* Request Patient Certificate: Allows Clinical systems to request PEMs for patients in order to encrypt messages for them
	
Instructions to update configuration files

Copy config directory to an external location e.g. C:\MediPiConcentrator\ (for windows machine) or /home/{user}/MediPiConcentrator (Linux based)
Open command prompt which is capable of executing .sh file. (Git bash if you are on windows. Terminal on linux installation is capable of executing sh files)
Go to config directory location on command prompt e.g. C:\MediPiConcentrator\config or /home/{user}/MediPiConcentrator/config
Execute setup-all-configurations.sh "{config-directory-location}" as './set-all-configurations.sh "C:/config"' or './set-all-configurations.sh "/home/{user}/config"'. This will replace all the relative paths in properties and guides files of the configuration.
The user name and password will need to be udated for the postgres DB.

## Postgres DB
The Concentrator uses an instance of Postgres (v9.4.8). The data is stored in an extensible manner, meaning that individual datapoints are stored in the recording_device_data table with their timestamp against their attributes in recording_device_attributes table. Many devices will record more than one datatype per reading (finger oximeters typically record heartrate and SpO2 levels) and this would result in 2 records on the recording_device_data table against 2 separate attributes on the recording_device_attribute table. The 2 data records are linkable via their timestamps. The design of the database in this manner means that any measurement device with any number of datatypes which it is capable of recording can be accomodated without any structural DB changes.

The published SQL dump of the database gives the structure and some data examples. Note this is a pg_dump file and will require to be restored from the command line using pg_dump

![Database Structure Document](https://github.com/rprobinson/MediPi/files/487667/Database_view.pdf)


## Certificates and PKI
The certs for the concentrator are published here (and are intended to work out-of-the-box) as java key stores and should allow testing of the concentrator and patient software. **The certs are for testing purposes and not suitable for use in any other circumstance.**

####Instructions to update configuration files
1. Copy config directory to an external location e.g. C:\MediPiConcentrator\ (for windows machine) or /home/{user}/MediPiConcentrator (Linux based)
2. Open command prompt which is capable of executing .sh file. (Git bash if you are on windows. Terminal on linux installation is capable of executing sh files)
3. Go to config directory location on command prompt e.g. C:\MediPiConcentrator\config or /home/{user}/MediPiConcentrator/config
4. Execute setup-all-configurations.sh "{config-directory-location}" as './set-all-configurations.sh "C:/config"' or './set-all-configurations.sh "/home/{user}/config"'. This will replace all the relative paths in properties and guides files of the configuration.

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

1. Refer the section `Instructions to update configuration files` in this document above to update the configurations files.

2. Build MediPiConcentrator.jar using maven build. Navigate to MediPi/MediPiConcentrator in the cloned repository and execute `mvn clean install`

3. Copy the `{medipi-repo-directory}/MediPi/MediPiConcentrator/MediPiConcentrator/target/MediPiConcentrator.jar` file to /home/{user}/MediPiConcentrator/ directory

4. Upgrade the Java Cryptography Extention. Download from http://www.oracle.com/technetwork/java/javase/downloads/jce8-download-2133166.html and follow the README.txt instructions included in the package. The certs included for demonstration purposes require greater strength binaries in the JRE than are present by default.

5. Install postgres database - the version tested is v9.4.8

6. Create a database 

7. Import pg_dump file

8. Execute MediPiConcentrator using:
        
        java -jar /home/{user}/MediPi/MediPiConcentrator.jar /home/{user}/MediPiConcentrator/config/MediPiConcentrator.properties --spring.config.location=/home/{user}/MediPiConcentrator/config/application.properties
