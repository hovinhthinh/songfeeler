package illusion.songfeeler.detect;

import java.io.DataOutputStream;
import java.io.OutputStream;
import java.util.ArrayList;

import util.EngineConfiguration;
import util.fft.RealDoubleFFT;
import util.song.model.FrequencySpectrum;
import util.song.model.FrequencySpectrumDescriptor;
import util.song.model.peak.PeakTester;
import util.song.model.window.Window;
import android.util.Log;

public class OutputStreamProcessor extends Thread {
	private static final String TRY_MATCH_TIME = "detector.try-match-time";
	private static final String TRY_MATCH_SHIFT = "detector.try-match-shift";
	private static final String FFT_SIZE = "frequency-spectrum.fft-size";
	private static final RealDoubleFFT transformer = new RealDoubleFFT(
			Integer.parseInt(EngineConfiguration.getInstance().get(FFT_SIZE)));
	private static final Window window = FrequencySpectrumDescriptor
			.getDefaultWindow(Integer.parseInt(EngineConfiguration
					.getInstance().get(FFT_SIZE)));
	private static final String DETECT_TIME_LIMIT = "detector.detect-time-limit";
	private static final String SAMPLE_RATE = "frequency-spectrum.sample-rate";
	private static final String PEAK_TEST_X_SIZE = "frequency-spectrum.peak.peak-tester.x-size";
	private static final String ASC_X_SIZE_MIN = "fingerprint.x-size-min";
	private static final String ASC_X_SIZE_MAX = "fingerprint.x-size-max";
	private static final String ASC_Y_SIZE = "fingerprint.y-size";
	private static final String ASC_LIM = "fingerprint.limit-associate-per-anchor";
	private static final double shortDoubleScale = (double) (1 << 15);
	private ArrayList<boolean[]>[] isPeak;
	private ArrayList<double[]>[] magnitude;
	private int[] rawProcessedCursor;
	private short[] raw;
	private int rawCursor;
	private int try_match_time;
	private int try_match_shift;

	private DetectStream detectStream;
	private DataOutputStream dataOutputStream;

	private boolean flagOutputing;

	public OutputStreamProcessor(DetectStream detectStream,
			OutputStream outputStream) {
		this.detectStream = detectStream;
		this.dataOutputStream = new DataOutputStream(outputStream);
		flagOutputing = false;

		int detect_time_limit = Integer.parseInt(EngineConfiguration
				.getInstance().get(DETECT_TIME_LIMIT));
		int sample_rate = Integer.parseInt(EngineConfiguration.getInstance()
				.get(SAMPLE_RATE));

		try_match_time = Integer.parseInt(EngineConfiguration.getInstance()
				.get(TRY_MATCH_TIME));
		try_match_shift = Integer.parseInt(EngineConfiguration.getInstance()
				.get(TRY_MATCH_SHIFT));

		raw = new short[sample_rate * detect_time_limit];
		rawCursor = 0;

		rawProcessedCursor = new int[try_match_time];
		magnitude = new ArrayList[try_match_time];
		isPeak = new ArrayList[try_match_time];
		for (int i = 0; i < try_match_time; i++) {
			magnitude[i] = new ArrayList<double[]>();
			isPeak[i] = new ArrayList<boolean[]>();
			rawProcessedCursor[i] = i * try_match_shift;
		}
	}

	public void startOutputing() {
		flagOutputing = true;
		this.start();
	}

	public void stopOutputing() {
		flagOutputing = false;
	}

	public boolean isOutputing() {
		return flagOutputing;
	}

	@Override
	public void run() {
		int fft_size = Integer.parseInt(EngineConfiguration.getInstance().get(
				FFT_SIZE));
		int peak_test_x_size = Integer.parseInt(EngineConfiguration
				.getInstance().get(PEAK_TEST_X_SIZE));

		int asc_x_min = Integer.parseInt(EngineConfiguration.getInstance().get(
				ASC_X_SIZE_MIN));
		int asc_x_max = Integer.parseInt(EngineConfiguration.getInstance().get(
				ASC_X_SIZE_MAX));
		int asc_y = Integer.parseInt(EngineConfiguration.getInstance().get(
				ASC_Y_SIZE));
		int asc_lim = Integer.parseInt(EngineConfiguration.getInstance().get(
				ASC_LIM));

		int resolution = fft_size / 2 + 1;
		double sample[] = new double[fft_size];

		try {
			sample_loop: for (int p = 0; p < detectStream
					.getScheduledSamplesCount(); ++p) {
				while (p >= detectStream.getRecordedSamplesCount()) {
					if (detectStream.isDetected() || !flagOutputing) {
						break sample_loop;
					}
				}
				if (detectStream.isDetected()) {
					break;
				}
				short nextRaw = detectStream.getSample(p);
				//
				raw[rawCursor++] = nextRaw;
				for (int i = 0; i < try_match_time; ++i) {
					if (rawProcessedCursor[i] + fft_size > rawCursor)
						continue;
					for (int j = 0; j < fft_size; ++j)
						sample[j] = raw[j + rawProcessedCursor[i]]
								/ shortDoubleScale;
					rawProcessedCursor[i] += fft_size;
					double[] magn = FrequencySpectrum.getFrequencyMagnitude(
							sample, transformer, window);
					magnitude[i].add(magn);
					isPeak[i].add(new boolean[resolution]);
					if (magnitude[i].size() > peak_test_x_size) {
						int col = magnitude[i].size() - peak_test_x_size - 1;

						for (int j = 0; j < resolution; ++j) {
							isPeak[i].get(col)[j] = PeakTester.test(
									magnitude[i], col, j);
							if (isPeak[i].get(col)[j]) {
								int ascCount = 0;
								loop_asc: for (int rx = asc_x_min; rx <= asc_x_max; ++rx)
									for (int ry = asc_y; ry >= -asc_y; --ry) {
										int newi = col - rx, newj = j + ry;
										if (newi < 0 || newj < 0
												|| newj >= resolution)
											continue;
										if (!isPeak[i].get(newi)[newj])
											continue;
										int hashCode = (rx << 20) | (j << 10)
												| newj;
										dataOutputStream.writeByte(i + 1); /*
																			 * 1-
																			 * based
																			 * index
																			 */
										dataOutputStream.writeInt(hashCode);
										dataOutputStream.writeShort(col);
										++ascCount;
										if (ascCount == asc_lim)
											break loop_asc;
									}
							}
						}
					}
				}
				//
			}
			dataOutputStream.writeByte(0);
		} catch (Exception e) {
			Log.e(getClass().getSimpleName(), e.getMessage());
		} finally {
			flagOutputing = false;
		}
	}
}
