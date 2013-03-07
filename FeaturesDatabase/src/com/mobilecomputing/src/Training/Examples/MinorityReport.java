package com.mobilecomputing.src.Training.Examples;

import com.mobilecomputing.src.Training.Persistence.threegears.HandTrackingAdapter;
import com.mobilecomputing.src.Training.Persistence.threegears.HandTrackingClient;
import com.mobilecomputing.src.Training.Persistence.threegears.HandTrackingMessage;
import com.mobilecomputing.src.Training.Persistence.threegears.PinchMessage;
import com.mobilecomputing.src.Training.Training.FeatureExtractors.VectorOps;

import javax.vecmath.Quat4f;
import javax.vecmath.Vector2d;
import javax.vecmath.Vector2f;
import javax.vecmath.Vector3f;
import java.awt.*;
import java.awt.event.InputEvent;

public class MinorityReport extends HandTrackingAdapter {

    private Robot robot;

    private Dimension screenSize;

    // Height in mm above which the mouse will become controlled by the right hand
    public static final float ACTIVE_HEIGHT_MM = 45;

    public MinorityReport() throws AWTException {
        Toolkit toolkit = Toolkit.getDefaultToolkit();
        screenSize = toolkit.getScreenSize();
        robot = new Robot();
    }

    public static void main(String[] args) throws Exception {
        HandTrackingClient client = new HandTrackingClient();
        client.addListener(new MinorityReport());
        client.connect();
    }

    @Override
    public void handleEvent(HandTrackingMessage baseMessage) {
        if (baseMessage instanceof PinchMessage) {
            PinchMessage message = (PinchMessage) baseMessage;

            // Ignore the left hand
            if (message.getHand() != HandTrackingMessage.Hand.RIGHT) return;
            HandTrackingMessage.HandState hand = message.getHandState(HandTrackingMessage.Hand.RIGHT.id());
            // Ignore the hand when it's close to the desk
            if (hand.getPosition().y < ACTIVE_HEIGHT_MM) return;
            Quat4f myQuat = hand.getRotation();
            double aZdistance = Math.max(hand.getPosition().z + 60, 0);

            double myYcoordinate = Math.min(Math.max(hand.getPosition().y, 0), 450) * -3.08 + 1386;
            double myXcoordinate = Math.min(Math.max(hand.getPosition().x, -300), 230) * 3.62 + 1086;

            //System.out.println(myXcoordinate + " : " + myYcoordinate + " : " + aZdistance);


            float aZcoordinate = myQuat.getZ();
            double aMagnitude = aZdistance / aZcoordinate;
            double aShiftedXCoord = hand.getPosition().x;
            double aShiftedYCoord = hand.getPosition().y;

            //System.out.println(myQuat.getX() + " : " + myQuat.getY());
            Vector2d myVec = new Vector2d(aMagnitude * myQuat.getX(), aMagnitude * myQuat.getY());

            Vector3f myUnit = VectorOps.Multiply(myQuat);


            Vector2d myProjection = new Vector2d(aMagnitude * myUnit.x, aMagnitude * myUnit.y);
            myProjection.add(myVec);

            //System.out.println(aShiftedXCoord);
            robot.mouseMove((int) myProjection.x, (int) myProjection.y);
            //System.out.println(myProjection.x + " : " +  myProjection.y);

            /*switch (message.getType()) {
                case MOVED:
                    // Move the mouse cursor to follow the hand
                    float scale = 3.25f;
                    int x = Math.round(scale * (hand.getPosition().x));
                    int y = Math.round(scale * (hand.getPosition().y - 50));
                    robot.mouseMove(x, y);
                    break;
                case PRESSED:
                    // Connect hand press and release events to the mouse
                    robot.mousePress(InputEvent.BUTTON1_MASK);
                    if (hand.getClickCount() == 2) robot.mousePress(InputEvent.BUTTON1_MASK); // double-click
                    break;
                case RELEASED:
                    robot.mouseRelease(InputEvent.BUTTON1_MASK);
                    break;
                default: break;
            }*/
        }
    }
}
