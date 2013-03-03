package com.mobilecomputing.src.Training.Persistence;

import com.mobilecomputing.src.Training.Persistence.threegears.PoseMessage;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class ContinuousGesture {
    private final File thePath;
    private boolean dirty;
    List<List<PoseMessage>> theContinuousSampling;

    private ContinuousGesture(File aFile, List<List<PoseMessage>> aPoseSet, boolean aDirty) {
        thePath = aFile;
        theContinuousSampling = aPoseSet;
        dirty = aDirty;
    }

    public static File GetPathName(String aUserName, String aGestureName, String theDirName) {
        return new File(new File(theDirName), ("/ContinuousGestureCapture_" + aUserName + "_" + aGestureName + ".txt"));
    }

    public static ContinuousGesture BuildGesture(String aUserName, String aGestureName, String theDirectoryName) {
        List<List<PoseMessage>> myMessages = new ArrayList<List<PoseMessage>>();
        BufferedReader br = null;
        File aFile = GetPathName(aUserName, aGestureName, theDirectoryName);
        try {
            String sCurrentLine;

            br = new BufferedReader(new FileReader(aFile));
            List<PoseMessage> myMessagese = new ArrayList<PoseMessage>();
            while ((sCurrentLine = br.readLine()) != null) {
                if(sCurrentLine.equals("New Sample")) {
                    if(myMessagese.size() != 0) {
                        myMessages.add(myMessagese);
                    }

                    myMessagese = new ArrayList<PoseMessage>();
                } else {
                    PoseMessage myMsg = (PoseMessage) PoseMessage.deserialize(sCurrentLine);
                    myMessagese.add(myMsg);
                }
            }

            if(myMessagese.size() != 0) {
                myMessages.add(myMessagese);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (br != null)br.close();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }

        return new ContinuousGesture(aFile, myMessages, false);
    }

    public static ContinuousGesture CreateNewGestureFile(String username, String gestureName, String theDirectoryName) {
        return new ContinuousGesture(GetPathName(username, gestureName, theDirectoryName), new ArrayList<List<PoseMessage>>(), true);
    }

    public void Persist() throws IOException {
        FileWriter fw = new FileWriter(thePath.getAbsoluteFile());
        BufferedWriter bw = new BufferedWriter(fw);

        for(int i = 0; i!= theContinuousSampling.size(); i++) {
            bw.write("New Sample");
            bw.newLine();

            List<PoseMessage> myMessages = theContinuousSampling.get(i);
            for(int j = 0; j!= myMessages.size(); j++) {
                bw.write(myMessages.get(j).serialize());
                bw.newLine();
            }
        }

        bw.close();
        dirty = false;
    }

    public void AddToMessageSet(List<List<PoseMessage>> myMessagesToSave) {
        theContinuousSampling.addAll(myMessagesToSave);
        dirty = true;
    }

    public void SetMessageSet(List<List<PoseMessage>> myMessages) {
        theContinuousSampling = myMessages;
        dirty = true;
    }

    public boolean isDirty() {
        return dirty;
    }

    public List<List<PoseMessage>> GetGestureSet() {
        return theContinuousSampling;
    }
}
