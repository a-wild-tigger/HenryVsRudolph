package com.mobilecomputing.src.Training.Training.FeatureExtractors;

import com.mobilecomputing.src.Training.Persistence.threegears.HandTrackingMessage;
import com.mobilecomputing.src.Training.Persistence.threegears.PoseMessage;

import javax.vecmath.Point3f;
import javax.vecmath.Quat4f;
import javax.vecmath.Vector3f;
import java.text.DecimalFormat;

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

    DecimalFormat df = new DecimalFormat("##.00");
    @Override
    public String toString() {
        return "Wrists Distance = " + String.valueOf(df.format(theWristsDistance)) +
                "    Hands Distance = " + String.valueOf(df.format(theHandsDistance)) +
                "    Index Distance = " + String.valueOf(df.format(theIndexDistance)) +
                "    Thumb Distance = " + String.valueOf(df.format(theThumbDistance)) +
                "\n\nMiddle Distance = " + String.valueOf(df.format(theMiddleDistance)) +
                "    Ring Distance = " + String.valueOf(df.format(theRingDistance)) +
                "    Pinky Distance = " + String.valueOf(df.format(thePinkyDistance)) +
                "\n\nLeft Hand Rotation = " + VectorOps.RenderRotation(theLeftHandRotation) +
                "\n\nRight Hand Rotation = " + VectorOps.RenderRotation(theRightHandRotation);
                //"\n\ntheLeftHandPosition = " + theLeftHandPosition +
                //"\n\ntheRightHandPosition = " + theRightHandPosition;
    }
}
