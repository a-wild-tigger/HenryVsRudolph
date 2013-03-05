package com.mobilecomputing.src.Training.Persistence.drawing;

import static org.lwjgl.opengl.GL11.GL_LIGHTING;
import static org.lwjgl.opengl.GL11.GL_LINES;
import static org.lwjgl.opengl.GL11.GL_TRIANGLE_FAN;
import static org.lwjgl.opengl.GL11.glBegin;
import static org.lwjgl.opengl.GL11.glColor3f;
import static org.lwjgl.opengl.GL11.glDisable;
import static org.lwjgl.opengl.GL11.glEnable;
import static org.lwjgl.opengl.GL11.glEnd;
import static org.lwjgl.opengl.GL11.glLineWidth;
import static org.lwjgl.opengl.GL11.glMultMatrix;
import static org.lwjgl.opengl.GL11.glPopMatrix;
import static org.lwjgl.opengl.GL11.glPushMatrix;
import static org.lwjgl.opengl.GL11.glTranslatef;
import static org.lwjgl.opengl.GL11.glVertex3f;

import java.nio.FloatBuffer;

import javax.vecmath.Matrix4f;
import javax.vecmath.Quat4f;
import javax.vecmath.Vector3f;

import org.lwjgl.BufferUtils;
import org.lwjgl.util.glu.GLU;
import org.lwjgl.util.glu.Sphere;

public class WorkspaceRenderingHelpers {

  /**
   * Draw a canonical ground plane grid with the specified grid spacing
   * 
   * @param gridSpacing
   */
  public static void drawGroundPlane(float gridSpacing) {
    glPushMatrix();
    glBegin(GL_LINES);
    
    int[] directions = new int[] {1,-1};
    
    float zmin = -200;
    float zmax = 200;
    
    float xmin = -400;
    float xmax = 400;
    
    float scale = 1;
    
    zmin *= scale;
    zmax *= scale;
    
    xmin *= scale;
    xmax *= scale;
    
    for (int d : directions) {
      // draw lines parallel to the x axis
      float z = 0;
      
      while (true) {
        glVertex3f(xmin, 0, z);
        glVertex3f(xmax, 0, z);
        z += d * gridSpacing;
        
        if (z > zmax || z < zmin) break;
      }
      
      // draw lines parallel to the z axis
      float x = 0;
      while (true) {
        glVertex3f(x, 0, zmin);
        glVertex3f(x, 0, zmax);
        x += d * gridSpacing;
        
        if (x > xmax || x < xmin) break;
      }
    }
    
    glEnd();
    glPopMatrix();
  }

  /**
   * Draw a filled circle around a projected point on the desk.
   * 
   * @param pointOnDesk
   * @param radius radius of circle
   * @param segments approximated by segment-sided polygon
   */
  public static void drawCircleOnDesk(Vector3f pointOnDesk, float radius, int segments) {
    glBegin(GL_TRIANGLE_FAN);
    
    glVertex3f(pointOnDesk.x, pointOnDesk.y, pointOnDesk.z);
    
    for (int i=0; i<=segments; i++) {
      double angle = (segments-i) / (float) segments * Math.PI * 2;
      
      Vector3f p = new Vector3f(
          radius * (float) Math.cos(angle), 0, 
          radius * (float) Math.sin(angle));
      
      p.add(pointOnDesk);
      glVertex3f(p.x, p.y, p.z);
    }
  
    glEnd();
  }

  /**
   * Draw a sphere and projected shadow at the specified cursor point
   * 
   * @param cursorPoint
   * @param radius sphere radius
   */
  public static void drawCursorPoint(Vector3f cursorPoint, float radius) {
    glDisable(GL_LIGHTING);
    glBegin(GL_LINES);
    Vector3f projection = new Vector3f(cursorPoint.x,0,cursorPoint.z);
    glVertex3f(cursorPoint.x, cursorPoint.y, cursorPoint.z);
    glVertex3f(projection.x, projection.y, projection.z);
    glEnd();
    
    glEnable(GL_LIGHTING);
    glPushMatrix();
    glTranslatef(cursorPoint.x, cursorPoint.y, cursorPoint.z);
    Sphere sphere = new Sphere();
    sphere.setOrientation(GLU.GLU_OUTSIDE);
    sphere.draw(radius, 15, 15);
    glPopMatrix();
    
    glDisable(GL_LIGHTING);
    drawCircleOnDesk(projection, 10, 15);
  }

  private static float[] toOpenGLMatrix(Matrix4f m) {
    return new float[] {m.m00, m.m10, m.m20, m.m30, m.m01, m.m11, m.m21, m.m31, m.m02, m.m12, m.m22, m.m32, m.m03, m.m13, m.m23, m.m33};
  }

  
  /**
   * Draw a coordinate frame using the cursor point and rotation
   * 
   * @param p
   * @param q
   * @param frameLength length of the coordinate frame
   */
  public static void drawCursorFrame(Vector3f p, Quat4f q, float frameLength) {
    // Build a transformation based on the cursor point and hand rotation
    // Convert the quaternion into a rotation matrix
    drawCoordinateFrame(new Matrix4f(q, p, 1), frameLength);
  }
  
  
  /**
   * Draw the 3-axes of a coordinate frame
   * 
   * @param m
   * @param frameLength
   */
  public static void drawCoordinateFrame(Matrix4f m, float frameLength) {
    FloatBuffer buf = BufferUtils.createFloatBuffer(16);
    buf.rewind();
    buf.put(toOpenGLMatrix(m));
    buf.rewind();
    
    glLineWidth(2);
    // Use the transformation to draw a standard coordinate axis
    glPushMatrix();
    glMultMatrix(buf);
    
    glBegin(GL_LINES);
    
    glColor3f(1,0,0);
    glVertex3f(0,0,0);
    glVertex3f(frameLength,0,0);
    
    glColor3f(0,1,0);
    glVertex3f(0,0,0);
    glVertex3f(0,frameLength,0);
    
    glColor3f(0,0,1);
    glVertex3f(0,0,0);
    glVertex3f(0,0,frameLength);
    
    glEnd();
    
    glPopMatrix();
    glLineWidth(1);
  }

}
