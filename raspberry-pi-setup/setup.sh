#!/bin/bash
export piSetupDirectory=$( cd $(dirname $0)/.. ; pwd -P )
#================================================================================
#			Configuring Raspberry Pi
#================================================================================
echo ''; echo -e "\033[32mconfiguring Raspberry Pi . . .\033[37m"
sudo cp ${piSetupDirectory}/raspberry-pi-setup/raspberry-pi-config/config.txt /boot/
sudo cp ${piSetupDirectory}/raspberry-pi-setup/raspberry-pi-config/lightdm.conf /etc/lightdm/


#================================================================================
#			Installing required softwares on Raspberry Pi
#================================================================================
echo ''; echo -e "\033[32minstalling required softwares on Raspberry Pi . . .\033[37m"
#sudo apt-mark hold raspberrypi-bootloader
#sudo apt-get update
#sudo apt-get upgrade

sudo chmod -R 777 ${piSetupDirectory}/raspberry-pi-setup/installables

if [ -f ${piSetupDirectory}/raspberry-pi-setup/installables/librxtx-java_2.2pre2-13_armhf.deb ]; then
	sudo dpkg -i ${piSetupDirectory}/raspberry-pi-setup/installables/librxtx-java_2.2pre2-13_armhf.deb
	sudo rm ${piSetupDirectory}/raspberry-pi-setup/installables/librxtx-java_2.2pre2-13_armhf.deb
else
	sudo apt-get install librxtx-java
fi

# Check if the openjfx-overlay file exists otherwise download it from the link

if [ -f ${piSetupDirectory}/raspberry-pi-setup/installables/openjfx-8u60-sdk-overlay-linux-armv6hf.zip ]; then
	echo -e "\033[32mOpenJFX installabe exists in the directory\033[37m"
else
	wget -O ${piSetupDirectory}/raspberry-pi-setup/installables/openjfx-8u60-sdk-overlay-linux-armv6hf.zip "http://chriswhocodes.com/downloads/openjfx-8u60-sdk-overlay-linux-armv6hf.zip"
fi

#================================================================================
#			Overlaying javafx on java installation
#================================================================================
echo ''; echo -e "\033[32moverlaying javafx on java installation . . .\033[37m"

java_installation=$(readlink -f $(which java))
java_installation_directory="${java_installation/\/jre\/bin\/java/''}"
echo -e "\033[32mJava installation directory is: " $java_installation_directory "\033[37m"

sudo chmod -R 777 ${java_installation_directory}
sudo unzip -o ${piSetupDirectory}/raspberry-pi-setup/installables/openjfx-8u60-sdk-overlay-linux-armv6hf.zip -d ${java_installation_directory}/
sudo rm ${piSetupDirectory}/raspberry-pi-setup/installables/openjfx-8u60-sdk-overlay-linux-armv6hf.zip
#================================================================================
#			Changing the config files to run MediPi
#================================================================================
echo ''; echo -e "\033[32mchanging the config files to run MediPi . . .\033[37m"
sudo ${piSetupDirectory}/config/set-all-configurations.sh ${piSetupDirectory}/config
sudo sed -i '/run-medi-pi.sh/d' /home/pi/.config/lxsession/LXDE-pi/autostart
echo '@lxterminal -e sudo '${piSetupDirectory}'/run-medi-pi.sh' >> /home/pi/.config/lxsession/LXDE-pi/autostart

#================================================================================
#			Configuring Raspberry Pi to communicate with USB devices
#================================================================================
echo ''; echo -e "\033[32mconfiguring Raspberry Pi to communicate with USB devices . . .\033[37m"

sudo adduser pi plugdev
sudo udevadm control --reload
sudo udevadm trigger
sudo cp ${piSetupDirectory}/raspberry-pi-setup/raspberry-pi-config/50-MediPi-v3.rules /etc/udev/rules.d/

#================================================================================
#			Expanding filesystem and restarting Raspberry Pi
#================================================================================
echo ''; echo -e "\033[32mexpanding filesystem and restarting Raspberry Pi . . .\033[37m"
sudo raspi-config --expand-rootfs
sudo reboot