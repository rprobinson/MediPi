#!/bin/bash
#
# Get the process id of any openvpn client
#
echo vpn_user=$1
pid=`ps aux | grep openvpn | grep "$vpn_user" | cut -c10-14`
if [ -n "$pid" ]; then
	#
	# If there is one, SIGTERM it to get it to stop
	#
	kill -TERM $pid;
	#
	# Then hang around until it has shut itself down
	#
#	while [ -n "$pid" ]; do
#		pid=`ps aux | grep openvpn | grep "$vpn_user" | cut -c10-14`
#	done
	#
	# Try to do without this, but put it in just in case we absolutely need
	# to make sure that the openvpn process is stopped before we let this
	# process exit
	sleep 1;
fi
