package com.mobilecomputing.src.Training.Training.FeatureExtractors;

import javax.vecmath.*;
import java.text.DecimalFormat;

public class VectorOps {
    public static Vector3f SubtractVector(Vector3f aFirstVector, Vector3f aSecondVector) {
        Vector3f myVector = (Vector3f) aFirstVector.clone();
        myVector.sub(aSecondVector);
        return myVector;
    }

    public static Vector3f VectorVelocity(Vector3f aFirstVector, Vector3f aSecondVector) {
        return SubtractVector(aSecondVector, aFirstVector);
    }

    public static double Distance(Vector3f aVector, Point3f aPoint) {
        Point3f myPoint = new Point3f(aVector.x, aVector.y, aVector.z);
        return myPoint.distance(aPoint);
    }


    public static double Distance(Vector3f aVector, Vector3f vector3f) {
        Point3f myPoint = new Point3f(aVector.x, aVector.y, aVector.z);
        return Distance(vector3f, myPoint);
    }

    public static DecimalFormat df = new DecimalFormat("##.00");
    public static String RenderRotation(Quat4f theLeftHandRotation) {
        float w = theLeftHandRotation.getW();
        float x = theLeftHandRotation.getX();
        float y = theLeftHandRotation.getY();
        float z = theLeftHandRotation.getZ();

        return "Angle, X, Y, Z = " + df.format(w) + " , " + df.format(x) + " , " + df.format(y) + " , " + df.format(z) +  "\n";
    }


    public static Quat4f InverseMultipy(Quat4f aQuat, Quat4f aQuat1) {
        Quat4f aQuatTemp = (Quat4f) aQuat.clone();
        aQuatTemp.inverse();

        aQuatTemp.mul(aQuat1);
        return aQuatTemp;
    }

    public static Vector3f Multiply(Quat4f aQuat) {
        double aAngle = (2 * Math.acos(aQuat.getW()));
        double c = Math.cos(aAngle);
        double s = Math.sin(aAngle);
        double ax = aQuat.x;
        double ay = aQuat.y;
        double az = aQuat.z;

        return new Vector3f((float) (c + ax*ax*(1-c)), (float) (ay*ax*(1-c)+az*s), (float) (az*ax*(1-c)-ay*s));
    }
}