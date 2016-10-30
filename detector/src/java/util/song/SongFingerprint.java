package util.song;

import org.apache.log4j.Logger;
import util.EngineConfiguration;
import util.Pair;
import util.song.model.FrequencySpectrum;
import util.song.model.FrequencySpectrumDescriptor;
import util.song.model.FrequencySpectrumException;
import util.song.model.FrequencySpectrumSerializableObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

/**
 * Created by thinhhv on 23/07/2014.
 */
public class SongFingerprint {
	private static final Logger LOG = Logger.getLogger(SongFingerprint.class);

	private static final String X_SIZE_MIN = "fingerprint.x-size-min";
	private static final String X_SIZE_MAX = "fingerprint.x-size-max";
	private static final String Y_SIZE = "fingerprint.y-size";
	private static final String ASC_LIM = "fingerprint.limit-associate-per-anchor";

	private int[] fingerprintLength;
	private int[][] hash;
	private short[][] offset;
	private FrequencySpectrumDescriptor descriptor;

	private SongFingerprint() {
		/* prohibit from self construction */
	}

	public static SongFingerprint createFingerprintFromFrequencySpectrumSerializableObject(FrequencySpectrumSerializableObject sobj)
			throws FrequencySpectrumException {
		LOG.info("generating fingerprint from frequency spectrum serializable object");
		if (!FrequencySpectrum.acceptFrequencySpectrumFromSerializableObject(sobj)) {
			throw new FrequencySpectrumException("sobj parameters not matched");
		}
		short[] peak_time_domain = sobj.getPeak_time_domain();
		short[] peak_frequency_domain = sobj.getPeak_frequency_domain();
		int xsizemin = Integer.parseInt(EngineConfiguration.getInstance().get(X_SIZE_MIN));
		int xsizemax = Integer.parseInt(EngineConfiguration.getInstance().get(X_SIZE_MAX));
		int ysize = Integer.parseInt(EngineConfiguration.getInstance().get(Y_SIZE));
		int asclim = Integer.parseInt(EngineConfiguration.getInstance().get(ASC_LIM));


		ArrayList<Pair<Integer, Short>>[] arr = new ArrayList[xsizemax - xsizemin + 1];
		for (int i = 0; i < arr.length; ++i) arr[i] = new ArrayList<Pair<Integer, Short>>();
		for (int i = 0; i < peak_time_domain.length; ++i) {
			short px = peak_time_domain[i];
			short py = peak_frequency_domain[i];
			int ascCount = 0;
			loop_asc:
			for (int j = i - 1; j >= 0; --j) {
				if (peak_time_domain[j] < px - xsizemax) break;
				if (peak_time_domain[j] > px - xsizemin || peak_frequency_domain[j] < py - ysize || peak_frequency_domain[j] > py + ysize)
					continue;
				int hashCode = (((int) px - peak_time_domain[j]) << 20) | (((int) py) << 10) | ((int) peak_frequency_domain[j]);
				arr[px - peak_time_domain[j] - xsizemin].add(new Pair<Integer, Short>(hashCode, px));
				++ascCount;
				if (ascCount == asclim) break loop_asc;
			}
		}

		SongFingerprint fingerprint = new SongFingerprint();
		int[][] hash = new int[1][];
		short[][] offset = new short[1][];
		int[] fingerprintLength = new int[1];
		for (int i = 0; i < arr.length; ++i) {
			Collections.sort(arr[i], new HashComparator());
			fingerprintLength[0] += arr[i].size();
		}
		hash[0] = new int[fingerprintLength[0]];
		offset[0] = new short[fingerprintLength[0]];

		int cursor = 0;
		for (int i = 0; i < arr.length; ++i) {
			for (Pair<Integer, Short> p : arr[i]) {
				hash[0][cursor] = p.first;
				offset[0][cursor] = p.second;
				++cursor;
			}
		}
		fingerprint.fingerprintLength = fingerprintLength;
		fingerprint.hash = hash;
		fingerprint.offset = offset;

		StringBuilder fingerprintLengthMess = null;
		for (int i = 0; i < fingerprintLength.length; ++i) {
			if (fingerprintLengthMess == null) fingerprintLengthMess = new StringBuilder("[" + hash[i].length);
			else fingerprintLengthMess.append(", " + hash[i].length);
		}
		fingerprintLengthMess.append("]");

		LOG.info("fingerprint generated: numHashes = " + fingerprintLengthMess.toString());
		return fingerprint;
	}

