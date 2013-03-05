package com.mobilecomputing.src.Training.Training.FeatureExtractors;

import com.mobilecomputing.src.Training.Persistence.threegears.BasicMessage;
import com.mobilecomputing.src.Training.Persistence.threegears.HandTrackingMessage;
import com.mobilecomputing.src.Training.Persistence.threegears.PoseMessage;

import javax.vecmath.Matrix4f;
import javax.vecmath.Point3f;
import javax.vecmath.Vector3f;

public class PositionInvariance {
    public PoseMessage ShiftToOrigin(PoseMessage aMessage) {
        BasicMessage myBasicMessage = aMessage.GetBasicMessage();
        Vector3f leftHandPosition = aMessage.getHandState(0).getPosition();
        Vector3f rightHandPosition = aMessage.getHandState(1).getPosition();

        Vector3f[] aLeftHandFrames = aMessage.GetJointTranslations(0).clone();
        Vector3f[] aRightHandFrames = aMessage.GetJointTranslations(1).clone();

        Point3f[] leftFingerTip = aMessage.getFingerTips(0).clone();
        Point3f[] rightFingerTip = aMessage.getFingerTips(1).clone();

        for (int i = 0; i < aLeftHandFrames.length; i++) {
            aLeftHandFrames[i].sub(leftHandPosition);
        }

        for(int i = 0; i < aRightHandFrames.length; i++) {
            aRightHandFrames[i].sub(rightHandPosition);
        }

        for (int i = 0; i < leftFingerTip.length; i++) {
            leftFingerTip[i].sub(leftHandPosition);
        }

        for (int i = 0; i < rightFingerTip.length; i++) {
            rightFingerTip[i].sub(rightHandPosition);
        }

        PoseMessage myNewMessage = new PoseMessage(myBasicMessage,
                aMessage.GetConfidenceEstimates(), aMessage.GetJointRotations(),
                new Vector3f[][]{ aLeftHandFrames, aRightHandFrames },
                new Point3f[][]{ leftFingerTip, rightFingerTip }, aMessage.GetHandPoseConfidences());

        return myNewMessage;
    }
}
