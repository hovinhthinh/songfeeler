package util.song.model.peak;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import util.EngineConfiguration;

/**
 * Created by thinhhv on 23/07/2014.
 */
public class PeakTester {

	private static final String X_SIZE = "frequency-spectrum.peak.peak-tester.x-size";
	private static int x_size = Integer.parseInt(EngineConfiguration.getInstance().get(X_SIZE));
	private static final String Y_SIZE = "frequency-spectrum.peak.peak-tester.y-size";
	private static int y_size = Integer.parseInt(EngineConfiguration.getInstance().get(Y_SIZE));
	private static final String FREQ_THRESHOLD = "frequency-spectrum.peak.frequency-threshold";
	private static int freq_threshold = Integer.parseInt(EngineConfiguration.getInstance().get(FREQ_THRESHOLD));
	private static final String MAGN_THRESHOLD = "frequency-spectrum.peak.magnitude-threshold";
	private static double magn_threshold = Double.parseDouble(EngineConfiguration.getInstance().get(MAGN_THRESHOLD));

	public static final boolean test(double[][] magnitude, int x, int y) {
		if (y <= freq_threshold || magnitude[x][y] <= magn_threshold || x < x_size || x >= magnitude.length - x_size)
			return false;

		for (int i = x - x_size; i <= x + x_size; ++i)
			for (int j = y - y_size; j <= y + y_size; ++j) {
				if (i < 0 || i >= magnitude.length || j < 0 || j >= magnitude[i].length) continue;
				if (i == x && j == y) continue;
				if (magnitude[i][j] >= magnitude[x][y]) return false;
			}
		return true;
	}

	public static final boolean test(ObjectArrayList<double[]> magnitude, int x, int y) {
		if (y <= freq_threshold || magnitude.get(x)[y] <= magn_threshold || x < x_size || x >= magnitude.size() - x_size)
			return false;

		for (int i = x - x_size; i <= x + x_size; ++i)
			for (int j = y - y_size; j <= y + y_size; ++j) {
				if (i < 0 || i >= magnitude.size() || j < 0 || j >= magnitude.get(i).length) continue;
				if (i == x && j == y) continue;
				if (magnitude.get(i)[j] >= magnitude.get(x)[y]) return false;
			}
		return true;
	}

}
