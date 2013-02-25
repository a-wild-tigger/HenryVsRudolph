package com.threegear.apps.demos;

import au.com.bytecode.opencsv.CSVReader;
import au.com.bytecode.opencsv.CSVWriter;
import com.sun.xml.internal.fastinfoset.util.StringArray;
import com.threegear.gloveless.network.PoseMessage;

import java.io.*;
import java.util.*;

public class StaticPersistence {
    private final String theDirectoryName;
    private final String theConfigurationName;

    private Map<String, Map<String, StaticGesture>> theStaticGestureMap = new HashMap<String, Map<String, StaticGesture>>();
    private Map<String, Map<String, ContinuousGesture>> theContinuousGestureMap = new HashMap<String, Map<String, ContinuousGesture>>();
    private Set<String> theUserSet = new HashSet<String>();

    public StaticPersistence(String aDirectoryName, String aConfigurationName) {
        theDirectoryName = aDirectoryName;
        theConfigurationName = aConfigurationName;
    }

    public Set<String> GetRegisteredUsers() {
        return theUserSet;
    }

    private Set<String> GetStaticGestures(String aUserName) {
        if(!theStaticGestureMap.containsKey(aUserName)) {
            return new HashSet<String>();
        }

        else return theStaticGestureMap.get(aUserName).keySet();
    }

    private Set<String> GetContinuousGestures(String aUserName) {
        if(!theContinuousGestureMap.containsKey(aUserName)) {
            return new HashSet<String>();
        }

        else return theContinuousGestureMap.get(aUserName).keySet();
    }

    private StaticGesture GetStaticGesture(String aUsername, String aGestureName) {
        return theStaticGestureMap.get(aUsername).get(aGestureName);
    }

    private ContinuousGesture GetContinuousGesture(String aUsername, String aGestureName) {
        return theContinuousGestureMap.get(aUsername).get(aGestureName);
    }

    // We store config information as User,Gesture,Static/Continuous,Path
    public void Start() throws IOException {
        File theDir = new File(theDirectoryName, theConfigurationName);
        CSVReader reader = new CSVReader(new FileReader(theDir));
        String [] nextLine;
        while ((nextLine = reader.readNext()) != null) {
            String aUserName = nextLine[0];
            String aGestureName = nextLine[1];
            String aStaticOrCTS = nextLine[2];
            String aPath = nextLine[3];

            if(aStaticOrCTS == "Static") {
                if(!theStaticGestureMap.containsKey(aUserName)) {
                    theStaticGestureMap.put(aUserName, new HashMap<String, StaticGesture>());
                }
                theStaticGestureMap.get(aUserName).put(aGestureName, StaticGesture.BuildGesture(new File(theDirectoryName, aPath)));
                theUserSet.add(aUserName);
            } else if(aStaticOrCTS == "Continuous") {
                if(!theContinuousGestureMap.containsKey(aUserName)) {
                    theContinuousGestureMap.put(aUserName, new HashMap<String, ContinuousGesture>());
                }
                theContinuousGestureMap.get(aUserName).put(aGestureName, ContinuousGesture.BuildGesture(new File(theDirectoryName, aPath)));
                theUserSet.add(aUserName);
            }
        }

        reader.close();
    }

    public void SaveAll() throws IOException {
        File theDir = new File(theDirectoryName, theConfigurationName);
        CSVWriter writer = new CSVWriter(new FileWriter(theDir), '\t');

        for (String aUser : theStaticGestureMap.keySet()) {
            Map<String, StaticGesture> aGesture = theStaticGestureMap.get(aUser);
            for (String aGestureName : aGesture.keySet()) {
                StaticGesture aInfo = aGesture.get(aGestureName);
                File aPath = aInfo.getPath();
                String[] entries = (aUser + "#" + aGestureName + "#Static#" + aPath).split("#");
                writer.writeNext(entries);

                if(aInfo.isDirty()) {
                    aInfo.Persist();
                }
            }
        }

        for(String aUser : theContinuousGestureMap.keySet()) {
            Map<String, ContinuousGesture> aGesture = theContinuousGestureMap.get(aUser);
            for (String aGestureName : aGesture.keySet()) {
                ContinuousGesture aInfo = aGesture.get(aGestureName);
                String aPath = aInfo.getPath();
                String[] entries = (aUser + "#" + aGestureName + "#Static#" + aPath).split("#");
                writer.writeNext(entries);

                if(aInfo.isDirty()) {
                    aInfo.Persist();
                }
            }
        }

        writer.close();
    }

    public void Stop() {
        theStaticGestureMap = new HashMap<String, Map<String, StaticGesture>>();
        theContinuousGestureMap = new HashMap<String, Map<String, ContinuousGesture>>();
        theUserSet = new HashSet<String>();
    }
}
