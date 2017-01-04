#/bin/bash
configDirectory=$PWD
if [ -z "$1" ]
  then
	echo "No config directory supplied. Assuming '" $configDirectory "' as config directory."
  else
    configDirectory=$1
fi

sed -i -e 's|${config-directory-location}|'${configDirectory}'|g' ${configDirectory}/application.properties
sed -i -e 's|${config-directory-location}|'${configDirectory}'|g' ${configDirectory}/MediPiClinical.properties

