package com.mobilecomputing.src.Training.Training;

import com.mobilecomputing.src.Training.Persistence.drawing.AbstractDisplay;
import com.mobilecomputing.src.Training.Persistence.threegears.HandTrackingAdapter;
import com.mobilecomputing.src.Training.Persistence.threegears.HandTrackingClient;
import com.mobilecomputing.src.Training.Persistence.threegears.HandTrackingMessage;
import com.mobilecomputing.src.Training.Persistence.threegears.PoseMessage;
import com.mobilecomputing.src.Training.Training.FeatureExtractors.*;
import org.lwjgl.LWJGLException;
import org.lwjgl.opengl.Display;

import javax.vecmath.Matrix4f;
import javax.vecmath.Point3f;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class LiveFeatureViewer extends HandTrackingAdapter {
    private static final int DISPLAY_FRAMERATE = 60;
    private boolean finished = false;
    public final AbstractDisplay myDisplay = new AbstractDisplay();

    public static void main(String[] args) throws LWJGLException, IOException {
        LiveFeatureViewer demo = new LiveFeatureViewer();

        HandTrackingClient myClient = new HandTrackingClient();
        myClient.addListener(demo);
        myClient.connect();
        demo.Run();
        myClient.stop();
    }

    boolean myOldSet = false;
    PoseMessage myOldMessage;
    VelocityFeatures myLeftVelocityFeatures;
    VelocityFeatures myRightVelocityFeatures;
    AppendageDistance myLeftAppendagesDistance;
    AppendageDistance myRightAppendagesDistance;
    AppendageStretch myLeftStretch;
    AppendageStretch myRightStretch;
    HandInteraction myHandInteraction;

    @Override
    public void handleEvent(HandTrackingMessage rawMessage) {
        // Cache the coordinate frames and finger tips of the skeleton for rendering
        if (rawMessage.getType() == HandTrackingMessage.MessageType.POSE) {
            PoseMessage message = (PoseMessage) rawMessage;
            if(!myOldSet) {
                myOldMessage = message;
                myOldSet = true;
                return;
            }

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


                myLeftStretch = new AppendageStretch(message, 0);
                myRightStretch = new AppendageStretch(message, 1);

                myLeftVelocityFeatures = new VelocityFeatures(myOldMessage, message, 0);
                myRightVelocityFeatures = new VelocityFeatures(myOldMessage, message, 1);

                myLeftAppendagesDistance = new AppendageDistance(message, 0);
                myRightAppendagesDistance = new AppendageDistance(message, 1);
                myHandInteraction = new HandInteraction(message);
                myOldMessage = message;
            }
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
                    String myString = "";
                    if(myRightStretch != null) {
                        myDisplay.render(myRightVelocityFeatures.toString());
                    } else {
                        myDisplay.render(myString);
                    }
                }

                Display.sync(DISPLAY_FRAMERATE);
            }
        }
        myDisplay.Stop();
    }
}
