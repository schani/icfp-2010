package at.ac.tuwien.tilab.icfp2010;

public class Permuter {
    static void heap (int[] a, int n, IPermutationConsumer consumer) {
	if (n == 1) {
	    consumer.consumePermutation (a);
	} else {
	    for (int i = 0; i < n; ++i) {
		heap (a, n - 1, consumer);
		if (n % 2 == 1) {
		    int x = a [n-1], y = a [0];
		    a [0] = x;
		    a [n-1] = y;
		} else {
		    int x = a [n-1], y = a [i];
		    a [i] = x;
		    a [n-1] = y;
		}
	    }
	}
    }

    public static void permuteArray (int[] a, IPermutationConsumer consumer) {
	heap (a, a.length, consumer);
    }

    public static void permuteRange (int n, IPermutationConsumer consumer) {
	int[] a = new int [n];

	for (int i = 0; i < n; ++i)
	    a [i] = i;

	permuteArray (a, consumer);
    }
}
