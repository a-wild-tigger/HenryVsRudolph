package com.threegear.apps.demos;

import static org.lwjgl.opengl.GL11.GL_COLOR_BUFFER_BIT;
import static org.lwjgl.opengl.GL11.GL_COLOR_MATERIAL;
import static org.lwjgl.opengl.GL11.GL_CULL_FACE;
import static org.lwjgl.opengl.GL11.GL_DEPTH_BUFFER_BIT;
import static org.lwjgl.opengl.GL11.GL_DEPTH_TEST;
import static org.lwjgl.opengl.GL11.GL_LIGHT0;
import static org.lwjgl.opengl.GL11.GL_MODELVIEW;
import static org.lwjgl.opengl.GL11.GL_PROJECTION;
import static org.lwjgl.opengl.GL11.GL_STENCIL_BUFFER_BIT;
import static org.lwjgl.opengl.GL11.glClear;
import static org.lwjgl.opengl.GL11.glColor3f;
import static org.lwjgl.opengl.GL11.glEnable;
import static org.lwjgl.opengl.GL11.glMatrixMode;
import static org.lwjgl.opengl.GL11.glViewport;

import javax.vecmath.Quat4f;
import javax.vecmath.Vector3f;

import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.DisplayMode;
import org.lwjgl.util.glu.GLU;

import com.threegear.gloveless.network.HandTrackingAdapter;
import com.threegear.gloveless.network.HandTrackingClient;
import com.threegear.gloveless.network.HandTrackingMessage;
import com.threegear.gloveless.network.PinchMessage;
import com.threegear.gloveless.network.HandTrackingMessage.Hand;
import com.threegear.gloveless.network.HandTrackingMessage.HandState;
import com.threegear.gloveless.network.HandTrackingMessage.MessageType;

/**
 * Example application that shows the hand positions and press-states in a 3D
 * virtual space. Uses the Light Weight Java Game Library for windows / OpenGL
 * support.
 */
public class Simple3D extends HandTrackingAdapter {

  private static final int DISPLAY_FRAMERATE = 60;

  private boolean finished;
  
  private HandTrackingClient client;

  // Cache the hand positions, rotations and contact state for rendering
  private Vector3f[] handPositions = new Vector3f[] {new Vector3f(), new Vector3f()};
  private Quat4f[] handRotations = new Quat4f[] {new Quat4f(), new Quat4f()};
  private boolean[] handPressed = new boolean[] { false, false };
  
  public static void main(String[] args) {
    Simple3D demo = new Simple3D();
    try {
      demo.init();
      demo.run();
    } catch (Exception e) {
      e.printStackTrace(System.err);
    } finally {
      demo.cleanup();
    }
  }

  private void init() throws Exception {
    // Initialize the client listener and connect to server
    client = new HandTrackingClient();
    client.addListener(this);
    client.connect();
    
    // Setup the window frame
    Display.setTitle("Simple 3D Demo");
    int width = 900;
    int height = 600;
    Display.setDisplayMode(new DisplayMode(width, height));
    Display.create();

    // Setup the OpenGL state.  Our world is measured in millimeters 
    glMatrixMode(GL_PROJECTION);
    GLU.gluPerspective(45, width / (float) height, 10, 10000);
    glMatrixMode(GL_MODELVIEW);
    GLU.gluLookAt(0, 250, 350, 0, 125, 0, 0, 1, 0);
    glViewport(0, 0, width, height);
    
    glEnable(GL_LIGHT0);
    glEnable(GL_COLOR_MATERIAL);
    glEnable(GL_CULL_FACE);
    glEnable(GL_DEPTH_TEST);
  }

  /**
   * The "main loop"
   */
  private void run() {
    while (!finished) {
      // Always call Window.update(), all the time
      Display.update();

      if (Display.isCloseRequested()) {
        // Check for O/S close requests
        finished = true;
      } else {
        render();
        Display.sync(DISPLAY_FRAMERATE);
      }
    }
  }

  private void cleanup() {
    client.stop();
    Display.destroy();
  }

  /**
   * Draw a ground plane and cursors for each hand
   */
  private synchronized void render() {
    glClear(GL_COLOR_BUFFER_BIT | GL_STENCIL_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
    glColor3f(1,1,1);
    
    WorkspaceRenderingHelpers.drawGroundPlane(50);

    // Show the cursor in yellow if pressing
    if (handPressed[0]) glColor3f(1,1,0);
    else glColor3f(1,0,0);
    WorkspaceRenderingHelpers.drawCursorPoint(handPositions[0], 10);
    WorkspaceRenderingHelpers.drawCursorFrame(handPositions[0], handRotations[0], 30);
    
    if (handPressed[1]) glColor3f(1,1,0);
    else glColor3f(0,1,0);
    WorkspaceRenderingHelpers.drawCursorPoint(handPositions[1], 10);
    WorkspaceRenderingHelpers.drawCursorFrame(handPositions[1], handRotations[1], 30);
  }

  @Override
  public synchronized void handleEvent(HandTrackingMessage baseMessage) {
    if (baseMessage instanceof PinchMessage) {
      PinchMessage message = (PinchMessage) baseMessage;
      // Cache the hand positions, rotations and contact state for rendering
      if (message.getType() == MessageType.MOVED) {
        HandState referencedHand = (message.getHand() == Hand.LEFT) ? 
            message.getHandState(0) : message.getHandState(1);
        
        handPositions[message.getHand().id()].set(referencedHand.getPosition());
        handRotations[message.getHand().id()].set(referencedHand.getRotation());
      }
      if (message.getType() == MessageType.PRESSED) {
        handPressed[message.getHand().id()] = true;
      }
      if (message.getType() == MessageType.RELEASED) {
        handPressed[message.getHand().id()] = false;
      }
    }
  }
}
