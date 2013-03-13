package com.mobilecomputing.src.Training.Classification;

import com.mobilecomputing.src.Training.Persistence.drawing.AbstractDisplay;
import com.mobilecomputing.src.Training.Persistence.threegears.HandTrackingClient;
import com.mobilecomputing.src.Training.Persistence.threegears.HandTrackingListener;
import com.mobilecomputing.src.Training.Persistence.threegears.HandTrackingMessage;
import com.mobilecomputing.src.Training.Persistence.threegears.PoseMessage;
import com.mobilecomputing.src.Training.Training.ContinuousTrainedParameters;
import com.mobilecomputing.src.Training.Training.FeatureExtractors.*;
import com.mobilecomputing.src.Training.Training.HMMModels.HMMModel;
import com.mobilecomputing.src.Training.Training.HMMModels.Viterbi;
import com.mobilecomputing.src.Training.Training.StaticTrainedParameters;
import org.lwjgl.LWJGLException;
import org.lwjgl.opengl.Display;

import javax.vecmath.Matrix4f;
import javax.vecmath.Point3f;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class ContinuousClassifier implements HandTrackingListener {
    private static final int DISPLAY_FRAMERATE = 60;
    private boolean finished = false;
    public final AbstractDisplay myDisplay = new AbstractDisplay();

    public static void Run(String aUsername, ContinuousTrainedParameters myCTSParams, StaticTrainedParameters myStaticParams) throws LWJGLException, IOException {
        HandTrackingClient myClient = new HandTrackingClient();
        ContinuousClassifier myClassifier = new ContinuousClassifier(aUsername, myCTSParams, myStaticParams);
        myClient.addListener(myClassifier);
        myClient.connect();
        myClassifier.Run();
        myClient.stop();
    }

    String aUser;
    ContinuousTrainedParameters theCTSParams;
    StaticTrainedParameters theStaticParams;
    Map<String, Viterbi> theViterbiMap = new HashMap<String, Viterbi>();
    public ContinuousClassifier(String aUsername, ContinuousTrainedParameters myCTSParams, StaticTrainedParameters myStaticParams) {
        aUser = aUsername;
        theCTSParams = myCTSParams;
        theStaticParams = myStaticParams;

        for (String s : myCTSParams.theSerializableMap.keySet()) {
            theViterbiMap.put(s, (Viterbi) myCTSParams.theSerializableMap.get(s));
        }
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

            int codePoint = HMMModel.GenerateCodePoint(myOldMessage, message, 1);
            for (String s : theViterbiMap.keySet()) {
                theViterbiMap.get(s).insert(codePoint);
            }

            theViterbiScores = GetScores();
            myOldMessage = message;
        }
    }

    String theViterbiScores = "";
    private String GetScores() {
        StringBuilder myBuilder = new StringBuilder();
        for (String s : theViterbiMap.keySet()) {
            Viterbi aViterbi = theViterbiMap.get(s);
            double aScore = aViterbi.prevChain[aViterbi.maxLikelihood()];
            myBuilder.append(s + " : ( Estimated State, Likelihood ) = ( " + aViterbi.maxLikelihood() + " , " + VectorOps.RenderDouble(aScore) + " )\n");
        }

        return myBuilder.toString();
    }

    @Override
    public void handleConnectionClosed() {
        //To change body of implemented methods use File | Settings | File Templates.
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
                        myDisplay.render("Left: " + myRightVelocityFeatures.toString() + "\n\nRight: " + myLeftVelocityFeatures.toString() + "\n" + theViterbiScores );
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
