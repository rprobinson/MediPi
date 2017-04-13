# -*- coding: utf-8

#######################################################################
# Copyright 2010 Signove Corporation - All rights reserved.
# Contact: Signove Corporation (contact@signove.com)
#
# This library is free software; you can redistribute it and/or modify
# it under the terms of version 2.1 of the GNU Lesser General Public
# License as published by the Free Software Foundation.
#
# This library is distributed in the hope that it will be useful,
# but WITHOUT ANY WARRANTY; without even the implied warranty of
# MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
# GNU Lesser General Public License for more details.
#
# You should have received a copy of the GNU Lesser General Public
# License along with this library; if not, write to the
# Free Software Foundation, Inc., 59 Temple Place, Suite 330,
# Boston, MA 02111-1307  USA
#
# If you have questions regarding the use of this file, please contact
# Signove at contact@signove.com.
#######################################################################

#From IEEE Standard 11073-10404 page 62

import sys
from hdp_utils import *


assoc_resp_msg = (
	0xe3, 0x00, #APDU CHOICE Type(AareApdu)
	0x00, 0x2c, #CHOICE.length = 44
	0x00, 0x00, #result=accept
	0x50, 0x79, #data-proto-id = 20601
	0x00, 0x26, #data-proto-info length = 38
	0x80, 0x00, 0x00, 0x00, #protocolVersion
	0x80, 0x00, #encoding rules = MDER
	0x80, 0x00, 0x00, 0x00, #nomenclatureVersion
	0x00, 0x00, 0x00, 0x00, #functionalUnits, normal Association
	0x80, 0x00, 0x00, 0x00, #systemType = sys-type-manager
	0x00, 0x08, #system-id length = 8 and value (manufacturer- and device- specific) 
	0x88, 0x77, 0x66, 0x55, 0x44, 0x33, 0x22, 0x11,
	0x00, 0x00, #Manager's response to config-id is always 0
	0x00, 0x00, #Manager's response to data-req-mode-flags is always 0
	0x00, 0x00, #data-req-init-agent-count and data-req-init-manager-count are always 0
	0x00, 0x00, 0x00, 0x00, #optionList.count = 0 | optionList.length = 0
	)


assoc_rel_resp = (
	0xe5, 0x00, #APDU CHOICE Type(RlreApdu)
	0x00, 0x02, #CHOICE.length = 2
	0x00, 0x00 #reason = normal
	)


def parse_message(msg):
	global assoc_resp_msg
	global assoc_rel_resp

	resp = None
	#print "incoming msg: "+str(msg)+'\n'

	if int(msg[0]) == 0xe2:
		#print 'IEEE association request\n'
		resp = assoc_resp_msg

	elif int(msg[0]) == 0xe7:
		#print 'IEEE agent data\n'
		resp = (
			0xe7, 0x00, #APDU CHOICE Type(PrstApdu)
			0x00, 0x12, #CHOICE.length = 18
			0x00, 0x10, #OCTET STRING.length = 16
			int(msg[6]), int(msg[7]), #invoke-id (mirrored from invocation) 
			0x02, 0x01, #CHOICE(Remote Operation Response | Confirmed Event Report)
			0x00, 0x0a, #CHOICE.length = 10
			0x00, 0x00, #obj-handle = 0 (MDS object)
			0x00, 0x00, 0x00, 0x00, #currentTime = 0
			0x0d, 0x1d, #event-type = MDC_NOTI_SCAN_REPORT_FIXED
			0x00, 0x00, #event-reply-info.length = 0
			)
                cc = str('%x' % (msg[50]))
                year = str('%x' % (msg[51]))
                month = str('%x' % (msg[52]))
                day = str('%x' % (msg[53]))
                hour = str('%x' % (msg[54]))
                minute = str('%x' % (msg[55]))
                seconds = str('%x' % (msg[56]))
                thousanths = str('%x' % (msg[57]))
                
                
		sys.stdout.write('DATA:')
                sys.stdout.write(cc.zfill(2)+year.zfill(2)+'-'+month.zfill(2)+'-'+day.zfill(2)+' '+hour.zfill(2)+':'+minute.zfill(2)+':'+seconds.zfill(2)+'.'+thousanths.zfill(3))
                sys.stdout.write('^')
                sys.stdout.write('%d' % int(msg[45]))
                sys.stdout.write('^')
                sys.stdout.write('%d' % int(msg[47]))
                sys.stdout.write('^')
		sys.stdout.write('%d' % int(msg[63]))
                sys.stdout.write('^')
		sys.stdout.write('%d' % int(msg[49]))
		sys.stdout.write('\n')
                sys.stdout.flush()

	elif int(msg[0]) == 0xe4:
		#print 'Association release request\n'
		resp = assoc_rel_resp

	#print "outgoing message: "+str(resp)+'\n'
	return resp


def parse_message_str(msg):
	return b2s(parse_message(s2b(msg)))
