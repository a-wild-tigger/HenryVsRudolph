#!/usr/bin/python

import pyaudio
import wave
import audioop
import cPickle
import audio
import pdb

CHUNK = 1024
FORMAT = pyaudio.paInt16
CHANNELS = 2
RATE = 16000
RECORD_SECONDS = 5
THRESHOLD = 1000
username = "default"
verbose = True

p = pyaudio.PyAudio()
stream = p.open(format=FORMAT, channels=CHANNELS, rate=RATE, input=True, frames_per_buffer=CHUNK) 

def main (user="default", method="default"):
  global db, username
  try: 
    db = cPickle.load (open ("database", "rb"))
  except:
    db = {}

  if (user == "default"):
    username = raw_input ("Username: ")
    if (not db.has_key (username)):
      db[username] = {"gestures":{}, "trained":False}
  else:
    username = user
    if (not db.has_key (username)):
      db[username] = {"gestures":{}, "trained":False}

  gestlist = ""
  for gesture in db[username]["gestures"].keys():
    gestlist += (gesture + ", ")

  print ("Welcome %s, you have loaded the following gestures: %s" % 
          (username, gestlist.strip(", ")))
  if db[username]["trained"]:
    print "The system has been trained since you last recorded."
  else:
    print "The system has not been trained since you last recorded."
    
  if (method == "default"):
    print "Menu: \t1) Record Gestures"
    print "\t2) Train System"
    print "\t3) Run Classifier"
    print "\t4) Listen to Gestures"
    method = input ("Choose option: ")
    print ""
    
    ret = 1
    if (method == 1):
      printv ("Starting recording system")
      run_record (username)
      printv ("Finished recording system")
    elif (method == 2):
      printv ("Starting training system")
      run_train (username)
      printv ("Finished training system")
    elif (method == 3):
      printv ("Starting classification system")
      run_classify (username)
      printv ("Finished classification system")
    elif (method == 4):
      printv ("Starting playback system")
      run_play (username)
      printv ("Finished playback system")
    else:
      ret = 0 

    f = open ("database", "wb")
    cPickle.dump (db, f)
    f.close ()
    print "------------------------------------------------------------"
  return (ret)


def run_record (username):
  global db

  gesturename = raw_input ("Gesture name: ")
  if (not db[username]["gestures"].has_key (gesturename)):
    db[username]["gestures"][gesturename] = []

  db[username]["trained"] = False
  frames = ""
  try:
    while (True):
      data = stream.read (CHUNK)
      amplitude = audioop.rms (data, 2)
      if (amplitude >= THRESHOLD):
        print ("Amplitude: " + str(amplitude))
        frames += data
      elif (len (frames) > 0):
        db[username]["gestures"][gesturename].append (frames)
        frames = ""
  except KeyboardInterrupt:
    out_stream = p.open (format = FORMAT, channels = CHANNELS, rate = RATE,
                         output = True)
    for gesture in db[username]["gestures"][gesturename]:
      out_stream.write (gesture)
    out_stream.stop_stream ()
    out_stream.close ()
    return

def run_train (username):
  params = audio.GenerateParams(db[username]["gestures"])
  db[username]["params"] = params
  db[username]["trained"] = True
  return

def run_classify (username):
  frames = ""
  classifier = audio.AudioClassifier (db[username]["params"])
  try:
    while (True):
      data = stream.read (CHUNK)
      amplitude = audioop.rms (data, 2)
      if (amplitude >= THRESHOLD):
        print ("Amplitude: " + str(amplitude))
        frames += data
      elif (len (frames) > 0):
        classifier.Classify (frames)
        frames = ""
  except KeyboardInterrupt:
    return

def run_play (username):
  gesturename = raw_input ("Gesture name: ")
  if (not db[username]["gestures"].has_key (gesturename)):
    print "No such gesture found for user: "+username
    return  

  out_stream = p.open (format = FORMAT, channels = CHANNELS, rate = RATE,
                       output = True)

  for gesture in db[username]["gestures"][gesturename]:
    out_stream.write (gesture)
  out_stream.stop_stream ()
  out_stream.close ()
  return

def printv (string):
  if (verbose):
    print string

ret = 1
while (ret != 0):
  ret = main (username)

stream.stop_stream()
stream.close()
p.terminate()
