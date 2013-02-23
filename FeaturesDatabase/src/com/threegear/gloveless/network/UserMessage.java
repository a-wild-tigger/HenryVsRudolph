package com.threegear.gloveless.network;

import java.util.Locale;

import javax.vecmath.Matrix4f;
import javax.vecmath.Point3f;
import javax.vecmath.Quat4f;
import javax.vecmath.Vector3f;

/**
 * Message that exposes the user name and skinning information for the user's
 * calibrated hands.  The skinning used by our system is "Linear blend skinning"
 * also known as "Smooth skinning."
 * 
 * http://graphics.ucsd.edu/courses/cse169_w05/3-Skin.htm
 */
public class UserMessage extends HandTrackingMessage {

  private String userProfileName;
  
  public String getUserProfileName() { return userProfileName; }
  
  private Point3f[][] restPositions = new Point3f[N_HANDS][];
  
  public Point3f[] getRestPositions(int hand) { return restPositions[hand]; } 
  
  private int[][][] triangles = new int[N_HANDS][][];
  
  public int[][] getTriangles(int hand) { return triangles[hand]; } 
  
  private int[][][] skinningIndices = new int[N_HANDS][][];
  
  public int[][] getSkinningIndices(int hand) { return skinningIndices[hand]; }
  
  private float[][][] skinningWeights = new float[N_HANDS][][];

  public float[][] getSkinningWeights(int hand) { return skinningWeights[hand]; }
  
  private Quat4f[][] restJointRotations = new Quat4f[N_HANDS][N_JOINTS];
  
  private Vector3f[][] restJointTranslations = new Vector3f[N_HANDS][N_JOINTS];
  
  public Matrix4f[] getRestJointFrames(int hand) { 
    Matrix4f[] jointFrames = new Matrix4f[N_JOINTS];
    for (int i=0; i<N_JOINTS; i++) {
      jointFrames[i] = new Matrix4f(restJointRotations[hand][i], restJointTranslations[hand][i], 1);
    }
    return jointFrames;
  }

  @Override
  public MessageType getType() { return MessageType.USER; }

  public UserMessage(String userProfileName, Point3f[][] restPositions,
      int[][][] triangles, int[][][] skinningIndices,
      float[][][] skinningWeights, Quat4f[][] restJointRotations, Vector3f[][] restJointTranslations) {
    this.userProfileName = userProfileName;
    this.restPositions = restPositions;
    this.triangles = triangles;
    this.skinningIndices = skinningIndices;
    this.skinningWeights = skinningWeights;
    this.restJointRotations = restJointRotations;
    this.restJointTranslations = restJointTranslations;
  }
  
  @Override
  public String serialize() {
    StringBuffer buffer = new StringBuffer();
    buffer.append(String.format(Locale.US, "%s User: %s", getType().toString(), userProfileName));
    
    for (int iHand=0; iHand<N_HANDS; iHand++) {
      buffer.append(String.format(Locale.US, " Hand: %d", iHand));
      buffer.append(String.format(Locale.US, " Rest-Positions: %d", restPositions[iHand].length));
      for (Point3f p : restPositions[iHand]) {
        buffer.append(String.format(Locale.US, " %f %f %f", p.x, p.y, p.z));
      }
      
      buffer.append(String.format(Locale.US, " Triangles: %d", triangles[iHand].length));
      for (int[] p : triangles[iHand]) {
        buffer.append(String.format(Locale.US, " %d %d %d", p[0], p[1], p[2]));
      }
      
      buffer.append(" Skinning-Weights:");
      for (int i=0; i<skinningIndices[iHand].length; i++) {
        int[] indices = skinningIndices[iHand][i];
        float[] weights = skinningWeights[iHand][i];
        
        buffer.append(' ');
        buffer.append(indices.length);
        
        for (int j=0; j<indices.length; j++) {
          buffer.append(' ');
          buffer.append(indices[j]);
          buffer.append(' ');
          buffer.append(weights[j]);
        }
      }
      
      buffer.append(" Rest-Joint-Frames:");
      for (int jJoint=0; jJoint<N_JOINTS; jJoint++) {
        Quat4f q = restJointRotations[iHand][jJoint];
        Vector3f t = restJointTranslations[iHand][jJoint];
        buffer.append(String.format(Locale.US, " %f %f %f %f", q.x, q.y, q.z, q.w));
        buffer.append(String.format(Locale.US, " %f %f %f", t.x, t.y, t.z));
      }
    }
    
    return buffer.toString();
  }
  
