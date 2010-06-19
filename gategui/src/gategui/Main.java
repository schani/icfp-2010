package gategui;

import at.ac.tuwien.tilab.icfp2010.Simulator;
import java.util.ArrayList;
import javax.print.DocFlavor.INPUT_STREAM;

/**
 *
 * @author meller
 */
public class Main {

    GateFrame frame = new GateFrame(this);
    // Simulator input
    int[] inputStream = new int[]{0, 1, 2, 0, 2, 1, 0, 1, 2, 1, 0, 2, 0, 1, 2, 0, 2};
    int inputIndex = 0;

    void init() {
        frame = new GateFrame(this);
        addTestGates(4);
        frame.setVisible(true);
    }

    void addTestGates(int n) {
        for (int i = 0; i < n; i++) {
            frame.paintComponent.addGate(new Gate());
        }
    }

    void simulate() {
        Simulator simulator = new Simulator();

        ArrayList<Gate> gates = frame.paintComponent.gates;
//        int circuit[] = new int[1 + gates.size() * 2];
        int circuit[] = new int[gates.size() * 2];
        final int offset = 1; // circuit[0] is gatenr connected to input
        circuit[0] = -99; // debug hint
        int inputLineIdx = -3;
        for (int i = 0; i < gates.size(); i++) {
            Gate gate = gates.get(i);
            // output line
            int outL = gate.outL;
            int outR = gate.outR;
            outL = ((outL == GatePainter.OUTPUT_LINE) ? 0 : 1+outL*2);// outline needs idx 0
            outR = ((outR == GatePainter.OUTPUT_LINE) ? 0 : 1+outR*2+1);// outline needs idx 0
            // input line
            if (gate.inL == GatePainter.INPUT_LINE) {
                inputLineIdx = offset + i * 2;
               //> circuit[0] = inputLineIdx;
            }
            if (gate.inR == GatePainter.INPUT_LINE) {
                inputLineIdx = offset + i * 2 + 1;
               //>  circuit[0] = inputLineIdx;
            }
//            circuit[offset + i * 2] = outL;
            circuit[i * 2] = outL;
//            circuit[offset + i * 2 + 1] = outR;
            circuit[i * 2 + 1] = outR;
        }

        if (circuit[0] < 0) { // assert
            throw new IllegalStateException("circuit contains no input line!!");
        }

        printIntArray("circuit", circuit);
        System.out.format("inputIndex: %d%n", inputLineIdx);
        int[] simResult = simulator.simulate(gates.size(), circuit, inputLineIdx, inputStream);
        printIntArray("simResult", simResult);
    }

    void printIntArray(String strPrefix, int[] simResult) {
        System.out.print(strPrefix + ": [");
        for (int i = 0; i < simResult.length; i++) {
            System.out.format("%d ", simResult[i]);
        }
        System.out.println("]");
    }

    void testSimulator() {
        Simulator simulator = new Simulator();
        int[] circuit = new int[]{0, 2};
        printIntArray("TEST: hardcoded circuit", circuit);
        int[] simResult = simulator.simulate(1, circuit, 1, inputStream);
        printIntArray("simResult", simResult);
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        Main main = new Main();
        main.init();
//        main.testSimulator();

    }
}
