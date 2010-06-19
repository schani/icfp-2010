package at.ac.tuwien.tilab.icfp2010;

public interface ISimulationConsumer {
    void consumeSimulation (int n, int[] circuit, int inputIndex, int[] inputStream, int[] outputStream);
}
