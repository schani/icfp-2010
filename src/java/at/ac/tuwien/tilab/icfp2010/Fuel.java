package at.ac.tuwien.tilab.icfp2010;

import java.util.Random;

public class Fuel {
    int n;
    long[][] v;

    public Fuel (int _n, long[][] _v) { n = _n; v = _v; }

    static long[][] matrix (int n) {
	return new long [n][n];
    }

    static long randLong (Random random, long max) {
	long x = random.nextLong ();
	if (x < 0)
	    x = -x;
	return x % max;
    }

    public int numIngredients () { return n; }
    public long[][] contents () { return v; }

    public Fuel multiply (Fuel b) {
	Fuel a = this;
	int n = a.n;
	assert n == b.n;
	long[][] am = a.v;
	long[][] bm = b.v;
	long[][] m = matrix (n);

	for (int i = 0; i < n; ++i) {
	    for (int j = 0; j < n; ++j) {
		long sum = 0;
		for (int k = 0; k < n; ++k)
		    sum += am [i][k] * bm [k][j];
		m [i][j] = sum;
	    }
	}

	return new Fuel (n, m);
    }

    public Fuel subtract (Fuel b) {
	Fuel a = this;
	int n = a.n;
	assert n == b.n;
	long[][] am = a.v;
	long[][] bm = b.v;
	long[][] m = matrix (n);

	for (int i = 0; i < n; ++i) {
	    for (int j = 0; j < n; ++j) {
		m [i][j] = am [i][j] - bm [i][j];
	    }
	}

	return new Fuel (n, m);
    }

    public Fuel mutate (Random random, long maxDiff) {
	long[][] m = matrix (n);

	for (int i = 0; i < n; ++i) {
	    for (int j = 0; j < n; ++j) {
		m [i][j] = v [i][j];
	    }
	}

	int i = random.nextInt (n), j = random.nextInt (n);
	long diff = randLong (random, maxDiff * 2 + 1) - maxDiff;

	long x = m [i][j] + diff;
	if (x < 0)
	    x = 0;
	else if (x == 0 && i == 0 && j == 0)
	    x = 1;

	m [i][j] = x;

	return new Fuel (n, m);
    }

    public long simpleScore (boolean main) {
	long pos = 0, neg = 0;
	long[][] m = v;
	for (int i = 0; i < n; ++i) {
	    for (int j = 0; j < n; ++j) {
		long x = m [i][j];
		if (x >= 0)
		    pos += x;
		else
		    neg += x;
	    }
	}
	if (neg < 0)
	    return neg;
	if (main && m [0][0] == 0)
	    return -1;
	return pos;
    }

    public static Fuel defaultFuel (int n) {
	long[][] m = matrix (n);
	m [0][0] = 1;
	return new Fuel (n, m);
    }

    public static Fuel randomFuel (Random random, int n, long max) {
	long[][] m = matrix (n);

	for (int i = 0; i < n; ++i) {
	    for (int j = 0; j < n; ++j) {
		long x;
		if (i == 0 && j == 0)
		    x = randLong (random, max - 1) + 1;
		else
		    x = randLong (random, max);
		m [i][j] = x;
	    }
	}

	return new Fuel (n, m);
    }
}
