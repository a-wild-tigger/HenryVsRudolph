package com.threegear.gloveless.network;

import java.util.Locale;

import javax.vecmath.Point3f;
import javax.vecmath.Vector3f;

/**
 * Message for capturing pointing gestures.  
 */
public class PointMessage extends HandTrackingMessage {

  private Hand hand;
  private Vector3f pointDirection;
  private Point3f pointEnd;
  private float confidence;

  @Override
  public MessageType getType()   { return MessageType.POINT; }
  
  /** Gets the direction we think the user is pointing in */
  public Vector3f getDirection() { return pointDirection; }
  
  /** This is the current estimate of the end of the finger */
  public Point3f getEnd()        { return pointEnd; }
  
  /** Which hand is pointing */
  public Hand getHand()          { return hand; }
  
  /**
   * @return Our confidence in the estimate.  At the moment this is always binary;
   * 1.0 means full confidence and 0.0 means no confidence.
   */
  public float getConfidence()   { return confidence; }

  public PointMessage(Hand hand, Vector3f pointDir, Point3f pointEnd, float confidence) {
    this.hand = hand;
    this.pointDirection = pointDir;
    this.pointEnd = pointEnd;
    this.confidence = confidence;
  }
  
  @Override
  public String serialize() {
    StringBuffer buffer = new StringBuffer();
    buffer.append(String.format(Locale.US, "%s %s %f %f %f %f %f %f %f", 
        getType().toString(), 
        hand.toString(),
        pointDirection.x, pointDirection.y, pointDirection.z, 
        pointEnd.x, pointEnd.y, pointEnd.z, 
        confidence));
    return buffer.toString();
  }

  public static HandTrackingMessage deserialize(String data) {
    String[] split = data.split(" ");
    int index = 0;
    if (!split[index++].equals(MessageType.POINT.toString()))
      throw new RuntimeException("Couldn't parse POINT message from message: "
          + split[index]);
    
    Hand hand = Hand.valueOf(split[index++]);
    
    Vector3f dir = new Vector3f();
    dir.x = Float.parseFloat(split[index++]);
    dir.y = Float.parseFloat(split[index++]);
    dir.z = Float.parseFloat(split[index++]);

    Point3f end = new Point3f();
    end.x = Float.parseFloat(split[index++]);
    end.y = Float.parseFloat(split[index++]);
    end.z = Float.parseFloat(split[index++]);
    
    float confidence = Float.parseFloat(split[index++]);

    return new PointMessage(hand, dir, end, confidence);
  }
}
