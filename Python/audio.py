import MFCC, numpy
import scipy.spatial.distance as dist

def DynamicTimeWarp(aSequence1, aSequence2, mfccDistFunction):
    firstLen = len(aSequence1)
    secondLen = len(aSequence2)
    
    myTable = numpy.empty((firstLen, secondLen))
    myTable[(0,0)] = mfccDistFunction(aSequence1[0], aSequence2[0])
    
    for row in range(1,firstLen):
        myTable[row][0] = mfccDistFunction(aSequence1[row], aSequence2[0]) + myTable[row - 1][0]
    
    for col in range(1,secondLen):
        myTable[0][col] = mfccDistFunction(aSequence1[0], aSequence2[col]) + myTable[0][col - 1]
        
    for row in range(1, firstLen):
        for col in range(1, secondLen):
            myTable[row][col] = mfccDistFunction(aSequence1[row], aSequence2[col]) + \
                                  min(myTable[row - 1][col],
                                      myTable[row - 1][col - 1],
                                      myTable[row][col - 1])
                                      
    return myTable[firstLen - 1][secondLen - 1]
    
class AudioClassifier ():
    def __init__ (self, params):
       self.params = params

    def Classify (self, sample, verbose = True):
      length = len (sample)
      features = MFCC.extract (numpy.frombuffer (sample, numpy.int16))
      gestures = {}
      for gesture in self.params:
        d = []
        for tsample in self.params[gesture]:
          total_distance = 0
          smpl_length = len(tsample)
          
          if(numpy.abs(length - smpl_length) <= 0):
             continue
          
          for i in range (min (len (features), len (tsample))):
            total_distance += dist.cityblock(features[i], tsample[i])
          
          d.append (total_distance/float (i))
        score = numpy.min(d)
        gestures[gesture] = score
        if(verbose):
            print "Gesture %s: %f" % (gesture, score)
        try:
          if (score < minimum):
            minimum = score
            lowest = gesture
        except:
          minimum = score
          lowest = gesture
      if verbose:
         print lowest, minimum
      if(minimum < 12):
        return lowest
      else:
        return None
      
def GenerateParams (gestures, verbose = True):
  params = {}
  for gesture in gestures:
    if(verbose):
      print "Processing " + gesture
    l = []
    for sample in gestures[gesture]:
      l.append (MFCC.extract (numpy.frombuffer (sample, numpy.int16)))
    params[gesture] = l
  return params
 