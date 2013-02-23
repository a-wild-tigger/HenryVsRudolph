package com.threegear.gloveless.network;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.Socket;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

/**
 * Client for communicating with a hand tracking server. Uses a callback model
 * to send events to registered listeners.
 * 
 */
public class HandTrackingClient implements Runnable {

  public static final String DEFAULT_HOST = "127.0.0.1";
  
  public static final int DEFAULT_PORT = 1988;
  
  public static final Charset UTF8 = Charset.forName("UTF-8");
  
  private Socket socket;
  
  private InputStream input;
  
  private boolean stopped;
  
  private List<HandTrackingListener> listeners = new ArrayList<HandTrackingListener>();

  /**
   * Launches a thread that connects to the default server and port, and passes
   * received events to the registered listeners.
   * 
   * @throws IOException
   */
  public void connect() throws IOException {
    connect(DEFAULT_HOST, DEFAULT_PORT);
  }
  
  /**
   * Launches a thread that connects to the specified server and passes received
   * events to the registered listeners.
   * 
   * @param hostname
   * @param port
   * @throws IOException
   */
  public void connect(String hostname, int port) throws IOException {
    try {
      socket = new Socket(hostname, port);
      input = socket.getInputStream();
    } catch (IOException ioe) {
      throw new IOException(String.format("%n" +
          "*******************************************************************************%n" +
          "ERROR: Could not connect to the hand-tracking server.  Make sure you've started%n" +
          "the hand-tracking server with something like: %n" +
          "%n" +
          "  handdriver.bat <your profile name>%n"+
          "%n" +
          "Original message: " + ioe.getMessage() + "%n" +
          "*******************************************************************************%n"));
    }
    stopped = false;

    new Thread(this).start();
  }
  
  /**
   * Stop listening for events
   */
  public void stop() {
    stopped = true;
  }
  
  /**
   * Registers the given listener with this client
   * 
   * @param listener
   */
  public void addListener(HandTrackingListener listener) {
    listeners.add(listener);
  }
  
  @Override
  public void run() {
    BufferedReader reader = new BufferedReader(new InputStreamReader(input, HandTrackingClient.UTF8));
    String line;
    try {
      while (!stopped && (line = reader.readLine()) != null) {
        HandTrackingMessage msg = HandTrackingMessage.deserialize(line);
        // if we can't parse this line, ignore it
        if (msg == null) continue;
        
        for (HandTrackingListener listener : listeners) {
          listener.handleEvent(msg);
        }
      }
      socket.close();
    } catch (IOException e) {
      e.printStackTrace();
    }
    
    // Tell all the listeners we've stopped
    for (HandTrackingListener l : listeners) l.handleConnectionClosed();
  }
}
