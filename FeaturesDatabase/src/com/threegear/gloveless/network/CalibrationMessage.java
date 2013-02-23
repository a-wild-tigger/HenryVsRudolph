package com.threegear.gloveless.network;

import java.util.Locale;

public class CalibrationMessage extends HandTrackingMessage {
  
  private float completed;
  
  public float getCompleted() { return completed; }
  
  private float quality;
  
  public float getQuality() { return quality; }
  
  public CalibrationMessage(float completed, float quality) {
    this.completed = completed;
    this.quality = quality;
  }

  @Override
  public MessageType getType() {
    return MessageType.CALIBRATING;
  }

  @Override
  public String serialize() {
    return String.format(Locale.US, "%s Completed: %f Quality: %f", completed, quality);
  }
  
  public static CalibrationMessage deserialize(String data) {
    String[] split = data.split(" ");
    if (!(split.length == 5) ||
        !split[0].equals(MessageType.CALIBRATING.toString()) ||
        !split[1].equals("Completed:") ||
        !split[3].equals("Quality:")) 
      throw new RuntimeException("Couldn't parse message: " + data);
    
    return new CalibrationMessage(Float.parseFloat(split[2]), Float.parseFloat(split[4]));
  }
}
