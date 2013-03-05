package com.mobilecomputing.src.Training.Training.FeatureExtractors;

import com.mobilecomputing.src.Training.Persistence.threegears.HandTrackingMessage;
import com.mobilecomputing.src.Training.Persistence.threegears.PoseMessage;

import javax.vecmath.Point3f;
import javax.vecmath.Quat4f;
import javax.vecmath.Vector3f;

public class HandInteraction {
    public double theWristsDistance;
    public double theHandsDistance;
    public double theIndexDistance;
    public double theThumbDistance;
    public double theMiddleDistance;
    public double theRingDistance;
    public double thePinkyDistance;

    public Quat4f theLeftHandRotation;
    public Quat4f theRightHandRotation;

    public Vector3f theLeftHandPosition;
    public Vector3f theRightHandPosition;

    public HandInteraction(PoseMessage aMessage) {
        HandTrackingMessage.HandState leftHandState = aMessage.getHandState(0);
        HandTrackingMessage.HandState rightHandState = aMessage.getHandState(1);

        theLeftHandRotation = leftHandState.getRotation();
        theRightHandRotation = rightHandState.getRotation();

        theLeftHandPosition = leftHandState.getPosition();
        theRightHandPosition = rightHandState.getPosition();

        Point3f[] leftFingerTips = aMessage.getFingerTips(0);
        Point3f[] rightFingerTips = aMessage.getFingerTips(1);

        theWristsDistance = VectorOps.Distance(aMessage.GetJointTranslations(0)[1], aMessage.GetJointTranslations(1)[1]);
        theHandsDistance = VectorOps.Distance(theLeftHandPosition, theRightHandPosition);

        theIndexDistance = leftFingerTips[1].distance(rightFingerTips[1]);
        theThumbDistance = leftFingerTips[0].distance(rightFingerTips[0]);
        theMiddleDistance = leftFingerTips[2].distance(rightFingerTips[2]);
        theRingDistance = leftFingerTips[3].distance(rightFingerTips[3]);
        thePinkyDistance = leftFingerTips[4].distance(rightFingerTips[4]);
    }
}
