import java.awt.*;
import java.awt.event.KeyEvent;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

public class STDINReader {
    public static void main(String[] args) throws AWTException {
        Scanner s = new Scanner(System.in);
        Robot myRobot = new Robot();

        int NeelDirection =  KeyEvent.VK_J;


        while(true) {
         String aInput = s.nextLine();
            switch (aInput.charAt(0)) {
                case 'j' : NeelDirection = KeyEvent.VK_J; break;
                case 'l' : NeelDirection = KeyEvent.VK_L; break;
                case '1' : Emit(myRobot, new int[] { KeyEvent.VK_R, NeelDirection, KeyEvent.VK_Y }, 100); break;
                case '2' : Emit(myRobot, new int[] { KeyEvent.VK_R, NeelDirection, KeyEvent.VK_SPACE }, 100); break;
                case '3' : Emit(myRobot, new int[] { KeyEvent.VK_R, KeyEvent.VK_K, KeyEvent.VK_SPACE }, 100); break;
                case '4' : Emit(myRobot, new int[] { KeyEvent.VK_R, KeyEvent.VK_I, KeyEvent.VK_SPACE }, 100); break;

            }
        }
    }

    private static void Emit(Robot myRobot, int[] myKeys, int ms) {
        for (int i = 0; i < myKeys.length; i++) {
            int myKey = myKeys[i];
            myRobot.keyPress(myKey);
            myRobot.delay(ms);
            myRobot.keyRelease(myKey);
        }
    }
}
