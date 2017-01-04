#!/usr/bin/env python

import sys
import os
import glib
from dummy_ieee10404 import parse_message_str
import dbus
import socket
import dbus.service
import gobject
from dbus.mainloop.glib import DBusGMainLoop

DBusGMainLoop(set_as_default=True)
loop = gobject.MainLoop()
bus = dbus.SystemBus()


watch_bitmap = glib.IO_IN | glib.IO_ERR | glib.IO_HUP | glib.IO_NVAL

def data_received(sk, evt):
	data = None
	if evt & glib.IO_IN:
		try:
			data = sk.recv(1024)
		except IOError:
			data = ""
		if data:
			#print "Data received"
			response = parse_message_str(data)
			if response:
				sk.send(response)
				#print "Response sent"

	more = (evt == glib.IO_IN and data)

	if not more:
		#print "EOF"
		try:
			sk.shutdown(2)
		except IOError:
			pass
		#print "Closing"
		sk.close()
                loop.quit()

	return more


class SignalHandler(object):
	def __init__(self):
		bus.add_signal_receiver(self.ChannelConnected,
			signal_name="ChannelConnected",
			bus_name="org.bluez",
			path_keyword="device",
			interface_keyword="interface",
			dbus_interface="org.bluez.HealthDevice1")

		bus.add_signal_receiver(self.ChannelDeleted,
			signal_name="ChannelDeleted",
			bus_name="org.bluez",
			path_keyword="device",
			interface_keyword="interface",
			dbus_interface="org.bluez.HealthDevice1")

	def ChannelConnected(self, channel, interface, device):
		#print "Device %s channel %s up" % (device, channel)
		channel = bus.get_object("org.bluez", channel)
		channel = dbus.Interface(channel, "org.bluez.HealthChannel1")
		fd = channel.Acquire()
		#print "Got raw rd %s" % fd
		# take fd ownership
		fd = fd.take()
		#print "FD number is %d" % fd
		# encapsulate numericfd in Python socket object
		sk = socket.fromfd(fd, socket.AF_UNIX, socket.SOCK_STREAM)
		# fromfd() does dup() so we need to close the original
		os.close(fd)
		#print "FD acquired"
		glib.io_add_watch(sk, watch_bitmap, data_received)

	def ChannelDeleted(self, channel, interface, device):
                return
                    #print "Device %s channel %s deleted" % (device, channel)


signal_handler = SignalHandler()

config = {"Role": "Sink", "DataType": dbus.types.UInt16(0x1004),
		"Description": "Oximeter sink"}

manager = dbus.Interface(bus.get_object("org.bluez", "/org/bluez"),
					"org.bluez.HealthManager1")
app = manager.CreateApplication(config)

print >> sys.stdout, "START"
sys.stdout.flush()

try:
	loop = glib.MainLoop()
	loop.run()
except KeyboardInterrupt:
	pass
finally:
	manager.DestroyApplication(app)

print >> sys.stdout, "END"
sys.stdout.flush()
