package at.ac.tuwien.tilab.icfp2010;

public class SearchSimulationConsumer implements ISimulationConsumer {
    int[][] searches;
    ISimulationConsumer matchConsumer;

    public SearchSimulationConsumer (int[][] _searches, ISimulationConsumer _matchConsumer) {
	searches = _searches;
	matchConsumer = _matchConsumer;
    }

    public void consumeSimulation (int n, int[] circuit, int inputIndex, int[] inputStream, int[] outputStream) {
	for (int i = 0; i < searches.length; ++i) {
	    int[] search = searches [i];
	    boolean match = true;
	    for (int j = 0; j < outputStream.length; ++j) {
		if (search [j] != outputStream [j]) {
		    match = false;
		    break;
		}
	    }
	    if (match) {
		matchConsumer.consumeSimulation (n, circuit, inputIndex, inputStream, outputStream);
		return;
	    }
	}

	int numNonNulls = 0;
	boolean allSame = true;

	for (int i = 0; i < outputStream.length; ++i) {
	    if (outputStream [i] != 0)
		++numNonNulls;
	    if (outputStream [i] != outputStream [0])
		allSame = false;
	}

	if (numNonNulls == 1 || allSame) {
	    matchConsumer.consumeSimulation (n, circuit, inputIndex, inputStream, outputStream);
	    return;
	}
    }
}
