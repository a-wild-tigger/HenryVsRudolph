package com.mobilecomputing.src.Training.Persistence;

import au.com.bytecode.opencsv.CSVReader;
import au.com.bytecode.opencsv.CSVWriter;
import com.mobilecomputing.src.Training.Persistence.threegears.PoseMessage;
import com.mobilecomputing.src.Training.Training.ContinuousTrainedParameters;
import com.mobilecomputing.src.Training.Training.StaticTrainedParameters;

import java.io.*;
import java.util.*;

public class Persistence {
    private final String theDirectoryName;
    private final String theConfigurationName;

    private Map<String, Map<String, ContinuousGesture>> theContinuousGestureMap = new HashMap<String, Map<String, ContinuousGesture>>();
    private Map<String, Map<String, StaticGesture>> theStaticGestureMap = new HashMap<String, Map<String, StaticGesture>>();
    private Map<String, StaticTrainedParameters> theStaticModelsMap = new HashMap<String, StaticTrainedParameters>();
    private Map<String, ContinuousTrainedParameters> theContinuousModelsMap = new HashMap<String, ContinuousTrainedParameters>();

    private Set<String> theUserSet = new HashSet<String>();
    public Persistence(String aDirectoryName, String aConfigurationName) {
        theDirectoryName = aDirectoryName;
        theConfigurationName = aConfigurationName;
    }

    // We store config information as User,Gesture,Static/Continuous
    public void Start() throws IOException, ClassNotFoundException {
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

            if(aStaticOrCTS.equals("StaticModel")) {
                theStaticModelsMap.put(aUserName, StaticTrainedParameters.BuildParams(aUserName, theDirectoryName));
                theUserSet.add(aUserName);
            }

            if(aStaticOrCTS.equals("ContinuousModel")) {
                theContinuousModelsMap.put(aUserName, ContinuousTrainedParameters.BuildParams(aUserName, theDirectoryName));
                theUserSet.add(aUserName);
            }
        }

        reader.close();
    }

    public void Stop() throws IOException {
        File theDir = new File(theDirectoryName, theConfigurationName);
        CSVWriter writer =  new CSVWriter(new FileWriter(theDir), ',');

        for (String aUsername : theStaticModelsMap.keySet()) {
            String[] entries = (aUsername + "#ALL#StaticModel").split("#");
            writer.writeNext(entries);
            theStaticModelsMap.get(aUsername).Persist();
        }

        for(String aUser : theContinuousModelsMap.keySet()) {
            String[] entries = (aUser + "#ALL#ContinuousModel").split("#");
            writer.writeNext(entries);
            theContinuousModelsMap.get(aUser).Persist();
        }

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

    public void SetContinuousTrainedModel(String aUsername, ContinuousTrainedParameters aParams) {
        theContinuousModelsMap.put(aUsername, aParams);
    }

    public void SetStaticTrainedModel(String aUsername, StaticTrainedParameters aParams) {
        theStaticModelsMap.put(aUsername, aParams);
    }

    public void CreateUser(String aUsername) {
        theUserSet.add(aUsername);
        theStaticGestureMap.put(aUsername, new HashMap<String, StaticGesture>());
        theContinuousGestureMap.put(aUsername, new HashMap<String, ContinuousGesture>());
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
        if(!theStaticGestureMap.containsKey(theCurrentUser)) { return new HashSet<String>(); }
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
        if(!theContinuousGestureMap.containsKey(theCurrentUser)) {
            return new HashSet<String>();
        }
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

    public Map<String, List<List<PoseMessage>>> GetAllCTSFeatures(String theCurrentUser) {
        Map<String, List<List<PoseMessage>>> myDataset = new HashMap<String, List<List<PoseMessage>>>();
        Set<String> theCTSGests = GetAvailableContinuousGestures(theCurrentUser);
        for (String theCTSGest : theCTSGests) {
            myDataset.put(theCTSGest, GetContinuousGestureSet(theCurrentUser, theCTSGest));
        }

        return myDataset;
    }

    public Map<String, List<PoseMessage>> GetAllStaticFeatures(String theCurrentUser) {
        Map<String, List<PoseMessage>> myDataset = new HashMap<String, List<PoseMessage>>();
        Set<String> theStaticGests = GetAvailableGestures(theCurrentUser);
        for (String theStaticGest : theStaticGests) {
            myDataset.put(theStaticGest, GetStaticGestureSet(theCurrentUser, theStaticGest));
        }

        return myDataset;
    }

    public ContinuousTrainedParameters GetTrainedCTSParams(String theCurrentUser) throws IOException, ClassNotFoundException {
        return ContinuousTrainedParameters.BuildParams(theCurrentUser, theDirectoryName);
    }

    public StaticTrainedParameters GetTrainedStaticParams(String theCurrentUser) throws IOException, ClassNotFoundException {
        return StaticTrainedParameters.BuildParams(theCurrentUser, theDirectoryName);
    }
}
