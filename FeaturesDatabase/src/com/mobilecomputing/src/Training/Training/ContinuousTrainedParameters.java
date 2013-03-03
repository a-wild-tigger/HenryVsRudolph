package com.mobilecomputing.src.Training.Training;

import com.mobilecomputing.src.Training.Persistence.threegears.PoseMessage;
import com.sun.xml.internal.ws.server.ServerRtException;

import java.io.*;
import java.util.List;
import java.util.Map;

public class ContinuousTrainedParameters implements Serializable {
    private File thePath;
    private ContinuousTrainedParameters(File aFile) {
        thePath = aFile;
    }

    public void Persist() throws IOException {
        FileOutputStream fileOut = new FileOutputStream(thePath);
        ObjectOutputStream out = new ObjectOutputStream(fileOut);
        out.writeObject(this);
        out.close();
        fileOut.close();
    }

    public static ContinuousTrainedParameters BuildParams(String aUserName, String theDirectoryName) throws IOException, ClassNotFoundException {
        FileInputStream fileIn = new FileInputStream(new File(new File(theDirectoryName), ("/ContinuousModel_" + aUserName + ".ser")));
        ObjectInputStream out = new ObjectInputStream(fileIn);
        ContinuousTrainedParameters myParams = (ContinuousTrainedParameters) out.readObject();
        out.close();
        fileIn.close();
        return myParams;
    }

    public static ContinuousTrainedParameters GenerateParams(Map<String, List<List<PoseMessage>>> myCTSFeatures, String aUsername, String aDirName) {
        File myFile = new File(new File(aDirName), ("/ContinuousModel_" + aUsername + ".ser"));
        return new ContinuousTrainedParameters(myFile);
    }
}
