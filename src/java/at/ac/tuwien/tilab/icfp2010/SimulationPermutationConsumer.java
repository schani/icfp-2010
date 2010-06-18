package at.ac.tuwien.tilab.icfp2010;

public class SimulationPermutationConsumer implements IPermutationConsumer {
    int n;
    int[] inputStream;
    ISimulationConsumer simulationConsumer;

    public SimulationPermutationConsumer (int _n, int[] _inputStream, ISimulationConsumer _simulationConsumer) {
	n = _n;
	inputStream = _inputStream;
	simulationConsumer = _simulationConsumer;
    }

    public void consumePermutation (int[] perm) {
	int inputIndex = perm [n * 2];
	int[] outputStream = Simulator.simulate (n, perm, inputIndex, inputStream);
	simulationConsumer.consumeSimulation (n, perm, inputIndex, inputStream, outputStream);
    }
}
