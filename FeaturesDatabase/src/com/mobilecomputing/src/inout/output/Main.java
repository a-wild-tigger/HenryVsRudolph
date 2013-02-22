import javax.swing.*;

public class Main extends JFrame {
    public static void main(String[] args) {
        Main myMain = new Main();
        myMain.addKeyListener(new OurExample());
        myMain.setVisible(true);
    }

}
