package com.mobilecomputing.src.Training.Persistence.threegears;


import javax.vecmath.Quat4f;
import javax.vecmath.Tuple3f;
import javax.vecmath.Vector3f;

/**
 * Base class for all hand-tracking messages / events. 
 */
public abstract class HandTrackingMessage {
  
  public static final int N_JOINTS = 17;
  
  public static final int N_FINGERS = 5;
  
  public static final int N_HANDS = 2;
  
  public static final int N_POSES = 7;
  
  public enum HandPose {
      HAND_CURLED(0),
      HAND_ELL(1),
      HAND_OKAY(2),
      HAND_PINCH(3),
      HAND_POINTING(4),
      HAND_RELAXEDOPEN(5),
      HAND_SPREAD(6);

      private int id;

      HandPose(int id) {
          this.id = id;
      }

      public int id() {
          return id;
      }
  }
  
  public enum Hand {
    LEFT(0),
    RIGHT(1),
    BOTH(2);
    
    private int id;
    
    Hand(int id) {
      this.id = id;
    }
    
    public int id() {
      return id;
    }

    public static Hand fromId(int id) {
      switch (id) {
      case 0: return LEFT;
      case 1: return RIGHT;
      case 2: return BOTH;
      }
      return null;
    }
    
    public Hand opposite() {
      if (id == 0) return RIGHT;
      if (id == 1) return LEFT;
      if (id == 2) return BOTH;
      return null; // can't get here
    }
  }
  
  public enum MessageType {
    /** The WelcomeMessage provides server and protocol versions */
    WELCOME, 

    /** The UserMessage provides the user profile name and skinning information of each hand */
    USER, 
    
    /** The PoseMessage provides full skeleton and finger-tip information of each hand */
    POSE, 
    
    // Pinch Messages
    PRESSED, 
    DRAGGED, 
    RELEASED, 
    MOVED, 

    // Bimanual Pinch Messages
    SIMULTANEOUSLY_PRESSED,
    INDIVIDUALLY_PRESSED,
    SIMULTANEOUSLY_RELEASED,
    INDIVIDUALLY_RELEASED, 
    DRAGGED_BIMANUAL,
    
    // Pointing Messages
    POINT,
    
    // Calibration Messages
    CALIBRATING
  }
  
  /**
   * Factory method that parses a HandTrackingMessage from a string
   * 
   * @param data
   * @return the parsed hand tacking message or null if parsing fails
   */
  public static HandTrackingMessage deserialize(String data) {
    // Read up until the first space
    if (data.indexOf(" ") == -1) return null;
    String firstToken = data.substring(0, data.indexOf(" "));
    
    try {
      switch (MessageType.valueOf(firstToken)) {
      case WELCOME: return WelcomeMessage.deserialize(data);
      case USER: return UserMessage.deserialize(data);
      case POSE: return PoseMessage.deserialize(data);
      case PRESSED: 
      case DRAGGED: 
      case RELEASED:  
      case MOVED: return PinchMessage.deserialize(data);
      case SIMULTANEOUSLY_PRESSED:
      case INDIVIDUALLY_PRESSED:
      case SIMULTANEOUSLY_RELEASED:
      case INDIVIDUALLY_RELEASED:
      case DRAGGED_BIMANUAL: return BimanualPinchMessage.deserialize(data);
      case POINT: return PointMessage.deserialize(data);
      case CALIBRATING: return CalibrationMessage.deserialize(data);
      }
    } catch (IllegalArgumentException e) {
      System.err.println("Couldn't parse message type: " + data);
    }
    return null;
  }

  /**
   * Structure holding the position, rotational frame and other information
   * relevant to the state of the hand.
   */
  public static class HandState {
    private Vector3f position;
    
    private Quat4f rotation;
    
    private int clickCount;
    
    public HandState(Tuple3f position, Quat4f rotation, int clickCount) {
      this.position = new Vector3f(position);
      this.rotation = new Quat4f(rotation);
      this.clickCount = clickCount;
    }
    
    public Vector3f getPosition() { return position; }
    
    public Quat4f getRotation() { return rotation; }
    
    public int getClickCount() { return clickCount; }
  }
  
  /**
   * @return the type of the message
   */
  public abstract MessageType getType();
  
  public abstract String serialize();
}
