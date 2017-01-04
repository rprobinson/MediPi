#!/bin/bash
#
#;
splashLocation=${config-directory-location}/nhs_splash.jpg
medipiJar=/home/riro/git/MediPi/MediPiPatient/MediPi/target/MediPi.jar
propertiesFileLocation=${config-directory-location}/MediPi.properties
adminPropertiesFileLocation=${config-directory-location}/MediPi.admin.properties


./timesync/timesync.sh &
java -Djava.library.path=/usr/lib/jni -splash:$splashLocation -jar $medipiJar --propertiesFile=$propertiesFileLocation
	if [ ${PIPESTATUS[0]} -eq 55 ]; then
		sudo java -Djava.library.path=/usr/lib/jni -splash:$splashLocation -jar $medipiJar --propertiesFile=$adminPropertiesFileLocation
	fi
