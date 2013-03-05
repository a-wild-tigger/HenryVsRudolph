package com.mobilecomputing.src.Training.Persistence;

import com.mobilecomputing.src.Training.Persistence.drawing.AbstractDisplay;
import com.mobilecomputing.src.Training.Persistence.threegears.PoseMessage;
import org.lwjgl.LWJGLException;
import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.Display;

import java.util.ArrayList;
import java.util.List;

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

    public static List<PoseMessage> SelectImagesToKeep(List<PoseMessage> myMessages, boolean isEditing) {
        StaticGestureViewer myViewer = new StaticGestureViewer(myMessages);
        try {
            List<PoseMessage> myReturnMessages = myViewer.Run();
            if(isEditing) { return myViewer.GetDataset(); }
            return myReturnMessages;
        } catch (LWJGLException e) {
            System.out.println("Could Not Run Viewer: " + e.toString());
        }

        if(isEditing) { return myMessages; }
        return new ArrayList<PoseMessage>();
    }

    public List<PoseMessage> Run() throws LWJGLException {
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

            myDisplay.RenderCurrent(theDataSet.get(theCurrentElement), false);
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

    private void ExecuteEnter() {
        System.out.println("Saving Current Image... Current Element is " + (theCurrentElement + 1) + " out of " + theDataSet.size());
        PoseMessage myMessage = theDataSet.remove(theCurrentElement);
        theSaveSet.add(myMessage);
    }

    private void ExecuteDelete() {
        System.out.println("Deleting Current Image...  Current Element is " + (theCurrentElement + 1) + " out of " + theDataSet.size());
        theDataSet.remove(theCurrentElement);
    }

    public List<PoseMessage> GetDataset() { return theDataSet; }
}
