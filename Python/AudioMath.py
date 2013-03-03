import numpy, scipy
from scipy.io.wavfile import read, write
from scipy.signal import lfilter
from scipy.fftpack import dct
import math

class AudioClassifier():
    def Classify(dataSampleAsScipyArray):
        print len(dataSample)
        
def processFrame(frame):
    input("Raw Frame: " + str(frame))
    hamming = frame * numpy.hamming(len(frame))

    
    input("Hamming: " + str(hamming))
    complexSpectrum = scipy.fft(hamming)

    input("Complex: " + str(complexSpectrum))
    powerSpectrum = abs(complexSpectrum) ** 2
    
    input("Power Spectrum: " + str(powerSpectrum))
    filteredSpectrum = numpy.dot(powerSpectrum, melFilterBank(len(frame)))
    
    input("Filtered Spectrum: " + str(filteredSpectrum))
    logSpectrum = numpy.log(filteredSpectrum)
    dctSpectrum = dct(logSpectrum, type=2)
    return dctSpectrum
    
def melFilterBank(blockSize):
    numCoefficients = 13

    def freqToMel(freq):
        return 1127.01048 * math.log(1 + freq / 700.0)

    def melToFreq(mel):
        return 700 * (math.exp(freq / 1127.01048 - 1))

    minHz = 0
    maxHz = 22.000 
    
    numBands = int(numCoefficients)
    maxMel = int(freqToMel(maxHz))
    minMel = int(freqToMel(minHz))
    filterMatrix = numpy.zeros((numBands, blockSize))

    melRange = numpy.array(xrange(numBands + 2))

    melCenterFilters = melRange * (maxMel - minMel) / (numBands + 1) + minMel

    # each array index represent the center of each triangular filter
    aux = numpy.log(1 + 1000.0 / 700.0) / 1000.0
    aux = (numpy.exp(melCenterFilters * aux) - 1) / 22050
    aux = 0.5 + 700 * blockSize * aux
    aux = numpy.floor(aux)  # Arredonda pra baixo
    centerIndex = numpy.array(aux, int)  # Get int values

    for i in xrange(numBands):
        start, centre, end = centerIndex[i:i + 3]
        k1 = numpy.float32(centre - start)
        k2 = numpy.float32(end - centre)
        up = (numpy.array(xrange(start, centre)) - start) / k1
        down = (end - numpy.array(xrange(centre, end))) / k2

        filterMatrix[i][start:centre] = up
        filterMatrix[i][centre:end] = down

    return filterMatrix.transpose()
        
def frame_block(apreemph):
    print "Running Frame Block on Total Length of " + str(len(apreemph))
    print "Note Divisibity is: " + str(len(apreemph) / 160.0)
    frames = []
    x = 0
    while(1):
        frames.append(apreemph[x:x+320])
        x += 160
        if(x >= len(apreemph)):
            break
    return frames
        
def get_mfcc(sample, rate):
    preemph = lfilter([1, -.95], 1, sample)
    frames = frame_block(preemph)
    return [processFrame(frame) for frame in frames]
        
def GenerateParams(listOfGestureRecordings):
    PostProcessedDict = dict()
    for gestureName in listOfGestureRecordings:
        mfccs = []
        for rate, audiosmpl in listOfGestureRecordings[gestureName]:
            mfccs.append(get_mfcc(audiosmpl, rate))
        PostProcessedDict[gestureName] = mfccs
    return PostProcessedDict
    
def BuildClassifier():
    return AudioClassifier