package com.threegear.gloveless.network;

import javax.vecmath.Quat4f;
import javax.vecmath.Tuple3f;

/**
 * Messages relating to simultaneous or individual pinching
 */
public class BimanualPinchMessage extends BasicMessage {

  protected Hand hand;
  
  public Hand getHand() { return hand; }
  
  protected BimanualPinchMessage(BasicMessage message, Hand hand) {
    super(message);
    this.hand = hand;
  }
  
  public BimanualPinchMessage(MessageType type, Hand hand, Tuple3f positionLeft,
      Quat4f rotationLeft, int clickCountLeft, Tuple3f positionRight,
      Quat4f rotationRight, int clickCountRight) {
    super(type, positionLeft, rotationLeft, clickCountLeft,
        positionRight, rotationRight, clickCountRight);
    this.hand = hand;
  }

  public String serialize() {
    StringBuffer buffer = new StringBuffer();
    buffer.append(super.serialize());
    buffer.append(' ');
    buffer.append(hand.toString());
    return buffer.toString();
  }
  
  public static HandTrackingMessage deserialize(String data) {
    String[] result = data.split(" ");
    ParseResult parseResult = parse(result);
    return new BimanualPinchMessage(parseResult.message, 
        Hand.valueOf(result[parseResult.parsed]));
  }
}
