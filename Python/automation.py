#!/usr/bin/python

import sys
import autopy
from maps import *
import socket
import time
import random

from network import HOST, PORT

SLEEPTIME = 0.07
CHUNK = 1024

sock = socket.socket (socket.AF_INET, socket.SOCK_DGRAM)
listen_addr = (HOST, PORT)
sock.setsockopt(socket.SOL_SOCKET, socket.SO_REUSEADDR, 1)
sock.bind (listen_addr)

def tap (key):
    autopy.key.toggle (key, True)
    time.sleep (SLEEPTIME)
    autopy.key.toggle (key, False)

def setdir (player, curdir):
    global lookup
    if (lookup[player]["DIR"] != lookup[player][curdir]):
        lookup[player]["DIR"] = lookup[player][curdir]
        print player, "is now facing", curdir
try:
	player = sys.argv[1]
except:
	player = "player2"

while True:
    data, addr = sock.recvfrom (CHUNK)
    command = data.strip().split("_")
    if (len (command) == 3):
        setdir (command[1], command[2])
        continue
    elif (command == "" or len (command) != 2):
        continue
    try:
        username = command[0]
        gesture = command[1]
        player = playermap[username]["player"]
        character = playermap[username]["character"]
        move = movemap[character][gesture]
        keylist = keymap[character][move]
        print username, gesture, player, character, move, keylist
        for keys in keylist:
            if (len (keys) == 1):
                tap (lookup[player][keys[0]])
            else:
                for key in keyarr:
                    autopy.key.toggle (key, True)
                time.sleep (SLEEPTIME)
                for key in keyarr:
                
                    autopy.key.toggle (key, False)

    except KeyError as e:
        print "Command not found:", command, e


        
        
        