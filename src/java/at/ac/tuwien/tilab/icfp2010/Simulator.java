package at.ac.tuwien.tilab.icfp2010;

public class Simulator {
    static int gateL (int l, int r) {
	int x = l - r;
	if (x < 0)
	    return x + 3;
	return x;
    }

    static int gateR (int l, int r) {
	int x = l * r - 1;
	if (x < 0)
	    return 2;
	return x % 3;
    }

    public static void step (int n, int[] circuit, int[] inputs) {
	for (int i = 0; i < n; ++i) {
	    int offset = i * 2;
	    int li = inputs [offset + 1];
	    int ri = inputs [offset + 2];
	    int lo = gateL (li, ri);
	    int ro = gateR (li, ri);
	    inputs [circuit [offset + 0]] = lo;
	    inputs [circuit [offset + 1]] = ro;
	}
    }

    public static int[] simulate (int n, int[] circuit, int inputIndex, int[] inputStream) {
	int[] inputs = new int [n * 2 + 1];
	int[] outputs = new int [inputStream.length];
	for (int i = 0; i < inputStream.length; ++i) {
	    inputs [inputIndex] = inputStream [i];
	    step (n, circuit, inputs);
	    outputs [i] = inputs [0];
	}
	return outputs;
    }
}
