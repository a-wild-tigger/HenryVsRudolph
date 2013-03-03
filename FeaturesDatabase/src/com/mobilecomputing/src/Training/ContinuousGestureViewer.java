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

public class ContinuousGestureViewer {
    private List<PoseMessage> theDataSet;
    private List<List<PoseMessage>> theSaveSet = new ArrayList<List<PoseMessage>>();
    private final AbstractDisplay myDisplay = new AbstractDisplay();
    int theCurrentElement = 0;
    boolean finished = false;

    public enum KeyPress {
        DELETE, ENTER, LEFT, RIGHT, ESC, NONE
    }

    public ContinuousGestureViewer(List<PoseMessage> aMessages) {
        theDataSet = aMessages;
    }

    public static List<List<PoseMessage>> SelectImagesToKeep(List<PoseMessage> myMessages) {
        ContinuousGestureViewer myViewer = new ContinuousGestureViewer(myMessages);
        try {
            return myViewer.Run();
        } catch (LWJGLException e) {
            System.out.println("Could Not Run Viewer: " + e.toString());
        }

        return new ArrayList<List<PoseMessage>>();
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
        if(SamplingState && theCurrentElement >= theStartElement) {
            DrawSimpleRectangle();
        }

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

    public List<PoseMessage> GetDataset() {
        return theDataSet;
    }

    public List<List<PoseMessage>> Run() throws LWJGLException {
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
                    case ESC: ExecuteESC(); break;
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

                if(Keyboard.getEventKey() == Keyboard.KEY_ESCAPE) {
                    return KeyPress.ESC;
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

    private void ExecuteDelete() {
        if(SamplingState) {
            System.out.println("Cannot Execute Delete In Sampling State");
            return;
        }
        System.out.println("Deleting Current Image...");
        theDataSet.remove(theCurrentElement);
    }

    private void ExecuteESC() {
        if(SamplingState) {
            System.out.println("Exiting Sampling State");
            SamplingState = false;
        }

        else {
            System.out.println("Currently not in Sampling State");
        }
    }

    private boolean SamplingState = false;
    private int theStartElement = 0;
    private void ExecuteEnter() {
        SamplingState = !SamplingState;
        if(SamplingState == true) {
            theStartElement = theCurrentElement;
        }

        if(SamplingState == false) {
            int theEndElement = theCurrentElement;
            List<PoseMessage> myMessages = new ArrayList<PoseMessage>();
            int numIters = theEndElement - theStartElement + 1;
            for(int i = 0; i != numIters; i++) {
                myMessages.add(theDataSet.remove(theStartElement));
            }

            theSaveSet.add(myMessages);
            System.out.println("Saving Current Image...");
        }
    }
}
