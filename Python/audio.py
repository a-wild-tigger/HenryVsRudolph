import MFCC, numpy
import scipy.spatial.distance as dist

METHOD = 2

class AudioClassifier ():
    def __init__ (self, params):
       self.params = params

    def Classify (self, sample, method = METHOD):
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
        score = numpy.mean (d)
        gestures[gesture] = score
        print "Gesture %s: %f" % (gesture, score)
        try:
          if (score < minimum):
            minimum = score
            lowest = gesture
        except:
          minimum = score
          lowest = gesture
      print "Identified %s with score of %f." % (lowest, minimum)

def GenerateParams (gestures):
  params = {}
  for gesture in gestures:
    print "Processing " + gesture
    l = []
    for sample in gestures[gesture]:
      l.append (MFCC.extract (numpy.frombuffer (sample, numpy.int16)))
    params[gesture] = l
  return params
