package com.mobilecomputing.src.Training.Classification;

import com.mobilecomputing.src.Training.Persistence.threegears.HandTrackingClient;
import com.mobilecomputing.src.Training.Persistence.threegears.HandTrackingListener;
import com.mobilecomputing.src.Training.Persistence.threegears.HandTrackingMessage;
import com.mobilecomputing.src.Training.Persistence.threegears.PoseMessage;
import com.mobilecomputing.src.Training.Training.ContinuousTrainedParameters;
import com.mobilecomputing.src.Training.Training.FeatureExtractors.AppendageDistance;
import com.mobilecomputing.src.Training.Training.FeatureExtractors.AppendageStretch;
import com.mobilecomputing.src.Training.Training.FeatureExtractors.HandInteraction;
import com.mobilecomputing.src.Training.Training.FeatureExtractors.VelocityFeatures;
import com.mobilecomputing.src.Training.Training.StaticTrainedParameters;
import com.sun.org.apache.xpath.internal.functions.FuncFalse;
import org.lwjgl.LWJGLException;

import java.io.IOException;

public class BaseClassifier implements HandTrackingListener {
    ContinuousTrainedParameters theCTSParams;
    StaticTrainedParameters theStaticParams;
    public final Object myLock = new Object();
    public volatile boolean theVar = true;
    public final String aUser;

    public BaseClassifier(String aUsername, ContinuousTrainedParameters myCTSParams, StaticTrainedParameters myStaticParams) {
        aUser = aUsername;
        theCTSParams = myCTSParams;
        theStaticParams = myStaticParams;
    }

    public static void Run(String aUser, ContinuousTrainedParameters myCTSParams, StaticTrainedParameters myStaticParams) {
        BaseClassifier myClassifier = new BaseClassifier(aUser, myCTSParams, myStaticParams);
        HandTrackingClient myClient = new HandTrackingClient();

        try {
            myClient.addListener(myClassifier);
            myClient.connect();
            while(myClassifier.theVar) {Thread.sleep(5000);}
        } catch (IOException e) {
            System.out.println("Could Not Connect: " + e.toString());
            return;
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            myClient.stop();
        }
    }

    private boolean FirstRound = true;
    private PoseMessage aFirstPoseMessage;
    @Override
    public void handleEvent(HandTrackingMessage message) {
        if (message.getType() == HandTrackingMessage.MessageType.POSE) {
            PoseMessage aPose = (PoseMessage) message;
            if(FirstRound) {
                aFirstPoseMessage = aPose;
                FirstRound = false;
                return;
            }

            if(aPose.getHandState(0).getPosition().y < 90 && aPose.getHandState(1).getPosition().y < 90) {
                aFirstPoseMessage = aPose;
                return;
            }

            AppendageStretch aLeftStretch = new AppendageStretch(aPose, 0);
            AppendageStretch aRightStretch = new AppendageStretch(aPose, 1);
            AppendageDistance aLeftDistances = new AppendageDistance(aPose, 0);
            AppendageDistance aRightDistances = new AppendageDistance(aPose, 1);
            VelocityFeatures aLeftVelocityFeatures = new VelocityFeatures(aFirstPoseMessage, aPose, 0);
            VelocityFeatures aRightVelocityFeatures = new VelocityFeatures(aFirstPoseMessage, aPose, 1);
            HandInteraction aHandInteraction = new HandInteraction(aPose);

            if(aHandInteraction.theHandsDistance < 100 && aLeftStretch.isHandFlat() && aRightStretch.isHandFlat() &&
                  aLeftVelocityFeatures.MovingInNegativeZDirection() && aRightVelocityFeatures.MovingInNegativeZDirection()) {
                System.out.println("Detected Hadouken");
                UDPClient.SendString(aUser, "hadouken");
            }

            if((aLeftStretch.isFacingCieling() && aLeftStretch.isHandFlat()) && !aLeftStretch.isFacingMonitor()
                    && aLeftVelocityFeatures.theMiddleDeltaVector.length() > 5 && aPose.getHandState(0).getPosition().y > 60) {
                System.out.println("Left Detected Ball Opening");
                UDPClient.SendString(aUser, "explosion");
            }

            if((aRightStretch.isFacingCieling() && aRightStretch.isHandFlat()) && !aRightStretch.isFacingMonitor()
                    && aRightVelocityFeatures.theMiddleDeltaVector.length() > 5 && aPose.getHandState(1).getPosition().y > 60) {
                //System.out.println("Right Detected Ball Opening");
            }

            if(aLeftDistances.isFist() && aLeftVelocityFeatures.MovingInPositiveXDirection() && aPose.getHandState(0).getPosition().y > 60) {
                //System.out.println("Left Fist Detected");
            }

            if(aRightDistances.isFist() && aRightVelocityFeatures.MovingInNegativeXDirection() && aPose.getHandState(1).getPosition().y > 60) {
                System.out.println("Right Fist Detected");
                UDPClient.SendString(aUser, "screenpunch");
            }

            if(aRightStretch.isFireGunGesture() && aPose.getHandState(1).getPosition().y > 60) {
                //System.out.println("Fire Gun Right Hand");
            }

            if(aLeftStretch.isFacingRight()) {
                //System.out.println("Left Hand Facing Right");
            }

            if(aRightStretch.isFacingLeft()) {
                //System.out.println("Right Hand Facing Left");
            }

            aFirstPoseMessage = aPose;
        }
    }

    @Override
    public synchronized void handleConnectionClosed() {
        theVar = false;
    }
}
