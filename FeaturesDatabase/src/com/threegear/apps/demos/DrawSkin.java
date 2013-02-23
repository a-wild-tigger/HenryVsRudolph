package com.threegear.apps.demos;

import static org.lwjgl.opengl.GL11.GL_COLOR_BUFFER_BIT;
import static org.lwjgl.opengl.GL11.GL_COLOR_MATERIAL;
import static org.lwjgl.opengl.GL11.GL_CULL_FACE;
import static org.lwjgl.opengl.GL11.GL_DEPTH_BUFFER_BIT;
import static org.lwjgl.opengl.GL11.GL_DEPTH_TEST;
import static org.lwjgl.opengl.GL11.GL_LIGHT0;
import static org.lwjgl.opengl.GL11.GL_LIGHTING;
import static org.lwjgl.opengl.GL11.GL_MODELVIEW;
import static org.lwjgl.opengl.GL11.GL_PROJECTION;
import static org.lwjgl.opengl.GL11.GL_STENCIL_BUFFER_BIT;
import static org.lwjgl.opengl.GL11.GL_TRIANGLES;
import static org.lwjgl.opengl.GL11.glBegin;
import static org.lwjgl.opengl.GL11.glClear;
import static org.lwjgl.opengl.GL11.glColor3f;
import static org.lwjgl.opengl.GL11.glEnable;
import static org.lwjgl.opengl.GL11.glEnd;
import static org.lwjgl.opengl.GL11.glDisable;
import static org.lwjgl.opengl.GL11.glMatrixMode;
import static org.lwjgl.opengl.GL11.glNormal3f;
import static org.lwjgl.opengl.GL11.glVertex3f;
import static org.lwjgl.opengl.GL11.glViewport;

import javax.vecmath.Matrix4f;
import javax.vecmath.Point3f;
import javax.vecmath.Vector3f;

import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.DisplayMode;
import org.lwjgl.util.glu.GLU;

import com.threegear.gloveless.network.HandTrackingAdapter;
import com.threegear.gloveless.network.HandTrackingClient;
import com.threegear.gloveless.network.HandTrackingMessage;
import com.threegear.gloveless.network.HandTrackingMessage.MessageType;
import com.threegear.gloveless.network.PoseMessage;
import com.threegear.gloveless.network.UserMessage;

public class DrawSkin extends HandTrackingAdapter {

  private static final int DISPLAY_FRAMERATE = 60;

  private boolean finished;
  
  private HandTrackingClient client;
  
  public static void main(String[] args) {
    DrawSkin demo = new DrawSkin();
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
    Display.setTitle("Draw Skin Demo");
    int width = 900;
    int height = 600;
    Display.setDisplayMode(new DisplayMode(width, height));
    Display.create();

    // Setup the OpenGL state.  Our world is measured in millimeters 
    glMatrixMode(GL_PROJECTION);
    GLU.gluPerspective(45, width / (float) height, 10, 10000);
    glMatrixMode(GL_MODELVIEW);
    GLU.gluLookAt(0, 450, 550, 0, 125, 0, 0, 1, 0);
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
    
    glDisable(GL_LIGHTING);
    WorkspaceRenderingHelpers.drawGroundPlane(50);

    glEnable(GL_LIGHTING);
    for (int iHand=0; iHand<2; iHand++) {
      drawSkin(skinnedPositions[iHand], skinnedVertexNormals[iHand], triangles[iHand]);
    }

    glEnd();
  }
  
  public static void drawSkin(Point3f[] points, Vector3f[] normals, int[][] triangles) {
    glBegin(GL_TRIANGLES);
    for (int[] triangle : triangles) {
      for (int v : triangle) {
        glNormal3f(normals[v].x, normals[v].y, normals[v].z);
        glVertex3f(points[v].x, points[v].y, points[v].z);
      }
    }
    glEnd();
  }
  
  private Point3f[][] restPositions = new Point3f[HandTrackingMessage.N_HANDS][];
  private int[][][] triangles = new int[HandTrackingMessage.N_HANDS][][];
  private int[][][] skinningIndices = new int[HandTrackingMessage.N_HANDS][][];
  private float[][][] skinningWeights = new float[HandTrackingMessage.N_HANDS][][];
  private Matrix4f[][] restJointFrames = new Matrix4f[HandTrackingMessage.N_HANDS][];
  
  private Point3f[][] skinnedPositions = new Point3f[HandTrackingMessage.N_HANDS][];
  private Vector3f[][] skinnedVertexNormals = new Vector3f[HandTrackingMessage.N_HANDS][];
  
