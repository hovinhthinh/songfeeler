package util.song.model;

import java.util.ArrayList;
import java.util.logging.Logger;

import util.EngineConfiguration;
import util.fft.Complex1D;
import util.fft.RealDoubleFFT;
import util.song.model.peak.PeakTester;
import util.song.model.window.Window;

/**
 * Created by thinhhv on 23/07/2014.
 */
public class FrequencySpectrum {
	private static final Logger LOG = Logger.getLogger(FrequencySpectrum.class.getName());
	private static final String TRIM_THRESHOLD = "frequency-spectrum.trim-threshold";
	private static final String MAX_DURATION = "frequency-spectrum.max-duration";
	private static final String MIN_DURATION = "frequency-spectrum.min-duration";

	private int[] signalLength;
	private int resolution;
	private double[][][] magnitude;
	private boolean[][][] isPeak;
	private boolean foundPeaks;
	private FrequencySpectrumDescriptor descriptor;

	private FrequencySpectrum() {
		/* Prohibited from self construction */
	}

	public static double[] getFrequencyMagnitude(double[] sample, RealDoubleFFT transformer, Window window) {
		Complex1D res = new Complex1D();
		res.x = new double[sample.length / 2 + 1];
		res.y = new double[sample.length / 2 + 1];
		if (window != null) {
			window.applyWindowing(sample);
		}
		transformer.ft(sample, res);

		/* Scale & Fold */
//		for (int i = 0; i < sample.length / 2 + 1; ++i) {
//            res.x[i] /= sample.length; res.y[i] /= sample.length;
//            if (i != 0 && i != sample.length / 2) {
//                res.x[i] *= 2; res.y[i] *= 2;
//            }
//        }

		double[] magnitude = new double[sample.length / 2 + 1];
		for (int i = 0; i < magnitude.length; ++i) {
			magnitude[i] = Math.sqrt(res.x[i] * res.x[i] + res.y[i] * res.y[i]);
		}
		return magnitude;
	}

	public static boolean acceptFrequencySpectrumFromSerializableObject(FrequencySpectrumSerializableObject sobj) {
		try {
			return
					sobj.getFrequency_spectrum_fft_size() == Integer.parseInt(EngineConfiguration.getInstance().get("frequency-spectrum.fft-size")) &&
							sobj.getFrequency_spectrum_sample_rate() == Integer.parseInt(EngineConfiguration.getInstance().get("frequency-spectrum.sample-rate")) &&
							sobj.getFrequency_spectrum_overlap() == Integer.parseInt(EngineConfiguration.getInstance().get("frequency-spectrum.overlap")) &&
							sobj.getFrequency_spectrum_trim_threshold() == Double.parseDouble(EngineConfiguration.getInstance().get("frequency-spectrum.trim-threshold")) &&
							sobj.getFrequency_spectrum_window().equals(EngineConfiguration.getInstance().get("frequency-spectrum.window")) &&
							sobj.getFrequency_spectrum_peak_frequency_threshold() == Integer.parseInt(EngineConfiguration.getInstance().get("frequency-spectrum.peak.frequency-threshold")) &&
							sobj.getFrequency_spectrum_peak_magnitude_threshold() == Double.parseDouble(EngineConfiguration.getInstance().get("frequency-spectrum.peak.magnitude-threshold")) &&
							sobj.getFrequency_spectrum_peak_peak_tester_x_size() == Integer.parseInt(EngineConfiguration.getInstance().get("frequency-spectrum.peak.peak-tester.x-size")) &&
							sobj.getFrequency_spectrum_peak_peak_tester_y_size() == Integer.parseInt(EngineConfiguration.getInstance().get("frequency-spectrum.peak.peak-tester.y-size")) &&
							sobj.getFingerprint_x_size_min() == Integer.parseInt(EngineConfiguration.getInstance().get("fingerprint.x-size-min")) &&
							sobj.getFingerprint_x_size_max() == Integer.parseInt(EngineConfiguration.getInstance().get("fingerprint.x-size-max")) &&
							sobj.getFingerprint_y_size() == Integer.parseInt(EngineConfiguration.getInstance().get("fingerprint.y-size")) &&
							sobj.getFingerprint_limit_associate_per_anchor() == Integer.parseInt(EngineConfiguration.getInstance().get("fingerprint.limit-associate-per-anchor"));
		} catch (Exception e) {
			return false;
		}
	}

