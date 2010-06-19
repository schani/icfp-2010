package gategui;

/**
 *
 * @author meller
 */
public class Main {

    GateFrame frame = new GateFrame();

    void init() {
        frame = new GateFrame();
        addTestGates(4);
        frame.setVisible(true);
    }

    void addTestGates(int n) {
        for (int i = 0; i < n; i++) {
           frame.paintComponent.addGate(new Gate());
        }

    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        Main main = new Main();
        main.init();
    }
}
