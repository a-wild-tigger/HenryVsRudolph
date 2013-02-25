package com.threegear.apps.demos;

import com.threegear.gloveless.network.HandTrackingClient;
import com.threegear.gloveless.network.PoseMessage;
import org.lwjgl.LWJGLException;

import java.io.File;
import java.io.IOException;
import java.util.List;

public class SamplingExample {
    public static void main(String[] args) throws IOException, LWJGLException {
        String databaseDirectory = "DatabaseDirectory";
        String configName = "Configuration.csv";
        String Username = "Anil";
        String GestureName = "GunPose";

        File myDatabaseDirectory = new File(databaseDirectory);
        if(!myDatabaseDirectory.exists()) {
            myDatabaseDirectory.mkdirs();
        }

        File myConfigurationFile = new File(databaseDirectory, configName);
        if(!myConfigurationFile.exists()) {
            myConfigurationFile.createNewFile();
        }

        HandTrackingClient myClient = new HandTrackingClient();
        Recorder myRecorder = new Recorder();
        myClient.addListener(myRecorder);
        myClient.connect();

        myRecorder.Run();
        myClient.stop();

        List<PoseMessage> myMessages = myRecorder.GetMessages();
        StaticPersistence myPersistence = new StaticPersistence(databaseDirectory, configName);
        myPersistence.Start();



        myPersistence.Stop();
    }
}
