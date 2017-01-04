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

def s2b(msg):
	if msg is None:
		return None
	return [ord(x) for x in msg]

def b2s(msg):
	if msg is None:
		return None
	return "".join([chr(int(x)) for x in msg])

def test():
	s = chr(0xe2) + "ABCDE\0\1\2"
	b = [0xe2, 65, 66, 67, 68, 69, 0, 1, 2]
	assert(b == s2b(s))
	assert(s == b2s(b))
	assert(b == s2b(b2s(b)))
	assert(s == b2s(s2b(s)))
	assert("" == b2s(None))
	assert([] == s2b(None))
	print "Test ok"

if __name__ == '__main__':
	test()
