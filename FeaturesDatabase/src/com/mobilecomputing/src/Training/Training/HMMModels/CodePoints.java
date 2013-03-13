package com.mobilecomputing.src.Training.Training.HMMModels;

import com.mobilecomputing.src.Training.Persistence.threegears.HandTrackingMessage;
import com.mobilecomputing.src.Training.Persistence.threegears.PoseMessage;

public class CodePoints {
    public static int GenerateCodePoint(PoseMessage aStart, PoseMessage aEnd, int aHand) {
        HandTrackingMessage.HandState myStart = aStart.getHandState(aHand);
        HandTrackingMessage.HandState myEnd = aEnd.getHandState(aHand);

        double xPosEnd = myEnd.getPosition().x;
        double xPosStart = myStart.getPosition().x;

        double yPosEnd = myEnd.getPosition().y;
        double yPosStart = myStart.getPosition().y;
        double aValue = (yPosEnd - yPosStart) / (xPosEnd - xPosStart);

        double aAngle = Math.atan(aValue);
        double aAngle2 = (aAngle > 0 ? aAngle : (2*Math.PI + aAngle)) * 360 / (2*Math.PI);

        if(aAngle2 == 360) { return 0; }
        return (int) (aAngle2 / 20);
    }
}
