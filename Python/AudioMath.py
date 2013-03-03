import MFCC, numpy
import scipy.spatial.distance as dist

class AudioClassifier():
    def __init__(self, paramDict):
       self.params = paramDict

    def Classify(dataSampleAsSciptArray):
        length = audiosmpl / 16000.0
        mfccs = MFCC.extract(audiosmpl)
        sample_avgs = self.params[length]
        
        print ("Encountered Sample of Length " + str(length))
        if(length in self.params):
            print "Length Not in Sample!"
            return
        
        for gesture in self.params[length].keys():
            e_score = dist.EuclideanDistance(mfccs, self.params[length][gesture])
            print "Score against " + gesture + " : " + e_score
        
def GenerateParams(listOfGestureRecordings):
    lengthset = {}
    lengths = set()
    PostProcessedDict = {}
    for gestureName in listOfGestureRecordings:
        mfccs = []
        for rate, audiosmpl in listOfGestureRecordings[gestureName]:
            length = len(audiosmpl) / 16000.0
            lengths.add(length)
            if(length not in lengthset):
                lengthset[length] = {}
            if(gestureName not in lengthset[length]):
                lengthset[length][gestureName] = []
            mfccData = MFCC.extract(audiosmpl)
            lengthset[length][gestureName].append(mfccData)
            mfccs.append(mfccData)
        PostProcessedDict[gestureName] = { "MFCC" : mfccs }
    
    def computeAvg(data):
        total = len(data) * 1.0
        avg = data[0]
        #print avg
        
        for sample in data[1:]:
            #print sample
            for i in range(len(sample)):
                for melRow in range(len(sample[i])):
                    avg[i][melRow] += sample[i][melRow]
        
        return avg / total
    
    avgedLengthset = {}
    for length in lengthset.keys():
        avgedLengthset[length] = {}
        for gesture in lengthset[length].keys():
            avgedLengthset[length][gesture] = computeAvg(lengthset[length][gesture])
    
    return avgedLengthset