import java.awt.*;
import java.awt.event.KeyEvent;

public class TestingRobots {
    public static void main(String[] args) throws AWTException, InterruptedException {
        Robot myRobot = new Robot();



        int[] myKeys = new int[] { KeyEvent.VK_H, KeyEvent.VK_E, KeyEvent.VK_L, KeyEvent.VK_L, KeyEvent.VK_O };
        for (int i = 0; i < myKeys.length; i++) {
            int myKey = myKeys[i];
            myRobot.keyPress(myKey);
            myRobot.delay(50);
            myRobot.keyRelease(myKey);
        }
    }
}
