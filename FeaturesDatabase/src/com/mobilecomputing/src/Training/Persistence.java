package com.mobilecomputing.src.Training;

import au.com.bytecode.opencsv.CSVReader;
import au.com.bytecode.opencsv.CSVWriter;
import com.threegear.gloveless.network.PoseMessage;

import java.io.*;
import java.util.*;

public class Persistence {
    private final String theDirectoryName;
    private final String theConfigurationName;

    private Map<String, Map<String, ContinuousGesture>> theContinuousGestureMap = new HashMap<String, Map<String, ContinuousGesture>>();
    private Map<String, Map<String, StaticGesture>> theStaticGestureMap = new HashMap<String, Map<String, StaticGesture>>();
    private Set<String> theUserSet = new HashSet<String>();

    public Persistence(String aDirectoryName, String aConfigurationName) {
        theDirectoryName = aDirectoryName;
        theConfigurationName = aConfigurationName;
    }

    // We store config information as User,Gesture,Static/Continuous
    public void Start() throws IOException {
        File theDir = new File(theDirectoryName, theConfigurationName);
        CSVReader reader = new CSVReader(new FileReader(theDir), ',');
        String [] nextLine;
        while ((nextLine = reader.readNext()) != null) {
            String aUserName = nextLine[0];
            String aGestureName = nextLine[1];
            String aStaticOrCTS = nextLine[2];

            if(aStaticOrCTS.equals("Static")) {
                if(!theStaticGestureMap.containsKey(aUserName)) {
                    theStaticGestureMap.put(aUserName, new HashMap<String, StaticGesture>());
                }

                StaticGesture aGesture = StaticGesture.BuildGesture(aUserName, aGestureName, theDirectoryName);
                theStaticGestureMap.get(aUserName).put(aGestureName, aGesture);
                theUserSet.add(aUserName);
            }

            if(aStaticOrCTS.equals("Continuous")) {
                if(!theContinuousGestureMap.containsKey(aUserName)) {
                    theContinuousGestureMap.put(aUserName, new HashMap<String, ContinuousGesture>());
                }

                ContinuousGesture aGesture = ContinuousGesture.BuildGesture(aUserName, aGestureName, theDirectoryName);
                theContinuousGestureMap.get(aUserName).put(aGestureName, aGesture);
                theUserSet.add(aUserName);
            }
        }

        reader.close();
    }

    public void Stop() throws IOException {
        File theDir = new File(theDirectoryName, theConfigurationName);
        CSVWriter writer =  new CSVWriter(new FileWriter(theDir), ',');

        for (String aUser : theStaticGestureMap.keySet()) {
            Map<String, StaticGesture> aGesture = theStaticGestureMap.get(aUser);
            for (String aGestureName : aGesture.keySet()) {
                StaticGesture aInfo = aGesture.get(aGestureName);
                String[] entries = (aUser + "#" + aGestureName + "#Static").split("#");
                writer.writeNext(entries);

                if(aInfo.isDirty()) {
                    aInfo.Persist();
                }
            }
        }

        for (String aUser : theContinuousGestureMap.keySet()) {
            Map<String, ContinuousGesture> aGesture = theContinuousGestureMap.get(aUser);
            for (String aGestureName : aGesture.keySet()) {
                ContinuousGesture aInfo = aGesture.get(aGestureName);
                String[] entries = (aUser + "#" + aGestureName + "#Continuous").split("#");
                writer.writeNext(entries);

                if(aInfo.isDirty()) {
                    aInfo.Persist();
                }
            }
        }

        writer.close();
    }

    public void CreateUser(String aUsername) {
        theUserSet.add(aUsername);
        theStaticGestureMap.put(aUsername, new HashMap<String, StaticGesture>());
    }

    public boolean DoesUserExist(String myUserName) {
        return theUserSet.contains(myUserName);
    }

    public void PushStaticGestures(String username, String gestureName, List<PoseMessage> myMessagesToSave) {
        if(!theStaticGestureMap.containsKey(username)) {
            theStaticGestureMap.put(username, new HashMap<String, StaticGesture>());
        }

        if(!theStaticGestureMap.get(username).containsKey(gestureName)) {
            StaticGesture myGesture = StaticGesture.CreateNewGestureFile(username, gestureName, theDirectoryName);
            theStaticGestureMap.get(username).put(gestureName, myGesture);
        }

        theStaticGestureMap.get(username).get(gestureName).AddToMessageSet(myMessagesToSave);
    }

    public Set<String> GetAvailableGestures(String theCurrentUser) {
        return theStaticGestureMap.get(theCurrentUser).keySet();
    }

    public boolean UserHasGesture(String theCurrentUser, String aGestureReq) {
        return theStaticGestureMap.get(theCurrentUser).containsKey(aGestureReq);
    }

    public List<PoseMessage> GetStaticGestureSet(String theCurrentUser, String aGestureReq) {
        return theStaticGestureMap.get(theCurrentUser).get(aGestureReq).GetGestureSet();
    }

    public void SetStaticGestureSet(String theCurrentUser, String aGestureReq, List<PoseMessage> theMessages) {
        theStaticGestureMap.get(theCurrentUser).get(aGestureReq).SetMessageSet(theMessages);
    }

    public void PushContinuousGestures(String theCurrentUser, String gesture, List<List<PoseMessage>> myMessagesToSave) {
        if(!theContinuousGestureMap.containsKey(theCurrentUser)) {
            theContinuousGestureMap.put(theCurrentUser, new HashMap<String, ContinuousGesture>());
        }

        if(!theContinuousGestureMap.get(theCurrentUser).containsKey(gesture)) {
            ContinuousGesture myGesture = ContinuousGesture.CreateNewGestureFile(theCurrentUser, gesture, theDirectoryName);
            theContinuousGestureMap.get(theCurrentUser).put(gesture, myGesture);
        }

        theContinuousGestureMap.get(theCurrentUser).get(gesture).AddToMessageSet(myMessagesToSave);
    }

    public Set<String> GetAvailableContinuousGestures(String theCurrentUser) {
        return theContinuousGestureMap.get(theCurrentUser).keySet();
    }

    public boolean UserHasContinuousGesture(String theCurrentUser, String aGestureReq) {
        return theContinuousGestureMap.get(theCurrentUser).containsKey(aGestureReq);
    }

    public List<List<PoseMessage>> GetContinuousGestureSet(String theCurrentUser, String aGestureReq) {
        return theContinuousGestureMap.get(theCurrentUser).get(aGestureReq).GetGestureSet();
    }

    public void SetContinuousGestureSet(String theCurrentUser, String aGestureReq, List<List<PoseMessage>> theMessages) {
        theContinuousGestureMap.get(theCurrentUser).get(aGestureReq).SetMessageSet(theMessages);
    }
}
