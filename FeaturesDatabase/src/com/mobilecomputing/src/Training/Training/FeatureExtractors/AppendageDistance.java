package com.mobilecomputing.src.Training.Training.FeatureExtractors;

import com.mobilecomputing.src.Training.Persistence.threegears.PoseMessage;

import javax.vecmath.Point3f;
import javax.vecmath.Vector3f;
import java.awt.*;

public class AppendageDistance {
    public double ThumbToIndexDistance;
    public double ThumbToMiddleDistance;
    public double ThumbToRingDistance;
    public double ThumbToPinkyDistance;
    public double ThumbToWristDistance;

    public double IndexToMiddleDistance;
    public double IndexToWristDistance;

    public double MiddleToRingDistance;
    public double MiddleToWristDistance;

    public double RingToPinkyDistance;
    public double RingToWristDistance;

    public double PinkyToWristDistance;
    public int theHand;

    public AppendageDistance(PoseMessage aMessage, int aHand) {
        theHand = aHand;

        Point3f[] thePoints = aMessage.getFingerTips(aHand);
        Point3f theThumb = thePoints[0];
        Point3f theIndex = thePoints[1];
        Point3f theMiddle = thePoints[2];
        Point3f theRing = thePoints[3];
        Point3f thePinky = thePoints[4];
        Vector3f theWrist = aMessage.GetJointTranslations(aHand)[0];

        ThumbToIndexDistance = theThumb.distance(theIndex);
        ThumbToMiddleDistance = theThumb.distance(theMiddle);
        ThumbToRingDistance = theThumb.distance(theRing);
        ThumbToPinkyDistance = theThumb.distance(thePinky);
        ThumbToWristDistance = VectorOps.Distance(theWrist, theThumb);

        IndexToMiddleDistance = theIndex.distance(theMiddle);
        IndexToWristDistance = VectorOps.Distance(theWrist, theIndex);

        MiddleToRingDistance = theMiddle.distance(theRing);
        MiddleToWristDistance = VectorOps.Distance(theWrist, theMiddle);

        RingToPinkyDistance = theRing.distance(thePinky);
        RingToWristDistance = VectorOps.Distance(theWrist, theRing);

        PinkyToWristDistance = VectorOps.Distance(theWrist, thePinky);

        theHand = aHand;
    }
}
