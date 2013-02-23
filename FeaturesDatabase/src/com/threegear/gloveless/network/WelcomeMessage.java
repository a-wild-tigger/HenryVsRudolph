package com.threegear.gloveless.network;

import java.util.Locale;

/**
 * A message sent upon connecting to the server, indicating the server and
 * protocol version.
 */
public class WelcomeMessage extends HandTrackingMessage {
  private String serverVersion;
  
  public String getServerVersion() { return serverVersion; }
  
  private String protocolVersion;
  
  public String getProtocolVersion() { return protocolVersion; }
  
  public WelcomeMessage(String serverVersion, String protocolVersion) {
    this.serverVersion = serverVersion;
    this.protocolVersion = protocolVersion;
  }

  @Override
  public MessageType getType() { return MessageType.WELCOME; }

  @Override
  public String serialize() {
    return String.format(Locale.US, "%s Server-Version: %s Protocol-Version: %s",
        getType(), getServerVersion(), getProtocolVersion());
  }
  
  public static WelcomeMessage deserialize(String data) {
    String[] split = data.split(" ");
    if (!(split.length == 5) ||
        !split[0].equals(MessageType.WELCOME.toString()) ||
        !split[1].equals("Server-Version:") ||
        !split[3].equals("Protocol-Version:")) 
      throw new RuntimeException("Couldn't parse message: " + data);
    
    return new WelcomeMessage(split[2], split[4]);
  }
}
