package com.mobilecomputing.src.Training.Training.FeatureExtractors;

import com.mobilecomputing.src.Training.Persistence.threegears.PoseMessage;

import javax.vecmath.Quat4f;

public class AppendageStretch {
    public boolean isThumbStretched;
    public boolean isIndexFingerStretched;
    public boolean isMiddleFingerStretched;
    public boolean isRingFingerStretched;
    public boolean isPinkyFingerStretched;

    public Quat4f theHandQuat;
    public Quat4f theThumbQuat;
    public Quat4f theIndexQuat;
    public Quat4f theMiddleQuat;
    public Quat4f theRingQuat;
    public Quat4f thePinkyQuat;

    public int theHand;
    public AppendageStretch(PoseMessage aMessage, int aHand) {
        theHand = aHand;

        Quat4f aQuat = (Quat4f) aMessage.getHandState(aHand).getRotation().clone();
        Quat4f[] aJointRotations = aMessage.GetJointRotations()[aHand].clone();

        theHandQuat = aQuat;
        theThumbQuat = VectorOps.InverseMultipy(aQuat, aJointRotations[4]);
        theIndexQuat = VectorOps.InverseMultipy(aQuat, aJointRotations[6]);
        theMiddleQuat = aJointRotations[9];
        theRingQuat = VectorOps.InverseMultipy(aQuat, aJointRotations[13]);
        thePinkyQuat = VectorOps.InverseMultipy(aQuat, aJointRotations[16]);

        isThumbStretched = (theThumbQuat.getW() > .92);
        isMiddleFingerStretched = (theMiddleQuat.getW() > .92);
        isIndexFingerStretched = (theIndexQuat.getW() > .92);
        isRingFingerStretched = (theRingQuat.getW() > .92);
        isPinkyFingerStretched = (thePinkyQuat.getW() > .92);
    }

    @Override
    public String toString() {
        String handString = theHand == 1 ? "Right Hand : " : "Left Hand : ";
        return handString +
                "  isThumbStretched = " + isThumbStretched +
                ", isIndexFingerStretched = " + isIndexFingerStretched +
                ", isMiddleFingerStretched = " + isMiddleFingerStretched +
                ",\n\nisRingFingerStretched = " + isRingFingerStretched +
                ", isPinkyFingerStretched = " + isPinkyFingerStretched +
                ",\n\ntheThumbQuat = " + VectorOps.RenderRotation(theThumbQuat) +
                ", theIndexQuat = " + VectorOps.RenderRotation(theIndexQuat) +
                ", theMiddleQuat = " + VectorOps.RenderRotation(theMiddleQuat) +
                ", theRingQuat = " + VectorOps.RenderRotation(theRingQuat) +
                ", theHandQuat = " + VectorOps.RenderRotation(theHandQuat);
    }
}