	public static SongFingerprint createFingerprintFromFrequencySpectrum(FrequencySpectrum spectrum) {
		LOG.info("generating fingerprint from frequency spectrum");
		int xsizemin = Integer.parseInt(EngineConfiguration.getInstance().get(X_SIZE_MIN));
		int xsizemax = Integer.parseInt(EngineConfiguration.getInstance().get(X_SIZE_MAX));
		int ysize = Integer.parseInt(EngineConfiguration.getInstance().get(Y_SIZE));
		int asclim = Integer.parseInt(EngineConfiguration.getInstance().get(ASC_LIM));

		SongFingerprint fingerprint = new SongFingerprint();
		fingerprint.descriptor = spectrum.getDescriptor();
		int[][] hash = new int[spectrum.getDescriptor().getTryMatchTime()][];
		short[][] offset = new short[spectrum.getDescriptor().getTryMatchTime()][];

		ArrayList<Pair<Integer, Short>> arr = new ArrayList<Pair<Integer, Short>>();
		for (int t = 0; t < spectrum.getDescriptor().getTryMatchTime(); ++t) {
			for (int i = 0; i < spectrum.getSignalLength(t); ++i)
				for (int j = 0; j < spectrum.getResolution(); ++j) {
					if (!spectrum.isPeak(t, i, j)) continue;
					int ascCount = 0;
					loop_asc:
					for (int rx = xsizemin; rx <= xsizemax; ++rx)
						for (int ry = ysize; ry >= -ysize; --ry) {
							int newi = i - rx, newj = j + ry;
							if (newi < 0 || newj < 0 || newi >= spectrum.getSignalLength(t) || newj >= spectrum.getResolution())
								continue;
							if (!spectrum.isPeak(t, newi, newj)) continue;
							int hashCode = (rx << 20) | (j << 10) | newj;
							arr.add(new Pair<Integer, Short>(hashCode, (short) i));
							++ascCount;
							if (ascCount == asclim) break loop_asc;
						}
				}
			Collections.sort(arr, new HashComparator());
			hash[t] = new int[arr.size()];
			offset[t] = new short[arr.size()];
			for (int i = 0; i < arr.size(); ++i) {
				hash[t][i] = arr.get(i).first;
				offset[t][i] = arr.get(i).second;
			}
			arr.clear();
		}

		fingerprint.hash = hash;
		fingerprint.offset = offset;
		int[] fingerprintLength = new int[spectrum.getDescriptor().getTryMatchTime()];

		StringBuilder fingerprintLengthMess = null;
		for (int i = 0; i < fingerprintLength.length; ++i) {
			fingerprintLength[i] = hash[i].length;
			if (fingerprintLengthMess == null) fingerprintLengthMess = new StringBuilder("[" + hash[i].length);
			else fingerprintLengthMess.append(", " + hash[i].length);
		}
		fingerprintLengthMess.append("]");

		fingerprint.fingerprintLength = fingerprintLength;
		LOG.info("fingerprint generated: numHashes = " + fingerprintLengthMess.toString());
		return fingerprint;
	}

	public int[] getHash(int tryMatchIndex) {
		return hash[tryMatchIndex];
	}

	public short[] getOffset(int tryMatchIndex) {
		return offset[tryMatchIndex];
	}

	public FrequencySpectrumDescriptor getDescriptor() {
		return descriptor;
	}

	public int getFingerprintLength(int tryMatchIndex) {
		return fingerprintLength[tryMatchIndex];
	}

	private static final class HashComparator implements Comparator<Pair<Integer, Short>> {
		@Override
		public int compare(Pair<Integer, Short> o1, Pair<Integer, Short> o2) {
			return o1.first - o2.first;
		}
	}
}
