package com.mobilecomputing.src.Training.Training.FeatureExtractors;

import com.mobilecomputing.src.Training.Persistence.threegears.HandTrackingMessage;
import com.mobilecomputing.src.Training.Persistence.threegears.PoseMessage;

import javax.vecmath.Quat4f;
import javax.vecmath.Vector3f;

public class VelocityFeatures {
    public Vector3f theHandDeltaVector;
    public Quat4f theChangeInQuaternion;
    public int theHand;

    public VelocityFeatures(PoseMessage aStartPose, PoseMessage aEndPose, int aHand) {
        theHand = aHand;
        HandTrackingMessage.HandState aHandStart = aStartPose.getHandState(0);
        HandTrackingMessage.HandState aHandEnd = aEndPose.getHandState(0);

        theChangeInQuaternion = VectorOps.InverseMultipy(aHandStart.getRotation(), aHandEnd.getRotation());
        theHandDeltaVector = VectorOps.VectorVelocity(aHandStart.getPosition(), aHandEnd.getPosition());
    }
}
