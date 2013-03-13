package com.mobilecomputing.src.Training.Training.HMMModels;

import be.ac.ulg.montefiore.run.jahmm.*;
import be.ac.ulg.montefiore.run.jahmm.learn.BaumWelchLearner;
import com.mobilecomputing.src.Training.Persistence.threegears.HandTrackingMessage;
import com.mobilecomputing.src.Training.Persistence.threegears.PoseMessage;

import javax.vecmath.Point3f;
import java.util.ArrayList;
import java.util.List;

public class HMMModel {
    public static int GenerateCodePoint(PoseMessage aStart, PoseMessage aEnd, int aHand) {
        Point3f myStart = aStart.getFingerTips(aHand)[1];
        Point3f myEnd = aEnd.getFingerTips(aHand)[1];

        double xPosEnd = myEnd.x;
        double xPosStart = myStart.x;

        double yPosEnd = myEnd.y;
        double yPosStart = myStart.y;
        double aValue = (yPosEnd - yPosStart) / (xPosEnd - xPosStart);

        double aAngle = Math.atan(aValue);
        double aAngle2 = (aAngle > 0 ? aAngle : (2*Math.PI + aAngle)) * 360 / (2*Math.PI);

        if(aAngle2 == 360) { return 0; }
        return (int) (aAngle2 / 20);
    }

    public static Viterbi LearnModel(List<List<PoseMessage>> aSetOfGestures) {
        int[][] aTrainingSet = new int[aSetOfGestures.size()][];
        for(int i = 0; i != aSetOfGestures.size(); i++) {
            List<PoseMessage> aSetOfGesture = aSetOfGestures.get(i);
            PoseMessage aOldMessage = aSetOfGesture.get(0);
            aTrainingSet[i] = new int[aSetOfGesture.size() - 1];
            for(int j = 0; j < aSetOfGesture.size() - 1; j++) {
                PoseMessage aNewMessage = aSetOfGesture.get(j);
                int aCode = GenerateCodePoint(aOldMessage, aNewMessage, 1);
                aTrainingSet[i][j] = aCode;
            }
        }

        int numStates = 5;
        int sigma = 18;
        double a = .5;

        HMM myHmm = new HMM(numStates, sigma);

        // We always start in state 0
        myHmm.pi[0] = 1;

        // We use uniform random emission probabilities
        for(int i = 0; i != 5; i++) {
            for(int j = 0; j != sigma; j++) {
                myHmm.b[i][j] = 1/(float)sigma;
            }
        }

        // Set bidiagonal matrix
        myHmm.a[0][0] = a;
        myHmm.a[0][1] = 1-a;

        myHmm.a[1][1] = a;
        myHmm.a[1][2] = 1-a;

        myHmm.a[2][2] = a;
        myHmm.a[2][3] = 1-a;

        myHmm.a[3][3] = a;
        myHmm.a[3][4] = 1-a;

        myHmm.a[4][4] = 1;

        myHmm.train(aTrainingSet, 10);
        assert(myHmm.validateModel());
        return new Viterbi(myHmm);
    }
}
