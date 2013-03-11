package com.mobilecomputing.src.Training.Training.BuiltInGestures;

import com.mobilecomputing.src.Training.Persistence.threegears.PoseMessage;
import com.mobilecomputing.src.Training.Training.FeatureExtractors.AppendageStretch;
import com.mobilecomputing.src.Training.Training.FeatureExtractors.HandInteraction;
import com.mobilecomputing.src.Training.Training.FeatureExtractors.VelocityFeatures;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class Hadouken implements Serializable {
    private Hadouken() {

    }

    public static Hadouken Process(List<List<PoseMessage>> aList) {
        return new Hadouken();
    }
}
