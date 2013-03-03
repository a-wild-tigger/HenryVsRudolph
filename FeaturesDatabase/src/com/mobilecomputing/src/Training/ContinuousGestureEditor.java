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
        DELETE, LEFT, RIGHT, NONE
    }

    private final AbstractDisplay myDisplay = new AbstractDisplay();
    private List<List<PoseMessage>> theDataSet;
    private int theCurrentIndex = 0;
    private int theCurrentSubIndex = 0;
    private boolean finished = false;

    public ContinuousGestureEditor(List<List<PoseMessage>> theMessages) {
        theDataSet = theMessages;
    }

    public List<List<PoseMessage>> Run() throws LWJGLException, InterruptedException {
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
                    case LEFT: ExecuteLeft(); keyClick = true; break;
                    case RIGHT: ExecuteRight(); keyClick = true; break;
                }
            }

            if(theDataSet.size() == 0) {
                break;
            }

            while(theCurrentIndex >= theDataSet.size()) {
                theCurrentIndex--;
                theCurrentSubIndex = 0;
            }

            if(keyClick) {
                System.out.println("Rendering Item " + (theCurrentIndex + 1) + " out of " + theDataSet.size());
                keyClick = false;
            }

            Thread.sleep(50);
            PoseMessage myMessage = theDataSet.get(theCurrentIndex).get(theCurrentSubIndex);
            myDisplay.RenderCurrent(myMessage, false);

            theCurrentSubIndex = (theCurrentSubIndex + 1) % theDataSet.get(theCurrentIndex).size();
        }

        System.out.println("Completed Processing...");

        Display.destroy();
        return theDataSet;
    }

    private void ExecuteRight() {
        if(theCurrentIndex + 1 < theDataSet.size()) {
            System.out.println("Moving to Next Capture...");
            theCurrentIndex++;
            theCurrentSubIndex = 0;
        } else {
            System.out.println("Cannot Move to Next Capture... Current Element is " + (theCurrentIndex + 1) + " out of " + theDataSet.size());
        }
    }

    private void ExecuteLeft() {
        if(theCurrentIndex >= 1) {
            System.out.println("Moving to Previous Capture...");
            theCurrentIndex--;
            theCurrentSubIndex = 0;
        } else {
            System.out.println("Cannot Move to Previous Capture... Current Element is " + (theCurrentIndex + 1) + " out of " + theDataSet.size());
        }
    }

    private void ExecuteDelete() {
        System.out.println("Deleting Current Image...");
        theDataSet.remove(theCurrentIndex);
    }

    public KeyPress pollInput() {
        while (Keyboard.next()) {
            if (Keyboard.getEventKeyState()) {
                if (Keyboard.getEventKey() == Keyboard.KEY_DELETE) {
                    return KeyPress.DELETE;
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

    public static List<List<PoseMessage>> ViewImages(List<List<PoseMessage>> theCurrentGestures) throws LWJGLException, InterruptedException {
        ContinuousGestureEditor myEditor = new ContinuousGestureEditor(theCurrentGestures);
        return myEditor.Run();
    }
}
