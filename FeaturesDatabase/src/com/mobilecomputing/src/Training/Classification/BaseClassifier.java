package com.mobilecomputing.src.Training.Classification;

import com.mobilecomputing.src.Training.Persistence.threegears.HandTrackingClient;
import com.mobilecomputing.src.Training.Persistence.threegears.HandTrackingListener;
import com.mobilecomputing.src.Training.Persistence.threegears.HandTrackingMessage;
import com.mobilecomputing.src.Training.Training.ContinuousTrainedParameters;
import com.mobilecomputing.src.Training.Training.StaticTrainedParameters;
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
            synchronized (myClassifier.myLock) {
                while(myClassifier.theVar) {
                    myClassifier.myLock.wait();
                }
            }
        } catch (IOException e) {
            System.out.println("Could Not Connect: " + e.toString());
            return;
        } catch (InterruptedException e) {
            return;
        } finally {
            myClient.stop();
        }
    }

    @Override
    public void handleEvent(HandTrackingMessage message) {

    }

    @Override
    public synchronized void handleConnectionClosed() { }
}
