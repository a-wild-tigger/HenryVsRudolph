package com.mobilecomputing.src.Training;

import com.threegear.gloveless.network.HandTrackingClient;
import com.threegear.gloveless.network.PoseMessage;
import org.lwjgl.LWJGLException;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Scanner;
import java.util.Set;

public class MainController {
    public static void main(String[] args) throws IOException, LWJGLException, InterruptedException {
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
        MainController myController = new MainController(myPersistence);
        myController.MainLoop();
    }

    private final Scanner theInput = new Scanner(System.in);
    private final Persistence thePersistence;
    private final Recorder theStaticRecorder;
    private String theCurrentUser;

    public MainController(Persistence aPersistence) {
        thePersistence = aPersistence;
        theStaticRecorder = new Recorder();
    }

    private void Start() throws IOException {
        thePersistence.Start();

        System.out.println("Enter User Name: ");
        String myUserName = theInput.nextLine();
        System.out.println("Thanks " + myUserName + ". We are looking you up in the database.");

        boolean myUserExists = thePersistence.DoesUserExist(myUserName);
        if(myUserExists) {
            System.out.println("We located you in the Database, " + myUserName + ".");
        } else {
            System.out.println("We were not able to find you, " + myUserName + ". We will create a profile for you now.");
            thePersistence.CreateUser(myUserName);
        }

        theCurrentUser = myUserName;
    }

    public void MainLoop() throws IOException, LWJGLException, InterruptedException {
        Start();
        while(HomeScreen());
        Stop();
    }

    private boolean HomeScreen() throws LWJGLException, InterruptedException {
        System.out.println("Input Options: ");
        System.out.println("\t1 : Record Static Gesture");
        System.out.println("\t2 : View/Edit Existing Static Gestures");
        System.out.println("\t3 : Record Continuous Gesture");
        System.out.println("\t4 : View/Edit Existing Continuous Gestures");
        System.out.println("\t5 : Quit");

        int aValue = theInput.nextInt();
        theInput.nextLine();
        if(aValue > 5 || aValue <= 0) {
            System.out.println("Invalid Input.");
            return HomeScreen();
        } else {
            switch(aValue) {
                case 1: RecordStaticGesture(); break;
                case 2: ViewEditStaticGestures(); break;
                case 3: RecordContinuousGesture(); break;
                case 4: ViewEditContinuousGesture(); break;
                case 5: return false;
            }
        }

        return true;
    }

    private void ViewEditContinuousGesture() throws LWJGLException, InterruptedException {
        Set<String> theAvailableGestures = thePersistence.GetAvailableContinuousGestures(theCurrentUser);
        if(theAvailableGestures.size() == 0) {
            System.out.println("No Gestures are available.");
            return;
        }

        System.out.println("The Current Gestures Available for " + theCurrentUser + " are: ");
        for (String aGesture : theAvailableGestures) {
            System.out.println("\t" + aGesture);
        }

        System.out.println("Please Input the Gesture you want to view/edit.");
        String aGestureReq = theInput.nextLine();

        if(!thePersistence.UserHasContinuousGesture(theCurrentUser, aGestureReq)) {
            System.out.println("The Gesture " + aGestureReq + " Does not Exist!");
            return;
        }

        List<List<PoseMessage>> theCurrentGestures = thePersistence.GetContinuousGestureSet(theCurrentUser, aGestureReq);
        List<List<PoseMessage>> theMessages = ContinuousGestureEditor.ViewImages(theCurrentGestures);
        thePersistence.SetContinuousGestureSet(theCurrentUser, aGestureReq, theMessages);
    }

    private void RecordContinuousGesture() {
        System.out.println("The Current Gestures Available for " + theCurrentUser + " are: ");
        for (String aGesture : thePersistence.GetAvailableContinuousGestures(theCurrentUser)) {
            System.out.println("\t" + aGesture);
        }

        System.out.println("Please Input the Gesture you want to Record or Create a New One.");
        String myGesture = theInput.nextLine();
        System.out.println("We are starting the Recorder for Gesture, " + myGesture + ". Hit (x) to Leave.");

        HandTrackingClient myClient = new HandTrackingClient();

        try {
            myClient.addListener(theStaticRecorder);
            myClient.connect();
            theStaticRecorder.Run();
        } catch (IOException e) {
            System.out.println("Could Not Connect: " + e.toString());
            return;
        } catch (LWJGLException e) {
            System.out.println("Could Not Start LWJGL: " + e.toString());
        } finally {
            myClient.stop();
        }

        List<PoseMessage> myMessages = theStaticRecorder.GetMessages();
        List<List<PoseMessage>> myMessagesToSave = ContinuousGestureViewer.SelectImagesToKeep(myMessages);
        thePersistence.PushContinuousGestures(theCurrentUser, myGesture, myMessagesToSave);
    }

    private void RecordStaticGesture() {
        System.out.println("The Current Gestures Available for " + theCurrentUser + " are: ");
        for (String aGesture : thePersistence.GetAvailableGestures(theCurrentUser)) {
            System.out.println("\t" + aGesture);
        }

        System.out.println("Please Input the Gesture you want to Record or Create a New One.");
        String myGesture = theInput.nextLine();
        System.out.println("We are starting the Recorder for Gesture, " + myGesture + ". Hit (x) to Leave.");

        HandTrackingClient myClient = new HandTrackingClient();

        try {
            myClient.addListener(theStaticRecorder);
            myClient.connect();
            theStaticRecorder.Run();
        } catch (IOException e) {
            System.out.println("Could Not Connect: " + e.toString());
            return;
        } catch (LWJGLException e) {
            System.out.println("Could Not Start LWJGL: " + e.toString());
        } finally {
            myClient.stop();
        }

        List<PoseMessage> myMessages = theStaticRecorder.GetMessages();
        List<PoseMessage> myMessagesToSave = StaticGestureViewer.SelectImagesToKeep(myMessages, false);
        thePersistence.PushStaticGestures(theCurrentUser, myGesture, myMessagesToSave);
    }

    private void ViewEditStaticGestures() {
        Set<String> theAvailableGestures = thePersistence.GetAvailableGestures(theCurrentUser);
        if(theAvailableGestures.size() == 0) {
            System.out.println("No Gestures are available.");
            return;
        }

        System.out.println("The Current Gestures Available for " + theCurrentUser + " are: ");
        for (String aGesture : theAvailableGestures) {
            System.out.println("\t" + aGesture);
        }

        System.out.println("Please Input the Gesture you want to view/edit.");
        String aGestureReq = theInput.nextLine();

        if(!thePersistence.UserHasGesture(theCurrentUser, aGestureReq)) {
            System.out.println("The Gesture " + aGestureReq + " Does not Exist!");
            return;
        }

        List<PoseMessage> theCurrentGestures = thePersistence.GetStaticGestureSet(theCurrentUser, aGestureReq);
        List<PoseMessage> theMessages = StaticGestureViewer.SelectImagesToKeep(theCurrentGestures, true);
        thePersistence.SetStaticGestureSet(theCurrentUser, aGestureReq, theMessages);
    }

    private void Stop() throws IOException {
        System.out.println("Would you like to save changes to the Database Session? (y,n)");
        String aValue = theInput.nextLine();
        if(aValue.equals("y")) {
            System.out.println("Saving Database");
            thePersistence.Stop();
        }
    }
}
