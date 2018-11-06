#!/bin/bash
export piSetupDirectory=$( cd $(dirname $0) ; pwd -P )
staticIPAddress="";

if [ -z "$1" ]
  then
	echo "No static IP address is supplied. Program exiting . . ."
	exit
  else
    staticIPAddress=$1
fi

#================================================================================
#			Configuring Mastek WiFi
#================================================================================
echo ''; echo -e "\033[32mconfiguring Mastek WiFi . . .\033[37m"

echo ''; echo -e "\033[32massigning static IP address:" $staticIPAddress " to Raspberry Pi . . .\033[37m"
sed -i -- 's/{static-ip-address}/'${staticIPAddress}'/g' ${piSetupDirectory}/config/interfaces

sudo cp ${piSetupDirectory}/config/interfaces /etc/network/
sudo cp ${piSetupDirectory}/config/wpa_supplicant.conf /etc/wpa_supplicant/

sudo ifdown wlan0
sleep 5
sudo ifup wlan0