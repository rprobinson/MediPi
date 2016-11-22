# MediPi Clinician Application


##Software
This is an implementation of a Clinician application (It is intended to be used in with the MediPi Patient and Medipi Concentrator implementation which is published alongside this GitHub repository). It has been developed to be flexible and extensible.

This project started as a demonstration of a general telehealth system but with clinical involvement from a Hertfordshire Community NHS Trust, it has been developed into a Heart Failure implementation.
The project is written using Java, Spring Boot, Hibernate, JQuery, JSP and Postgres Database. The application is designed on the SOA principles. The medipi-clinician-service exposes the APIs to get/post the data to/from the database and medipi-clinician-web-application provides the UI for clinicians to monitor the patients which in-turn access the APIs exposed by medipi-clinician-service.

It is intended to be used in with the MediPi Patient and Medipi Concentrator implementation which is published alongside this GitHub repository.

Functionality:

* Medipi clinician service:
	* APIs related to patients: 
        * Get all patients registered in the system
        * Get patient details with `patientUUID`
        * Get recorded patient measurements with `patientUUID` and `attributeName`
    * APIs related to Patient Attribute Thresholds: For each patient there will be different threshold for each measurement attribute. These threshold values will determine whether measurements submitted by patients are within the permissible range.
        * Get the attribute threshold for `patientUUID` and `attributeName`
        * Add the new attribute threshold for `patientUUID` and `attributeName`

* Medipi clinician web:
	* Patients Monitoring: A graphical interface to monitor all the registerd patients in the tile format. Red, Amber, Green and Readings unavailable tiles to indicate patients health according to last measurements submitted.
	* Patient History: The screen which presents all the measurements submitted by patients in the graphs format with threshold indicators.
    * Display & Modify Thresholds: On the patient details page, recent measurement and attribute threshold is also displayed. Clinician can modify these thresholds which will be effective from the date of modification.

## Postgres DB
The Clinician service uses an instance of Postgres (v9.4.8). The data in this database is a subset of the data from Concentrator database. A scheduler will run in the background which will synchronize the data between these two database instances. 

For testing, you can create a new postgres database with name 'Clinical' and execute the script `{medipi-repo-directory}/MediPi/Application/Database-Scripts/MediPi-Clinical_DDL_DML.sql`

![Database Structure Document](https://github.com/rprobinson/MediPi/files/487667/Database_view.pdf)


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

1. Build the required service jar and application war using maven build. Navigate to `{medipi-repo-directory}/MediPi/Application` in the cloned repository and execute `mvn clean install`

2. Copy the deployables
    * `{medipi-repo-directory}/MediPi/Application/Services/medipi-clinician-service/target/medipiClinicianService.jar` file to `C:\MediPiClinical` (Windows) or `/home/{user}/MediPiClinical/` (Linux) directory
    * `{medipi-repo-directory}/MediPi/Application/Web-Aplication/medipi-clinician-web-application/target/medipiClinicianWebApp.war` file to `C:\MediPiClinical` (Windows) or `/home/{user}/MediPiClinical/` (Linux) directory

3. Copy the `{medipi-repo-directory}/MediPi/Application/configurations/services.properties` file to `C:\MediPiClinical` (Windows) or `/home/{user}/MediPiClinical/` (Linux) directory and update the database related properties

    <pre><code>#-----------Database configurations-----------
    medipi.clinician.service.jdbc.driver=org.postgresql.Driver
    medipi.clinician.service.jdbc.url=jdbc:postgresql://localhost:5432/Clinical
    medipi.clinician.service.jdbc.username=postgres
    medipi.clinician.service.jdbc.password=Password1</code></pre>

4. Install postgres database - the version tested is v9.4.8

5. Create a database 'Clinical'

6. Execute sql script `{medipi-repo-directory}/MediPi/Application/Database-Scripts/MediPi-Clinical_DDL_DML.sql` on the Clinical database

7. Execute MediPi Clinician service using:

        For Windows: java -jar /home/{user}/MediPiClinical/medipiClinicianService.jar --spring.config.location=/home/{user}/MediPiClinical/services.properties
        For Linux: java -jar C:/MediPiClinical/medipiClinicianService.jar --spring.config.location=file:/C:/MediPiClinical/services.properties

8. Execute MediPi Clinician web application using:

        For Windows: java -jar /home/{user}/MediPiClinical/medipiClinicianWebApp.war --spring.config.location=/home/{user}/MediPiClinical/services.properties
        For Linux: java -jar C:/MediPiClinical/medipiClinicianWebApp.war --spring.config.location=file:/C:/MediPiClinical/services.properties
