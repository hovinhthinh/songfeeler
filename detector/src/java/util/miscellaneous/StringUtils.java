package util.miscellaneous;

/**
 * Created by thinhhv on 25/09/2014.
 */
public class StringUtils {
	public static long getStringHashCode(String s) {
		long modulo = (long) 1e9 + 7;
		long currentModule = 1;
		long hashCode = 0;
		for (int i = 0; i < s.length(); ++i) {
			hashCode += ((long) s.charAt(i)) * currentModule;
			currentModule *= modulo;
		}
		return hashCode;
	}
}
