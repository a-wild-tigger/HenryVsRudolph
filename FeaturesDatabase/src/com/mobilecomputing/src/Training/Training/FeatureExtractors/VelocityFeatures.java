package com.mobilecomputing.src.Training.Training.FeatureExtractors;

import com.mobilecomputing.src.Training.Persistence.threegears.HandTrackingMessage;
import com.mobilecomputing.src.Training.Persistence.threegears.PoseMessage;

import javax.vecmath.Point3f;
import javax.vecmath.Quat4f;
import javax.vecmath.Vector3f;

public class VelocityFeatures {
    public Vector3f theHandDeltaVector;
    public Vector3f theThumbDeltaVector;
    public Vector3f theIndexDeltaVector;
    public Vector3f theMiddleDeltaVector;
    public Vector3f theRingDeltaVector;
    public Vector3f thePinkyDeltaVector;
    public Quat4f theChangeInQuaternion;
    public int theHand;

    public VelocityFeatures(PoseMessage aStartPose, PoseMessage aEndPose, int aHand) {
        theHand = aHand;
        HandTrackingMessage.HandState aHandStart = aStartPose.getHandState(aHand);
        HandTrackingMessage.HandState aHandEnd = aEndPose.getHandState(aHand);

        Point3f[] startFingerTips = aStartPose.getFingerTips(aHand);
        Point3f[] endFingerTips = aEndPose.getFingerTips(aHand);

        theChangeInQuaternion = VectorOps.InverseMultipy(aHandStart.getRotation(), aHandEnd.getRotation());
        theHandDeltaVector = VectorOps.VectorVelocity(aHandStart.getPosition(), aHandEnd.getPosition());
        theThumbDeltaVector = VectorOps.VectorVelocity(startFingerTips[0], endFingerTips[0]);
        theIndexDeltaVector = VectorOps.VectorVelocity(startFingerTips[1], endFingerTips[1]);
        theMiddleDeltaVector = VectorOps.VectorVelocity(startFingerTips[2], endFingerTips[2]);
        theRingDeltaVector = VectorOps.VectorVelocity(startFingerTips[3], endFingerTips[3]);
        thePinkyDeltaVector = VectorOps.VectorVelocity(startFingerTips[4], endFingerTips[4]);
    }
}
