package detector;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import org.apache.log4j.Logger;
import util.EngineConfiguration;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.net.Socket;
import java.util.Arrays;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by thinhhv on 09/09/2014.
 */
public class DetectStream {
	private static final Logger LOG = Logger.getLogger(DetectStream.class);

	private static final String THREAD_PER_DETECT = "detector.thread-per-detect";
	private static final String TRY_MATCH_TIME = "detector.try-match-time";
	private static final String DETECT_TIME_LIMIT = "detector.detect-time-limit";
	private static final String SAMPLE_RATE = "frequency-spectrum.sample-rate";
	private static final String PEAK_TEST_X_SIZE = "frequency-spectrum.peak.peak-tester.x-size";
	private static final String PEAK_TEST_Y_SIZE = "frequency-spectrum.peak.peak-tester.y-size";
	private static final String LIMIT_ASSOCIATE_PER_ANCHOR = "fingerprint.limit-associate-per-anchor";
	private static final String MATCH_THRESHOLD = "detector.match-threshold";
	private static final String DIFF_THRESHOLD = "detector.diff-threshold";

	protected int detectSize;
	protected int[][] hash;
	protected short[][] offset;
	protected int[] fingerprintCursor;
	protected ObjectArrayList<byte[]>[] detectTable;

	protected boolean detected;
	protected boolean streaming;
	protected AtomicInteger nullCounter;
	protected Object detectedNotifyObject;
	protected Object newDataNotifyObject;

	protected DataInputStream in;
	protected DataOutputStream out;

	private int match_threshold;
	private int diff_threshold;
	private long startTime;
	private int signalCount;
	private int[] processedSignalCount;

	protected DetectStream() {
		int try_match_time = Integer.parseInt(EngineConfiguration.getInstance().get(TRY_MATCH_TIME));
		int detect_time_limit = Integer.parseInt(EngineConfiguration.getInstance().get(DETECT_TIME_LIMIT));
		int sample_rate = Integer.parseInt(EngineConfiguration.getInstance().get(SAMPLE_RATE));
		int peak_test_x_size = Integer.parseInt(EngineConfiguration.getInstance().get(PEAK_TEST_X_SIZE));
		int peak_test_y_size = Integer.parseInt(EngineConfiguration.getInstance().get(PEAK_TEST_Y_SIZE));
		int limit_associate_per_anchor = Integer.parseInt(EngineConfiguration.getInstance().get(LIMIT_ASSOCIATE_PER_ANCHOR));
		int thread_per_detect = Integer.parseInt(EngineConfiguration.getInstance().get(THREAD_PER_DETECT));

		this.match_threshold = Integer.parseInt(EngineConfiguration.getInstance().get(MATCH_THRESHOLD));
		this.diff_threshold = Integer.parseInt(EngineConfiguration.getInstance().get(DIFF_THRESHOLD));

		int limit_per_try_match = (int) (((double) sample_rate * detect_time_limit * limit_associate_per_anchor) / (2 * (1 + peak_test_x_size) * (1 + peak_test_y_size)) * 1.05f);
		hash = new int[try_match_time][limit_per_try_match];
		offset = new short[try_match_time][limit_per_try_match];
		fingerprintCursor = new int[try_match_time];

		detectTable = new ObjectArrayList[try_match_time];
		for (int i = 0; i < try_match_time; ++i) detectTable[i] = new ObjectArrayList<byte[]>();

		detectedNotifyObject = new Object();
		newDataNotifyObject = new Object();

		nullCounter = new AtomicInteger();
		signalCount = 0;
		processedSignalCount = new int[thread_per_detect];
		for (int i = 0; i < thread_per_detect; ++i) processedSignalCount[i] = 0;
	}

	public long getElapsedTime() {
		return System.currentTimeMillis() - startTime;
	}

	public int getSignalCount() {
		return signalCount;
	}

	public void increaseSignalCount() {
		++signalCount;
	}

	public String getProcessedSignalCountString() {
		String sb = "[";
		for (int i = 0; i < processedSignalCount.length; ++i) {
			if (i != 0) sb += ",";
			sb += processedSignalCount[i];
		}
		return sb + "]";
	}

	public void increaseProcessedSignalCount(int index) {
		++processedSignalCount[index];
	}

	protected void init(Socket sc, int detectSize) throws Exception {
		detected = false;
		streaming = true;
		nullCounter.set(Integer.parseInt(EngineConfiguration.getInstance().get(THREAD_PER_DETECT)));

		this.in = new DataInputStream(sc.getInputStream());
		this.out = new DataOutputStream(sc.getOutputStream());
		Arrays.fill(fingerprintCursor, 0);
		for (int i = 0; i < detectTable.length; ++i) {
			if (detectSize != this.detectSize) detectTable[i].size(detectSize);
			if (this.detectSize < detectSize) {
				for (int j = this.detectSize; j < detectSize; ++j) {
					byte[] byteArr = detectTable[i].get(j);
					if (byteArr != null) {
						Arrays.fill(byteArr, (byte) 0);
					} else {
						byteArr = new byte[diff_threshold];
						detectTable[i].set(j, byteArr);
					}
				}
			}
		}
		this.detectSize = detectSize;
		startTime = System.currentTimeMillis();
	}

	protected void reset() {
		for (int i = 0; i < detectTable.length; ++i) {
			for (int j = 0; j < detectSize; ++j) {
				byte[] byteArr = detectTable[i].get(j);
				Arrays.fill(byteArr, (byte) 0);
			}
		}
		signalCount = 0;
		int thread_per_detect = Integer.parseInt(EngineConfiguration.getInstance().get(THREAD_PER_DETECT));
		for (int i = 0; i < thread_per_detect; ++i) processedSignalCount[i] = 0;
	}

	/* each index must be accessed by maximum 1 DetectorWorker */
	/* return true if detected, false if not */
	public final boolean add(int tryMatchTime, int detectIndex, int diff) {
		if (diff < 0) return false;
		if (diff >= diff_threshold) {
			LOG.info("diff out of bound");
			return false;
		}
		byte current = ++detectTable[tryMatchTime].get(detectIndex)[diff];
		if (current >= match_threshold) return true;
		return false;
	}
}
