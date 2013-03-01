import pyaudio
import wave
import audioop
import shelve
import os
import AudioMath

class StreamObject:
    def __init__(self, numSecondsPerFrame):
        py = pyaudio.PyAudio()
        numDevices = py.get_device_count()

        print("Available Streams: ")
        for i in range(numDevices):
            print("\t" + str(i) + " : " + py.get_device_info_by_index(i)['name'])

        self.aIndex = int(raw_input("\nWhich Stream: "))
        assert(self.aIndex >= 0 and self.aIndex < py.get_device_count())
        self.deviceinfo = py.get_device_info_by_index(i)
        self.sample_rate = int(self.deviceinfo["defaultSampleRate"])
        self.frames_per_buffer = int(numSecondsPerFrame * self.sample_rate)
        self.num_seconds_per_frame = numSecondsPerFrame
        self.num_channels = self.deviceinfo["maxInputChannels"]
        self.format = pyaudio.paInt16
        self.samp_width = py.get_sample_size(self.format)
        self.py = py

    def __enter__(self):
        self.stream = self.py.open(format = self.format,
                                   channels = self.num_channels,
                                   rate = self.sample_rate,
                                   input = True,
                                   input_device_index = self.aIndex,
                                   frames_per_buffer = self.frames_per_buffer)
        return self
        
    def __exit__(self, type, value, traceback):
        self.stream.stop_stream()
        self.stream.close()

class Persistence:
    def __init__(self, aFilename):
        self.myStore = shelve.open(aFilename)
        if(not self.myStore.has_key("TrainingSets")):
            self.myStore["TrainingSets"] = {}
        if(not self.myStore.has_key("Classifiers")):
            self.myStore["Classifiers"] = {}           
        
    def Close(self):
        self.myStore.close()
        
    def InsertNewGestures(self, aUsername, aGestureName, ListOfBytes, Stream):
        if(not self.myStore["TrainingSets"].has_key(aUsername)):
            mySet = self.myStore["TrainingSets"]
            mySet[aUsername] = {}
            self.myStore["TrainingSets"] = mySet

        dataSet = []
        for i, item in enumerate(ListOfBytes):
            filename = "Database/" + aUsername + "_" + aGestureName + "_" + str(i + len(self.myStore["TrainingSets"][aUsername])) + ".wav"
            WriteWaveFile(filename, item, Stream.num_channels,
                          Stream.samp_width, Stream.sample_rate)
            dataSet.append(filename)
        
        if(not self.myStore["TrainingSets"][aUsername].has_key(aGestureName)):
            mySet = self.myStore["TrainingSets"][aUsername]
            mySet[aGestureName] = []
            myUSet = self.myStore["TrainingSets"]
            myUSet[aUsername] = mySet
            self.myStore["TrainingSets"] = myUSet
        
        data = self.myStore["TrainingSets"][aUsername]
        for i in dataSet:
            data[aGestureName].append(i)
        
        mySet = self.myStore["TrainingSets"]
        mySet[aUsername] = data
        
        self.myStore["TrainingSets"] = mySet
        
    def PlayBackRecords(self, aUsername, aGestureName):
        if(not self.myStore["TrainingSets"].has_key(aUsername)):
            print ("Could Not Find User " + aUsername)
            return
        
        if(not self.myStore["TrainingSets"][aUsername].has_key(aGestureName)):
            print ("User " + aUsername + " has no Gesture Named " + aGestureName)
            return
        
        i = 0
        while(True):
            if(i >= len(self.myStore["TrainingSets"][aUsername][aGestureName])): break
            if(not os.path.exists(self.myStore["TrainingSets"][aUsername][aGestureName][i])):
                print "Path ( " + self.myStore["TrainingSets"][aUsername][aGestureName][i] + " ) does not exist. Removing DB Record"
                del self.myStore["TrainingSets"][aUsername][aGestureName][i]
            else: 
                path = self.myStore["TrainingSets"][aUsername][aGestureName][i]
                print "Playing Back " + path
                PlaybackWaveFile(path)
                print "Completed Playback of " + path
                i = i+1            
        print "Completed All Playback"
            
    def PrintInfo(self):
        self.ViewAvailableUsers()
        for user in self.myStore["TrainingSets"].keys():
            self.ViewAvailableGestures(user)
            
    def ViewAvailableUsers(self):
        if(len(self.myStore["TrainingSets"].keys()) == 0):
            print "No Users Currently Registered"
            return
            
        print "Available Users: "
        for key in self.myStore["TrainingSets"].keys():
            print "\t" + key
        
    def ViewAvailableGestures(self, aUsername):
        if(not self.myStore["TrainingSets"].has_key(aUsername)):
            print ("Could Not Find User " + aUsername)
        
        if(len(self.myStore["TrainingSets"][aUsername].keys()) == 0):
            print "No Gestures Registered for " + aUsername
            return
        
        print "Available Gestures for " + aUsername
        for gesture in self.myStore["TrainingSets"][aUsername].keys():
            print "\t" + gesture
        
    def IsTrained(self, aUsername):
        return self.myStore["Classifiers"].hasKey("aUsername")
        
    def SetClassifierParams(self, aParams, aUsername):
        aSet = self.myStore["Classifiers"]
        aSet[aUsername] = aParams
        self.myStore["Classifiers"] = aSet
        
    def GetClassifierParams(self, aUsername):
        return self.myStore["Classifiers"][aUsername]
    
        