	public static FrequencySpectrum createFrequencySpectrumFromSerializableObject(FrequencySpectrumSerializableObject sobj)
			throws FrequencySpectrumException {
		LOG.info("generating frequency spectrum from serializable object");
		if (!acceptFrequencySpectrumFromSerializableObject(sobj)) {
			throw new FrequencySpectrumException("sobj parameters not matched");
		}
		FrequencySpectrum f = new FrequencySpectrum();
		f.descriptor = FrequencySpectrumDescriptor.getDefaultFrequencySpectrumDescriptorForSong();
		f.resolution = f.getDescriptor().getFftSize() / 2 + 1;
		f.foundPeaks = true;
		boolean[][][] isPeak = new boolean[f.getDescriptor().getTryMatchTime()][][];
		int[] signalLength = new int[f.getDescriptor().getTryMatchTime()];
		int signalMaxLength = 0;
		for (int i = 0; i < sobj.getPeak_time_domain().length; ++i) {
			signalMaxLength = Math.max(sobj.getPeak_time_domain()[i] + 1, signalMaxLength);
		}
		isPeak[0] = new boolean[signalMaxLength][f.getDescriptor().getFftSize()];
		signalLength[0] = signalMaxLength;
		for (int i = 0; i < sobj.getPeak_time_domain().length; ++i) {
			isPeak[0][sobj.getPeak_time_domain()[i]][sobj.getPeak_frequency_domain()[i]] = true;
		}
		f.isPeak = isPeak;
		f.signalLength = signalLength;
		return f;
	}

	public static FrequencySpectrum createFrequencySpectrumFromRaw(FrequencySpectrumDescriptor descriptor, double[] raw) {
		LOG.info("generating frequency spectrum from raw");
		int max_duration = Integer.parseInt(EngineConfiguration.getInstance().get(MAX_DURATION));
		int min_duration = Integer.parseInt(EngineConfiguration.getInstance().get(MIN_DURATION));
		int duration = raw.length / (descriptor.getFftSize() - descriptor.getOverlap()) * descriptor.getFftSize() / descriptor.getSampleRate();
		if (descriptor.getType() == FrequencySpectrumDescriptor.Type.SONG && duration > max_duration) {
			LOG.info("not allow duration greater than " + max_duration + ": " + duration);
			return null;
		}
		if (descriptor.getType() == FrequencySpectrumDescriptor.Type.SONG && duration < min_duration) {
			LOG.info("not allow duration less than " + min_duration + ": " + duration);
			return null;
		}

		FrequencySpectrum f = new FrequencySpectrum();
		f.descriptor = descriptor;
		f.resolution = descriptor.getFftSize() / 2 + 1;
		f.foundPeaks = false;

		double[][][] magnitude = new double[descriptor.getTryMatchTime()][][];
		int[] signalLength = new int[descriptor.getTryMatchTime()];
		RealDoubleFFT fftTransformer = new RealDoubleFFT(descriptor.getFftSize());
		ArrayList<double[]> magnitudeIncrement = new ArrayList<double[]>();
		double sample[] = new double[descriptor.getFftSize()];

		double trimThreshold = Double.parseDouble(EngineConfiguration.getInstance().get(TRIM_THRESHOLD));
		int startIndex = 0, endIndex = raw.length;

		while (startIndex < endIndex && Math.abs(raw[startIndex]) <= trimThreshold) ++startIndex;
		while (endIndex > startIndex && Math.abs(raw[endIndex - 1]) <= trimThreshold) --endIndex;
		LOG.info("[frequency spectrum] trimed signal length: " + startIndex + " | " + (raw.length - endIndex));

		for (int t = 0; t < descriptor.getTryMatchTime(); ++t) {
			int tryStartIndex = startIndex + t * descriptor.getTryMatchShift();
			while (tryStartIndex < endIndex) {
				if (tryStartIndex + descriptor.getFftSize() <= endIndex) {
					for (int i = 0; i < descriptor.getFftSize(); ++i) {
						sample[i] = raw[tryStartIndex + i];
					}
					magnitudeIncrement.add(getFrequencyMagnitude(sample.clone(), fftTransformer, descriptor.getWindowFuntion()));
				}
				tryStartIndex += descriptor.getFftSize() - descriptor.getOverlap();
			}
			magnitude[t] = new double[magnitudeIncrement.size()][];
			for (int i = 0; i < magnitudeIncrement.size(); ++i) magnitude[t][i] = magnitudeIncrement.get(i);
			signalLength[t] = magnitudeIncrement.size();
			magnitudeIncrement.clear();
		}
		f.magnitude = magnitude;
		f.signalLength = signalLength;
		return f;
	}


