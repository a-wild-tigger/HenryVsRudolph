package com.mobilecomputing.src.Training;

import com.threegear.gloveless.network.PoseMessage;
import org.lwjgl.LWJGLException;
import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.Display;

import javax.vecmath.Matrix4f;
import javax.vecmath.Point3f;
import java.util.ArrayList;
import java.util.List;

import static org.lwjgl.opengl.GL11.*;

public class StaticGestureViewer {
    private List<PoseMessage> theDataSet;
    private List<PoseMessage> theSaveSet = new ArrayList<PoseMessage>();
    private final AbstractDisplay myDisplay = new AbstractDisplay();
    int theCurrentElement = 0;
    boolean finished = false;

    public enum KeyPress {
        DELETE, ENTER, LEFT, RIGHT, NONE
    }

    public StaticGestureViewer(List<PoseMessage> aMessages) {
        theDataSet = aMessages;
    }

    public static List<PoseMessage> SelectImagesToKeep(List<PoseMessage> myMessages) {
        StaticGestureViewer myViewer = new StaticGestureViewer(myMessages);
        try {
            return myViewer.Run();
        } catch (LWJGLException e) {
            System.out.println("Could Not Run Viewer: " + e.toString());
        }

        return new ArrayList<PoseMessage>();
    }

    private void RenderCurrent() {
        glClear(GL_COLOR_BUFFER_BIT | GL_STENCIL_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
        glColor3f(1,1,1);

        WorkspaceRenderingHelpers.drawGroundPlane(50);
        PoseMessage message =  theDataSet.get(theCurrentElement);

        for (int iHand=0; iHand<myDisplay.jointFrames.length; iHand++) {
            Matrix4f[] myJointFrames = message.getJointFrames(iHand);
            for (int jJoint=0; jJoint<myJointFrames.length; jJoint++) {
                myDisplay.jointFrames[iHand][jJoint].set(myJointFrames[jJoint]);
            }
        }

        for (int iHand=0; iHand<myDisplay.fingerTips.length; iHand++) {
            for (int jFinger=0; jFinger<myDisplay.fingerTips[0].length; jFinger++) {
                Point3f[] myFingerTips = message.getFingerTips(iHand);
                myDisplay.fingerTips[iHand][jFinger].set(myFingerTips[jFinger]);
            }
        }

        for (int iHand=0; iHand<2; iHand++) {
            myDisplay.drawSkeleton(myDisplay.jointFrames[iHand], myDisplay.fingerTips[iHand]);
        }

        glEnd();

        Display.update();
    }

    private void DrawSimpleRectangle() {
        // set the color of the quad (R,G,B,A)
        glColor3f(0.5f,0.5f,1.0f);

        // draw quad
        glBegin(GL_QUADS);
        glVertex2f(100,100);
        glVertex2f(100+50,100);
        glVertex2f(100+50,100+50);
        glVertex2f(100,100+50);
        glEnd();
    }

    public static List<PoseMessage> ViewImages(List<PoseMessage> theCurrentGestures) {
        StaticGestureViewer myViewer = new StaticGestureViewer(theCurrentGestures);
        try {
            myViewer.Run();
            return myViewer.GetDataset();
        } catch (LWJGLException e) {
            System.out.println("Could Not Run Viewer: " + e.toString());
        }

        return new ArrayList<PoseMessage>();    }

    public List<PoseMessage> GetDataset() {
        return theDataSet;
    }



    public List<PoseMessage> Run() throws LWJGLException {
        myDisplay.Init();
        while(!finished) {
            if (Display.isCloseRequested()) {
                finished = true;
            } else {
                if(theDataSet.size() == 0) break;

                KeyPress myPress = this.pollInput();
                switch(myPress) {
                    case DELETE: ExecuteDelete(); break;
                    case ENTER: ExecuteEnter(); break;
                    case LEFT: ExecuteLeft(); break;
                    case RIGHT: ExecuteRight(); break;
                }
            }
            RenderCurrent();
        }

        System.out.println("Completed Processing...");

        Display.destroy();
        return theSaveSet;
    }

    public KeyPress pollInput() {
        while (Keyboard.next()) {
            if (Keyboard.getEventKeyState()) {
                if (Keyboard.getEventKey() == Keyboard.KEY_DELETE) {
                    return KeyPress.DELETE;
                }

                if (Keyboard.getEventKey() == Keyboard.KEY_RETURN) {
                    return KeyPress.ENTER;
                }

                if (Keyboard.getEventKey() == Keyboard.KEY_LEFT) {
                    return KeyPress.LEFT;
                }

                if(Keyboard.getEventKey() == Keyboard.KEY_RIGHT) {
                    return KeyPress.RIGHT;
                }

                return KeyPress.NONE;
            }

            return KeyPress.NONE;
        }

        return KeyPress.NONE;
    }

    private void ExecuteRight() {
        if(theCurrentElement + 1 < theDataSet.size()) {
            System.out.println("Moving to Next Capture...");
            theCurrentElement++;
        } else {
            System.out.println("Cannot Move to Next Capture...");
        }
    }

    private void ExecuteLeft() {
        if(theCurrentElement - 1 > 0) {
            System.out.println("Moving to Previous Capture...");
            theCurrentElement--;
        } else {
            System.out.println("Cannot Move to Previous Capture...");
        }
    }

    private void ExecuteEnter() {
        System.out.println("Saving Current Image...");
        PoseMessage myMessage = theDataSet.remove(theCurrentElement);
        theSaveSet.add(myMessage);
    }

    private void ExecuteDelete() {
        System.out.println("Deleting Current Image...");
        theDataSet.remove(theCurrentElement);
    }
}
