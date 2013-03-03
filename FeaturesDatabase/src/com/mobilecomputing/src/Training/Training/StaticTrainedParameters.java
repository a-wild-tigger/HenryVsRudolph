package com.mobilecomputing.src.Training.Training;

import com.mobilecomputing.src.Training.Persistence.threegears.PoseMessage;

import java.io.*;
import java.util.List;
import java.util.Map;

public class StaticTrainedParameters implements Serializable {
    private File thePath;
    public StaticTrainedParameters(File aPath) {
        thePath = aPath;
    }

    public void Persist() throws IOException {
        FileOutputStream fileOut = new FileOutputStream(thePath);
        ObjectOutputStream out = new ObjectOutputStream(fileOut);
        out.writeObject(this);
        out.close();
        fileOut.close();
    }

    public static StaticTrainedParameters BuildParams(String aUserName, String theDirectoryName) throws IOException, ClassNotFoundException {
        FileInputStream fileIn = new FileInputStream(new File(new File(theDirectoryName), ("/StaticModel_" + aUserName + ".ser")));
        ObjectInputStream out = new ObjectInputStream(fileIn);
        StaticTrainedParameters myParams = (StaticTrainedParameters) out.readObject();
        out.close();
        fileIn.close();
        return myParams;
    }

    public static StaticTrainedParameters GenerateParams(Map<String, List<PoseMessage>> myStaticFeatures, String aUsername, String theDirectoryName) {
        File myFile = new File(new File(theDirectoryName), ("/StaticModel_" + aUsername + ".ser"));
        return new StaticTrainedParameters(myFile);
    }
}
