package com.mobilecomputing.src.Training.Persistence;

import com.mobilecomputing.src.Training.Persistence.threegears.PoseMessage;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class StaticGesture {
    private final File path;
    private boolean dirty;
    private List<PoseMessage> thePoseMessages;

    private StaticGesture(File aFile, List<PoseMessage> aPoseSet, boolean isdirty) {
        path = aFile;
        thePoseMessages = aPoseSet;
        dirty = isdirty;
    }

    public static File GetPathName(String aUserName, String aGestureName, String theDirName) {
        return new File(new File(theDirName), ("/StaticGestureCapture_" + aUserName + "_" + aGestureName + ".txt"));
    }

    public static StaticGesture BuildGesture(String aUserName, String aGestureName, String theDirectoryName) {
        List<PoseMessage> myMessages = new ArrayList<PoseMessage>();
        BufferedReader br = null;
        File aFile = GetPathName(aUserName, aGestureName, theDirectoryName);
        try {
            String sCurrentLine;

            br = new BufferedReader(new FileReader(aFile));
            while ((sCurrentLine = br.readLine()) != null) {
                PoseMessage myMsg = (PoseMessage) PoseMessage.deserialize(sCurrentLine);
                myMessages.add(myMsg);
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

        return new StaticGesture(aFile, myMessages, false);
    }

    public static StaticGesture CreateNewGestureFile(String username, String gestureName, String theDirectoryName) {
        return new StaticGesture(GetPathName(username, gestureName, theDirectoryName), new ArrayList<PoseMessage>(), true);
    }

    public void Persist() throws IOException {
        FileWriter fw = new FileWriter(path.getAbsoluteFile());
        BufferedWriter bw = new BufferedWriter(fw);

        for(int i = 0; i!= thePoseMessages.size(); i++) {
            bw.write(thePoseMessages.get(i).serialize());
            bw.newLine();
        }

        bw.close();
        dirty = false;
    }

    public void AddToMessageSet(List<PoseMessage> myMessagesToSave) {
        thePoseMessages.addAll(myMessagesToSave);
        dirty = true;
    }

    public void SetMessageSet(List<PoseMessage> myMessages) {
        thePoseMessages = myMessages;
        dirty = true;
    }

    public boolean isDirty() {
        return dirty;
    }

    public List<PoseMessage> GetGestureSet() {
        return thePoseMessages;
    }
}
