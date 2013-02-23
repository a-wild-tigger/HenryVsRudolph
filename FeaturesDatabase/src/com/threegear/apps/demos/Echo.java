package com.threegear.apps.demos;

import java.io.IOException;

import com.threegear.gloveless.network.HandTrackingAdapter;
import com.threegear.gloveless.network.HandTrackingClient;
import com.threegear.gloveless.network.HandTrackingMessage;
import com.threegear.gloveless.network.PinchMessage;

public class Echo {
  
  public static void main(String[] args) throws IOException {
    HandTrackingClient client = new HandTrackingClient();
    client.addListener(new HandTrackingAdapter() {
      @Override
      public void handleEvent(HandTrackingMessage baseMessage) {
        if (baseMessage instanceof PinchMessage) {
          PinchMessage message = (PinchMessage) baseMessage;
          System.out.printf("Received event %s for hand %s at position %s\n", 
              message.getType(),
              message.getHand(),
              message.getHandState(message.getHand().id()).getPosition());
        }
      }
    });
    client.connect();
  }
}