	public FrequencySpectrumSerializableObject toSerializableObject() throws FrequencySpectrumException {
		if (getDescriptor().getTryMatchTime() != 1) {
			throw new FrequencySpectrumException("only support frequency spectrum for song");
		}
		FrequencySpectrumSerializableObject sobj = new FrequencySpectrumSerializableObject();
		sobj.setFrequency_spectrum_fft_size(Integer.parseInt(EngineConfiguration.getInstance().get("frequency-spectrum.fft-size")));
		sobj.setFrequency_spectrum_sample_rate(Integer.parseInt(EngineConfiguration.getInstance().get("frequency-spectrum.sample-rate")));
		sobj.setFrequency_spectrum_overlap(Integer.parseInt(EngineConfiguration.getInstance().get("frequency-spectrum.overlap")));
		sobj.setFrequency_spectrum_trim_threshold(Double.parseDouble(EngineConfiguration.getInstance().get("frequency-spectrum.trim-threshold")));
		sobj.setFrequency_spectrum_window(EngineConfiguration.getInstance().get("frequency-spectrum.window"));
		sobj.setFrequency_spectrum_peak_frequency_threshold(Integer.parseInt(EngineConfiguration.getInstance().get("frequency-spectrum.peak.frequency-threshold")));
		sobj.setFrequency_spectrum_peak_magnitude_threshold(Double.parseDouble(EngineConfiguration.getInstance().get("frequency-spectrum.peak.magnitude-threshold")));
		sobj.setFrequency_spectrum_peak_peak_tester_x_size(Integer.parseInt(EngineConfiguration.getInstance().get("frequency-spectrum.peak.peak-tester.x-size")));
		sobj.setFrequency_spectrum_peak_peak_tester_y_size(Integer.parseInt(EngineConfiguration.getInstance().get("frequency-spectrum.peak.peak-tester.y-size")));
		sobj.setFingerprint_x_size_min(Integer.parseInt(EngineConfiguration.getInstance().get("fingerprint.x-size-min")));
		sobj.setFingerprint_x_size_max(Integer.parseInt(EngineConfiguration.getInstance().get("fingerprint.x-size-max")));
		sobj.setFingerprint_y_size(Integer.parseInt(EngineConfiguration.getInstance().get("fingerprint.y-size")));
		sobj.setFingerprint_limit_associate_per_anchor(Integer.parseInt(EngineConfiguration.getInstance().get("fingerprint.limit-associate-per-anchor")));

		int peakSize = 0;
		for (int i = 0; i < getSignalLength(0); ++i)
			for (int j = 0; j < getResolution(); ++j) {
				if (isPeak(0, i, j)) ++peakSize;
			}

		short[] peak_time_domain = new short[peakSize];
		short[] peak_frequency_domain = new short[peakSize];

		peakSize = 0;
		for (int i = 0; i < getSignalLength(0); ++i)
			for (int j = getResolution() - 1; j >= 0; --j) {
				if (!isPeak(0, i, j)) continue;
				peak_time_domain[peakSize] = (short) i;
				peak_frequency_domain[peakSize] = (short) j;
				++peakSize;
			}
		sobj.setPeak_time_domain(peak_time_domain);
		sobj.setPeak_frequency_domain(peak_frequency_domain);
		return sobj;
	}

	public int getSignalLength(int tryMatchIndex) {
		return signalLength[tryMatchIndex];
	}

	public int getResolution() {
		return resolution;
	}

	public double[][] getMagnitude(int tryMatchIndex) {
		return magnitude[tryMatchIndex];
	}

	public FrequencySpectrumDescriptor getDescriptor() {
		return descriptor;
	}

	public void generatePeaks() {
		LOG.info("generating peaks");

		isPeak = new boolean[descriptor.getTryMatchTime()][][];
		for (int t = 0; t < descriptor.getTryMatchTime(); ++t) {
			isPeak[t] = new boolean[signalLength[t]][resolution];
			for (int i = 0; i < signalLength[t]; ++i)
				for (int j = 0; j < resolution; ++j) {
					isPeak[t][i][j] = PeakTester.test(magnitude[t], i, j);
				}
		}
		foundPeaks = true;
	}

	public final boolean isPeak(int tryMatchIndex, int signalIndex, int freqIndex) {
		if (!foundPeaks) {
			generatePeaks();
		}
		return isPeak[tryMatchIndex][signalIndex][freqIndex];
	}

}
