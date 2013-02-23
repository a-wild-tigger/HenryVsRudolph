package com.threegear.apps.demos;

import java.awt.AWTException;
import java.awt.Dimension;
import java.awt.Robot;
import java.awt.Toolkit;
import java.awt.event.InputEvent;

import com.threegear.gloveless.network.HandTrackingAdapter;
import com.threegear.gloveless.network.HandTrackingClient;
import com.threegear.gloveless.network.HandTrackingMessage;
import com.threegear.gloveless.network.HandTrackingMessage.Hand;
import com.threegear.gloveless.network.HandTrackingMessage.HandState;
import com.threegear.gloveless.network.PinchMessage;

/**
 * Example 2D application that drives the mouse cursor with hand-tracking using
 * Java's Robot utility
 *
 * @see java.awt.Robot
 */
public class Simple2D extends HandTrackingAdapter {
  
  private Robot robot;
  
  private Dimension screenSize;
  
  // Height in mm above which the mouse will become controlled by the right hand 
  public static final float ACTIVE_HEIGHT_MM = 45;
  
  public Simple2D() throws AWTException {
    Toolkit toolkit = Toolkit.getDefaultToolkit();
    screenSize = toolkit.getScreenSize();
    robot = new Robot();
  }
  
  public static void main(String[] args) throws Exception {
    HandTrackingClient client = new HandTrackingClient();
    client.addListener(new Simple2D());
    client.connect();
  }

  @Override
  public void handleEvent(HandTrackingMessage baseMessage) {
    if (baseMessage instanceof PinchMessage) {
      PinchMessage message = (PinchMessage) baseMessage;
      
      // Ignore the left hand
      if (message.getHand() != Hand.RIGHT) return;
      HandState hand = message.getHandState(Hand.RIGHT.id());
      // Ignore the hand when it's close to the desk
      if (hand.getPosition().y < ACTIVE_HEIGHT_MM) return;
      
      switch (message.getType()) {
      case MOVED:
        // Move the mouse cursor to follow the hand
        float scale = 3.25f;
        int x = Math.round(scale * (hand.getPosition().x     ) + (int) screenSize.getWidth()  / 2);
        int z = Math.round(scale * (hand.getPosition().z - 50) + (int) screenSize.getHeight() / 2);
        robot.mouseMove(x, z);
        break;
      case PRESSED:
        // Connect hand press and release events to the mouse
        robot.mousePress(InputEvent.BUTTON1_MASK);
        if (hand.getClickCount() == 2) robot.mousePress(InputEvent.BUTTON1_MASK); // double-click
        break;
      case RELEASED:
        robot.mouseRelease(InputEvent.BUTTON1_MASK);
        break;
      default: break;
      }
    }
  }
}
