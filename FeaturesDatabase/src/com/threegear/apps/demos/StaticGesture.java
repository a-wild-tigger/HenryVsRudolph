package com.threegear.apps.demos;

import com.threegear.gloveless.network.PoseMessage;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class StaticGesture {
    private File path;
    private boolean dirty = false;
    List<PoseMessage> thePoseMessages;

    public StaticGesture(File aFile, List<PoseMessage> aPoseSet) {
        path = aFile;
        thePoseMessages = aPoseSet;
    }

    public List<PoseMessage> GetMessageSet() {
        return thePoseMessages;
    }

    public void SetMessageSet(List<PoseMessage> aMessages) {
        dirty = true;
        thePoseMessages = aMessages;
    }

    public void AddToMessageSet(List<PoseMessage> aMessages) {
        thePoseMessages.addAll(aMessages);
        dirty = true;
    }

    public static StaticGesture BuildGesture(File file) {
        List<PoseMessage> myMessages = new ArrayList<PoseMessage>();

        BufferedReader br = null;
        try {

            String sCurrentLine;

            br = new BufferedReader(new FileReader(file));
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

        return new StaticGesture(file, myMessages);
    }

    public File getPath() {
        return path;
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

    public boolean isDirty() {
        return dirty;
    }
}
