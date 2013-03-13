package com.mobilecomputing.src.Training.Training.HMMModels;

import be.ac.ulg.montefiore.run.jahmm.*;
import be.ac.ulg.montefiore.run.jahmm.learn.BaumWelchLearner;
import com.mobilecomputing.src.Training.Persistence.threegears.PoseMessage;

import java.util.ArrayList;
import java.util.List;

public class HMMModel {
    public static Hmm<ObservationInteger> LearnModel(List<List<PoseMessage>> aSetOfGestures) {
        List<List<ObservationInteger>> aIntegerSet = new ArrayList<List<ObservationInteger>>();
        for (List<PoseMessage> aSetOfGesture : aSetOfGestures) {
            PoseMessage aOldMessage = aSetOfGesture.get(0);
            List<ObservationInteger> aCodesSet = new ArrayList<ObservationInteger>();
            for(int i = 1; i < aSetOfGesture.size(); i++) {
                PoseMessage aNewMessage = aSetOfGesture.get(i);
                int aCode = CodePoints.GenerateCodePoint(aOldMessage, aNewMessage, 1);
                aOldMessage = aNewMessage;
                ObservationInteger aInteger = new ObservationInteger(aCode);
                aCodesSet.add(aInteger);
            }

            aIntegerSet.add(aCodesSet);
        }

        Hmm<ObservationInteger> aUntrainedHMM = LearnDataSet();
        BaumWelchLearner bwl = new BaumWelchLearner();
        Hmm<ObservationInteger> learntHMM = bwl.learn(aUntrainedHMM, aIntegerSet);
        return learntHMM;
    }

    public static Hmm<ObservationInteger> LearnDataSet() {
        OpdfIntegerFactory factory = new OpdfIntegerFactory(10);
        Hmm<ObservationInteger> hmm = new Hmm<ObservationInteger>(5, factory);

        hmm.setPi(0, 1);
        hmm.setPi(1, 0);
        hmm.setPi(2, 0);
        hmm.setPi(3, 0);
        hmm.setPi(4, 0);

        for(int i = 0; i!=5; i++) {
            double[] myArray = new double[18];
            for (int j = 0; j < myArray.length; j++) {
                myArray[j] = 1/18;
            }

            hmm.setOpdf(i, new OpdfInteger(myArray));
        }

        double aVal = .5;
        hmm.setAij(0, 0, aVal);
        hmm.setAij(0, 1, 1-aVal);
        hmm.setAij(0, 2, 0);
        hmm.setAij(0, 3, 0);
        hmm.setAij(0, 4, 0);

        hmm.setAij(1, 0, 0);
        hmm.setAij(1, 1, aVal);
        hmm.setAij(1, 2, 1-aVal);
        hmm.setAij(1, 3, 0);
        hmm.setAij(1, 4, 0);

        hmm.setAij(2, 0, 0);
        hmm.setAij(2, 1, 0);
        hmm.setAij(2, 2, aVal);
        hmm.setAij(2, 3, 1-aVal);
        hmm.setAij(2, 4, 0);

        hmm.setAij(3, 0, 0);
        hmm.setAij(3, 1, 0);
        hmm.setAij(3, 2, 0);
        hmm.setAij(3, 3, aVal);
        hmm.setAij(3, 4, 1-aVal);

        hmm.setAij(4, 0, 0);
        hmm.setAij(4, 1, 0);
        hmm.setAij(4, 2, 0);
        hmm.setAij(4, 3, 0);
        hmm.setAij(4, 4, 1);

        return hmm;
    }
}
