#!/usr/bin/python

import pyaudio
import wave
import audioop
import cPickle
import audio
import pdb
import os
import sys
import socket
from network import HOST, PORT

CHUNK = 1024
FORMAT = pyaudio.paInt16
CHANNELS = 2
RATE = 16000
RECORD_SECONDS = 5
THRESHOLD = 1000

try:
    username = sys.argv[1]
except:
    username = "default"

try:
    method = sys.argv[2]
except:
    method = "default"
    
verbose = True

p = pyaudio.PyAudio()

def StreamBuild(channels):
    return p.open(format=FORMAT, channels = CHANNELS, rate=RATE, input=True, frames_per_buffer=CHUNK)

stream = ""
try:
    stream = StreamBuild(CHANNELS) 
except IOError as e:
    print "Found Exception: " + str(e) +". Trying 1 Channel";
    CHANNELS = 1
    stream = StreamBuild(CHANNELS) 
       
out_stream = p.open(format = FORMAT, channels = CHANNELS, rate = RATE,
                    output = True)
       
       
def main (user="default", method="default"):
  global db, username
  try: 
    db = cPickle.load (open ("database", "rb"))
  except:
    db = {}
    
  if (method == "live"):
    run_classify (user, False)
    
  if (user == "default"):
    username = raw_input ("Username: ")
    if (not db.has_key (username)):
      db[username] = {"gestures":{}, "trained":False, "no_class_audio" : []}
  else:
    username = user
    if (not db.has_key (username)):
      db[username] = {"gestures":{}, "trained":False, "no_class_audio" : []}

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
    print "\t5) Save Audio to File"
    print "\t6) View Stats for User"
    print "\t7) Run Testing Module"
    print "\t8) Quit"
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
    elif (method == 5):
      printv("Saving Audio To Files")
      run_save_audio(username)
      printv("Finished Saving Audio")
    elif(method == 6):
      printv("Viewing User Info")
      run_view_avail_gestures(username)
      printv("Finished Viewing User Info")
    elif(method == 7):
      printv("Starting Test Sequence")
      run_test_sequence(username)
      printv("Ending Test Sequence")
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
  if (gesturename[0] == '-'):
    db[username]["gestures"].pop(gesturename[1:])
    return
  
  if (not db[username]["gestures"].has_key (gesturename)):
    db[username]["gestures"][gesturename] = []

  db[username]["trained"] = False
  Run_On_Every_Frame(lambda frames : db[username]["gestures"][gesturename].append(frames))

  for gesture in db[username]["gestures"][gesturename]:
    playv(gesture)

def run_train (username):
  params = audio.GenerateParams(db[username]["gestures"], verbose)
  db[username]["params"] = params
  db[username]["trained"] = True

def run_classify (username, verbose = True):
  frames = ""
  classifier = audio.AudioClassifier (db[username]["params"])
  Run_On_Every_Frame(lambda frames : SendString (classifier.Classify(frames, verbose)))

def run_play (username):
  gesturename = raw_input ("Gesture name: ")
  if (not db[username]["gestures"].has_key (gesturename)):
    print "No such gesture found for user: "+username
    return

  for gesture in db[username]["gestures"][gesturename]:
    out_stream.write (gesture)

  return

def run_save_audio(username):
  if(os.path.isdir(username)):
    ret = raw_input("Directory %s Exists. Would you like to delete it? (y,n)" % username)
    if(ret == "n"): return
    import shutil
    shutil.rmtree(username)
    os.mkdir(username)
    
  import scipy.io.wavfile
  import numpy
  for gesture in db[username]["gestures"]:
    for i, audio in enumerate(db[username]["gestures"][gesture]):
      path = os.path.join(username, gesture + "_" + str(i) + ".wav")
      scipy.io.wavfile.write(path, RATE, numpy.frombuffer(audio, numpy.int16))

def run_view_avail_gestures(username):
  for i,gesture in enumerate(db[username]["gestures"]):
    print "\t" + str(i) + " : " + gesture
  print ""
            
