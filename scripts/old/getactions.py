#!/usr/bin/python

import string
import sys

reply=sys.stdin.readlines()
oneLine=''.join(reply)
actionList=oneLine.split("action=")
for action in actionList:
#	print "at:",action
	if action.startswith("\"/icfp10/instance/"):
		print action.split('/')[3]

