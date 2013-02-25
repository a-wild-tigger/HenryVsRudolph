package com.threegear.apps.demos;

import com.threegear.gloveless.network.*;
import org.lwjgl.LWJGLException;
import org.lwjgl.opengl.Display;

import javax.vecmath.Matrix4f;
import javax.vecmath.Point3f;
import java.util.ArrayList;
import java.util.List;

public class Recorder extends HandTrackingAdapter {
    private static final int DISPLAY_FRAMERATE = 60;
    private boolean finished = false;
    private final List<PoseMessage> theDataSet = new ArrayList<PoseMessage>();
    private final AbstractDisplay myDisplay = new AbstractDisplay();

    @Override
    public void handleEvent(HandTrackingMessage rawMessage) {
        // Cache the coordinate frames and finger tips of the skeleton for rendering
        if (rawMessage.getType() == HandTrackingMessage.MessageType.POSE) {
            PoseMessage message = (PoseMessage) rawMessage;
            synchronized (myDisplay) {
                Matrix4f[][] jointFrames = myDisplay.jointFrames;
                Point3f[][] fingerTips = myDisplay.fingerTips;

                for (int iHand=0; iHand < jointFrames.length; iHand++) {
                    Matrix4f[] jointFrame = message.getJointFrames(iHand);
                    for (int jJoint=0; jJoint<jointFrame.length; jJoint++) {
                        jointFrames[iHand][jJoint].set(jointFrame[jJoint]);
                    }
                }

                for (int iHand=0; iHand < fingerTips.length; iHand++) {
                    for (int jFinger=0; jFinger < fingerTips[0].length; jFinger++) {
                        Point3f[] fingerTip = message.getFingerTips(iHand);
                        fingerTips[iHand][jFinger].set(fingerTip[jFinger]);
                    }
                }
            }

            theDataSet.add(message);
        }
    }

    public void Run() throws LWJGLException {
        myDisplay.Init();
        while (!finished) {
            // Always call Window.update(), all the time
            Display.update();
            if (Display.isCloseRequested()) {
                finished = true;
            } else {
                synchronized (myDisplay) {
                    myDisplay.render();
                }

                Display.sync(DISPLAY_FRAMERATE);
            }
        }
        myDisplay.Stop();
    }

    public List<PoseMessage> GetMessages() {
        return theDataSet;
    }
}
