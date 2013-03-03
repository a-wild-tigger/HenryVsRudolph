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

    public List<List<PoseMessage>> Run() throws LWJGLException {
        myDisplay.Init();
        boolean keyClick = false;
        while(!finished) {
            if (Display.isCloseRequested()) {
                finished = true;
            } else {
                if(theDataSet.size() == 0) break;

                KeyPress myPress = this.pollInput();
                switch(myPress) {
                    case DELETE: ExecuteDelete(); keyClick = true; break;
                    case ENTER: ExecuteEnter(); keyClick = true; break;
                    case LEFT: ExecuteLeft(); keyClick = true; break;
                    case RIGHT: ExecuteRight(); keyClick = true; break;
                    case ESC: ExecuteESC(); keyClick = true; break;
                }
            }

            if(theDataSet.size() == 0) {
                break;
            }

            while(theCurrentElement >= theDataSet.size()) {
                theCurrentElement--;
            }

            if(keyClick) {
                System.out.println("Rendering Item " + (theCurrentElement + 1) + " out of " + theDataSet.size());
                keyClick = false;
            }

            myDisplay.RenderCurrent(theDataSet.get(theCurrentElement), (SamplingState && theCurrentElement >= theStartElement));
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
            System.out.println("Cannot Move to Next Capture... Current Element is " + (theCurrentElement + 1) + " out of " + theDataSet.size());
        }
    }

    private void ExecuteLeft() {
        if(theCurrentElement >= 1) {
            System.out.println("Moving to Previous Capture...");
            theCurrentElement--;
        } else {
            System.out.println("Cannot Move to Previous Capture... Current Element is " + (theCurrentElement + 1) + " out of " + theDataSet.size());
        }
    }

    private void ExecuteDelete() {
        if(SamplingState) {
            System.out.println("Cannot Delete Current Image since we are in Sampling Mode");
            return;
        }
        System.out.println("Deleting Current Image...  Current Element is " + (theCurrentElement + 1) + " out of " + theDataSet.size());
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
        if(SamplingState) {
            theStartElement = theCurrentElement;
        }

        if(!SamplingState) {
            int theEndElement = theCurrentElement;
            List<PoseMessage> myMessages = new ArrayList<PoseMessage>();
            int numIters = theEndElement - theStartElement + 1;
            for(int i = 0; i != numIters; i++) {
                myMessages.add(theDataSet.remove(theStartElement));
            }

            theSaveSet.add(myMessages);
            theCurrentElement = 0;
            System.out.println("Saving Current Image...");
        }
    }

    public List<PoseMessage> GetDataset() {
        return theDataSet;
    }
}
