package com.mobilecomputing.src.Training.Classification;

import com.mobilecomputing.src.Training.Persistence.threegears.HandTrackingClient;
import com.mobilecomputing.src.Training.Persistence.threegears.HandTrackingListener;
import com.mobilecomputing.src.Training.Persistence.threegears.HandTrackingMessage;
import com.mobilecomputing.src.Training.Persistence.threegears.PoseMessage;
import com.mobilecomputing.src.Training.Training.ContinuousTrainedParameters;
import com.mobilecomputing.src.Training.Training.HMMModels.CodePoints;
import com.mobilecomputing.src.Training.Training.StaticTrainedParameters;

import java.io.IOException;

public class BaseHMMClassifier implements HandTrackingListener {
    ContinuousTrainedParameters theCTSParams;
    StaticTrainedParameters theStaticParams;
    public final Object myLock = new Object();
    public volatile boolean theVar = true;
    public final String aUser;

    public BaseHMMClassifier(String username, ContinuousTrainedParameters myCTSParams, StaticTrainedParameters myStaticParams) {
        theCTSParams = myCTSParams;
        theStaticParams = myStaticParams;
        aUser = username;
    }

    public static void Run(String aUsername, ContinuousTrainedParameters myCTSParams, StaticTrainedParameters myStaticParams) {
        BaseClassifier myClassifier = new BaseClassifier(aUsername, myCTSParams, myStaticParams);
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

            int aPoint = CodePoints.GenerateCodePoint(aFirstPoseMessage, aPose, 0);

        }
    }

    @Override
    public synchronized void handleConnectionClosed() {
        theVar = false;
    }
}
