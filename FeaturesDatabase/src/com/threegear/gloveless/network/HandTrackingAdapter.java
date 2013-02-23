package com.threegear.gloveless.network;

/**
 * Convenience class for selective implementation of HandTrackingListener
 * @see HandTrackingListener
 */
public class HandTrackingAdapter implements HandTrackingListener {

  @Override
  public void handleEvent(HandTrackingMessage message) { }

  @Override
  public void handleConnectionClosed() { }
}
