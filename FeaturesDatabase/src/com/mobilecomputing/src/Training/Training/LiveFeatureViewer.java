package com.mobilecomputing.src.Training.Training;

import com.mobilecomputing.src.Training.Classification.UDPClient;
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

    private boolean isInit = false;
    private boolean FirstRound = true;
    private PoseMessage aFirstPoseMessage;
    private String aUsername = "anil";

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

            }

            if(message.getHandState(0).getPosition().y < 90 && message.getHandState(1).getPosition().y < 90) {
                myOldMessage = message;
                return;
            }

            AppendageStretch aLeftStretch = new AppendageStretch(message, 0);
            AppendageStretch aRightStretch = new AppendageStretch(message, 1);
            AppendageDistance aLeftDistances = new AppendageDistance(message, 0);
            AppendageDistance aRightDistances = new AppendageDistance(message, 1);

            VelocityFeatures aLeftVelocityFeatures = new VelocityFeatures(myOldMessage, message, 0);
            VelocityFeatures aRightVelocityFeatures = new VelocityFeatures(myOldMessage, message, 1);
            HandInteraction aHandInteraction = new HandInteraction(message);

            if(aHandInteraction.theHandsDistance < 100 && aLeftStretch.isHandFlat() && aRightStretch.isHandFlat() &&
                    aLeftVelocityFeatures.MovingInNegativeZDirection() && aRightVelocityFeatures.MovingInNegativeZDirection()) {
                System.out.println("Detected Hadouken");
                UDPClient.SendString(aUsername, "hadouken");
            }

            if((aLeftStretch.isFacingCieling() && aLeftStretch.isHandFlat()) && !aLeftStretch.isFacingMonitor()
                    && aLeftVelocityFeatures.theMiddleDeltaVector.length() > 5 && message.getHandState(0).getPosition().y > 60) {
                System.out.println("Left Detected Ball Opening");
                UDPClient.SendString(aUsername, "explosion");
            }

            if((aRightStretch.isFacingCieling() && aRightStretch.isHandFlat()) && !aRightStretch.isFacingMonitor()
                    && aRightVelocityFeatures.theMiddleDeltaVector.length() > 5 && message.getHandState(1).getPosition().y > 60) {
                //System.out.println("Right Detected Ball Opening");
            }

            if(aLeftDistances.isFist() && aLeftVelocityFeatures.MovingInPositiveXDirection() && message.getHandState(0).getPosition().y > 60) {
                //System.out.println("Left Fist Detected");
            }

            if(aRightDistances.isFist() && aRightVelocityFeatures.MovingInNegativeXDirection() && message.getHandState(1).getPosition().y > 100) {
                System.out.println("Right Fist Detected");
                UDPClient.SendString(aUsername, "screenpunch");
            }

            if(aRightStretch.isFireGunGesture() && message.getHandState(1).getPosition().y > 60) {
                //System.out.println("Fire Gun Right Hand");
            }

            if(aLeftStretch.isFacingRight()) {
                //System.out.println("Left Hand Facing Right");
            }

            if(aRightStretch.isFacingLeft()) {
                //System.out.println("Right Hand Facing Left");
            }

            myOldMessage = message;
        }
    }


    public void Init() {
        try {
            myDisplay.Init();
        } catch (LWJGLException e) {
            e.printStackTrace();
        }
        Display.update();
    }

    public void Run() throws LWJGLException {
        myDisplay.Init();
        while (!finished) {
            Display.update();
            if (Display.isCloseRequested()) {
                finished = true;
            } else {
                synchronized (myDisplay) {
                    String myString = "";
                    if(myRightStretch != null) {
                        myDisplay.render("Left: " + myRightVelocityFeatures.toString() + "\n\nRight: " + myLeftVelocityFeatures.toString());
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
