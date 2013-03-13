package com.mobilecomputing.src.Training.Training.HMMModels;

import java.io.Serializable;

public class Viterbi implements Serializable {
    private final HMM aModel;
    public double[] prevChain;

    public Viterbi(HMM aModel) {
        this.aModel = aModel;
        prevChain = new double[aModel.numStates];
    }

    public int maxLikelihood() {
        int index = 0;
        double max = prevChain[0];
        for(int i = 1; i < aModel.numStates; i++) {
            if(prevChain[i] > max) {
                max = prevChain[i];
                index = i;
            }
        }

        return index;
    }

    public void insert(int aObservation) {
        if(AllZeros()) {
            for(int i = 0; i != aModel.numStates; i++) {
                prevChain[i] = aModel.pi[i] * aModel.b[i][aObservation];
            }
            return;
        }

        for(int i = 0; i != aModel.numStates; i++) {
            double theMax = aModel.a[0][0] * prevChain[0];
            for(int j = 1; j < aModel.numStates; j++) {
                double test = aModel.a[j][i] * prevChain[j];
                if(test > theMax) theMax = test;
            }

            prevChain[i] = aModel.b[i][aObservation] * theMax;
        }
    }

    private boolean AllZeros() {
        for(int i = 0; i != prevChain.length; i++) {
            if(prevChain[i] != 0) { return false; }
        }

        return true;
    }
}
