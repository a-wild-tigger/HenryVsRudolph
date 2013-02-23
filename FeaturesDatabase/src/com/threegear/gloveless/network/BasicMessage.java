package com.threegear.gloveless.network;

import java.util.Locale;

import javax.vecmath.Quat4f;
import javax.vecmath.Tuple3f;
import javax.vecmath.Vector3f;

/**
 * The most basic form of the hand tracking message includes a type, which hand
 * was updated, and the position and rotational frame of each hand. The hand
 * rotation is specified as a quaternion (x,y,z,w).
 */
public class BasicMessage extends HandTrackingMessage {

  protected HandState[] hands = new HandState[N_HANDS];
  
  public HandState getHandState(int hand) { return hands[hand]; }
  
  protected MessageType type;
  
  @Override
  public MessageType getType() { return type; }
  
  /**
   * @param type
   * @param positionLeft
   * @param rotationLeft
   * @param clickCountLeft
   * @param positionRight
   * @param rotationRight
   * @param clickCountRight
   */
  protected BasicMessage(MessageType type, 
      Tuple3f positionLeft, Quat4f rotationLeft, int clickCountLeft, 
      Tuple3f positionRight, Quat4f rotationRight, int clickCountRight) {
    this.type = type;
    
    this.hands = new HandState[] { 
        new HandState(positionLeft, rotationLeft, clickCountLeft),
        new HandState(positionRight, rotationRight, clickCountRight) };
  }
  
  /**
   * @param message
   */
  protected BasicMessage(BasicMessage message) {
    this.type = message.getType();
    
    this.hands = new HandState[] { 
        new HandState(
            message.getHandState(0).getPosition(), 
            message.getHandState(0).getRotation(), 
            message.getHandState(0).getClickCount()),
        new HandState(
            message.getHandState(1).getPosition(), 
            message.getHandState(1).getRotation(), 
            message.getHandState(1).getClickCount()) };
  }
  
  public String serialize() {
    StringBuffer buffer = new StringBuffer();
    buffer.append(String.format(Locale.US, "%s %f %f %f %f %f %f %f %d %f %f %f %f %f %f %f %d", 
        type.toString(), 
        hands[0].getPosition().x, hands[0].getPosition().y, hands[0].getPosition().z,
        hands[0].getRotation().x, hands[0].getRotation().y, hands[0].getRotation().z, hands[0].getRotation().w, 
        hands[0].getClickCount(),
        hands[1].getPosition().x, hands[1].getPosition().y, hands[1].getPosition().z,
        hands[1].getRotation().x, hands[1].getRotation().y, hands[1].getRotation().z, hands[1].getRotation().w, 
        hands[1].getClickCount()));
    return buffer.toString();
  }
  
  /**
   * @param data Converts the UTF-8 formatted data into a message
   * @return a new message constructed from the data
   */
  public static HandTrackingMessage deserialize(String data) {
    String[] result = data.split(" ");
    return parse(result).message;
  }
  
  public static class ParseResult {
    public BasicMessage message;
    public int parsed;
  }
  
  public static ParseResult parse(String[] result) {
    MessageType type = MessageType.valueOf(result[0]);
    
    Vector3f positionLeft = new Vector3f(
        Float.parseFloat(result[1]),
        Float.parseFloat(result[2]),
        Float.parseFloat(result[3]));
    
    Quat4f rotationLeft = new Quat4f(
        Float.parseFloat(result[4]),
        Float.parseFloat(result[5]),
        Float.parseFloat(result[6]),
        Float.parseFloat(result[7]));
    
    int ccLeft = Integer.parseInt(result[8]);
    
    Vector3f positionRight = new Vector3f(
        Float.parseFloat(result[9]),
        Float.parseFloat(result[10]),
        Float.parseFloat(result[11]));
    
    Quat4f rotationRight = new Quat4f(
        Float.parseFloat(result[12]),
        Float.parseFloat(result[13]),
        Float.parseFloat(result[14]),
        Float.parseFloat(result[15]));
    
    int ccRight = Integer.parseInt(result[16].trim());
    
    ParseResult parseResult = new ParseResult();
    parseResult.message = new BasicMessage(type,  
        positionLeft, rotationLeft, ccLeft,
        positionRight, rotationRight, ccRight);
    parseResult.parsed = 17;
    return parseResult;
  }
}
