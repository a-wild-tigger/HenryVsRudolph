package com.mobilecomputing.src.Training.Persistence.threegears;

import java.util.Locale;

import javax.vecmath.Matrix4f;
import javax.vecmath.Point3f;
import javax.vecmath.Quat4f;
import javax.vecmath.Vector3f;

/**
 * Message that exposes the hand skeleton information for both hands.
 */
public class PoseMessage extends BasicMessage {

  public PoseMessage(BasicMessage pinchMessage,
      float[] confidenceEstimates,
      Quat4f[][] jointRotations,
      Vector3f[][] jointTranslations,
      Point3f[][] fingerTips,
      float[][] handPoseConfidences) {
    super(pinchMessage);

    this.confidenceEstimates = confidenceEstimates;
    this.jointRotations = jointRotations;
    this.jointTranslations = jointTranslations;
    this.fingerTips = fingerTips;
    this.handPoseConfidences = handPoseConfidences;
  }

  protected float[] confidenceEstimates = new float[N_HANDS];
  
  public float getConfidenceEstimate(int hand) { return confidenceEstimates[hand]; }
  
  protected Quat4f[][] jointRotations = new Quat4f[N_HANDS][N_JOINTS];
  protected Vector3f[][] jointTranslations = new Vector3f[N_HANDS][N_JOINTS];
  
  public Matrix4f[] getJointFrames(int hand) {
    Matrix4f[] jointFrames = new Matrix4f[N_JOINTS];
    for (int i=0; i<N_JOINTS; i++) {
      jointFrames[i] = new Matrix4f(jointRotations[hand][i], jointTranslations[hand][i], 1);
    }
    return jointFrames;
  }
  
  protected Point3f[][] fingerTips = new Point3f[N_HANDS][N_FINGERS];
  
  public Point3f[] getFingerTips(int hand) { return fingerTips[hand]; }
  
  protected float[][] handPoseConfidences = new float[N_HANDS][N_POSES];
  
  public float[] getHandPoseConfidences(int hand) {return handPoseConfidences[hand]; }
  
  public String serialize() {
    StringBuffer buffer = new StringBuffer();
    buffer.append(super.serialize());
    
    for (int iHand=0; iHand<N_HANDS; iHand++) {
      buffer.append(' ');
      buffer.append(confidenceEstimates[iHand]);
      
      for (int jJoint=0; jJoint<N_JOINTS; jJoint++) {
        Quat4f q = jointRotations[iHand][jJoint];
        Vector3f t = jointTranslations[iHand][jJoint];
        
        buffer.append(String.format(Locale.US, " %f %f %f %f", q.x, q.y, q.z, q.w));
        buffer.append(String.format(Locale.US, " %f %f %f", t.x, t.y, t.z));
      }
      
      for (int jFingerTip=0; jFingerTip < N_FINGERS; jFingerTip++) {
        Point3f t = fingerTips[iHand][jFingerTip];
        buffer.append(String.format(Locale.US, " %f %f %f", t.x, t.y, t.z));
      }
    }

    /*
    for (int iHand=0; iHand<N_HANDS; iHand++) {
      for (int jPose=0; jPose < N_POSES; jPose++) {
        float f = handPoseConfidences[iHand][jPose];
        buffer.append(String.format(Locale.US, " %f", f));
      }
    }
    */
    
    return buffer.toString();
  }
  
  public static HandTrackingMessage deserialize(String data) {
    String[] strings = data.split(" ");
    ParseResult parseResult = parse(strings);
    
    float[] confidenceEstimates = new float[N_HANDS];
    Quat4f[][] jointRotations = new Quat4f[N_HANDS][N_JOINTS];
    Vector3f[][] jointTranslations = new Vector3f[N_HANDS][N_JOINTS];
    Point3f[][] fingerTips = new Point3f[N_HANDS][N_FINGERS];
    float[][] handPoseConfidences = new float[N_HANDS][N_POSES];
    
    int index = parseResult.parsed;
    
    for (int iHand=0; iHand<N_HANDS; iHand++) {
      confidenceEstimates[iHand] = Float.parseFloat(strings[index++]);
      
      for (int jJoint=0; jJoint<N_JOINTS; jJoint++) {
        Quat4f q = new Quat4f(Float.parseFloat(strings[index++]), Float.parseFloat(strings[index++]), Float.parseFloat(strings[index++]), Float.parseFloat(strings[index++]));
        Vector3f t = new Vector3f(Float.parseFloat(strings[index++]), Float.parseFloat(strings[index++]), Float.parseFloat(strings[index++]));
        
        jointRotations[iHand][jJoint] = q;
        jointTranslations[iHand][jJoint] = t;
      }
      
      for (int jFingerTip=0; jFingerTip < N_FINGERS; jFingerTip++) {
        fingerTips[iHand][jFingerTip] = new Point3f(
            Float.parseFloat(strings[index++]), 
            Float.parseFloat(strings[index++]),
            Float.parseFloat(strings[index++]));
      }
    }
    
    /*
    for (int iHand=0; iHand<N_HANDS; iHand++) {        
      for (int jPose=0; jPose<N_POSES; jPose++) {
        handPoseConfidences[iHand][jPose] = Float.parseFloat(strings[index++]);
      }
    }
    */
    
    return new PoseMessage(parseResult.message, confidenceEstimates, jointRotations, jointTranslations, fingerTips, handPoseConfidences);
  }

  public int getBestHandPose(int hand) {
    int bestPose = -1;
    float bestScore = 0;
    for(int i = 0; i < N_POSES; i++) {
      if(handPoseConfidences[hand][i] > bestScore) {
        bestScore = handPoseConfidences[hand][i];
        bestPose = i;
      }
    }
    return bestPose;
  }
  
  public int[] getBestHandPoseRanking(int hand) {
      int poseRanking[] = new int[N_POSES];
      int defaultRanking[] = new int[N_POSES];
      for(int iPose = 0; iPose < N_POSES; iPose++) {
          poseRanking[iPose] = iPose;
          defaultRanking[iPose] = iPose;
      }
      int lastBestPose = getBestHandPose(hand);
      if(lastBestPose == -1) return defaultRanking;
      
      float lastBestConfidence = handPoseConfidences[hand][lastBestPose];
      poseRanking[0] = lastBestPose;

      for(int iPose = 1; iPose < N_POSES; iPose++) {
          int nextBestPose = -1;
          float nextBestConfidence = 0;
          for(int i = 0; i < N_POSES; i++) {
              if(handPoseConfidences[hand][i] > nextBestConfidence && handPoseConfidences[hand][i] <= lastBestConfidence && iPose != lastBestPose) {
                  nextBestConfidence = handPoseConfidences[hand][i];
                  nextBestPose = i;
              }
          }
          if(nextBestPose == -1) return defaultRanking;
          poseRanking[iPose] = nextBestPose;
          lastBestPose = nextBestPose;
          lastBestConfidence = nextBestConfidence;
      }

      return poseRanking;
  }
}
