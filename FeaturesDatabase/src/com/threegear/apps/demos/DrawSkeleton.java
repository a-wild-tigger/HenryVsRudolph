package com.threegear.apps.demos;

import static org.lwjgl.opengl.GL11.GL_COLOR_BUFFER_BIT;
import static org.lwjgl.opengl.GL11.GL_COLOR_MATERIAL;
import static org.lwjgl.opengl.GL11.GL_CULL_FACE;
import static org.lwjgl.opengl.GL11.GL_DEPTH_BUFFER_BIT;
import static org.lwjgl.opengl.GL11.GL_DEPTH_TEST;
import static org.lwjgl.opengl.GL11.GL_LIGHT0;
import static org.lwjgl.opengl.GL11.GL_LINES;
import static org.lwjgl.opengl.GL11.GL_MODELVIEW;
import static org.lwjgl.opengl.GL11.GL_POINTS;
import static org.lwjgl.opengl.GL11.GL_PROJECTION;
import static org.lwjgl.opengl.GL11.GL_STENCIL_BUFFER_BIT;
import static org.lwjgl.opengl.GL11.glBegin;
import static org.lwjgl.opengl.GL11.glClear;
import static org.lwjgl.opengl.GL11.glColor3f;
import static org.lwjgl.opengl.GL11.glEnable;
import static org.lwjgl.opengl.GL11.glEnd;
import static org.lwjgl.opengl.GL11.glMatrixMode;
import static org.lwjgl.opengl.GL11.glPointSize;
import static org.lwjgl.opengl.GL11.glVertex3f;
import static org.lwjgl.opengl.GL11.glViewport;

import javax.vecmath.Matrix4f;
import javax.vecmath.Point3f;

import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.DisplayMode;
import org.lwjgl.util.glu.GLU;

import com.threegear.gloveless.network.HandTrackingAdapter;
import com.threegear.gloveless.network.HandTrackingClient;
import com.threegear.gloveless.network.HandTrackingMessage;
import com.threegear.gloveless.network.HandTrackingMessage.MessageType;
import com.threegear.gloveless.network.PoseMessage;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class DrawSkeleton extends HandTrackingAdapter {
  private static final int DISPLAY_FRAMERATE = 60;
  private boolean finished;
  private HandTrackingClient client;

  private List<PoseMessage> theDataSet = new ArrayList<PoseMessage>();

  // Cache the hand positions, rotations and contact state for rendering
  private Matrix4f[][] jointFrames = new Matrix4f[HandTrackingMessage.N_HANDS][HandTrackingMessage.N_JOINTS];
  private Point3f[][] fingerTips = new Point3f[HandTrackingMessage.N_HANDS][HandTrackingMessage.N_FINGERS];
  
  public DrawSkeleton() {
    for (int i=0; i<jointFrames.length; i++) 
      for (int j=0; j<jointFrames[0].length; j++) {
        jointFrames[i][j] = new Matrix4f();
        jointFrames[i][j].setIdentity();
      }
    
    for (int i=0; i<fingerTips.length; i++) 
      for (int j=0; j<fingerTips[0].length; j++) {
        fingerTips[i][j] = new Point3f();
      }
  }
  
  public static void main(String[] args) throws IOException {
    DrawSkeleton demo = new DrawSkeleton();
    try {
      demo.init();
      demo.run();
    } catch (Exception e) {
      e.printStackTrace(System.err);
    } finally {
      demo.cleanup();
    }
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

  private void cleanup() throws IOException {
    client.stop();
    Display.destroy();


      String content = "This is the content to write into file";

      File file = new File("testoutfile.txt");

      // if file doesnt exists, then create it
      if (!file.exists()) {
          file.createNewFile();
      }

      FileWriter fw = new FileWriter(file.getAbsoluteFile());
      BufferedWriter bw = new BufferedWriter(fw);

      for(int i = 0; i!= theDataSet.size(); i++) {
        bw.write(theDataSet.get(i).serialize());
          bw.newLine();
      }

      bw.close();

      System.out.println("Done " + Integer.toString(theDataSet.size()));

  }


    private void init() throws Exception {
        // Initialize the client listener and connect to server
        client = new HandTrackingClient();
        client.addListener(this);
        client.connect();

        // Setup the window frame
        Display.setTitle("Draw Skeleton Demo");
        int width = 900;
        int height = 600;
        Display.setDisplayMode(new DisplayMode(width, height));
        Display.create();

        // Setup the OpenGL state.  Our world is measured in millimeters
        glMatrixMode(GL_PROJECTION);
        GLU.gluPerspective(55, width / (float) height, 10, 10000);
        glMatrixMode(GL_MODELVIEW);
        GLU.gluLookAt(0, 450, 550, 0, 125, 0, 0, 1, 0);
        glViewport(0, 0, width, height);

        glEnable(GL_LIGHT0);
        glEnable(GL_COLOR_MATERIAL);
        glEnable(GL_CULL_FACE);
        glEnable(GL_DEPTH_TEST);
    }

  /**
   * Draw a ground plane and cursors for each hand
   */
  private synchronized void render() {
    glClear(GL_COLOR_BUFFER_BIT | GL_STENCIL_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
    glColor3f(1,1,1);
    
    WorkspaceRenderingHelpers.drawGroundPlane(50);

    for (int iHand=0; iHand<2; iHand++) {
      drawSkeleton(jointFrames[iHand], fingerTips[iHand]);
    }

    glEnd();
  }
  

  
  @Override
  public synchronized void handleEvent(HandTrackingMessage rawMessage) {
    // Cache the coordinate frames and finger tips of the skeleton for rendering
    if (rawMessage.getType() == MessageType.POSE) {
      PoseMessage message = (PoseMessage) rawMessage;
      for (int iHand=0; iHand<jointFrames.length; iHand++) { 
        Matrix4f[] jointFrames = message.getJointFrames(iHand);
        for (int jJoint=0; jJoint<jointFrames.length; jJoint++) {
          this.jointFrames[iHand][jJoint].set(jointFrames[jJoint]);
        }
      }
      
      for (int iHand=0; iHand<fingerTips.length; iHand++) { 
        for (int jFinger=0; jFinger<fingerTips[0].length; jFinger++) {
          Point3f[] fingerTips = message.getFingerTips(iHand);
          this.fingerTips[iHand][jFinger].set(fingerTips[jFinger]);
        }
      }

      theDataSet.add(message);
    }
  }
}