  /**
   * A straightforward implementation of linear blend skinning.
   * http://graphics.ucsd.edu/courses/cse169_w05/3-Skin.htm
   * 
   * Given skinning weights w, current joint frames F, rest joint frames F^r and
   * rest positions v^r, we compute the position of vertex v as follows:
   * 
   * v = sum_j w_j F_j (F^r_j)^-1 v^r
   * 
   * @param restPositions
   * @param triangles
   * @param skinningIndices
   * @param skinningWeights
   * @param restJointFrames
   * @param currentJointFrames
   * @param skinnedPositions
   * @param skinnedNormals
   */
  public static void skin(Point3f[] restPositions, int[][] triangles,
      int[][] skinningIndices, float[][] skinningWeights,
      Matrix4f[] restJointFrames, Matrix4f[] currentJointFrames,
      Point3f[] skinnedPositions, Vector3f[] skinnedNormals) {

    // compute the relative joint frames
    Matrix4f[] relativeJointFrames = new Matrix4f[restJointFrames.length];
    for (int i=0; i<restJointFrames.length; i++) {
      relativeJointFrames[i] = new Matrix4f(restJointFrames[i]);
      relativeJointFrames[i].invert();
      relativeJointFrames[i].mul(currentJointFrames[i], relativeJointFrames[i]);
    }
    
    // skin the vertices
    Matrix4f currentTransform = new Matrix4f();
    Matrix4f tempTransform = new Matrix4f();
    Point3f localPoint = new Point3f();
    for (int i=0; i<skinnedPositions.length; i++) {
      currentTransform.set(0);
      for (int j=0; j<skinningIndices[i].length; j++) {
        tempTransform.set(relativeJointFrames[skinningIndices[i][j]]);
        tempTransform.mul(skinningWeights[i][j]);
        currentTransform.add(tempTransform);
      }
      
      localPoint.set(restPositions[i]);
      currentTransform.transform(localPoint);
      skinnedPositions[i].set(localPoint);
    }
    
    // compute the vertex normals
    for (int i=0; i<skinnedNormals.length; i++) skinnedNormals[i].set(0, 0, 0);
    Vector3f u = new Vector3f();
    Vector3f v = new Vector3f();
    for (int[] triangle : triangles) {
      u.sub(skinnedPositions[triangle[1]], skinnedPositions[triangle[0]]);
      v.sub(skinnedPositions[triangle[2]], skinnedPositions[triangle[0]]);
      u.cross(u, v);
      
      for (int vertex : triangle) {
        skinnedNormals[vertex].add(u);
      }
    }
    
    for (int i=0; i<skinnedNormals.length; i++) skinnedNormals[i].normalize();
  }
  
  @Override
  public synchronized void handleEvent(HandTrackingMessage rawMessage) {
    // Cache the skinning information for each hand
    if (rawMessage.getType() == MessageType.USER) {
      UserMessage message = (UserMessage) rawMessage;
      for (int iHand=0; iHand<HandTrackingMessage.N_HANDS; iHand++) {
        restPositions[iHand] = message.getRestPositions(iHand);
        triangles[iHand] = message.getTriangles(iHand);
        skinningIndices[iHand] = message.getSkinningIndices(iHand);
        skinningWeights[iHand] = message.getSkinningWeights(iHand);
        restJointFrames[iHand] = message.getRestJointFrames(iHand);
        
        skinnedPositions[iHand] = new Point3f[restPositions[iHand].length];
        skinnedVertexNormals[iHand] = new Vector3f[restPositions[iHand].length];
        for (int i=0; i<skinnedPositions[iHand].length; i++) {
          skinnedPositions[iHand][i] = new Point3f();
          skinnedVertexNormals[iHand][i] = new Vector3f();
        }
      }
    }

    // Deform (i.e. skin) each hand mesh according to the skeletal pose
    if (rawMessage.getType() == MessageType.POSE) {
      PoseMessage message = (PoseMessage) rawMessage;
      for (int iHand=0; iHand<2; iHand++) {
        Matrix4f[] jointFrames = message.getJointFrames(iHand);
        skin(restPositions[iHand], triangles[iHand], skinningIndices[iHand],
            skinningWeights[iHand], restJointFrames[iHand], jointFrames,
            skinnedPositions[iHand], skinnedVertexNormals[iHand]);
      }
    }
  }
}