#/bin/bash
configDirectory=$PWD
if [ -z "$1" ]
  then
	echo "No config directory supplied. Assuming '" $configDirectory "' as config directory."
  else
    configDirectory=$1
fi

sed -i -e 's|${config-directory-location}|'${configDirectory}'|g' ${configDirectory}/MediPi.properties
sed -i -e 's|${config-directory-location}|'${configDirectory}'|g' ${configDirectory}/MediPi.admin.properties
sed -i -e 's|${config-directory-location}|'${configDirectory}'|g' ${configDirectory}/guides/*.guide
sed -i -e 's|${config-directory-location}|'${configDirectory}'|g' ${configDirectory}/vpn/OpenVPN.sh