  public static HandTrackingMessage deserialize(String data) {
    String[] split = data.split(" ");
    int index = 0;
    if (!split[index++].equals(MessageType.USER.toString()))
      throw new RuntimeException("Couldn't parse User message from message: "
          + split[index]);
    if (!split[index++].equals("User:"))
      throw new RuntimeException("Parse error for User message at index: " + index);

    String userProfileName = split[index++];
    Point3f[][] restPositions = new Point3f[N_HANDS][];
    int[][][] triangles = new int[N_HANDS][][];
    int[][][] skinningIndices = new int[N_HANDS][][];
    float[][][] skinningWeights = new float[N_HANDS][][];
    Quat4f[][] restJointRotations = new Quat4f[N_HANDS][N_JOINTS];
    Vector3f[][] restJointTranslations = new Vector3f[N_HANDS][N_JOINTS];
    
    for (int iHand=0; iHand<N_HANDS; iHand++) {
      if (!split[index++].equals("Hand:"))
        throw new RuntimeException("Parse error for User message at index: " + index);
      
      if (Integer.parseInt(split[index++]) != iHand)
        throw new RuntimeException("Parse error for User message at index: " + index);

      if (!split[index++].equals("Rest-Positions:"))
        throw new RuntimeException("Parse error for User message at index: " + index);
      
      restPositions[iHand] = new Point3f[Integer.parseInt(split[index++])];
      skinningIndices[iHand] = new int[restPositions[iHand].length][];
      skinningWeights[iHand] = new float[restPositions[iHand].length][];
      
      for (int i=0; i<restPositions[iHand].length; i++) {
        float x = Float.parseFloat(split[index++]);
        float y = Float.parseFloat(split[index++]);
        float z = Float.parseFloat(split[index++]);
        restPositions[iHand][i] = new Point3f(x,y,z);
      }
      
      if (!split[index++].equals("Triangles:"))
        throw new RuntimeException("Parse error for User message at index: " + index);
      
      triangles[iHand] = new int[Integer.parseInt(split[index++])][3];
      
      for (int i=0; i<triangles[iHand].length; i++) {
        for (int j=0; j<triangles[iHand][i].length; j++) {
          triangles[iHand][i][j] = Integer.parseInt(split[index++]);
        }
      }
      
      if (!split[index++].equals("Skinning-Weights:"))
        throw new RuntimeException("Parse error for User message at index: " + index);
      
      for (int i=0; i<skinningIndices[iHand].length; i++) {
        int nInfluences = Integer.parseInt(split[index++]);
        skinningIndices[iHand][i] = new int[nInfluences];
        skinningWeights[iHand][i] = new float[nInfluences];
        for (int j=0; j<nInfluences; j++) {
          skinningIndices[iHand][i][j] = Integer.parseInt(split[index++]);
          skinningWeights[iHand][i][j] = Float.parseFloat(split[index++]);
        }
      }
      
      if (!split[index++].equals("Rest-Joint-Frames:"))
        throw new RuntimeException("Parse error for User message at index: " + index);

      for (int jJoint=0; jJoint<N_JOINTS; jJoint++) {
        Quat4f q = new Quat4f(
            Float.parseFloat(split[index++]), 
            Float.parseFloat(split[index++]), 
            Float.parseFloat(split[index++]), 
            Float.parseFloat(split[index++]));
        Vector3f t = new Vector3f(
            Float.parseFloat(split[index++]), 
            Float.parseFloat(split[index++]), 
            Float.parseFloat(split[index++]));

        restJointRotations[iHand][jJoint] = q;
        restJointTranslations[iHand][jJoint] = t;
      }
    }
    return new UserMessage(userProfileName, restPositions, triangles,
        skinningIndices, skinningWeights, restJointRotations,
        restJointTranslations);
  }

}
