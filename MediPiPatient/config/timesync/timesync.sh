#!/bin/bash

# Find this directory
DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"
file=$DIR/timesync.txt
synchronisedTime=false

# Loop until a sucessful connection is made to the ntp server then quit 

while [ "$synchronisedTime" = false ]
do
	if [ -f $file ] ; then
		rm $file
	fi
	# call ntpdate to synchronise and output to file
	sudo /usr/sbin/ntpdate -u pool.ntp.org | tee $DIR/timesync.txt;

	# on successful synchronisation exit
	if [ ${PIPESTATUS[0]} -eq 0 ]; then
		synchronisedTime=true
		break
	fi
	sleep 5
done



