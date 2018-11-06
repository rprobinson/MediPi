#!/bin/bash
export piSetupDirectory=$( cd $(dirname $0) ; pwd -P )

#================================================================================
#			Running MediPi application on Raspberry Pi
#================================================================================
echo ''; echo -e "\033[32mrunning MediPi application on Raspberry Pi . . .\033[37m"
sudo java -Djava.library.path=/usr/lib/jni -splash:${piSetupDirectory}/nhs_splash.jpg -jar ${piSetupDirectory}/MediPi.jar --propertiesFile=${piSetupDirectory}/config/MediPi.properties