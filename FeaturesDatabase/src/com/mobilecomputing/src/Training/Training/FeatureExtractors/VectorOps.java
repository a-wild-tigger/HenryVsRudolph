package com.mobilecomputing.src.Training.Training.FeatureExtractors;

import javax.vecmath.*;

public class VectorOps {
    public static Vector2d SubtractVector(Vector2d aFirstVector, Vector2d aSecondVector) {
        Vector2d myVector = (Vector2d) aFirstVector.clone();
        myVector.sub(aSecondVector);
        return myVector;
    }

    public static Vector2d VectorVelocity(Vector2d aFirstVector, Vector2d aSecondVector) {
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
}