package util.miscellaneous;

/**
 * Created by thinhhv on 02/08/2014.
 */
public class Number {

	public static long power(long b, long p, long mod) { /* support 32bit integer only */
		if (b >= (1L << 31)) throw new RuntimeException("support 32bit integer only");
		if (p >= (1L << 31)) throw new RuntimeException("support 32bit integer only");
		if (mod >= (1L << 31)) throw new RuntimeException("support 32bit integer only");

		long result = 1;
		while (p > 0) {
			if ((p & 1) > 0) {
				result = (result * b) % mod;
			}
			p >>= 1;
			b = (b * b) % mod;
		}
		return result;
	}

	/* Rabin Miller probability check */
	public static boolean isPrime(long n) {
		if (n >= (1L << 31)) throw new RuntimeException("support 32bit integer only");
		if (n == 2) return true;
		if (n < 2 || (n & 1) == 0) return false;

		long p[] = new long[]{3, 5, 7};
		long a, d = n - 1, mx = 3;
		int i, r, s = 0;

		while ((d & 1) == 0) {
			++s;
			d >>= 1;
		}
		for (i = 0; i < mx; ++i) {
			if (n == p[i]) return true;
			if ((n % p[i]) == 0) return false;
			a = power(p[i], d, n);
			if (a != 1) {
				for (r = 0; r < s && a != n - 1; ++r) a = (a * a) % n;
				if (r == s) return false;
			}
		}
		return true;
	}

}
