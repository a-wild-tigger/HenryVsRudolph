package com.mobilecomputing.src.inout.output;

import com.threegear.apps.demos.WorkspaceRenderingHelpers;
import com.threegear.gloveless.network.HandTrackingAdapter;
import com.threegear.gloveless.network.HandTrackingClient;
import com.threegear.gloveless.network.HandTrackingMessage;
import com.threegear.gloveless.network.PoseMessage;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.DisplayMode;
import org.lwjgl.util.glu.GLU;

import javax.vecmath.Matrix4f;
import javax.vecmath.Point3f;

import java.util.List;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL11.glColor3f;

public class StaticGestureRecorder extends HandTrackingAdapter {
    private static final int DISPLAY_FRAMERATE = 60;
    private boolean finished;





    private HandTrackingClient client;




    // Cache the hand positions, rotations and contact state for rendering
    private Matrix4f[][] jointFrames = new Matrix4f[HandTrackingMessage.N_HANDS][HandTrackingMessage.N_JOINTS];
    private Point3f[][] fingerTips = new Point3f[HandTrackingMessage.N_HANDS][HandTrackingMessage.N_FINGERS];

    public StaticGestureRecorder() {
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

    public static void main(String[] args) {
        StaticGestureRecorder demo = new StaticGestureRecorder();
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
        Display.setTitle("Draw Skeleton Demo");
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

        WorkspaceRenderingHelpers.drawGroundPlane(50);

        for (int iHand=0; iHand<2; iHand++) {
            drawSkeleton(jointFrames[iHand], fingerTips[iHand]);
        }

        glEnd();
    }

    private static void drawLineBetween(Matrix4f frame0, Matrix4f frame1) {
        glBegin(GL_LINES);
        glVertex3f(frame0.m03, frame0.m13, frame0.m23);
        glVertex3f(frame1.m03, frame1.m13, frame1.m23);
        glEnd();
    }

    private static void drawLineBetween(Matrix4f frame0, Point3f point) {
        glBegin(GL_LINES);
        glVertex3f(frame0.m03, frame0.m13, frame0.m23);
        glVertex3f(point.x, point.y, point.z);
        glEnd();
    }

    public static void drawSkeleton(Matrix4f[] jointFrames, Point3f[] fingerTips) {
        // Draw the coordinate frame for each joint
        for (int i=0; i<jointFrames.length; i++) {
            WorkspaceRenderingHelpers.drawCoordinateFrame(jointFrames[i], 5);
        }

        // Draw the finger tips as points
        glColor3f(0,0,1);
        glPointSize(5);
        glBegin(GL_POINTS);
        for (int i=0; i<fingerTips.length; i++)
            glVertex3f(fingerTips[i].x,
                    fingerTips[i].y,
                    fingerTips[i].z);
        glEnd();

        // Draw lines connecting the joint locations

        //
        //              T2
        //               |
        //    T1         |        T3
        //     \         |        /
        //      J7     J10     J13     T4
        //       \       |      /      /
        //        J6    J9    J12   J16
        //  T0     \     |    /      /
        //   \      \    |   /    J15
        //    J4    J5  J8 J11     /
        //     \     |   |   |  J14
        //     J3    |   |   |   /
        //       \   \   |   /  /
        //        J2  \  |  /  /
        //          \  \ | /  /
        //           \__\|/__/
        //              J1
        //               |
        //              J0
        //
        //          J0: Root
        //          J1: Wrist
        //       J2-T0: Thumb
        //       J5-T1: Index finger
        //       J8-T2: Middle finger
        //      J11-T3: Ring finger
        //      J14-T4: Pinky finger
        //

        glColor3f(0,1,1);
        // Draw a line between the root and the wrist joint
        drawLineBetween(jointFrames[0], jointFrames[1]);

        // Draw the thumb from wrist to tip
        drawLineBetween(jointFrames[1], jointFrames[2]);
        drawLineBetween(jointFrames[2], jointFrames[3]);
        drawLineBetween(jointFrames[3], jointFrames[4]);
        drawLineBetween(jointFrames[4], fingerTips[0]);

        // Draw the index finger from wrist to tip
        drawLineBetween(jointFrames[1], jointFrames[5]);
        drawLineBetween(jointFrames[5], jointFrames[6]);
        drawLineBetween(jointFrames[6], jointFrames[7]);
        drawLineBetween(jointFrames[7], fingerTips[1]);

        // Draw the middle finger from wrist to tip
        drawLineBetween(jointFrames[1], jointFrames[8]);
        drawLineBetween(jointFrames[8], jointFrames[9]);
        drawLineBetween(jointFrames[9], jointFrames[10]);
        drawLineBetween(jointFrames[10], fingerTips[2]);

        // Draw the ring finger from wrist to tip
        drawLineBetween(jointFrames[1], jointFrames[11]);
        drawLineBetween(jointFrames[11], jointFrames[12]);
        drawLineBetween(jointFrames[12], jointFrames[13]);
        drawLineBetween(jointFrames[13], fingerTips[3]);

        // Draw the pinky finger from wrist to tip
        drawLineBetween(jointFrames[1], jointFrames[14]);
        drawLineBetween(jointFrames[14], jointFrames[15]);
        drawLineBetween(jointFrames[15], jointFrames[16]);
        drawLineBetween(jointFrames[16], fingerTips[4]);
    }

    @Override
    public synchronized void handleEvent(HandTrackingMessage rawMessage) {
        // Cache the coordinate frames and finger tips of the skeleton for rendering
        if (rawMessage.getType() == HandTrackingMessage.MessageType.POSE) {
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
        }
    }

    public List<PoseMessage> Record() {

    }
}
