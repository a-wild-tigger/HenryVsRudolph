package com.threegear.apps.demos;

import com.mobilecomputing.src.inout.output.StaticGestureRecorder;
import com.threegear.gloveless.network.HandTrackingClient;
import com.threegear.gloveless.network.PoseMessage;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Scanner;

public class MainController {
    public static void main(String[] args) throws IOException {
        String databaseDirectory = "DatabaseDirectory";
        String configName = "Configuration.csv";

        File myDatabaseDirectory = new File(databaseDirectory);
        if(!myDatabaseDirectory.exists()) {
            myDatabaseDirectory.mkdirs();
        }

        File myConfigurationFile = new File(databaseDirectory, configName);
        if(!myConfigurationFile.exists()) {
            myConfigurationFile.createNewFile();
        }

        Persistence myPersistence = new Persistence(databaseDirectory, configName);

        myPersistence.Start();

        MainController myController = new MainController(myPersistence);
        myController.MainLoop();

        myPersistence.Stop();
    }

    private final Scanner theInput = new Scanner(System.in);
    private final Persistence thePersistence;
    private final StaticGestureRecorder theStaticRecorder;
    private final StaticGestureViewer theViewer;
    private String theCurrentUser;

    public MainController(Persistence aPersistence) {
        thePersistence = aPersistence;
        theViewer = new StaticGestureViewer();
        theStaticRecorder = new StaticGestureRecorder();
    }

    private void Start() {
        System.out.println("Enter User Name: ");
        String myUserName = theInput.nextLine();
        System.out.println(String.format("Thanks {0}. We are looking you up in the database.", myUserName));

        boolean myUserExists = thePersistence.DoesUserExist(myUserName);
        if(myUserExists) {
            System.out.println(String.format("We located you in the Database, {0}.", myUserName));
        } else {
            System.out.println(String.format("We were not able to find you, {0}. We will create a profile for you now.", myUserName));
            thePersistence.CreateUser(myUserName);
        }

        theCurrentUser = myUserName;
    }

    public void MainLoop() throws IOException {
        Start();
        while(HomeScreen());
        Stop();
    }

    private boolean HomeScreen() {
        System.out.println("Input Options: ");
        System.out.println("\t1 : Record Static Gesture");
        System.out.println("\t2 : View/Edit Existing Static Gestures");
        System.out.println("\t3 : Quit");

        int aValue = theInput.nextInt();
        if(aValue > 3 || aValue <= 0) {
            System.out.println("Invalid Input.");
            return HomeScreen();
        } else {
            switch(aValue) {
                case 1: RecordStaticGesture(); break;
                case 2: ViewEditStaticGestures(); break;
                case 3: return true;
            }
        }

        return false;
    }

    private void RecordStaticGesture() {
        System.out.println("The Current Gestures Available for {0} are: ");
        for (String aGesture : thePersistence.GetAvailableGestures(theCurrentUser)) {
            System.out.println("\t" + aGesture);
        }

        System.out.println("Please Input the Gesture you want to Record or Create a New One.");
        String myGesture = theInput.nextLine();

        System.out.println(String.format("We are starting the Recorder for Gesture, {0}. Hit (x) to Leave.", myGesture));

        List<PoseMessage> myMessages = theStaticRecorder.Record();
        List<PoseMessage> myMessagesToSave = theViewer.SelectImagesToKeep(myMessages);
        thePersistence.PushStaticGestures(theCurrentUser, myGesture, myMessagesToSave);
    }

    private void ViewEditStaticGestures() {
        List<String> theAvailableGestures = thePersistence.GetAvailableGestures(theCurrentUser);
        if(theAvailableGestures.size() == 0) {
            System.out.println("No Gestures are available.");
            return;
        }

        System.out.println("The Current Gestures Available for {0} are: ");
        for (String aGesture : theAvailableGestures) {
            System.out.println("\t" + aGesture);
        }

        System.out.println("Please Input the Gesture you want to view/edit.");
        String aGestureReq = theInput.nextLine();

        if(!thePersistence.UserHasGesture(theCurrentUser, aGestureReq)) {
            System.out.println(String.format("The Gesture {0} Does not Exist!", aGestureReq));
            return;
        }

        List<PoseMessage> theCurrentGestures = thePersistence.GetStaticGestureSet(theCurrentUser, aGestureReq);
        List<PoseMessage> theMessages = theViewer.ViewImages(theCurrentGestures);
        thePersistence.SetStaticGestureSet(theCurrentUser, aGestureReq, theMessages);
    }

    private void Stop() throws IOException {
        System.out.println("Would you like to save changes to the Database Session? (y,n)");
        String aValue = theInput.nextLine();
        if(aValue == "y\n") {
            System.out.println("Saving Database");
        }
        thePersistence.SaveAll();
    }
}
