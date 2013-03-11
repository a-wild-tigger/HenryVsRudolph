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

    public BaseClassifier(ContinuousTrainedParameters myCTSParams, StaticTrainedParameters myStaticParams) {
        theCTSParams = myCTSParams;
        theStaticParams = myStaticParams;
    }

    public static void Run(ContinuousTrainedParameters myCTSParams, StaticTrainedParameters myStaticParams) {
        BaseClassifier myClassifier = new BaseClassifier(myCTSParams, myStaticParams);
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

            AppendageStretch aLeftStretch = new AppendageStretch(aPose, 0);
            AppendageStretch aRightStretch = new AppendageStretch(aPose, 1);
            AppendageDistance aLeftDistances = new AppendageDistance(aPose, 0);
            AppendageDistance aRightDistances = new AppendageDistance(aPose, 1);
            VelocityFeatures aLeftVelocityFeatures = new VelocityFeatures(aFirstPoseMessage, aPose, 0);
            VelocityFeatures aRightVelocityFeatures = new VelocityFeatures(aFirstPoseMessage, aPose, 0);
            HandInteraction aHandInteraction = new HandInteraction(aPose);

            if(aHandInteraction.theHandsDistance < 100 && aLeftStretch.isHandFlat() && aRightStretch.isHandFlat()
               && aLeftStretch.isFacingMonitor() && aRightStretch.isFacingMonitor()) {
                System.out.println("Detected Hadouken");
            }

            if((aLeftStretch.isFacingCieling() && aLeftStretch.isHandFlat()) && !aLeftStretch.isFacingMonitor()) {
                System.out.println("Left Detected Ball Opening");
            }

            if((aRightStretch.isFacingCieling() && aRightStretch.isHandFlat()) && !aRightStretch.isFacingMonitor()) {
                System.out.println("Left Detected Ball Opening");
            }

            if(aLeftStretch.isFacingRight()) {
                System.out.println("Left Hand Facing Right");
            }

            if(aRightStretch.isFacingLeft()) {
                System.out.println("Right Hand Facing Left");
            }

            aFirstPoseMessage = aPose;
        }
    }

    @Override
    public synchronized void handleConnectionClosed() {
        theVar = false;
    }
}
