import MFCC, numpy
import scipy.spatial.distance as dist

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
          for i in range (min (len (features), len (tsample))):
            total_distance += dist.euclidean (features[i], tsample[i])
          d.append (total_distance/float (i))
        score = numpy.min (d)
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
      if(verbose):
        print "Identified %s with score of %f." % (lowest, minimum)
      return lowest
      
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