def run_test_sequence(username):
  val = raw_input("Record unclassifiable audio samples? Currently have %s samples. (y,n): "
                  % len(db[username]["no_class_audio"]))
  if(val == "y"):
    Run_On_Every_Frame(lambda frames : db[username]["no_class_audio"].append(frames))
    
  trainData = {}
  testData = {}
  
  import random
  for gesture in db[username]["gestures"]:
    train = True
    for smpl in db[username]["gestures"][gesture]:
      if(train):
        trainData.setdefault(gesture, []).append(smpl)
      else:
        testData.setdefault(gesture, []).append(smpl)
      train = not train
  
  aClassifier = audio.AudioClassifier(audio.GenerateParams(trainData, False))
  correctPredictions = {}
  incorrectPredictions = {}
  print ""
  for gesture in testData:
    for i, sample in enumerate(testData[gesture]):
      aClassification = aClassifier.Classify(sample, False)
      # Incorrect Classification
      if(aClassification != gesture):
        printv("FAIL : Incorrectly Classified %s as being %s" % (gesture, aClassification))
        playv(sample)
        incorrectPredictions.setdefault(gesture, {}).setdefault(aClassification, []).append(i)
        
      #Correct Classification
      else:
        printv("SUCCESS : Correctly Classified %s as being %s" % (gesture, gesture))
        correctPredictions.setdefault(gesture, []).append(i)
          
  correctNoClass = []
  incorrectNoClass = {} 
  for i, no_class_audio in enumerate(db[username]["no_class_audio"]):
    aClassification = aClassifier.Classify(no_class_audio, False)
    
    # Incorrect No Class Classification
    if(aClassification != None):
      incorrectNoClass.setdefault(aClassification, []).append(i)
      printv("FAIL : Gave No Class Audio Label %s" % aClassification)
      playv(no_class_audio)
      
    # Correct No Class Assignment
    else:
      printv("SUCCESS : Gave Correct No Class Assignment")
      correctNoClass.append(i)
  
  view_test_results(correctPredictions, incorrectPredictions, correctNoClass,
                    incorrectNoClass, testData, db[username]["no_class_audio"])      

def view_test_results(correctPredictions, incorrectPredictions, correctNoClass,
                      incorrectNoClass, testData, noClassTests):
    
  def printPercentages(numCorrect, total):
    if(total == 0):
      print "No Samples\n"
      return
    percentCorrect = int(100 * float(numCorrect) / total)
    percentIncorrect = 100 - percentCorrect
    printv("\tSUCCESS (%s%%)\t: %s" % (str(percentCorrect), "*" * (percentCorrect / 10)))
    printv("\tFAIL    (%s%%)\t: %s" % (str(percentIncorrect), "*" * (percentIncorrect / 10)))
  
  total_correct = 0
  total = 0
  
  for item in testData.keys():
    printv("\nGesture %s" % item)
    total_correct = 0
    if(item in correctPredictions):
        total_correct = len(correctPredictions[item])
    total = len(testData[item])
    printPercentages(total_correct, total)    
  
  for key in testData.keys():
    if(key in correctPredictions):
        total_correct += len(correctPredictions[key])    
    total += len(testData[key])

  printv("\n\nTotals for Classification with Labels")
  printPercentages(total_correct, total)
  
  total_correct = len(correctNoClass)
  total = float(len(noClassTests))
  printv("\nTotals for Classification with No Labels")
  printPercentages(total_correct, total)
  printv("")
  
def Run_On_Every_Frame(execute):
  frames = ""
  try:
    while (True):
      data = stream.read (CHUNK)
      amplitude = audioop.rms (data, 2)
      if (amplitude >= THRESHOLD):
        #printv("Amplitude: " + str(amplitude))
        frames += data
      elif (len (frames) > 0):
        execute(frames)
        frames = ""
  except KeyboardInterrupt:
    return
    
def SendString (string):
  if (string != None):
    client_socket = socket.socket (socket.AF_INET, socket.SOCK_DGRAM)
    print username+"_"+string
    client_socket.sendto (username+"_"+string, (HOST, PORT))
    client_socket.close ()
    
def printv (string):
  if (verbose):
    print string

def playv(audio):
  if (verbose):
    out_stream.write(audio)
    
try:
  ret = 1
  while(ret != 0):
    ret = main (username, method)

except KeyboardInterrupt:
  raise
finally:
  try:
    out_stream.stop_stream()
    out_stream.close()
    stream.stop_stream()
    stream.close()
    p.terminate()
  except Exception as e:
    print str(e)