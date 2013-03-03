package com.mobilecomputing.src.Training;

import com.threegear.gloveless.network.PoseMessage;
import org.lwjgl.LWJGLException;
import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.Display;

import javax.vecmath.Matrix4f;
import javax.vecmath.Point3f;
import java.util.List;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL11.glColor3f;
import static org.lwjgl.opengl.GL11.glEnd;

public class ContinuousGestureEditor {
    public enum KeyPress {
        DELETE, ENTER, LEFT, RIGHT, ESC, NONE
    }

    private final AbstractDisplay myDisplay = new AbstractDisplay();

    public ContinuousGestureEditor(List<List<PoseMessage>> theMessages) {
        theDataSet = theMessages;
        theCurrentSubMessages = theMessages.get(theCurrentSubIndex);
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
                    case LEFT: ExecuteLeft(); break;
                    case RIGHT: ExecuteRight(); break;
                }
            }
            RenderCurrent();
        }

        System.out.println("Completed Processing...");

        Display.destroy();
        return theDataSet;
    }

    private List<List<PoseMessage>> theDataSet;
    private int theCurrentIndex = 0;
    boolean finished = false;
    private int theCurrentSubIndex = 0;
    private List<PoseMessage> theCurrentSubMessages;

    private void ExecuteRight() {
        if(theCurrentIndex + 1 < theDataSet.size()) {
            System.out.println("Moving to Next Capture...");
            theCurrentIndex++;
            theCurrentSubIndex = 0;
            theCurrentSubMessages = theDataSet.get(theCurrentIndex);
        } else {
            System.out.println("Cannot Move to Next Capture...");
        }
    }

    private void ExecuteLeft() {
        if(theCurrentIndex - 1 > 0) {
            System.out.println("Moving to Previous Capture...");
            theCurrentIndex--;
            theCurrentSubIndex = 0;
            theCurrentSubMessages = theDataSet.get(theCurrentIndex);
        } else {
            System.out.println("Cannot Move to Previous Capture...");
        }
    }

    private void ExecuteDelete() {
        System.out.println("Deleting Current Image...");
        ExecuteLeft();
        theDataSet.remove(theCurrentIndex);
    }

    private void RenderCurrent() {
        glClear(GL_COLOR_BUFFER_BIT | GL_STENCIL_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
        glColor3f(1,1,1);

        WorkspaceRenderingHelpers.drawGroundPlane(50);
        PoseMessage message =  theCurrentSubMessages.get(theCurrentSubIndex);
        theCurrentSubIndex++;

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
        System.out.println("Rendering Frame " + theCurrentSubIndex + " of Sample " + theCurrentIndex);

        Display.update();
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

    public static List<List<PoseMessage>> ViewImages(List<List<PoseMessage>> theCurrentGestures) throws LWJGLException {
        ContinuousGestureEditor myEditor = new ContinuousGestureEditor(theCurrentGestures);
        return myEditor.Run();
    }
}
