package at.ac.tuwien.tilab.icfp2010;

public class PrefixPermutationConsumer implements IPermutationConsumer {
    int[] prefix;
    IPermutationConsumer consumer;

    public PrefixPermutationConsumer (int[] _prefix, IPermutationConsumer _consumer) {
	prefix = _prefix;
	consumer = _consumer;
    }

    public void consumePermutation (int[] perm) {
	int[] full = new int [prefix.length + perm.length];
	for (int i = 0; i < prefix.length; ++i)
	    full [i] = prefix [i];
	for (int i = 0; i < perm.length; ++i)
	    full [i + prefix.length] = perm [i];
	consumer.consumePermutation (full);
    }
}