def WriteWaveFile(outFilename, frames, num_channels,
                  samp_width, sample_rate):
    wf = wave.open(outFilename, 'wb')
    wf.setnchannels(num_channels)
    wf.setsampwidth(samp_width)
    wf.setframerate(sample_rate)
    wf.writeframes(frames)
    wf.close()

def PlaybackWaveFile(aFilename):
    wf = wave.open(aFilename, 'rb')
    p = pyaudio.PyAudio()
    stream = p.open(format=p.get_format_from_width(wf.getsampwidth()),
                    channels=wf.getnchannels(),
                    rate=wf.getframerate(),
                    output=True)
    data = wf.readframes(1024)
    while data != '':
        stream.write(data)
        data = wf.readframes(1024)
    stream.stop_stream()
    stream.close()
    p.terminate()

def RunStreamingClassifier(aStream, ampThreshold, executeCommand, log):
    frames = ""
    try:
        while(True):
            data = aStream.stream.read(aStream.frames_per_buffer)
            amplitude = audioop.rms(data, 2)
            if(amplitude >= ampThreshold):
                if(log): print("Big Amplitude 2: " + str(amplitude))
                frames += data
            elif (frames != ""):
                executeCommand(frames)
                frames = ""
    except KeyboardInterrupt:
        return
   
def StoreObject(aData):
    def Update(aNewPoint):
        aData.append(aNewPoint)
    return Update
    
def RecordSamples(persist, aUsername):
    aGestureName = raw_input("Gesture name: ")
    myStore = []
    
    with StreamObject(.1) as myStream:
        RunStreamingClassifier(myStream, 1000, StoreObject(myStore), True)        
        persist.InsertNewGestures(aUsername, aGestureName, myStore, myStream)                      
        print(str(len(myStore)) + " new samples for " + aGestureName + " recorded")
        
    persist.PlayBackRecords(aUsername, aGestureName)
        
def RunTrainer(persist, aUsername):
    listofData = persist.GetGestureSet()
    params = AudioMath.GenerateParams(listofData)
    persist.SetClassifierParams(params, aUsername)
    
def RunClassifier(persist, aUsername):
    if(not persist.IsTrained(aUsername)):
        print "User Not Trained! Please Train Before Running Classifier"
        return

    aParams = persist.GetClassifierParams(aUsername)
    AudioClassifier = AudioMath.BuildClassifier(aParams)
    
    with StreamObject(.1) as myStream:
        RunStreamingClassifier(myStream, 1000, lambda data : AudioClassifer.Classify(data))
        
if __name__ == "__main__":
    persist = Persistence("Database/BasicAudioDB")
    persist.PrintInfo()

    username = raw_input("Username: ")    
    method = input("Would you like to record more gestures (1) / train the gestural system (2) / run classifier (3) / listen to existing gestures (4): ")
    
    if(method == 1):
        print "Starting Recording System"
        RecordSamples(persist, username)
        print "Finished Recording System"
        
    elif(method == 2):
        print "Starting Training Procedure for user " + username + ". This deletes previous training" 
        RunTrainer(persist, username)
        print "Training Complete for user " + username + "."
        
    elif(method == 3):
        print "Starting Realtime Handler on user " + username + "."
        RunClassifier(persist, username)
        print "Shutting Down Classifier"
        
    elif(method == 4):
        persist.PlayBackRecords(username, raw_input("Input Gesture: "))
        
    persist.Close()