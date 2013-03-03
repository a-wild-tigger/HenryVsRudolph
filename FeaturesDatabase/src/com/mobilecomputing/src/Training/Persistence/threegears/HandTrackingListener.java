package com.mobilecomputing.src.Training.Persistence.threegears;

/**
 * A listener interface for receiving hand update events.
 */
public interface HandTrackingListener {
  /**
   * Invoked when a hand tracking event is received.
   * @param message
   */
  public void handleEvent(HandTrackingMessage message);

  /**
   * Invoked when the connection to the server is lost
   */
  public void handleConnectionClosed();
}
