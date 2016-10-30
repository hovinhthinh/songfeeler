package util.song.model;

import org.apache.log4j.Logger;

import java.io.*;

/**
 * Created by thinhhv on 03/09/2014.
 */
public class FrequencySpectrumSerializableObject implements Serializable {
	private static transient final Logger LOG = Logger.getLogger(FrequencySpectrumSerializableObject.class);
	private int frequency_spectrum_fft_size;
	private int frequency_spectrum_sample_rate;
	private int frequency_spectrum_overlap;
	private double frequency_spectrum_trim_threshold;
	private String frequency_spectrum_window;
	private int frequency_spectrum_peak_frequency_threshold;
	private double frequency_spectrum_peak_magnitude_threshold;
	private int frequency_spectrum_peak_peak_tester_x_size;
	private int frequency_spectrum_peak_peak_tester_y_size;
	private int fingerprint_x_size_min;
	private int fingerprint_x_size_max;
	private int fingerprint_y_size;
	private int fingerprint_limit_associate_per_anchor;

	private short[] peak_time_domain;
	private short[] peak_frequency_domain;

	public static FrequencySpectrumSerializableObject fromDataBytes(byte[] dataBytes) {
		LOG.info("loading serializable object");
		try {
			ObjectInputStream in = new ObjectInputStream(new ByteArrayInputStream(dataBytes));
			return (FrequencySpectrumSerializableObject) in.readObject();
		} catch (Exception e) {
			return null;
		}
	}

	public static FrequencySpectrumSerializableObject fromFile(File file) {
		LOG.info("loading serializable object");
		try {
			ObjectInputStream in = new ObjectInputStream(new FileInputStream(file));
			return (FrequencySpectrumSerializableObject) in.readObject();
		} catch (Exception e) {
			return null;
		}
	}

	public boolean toFile(File file) {
		LOG.info("saving serializable object");
		try {
			ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(file));
			out.writeObject(this);
			out.close();
			return true;
		} catch (Exception e) {
			return false;
		}
	}

	public short[] getPeak_time_domain() {
		return peak_time_domain;
	}

	public void setPeak_time_domain(short[] peak_time_domain) {
		this.peak_time_domain = peak_time_domain;
	}

	public short[] getPeak_frequency_domain() {
		return peak_frequency_domain;
	}

	public void setPeak_frequency_domain(short[] peak_frequency_domain) {
		this.peak_frequency_domain = peak_frequency_domain;
	}

	public int getFrequency_spectrum_fft_size() {
		return frequency_spectrum_fft_size;
	}

	public void setFrequency_spectrum_fft_size(int frequency_spectrum_fft_size) {
		this.frequency_spectrum_fft_size = frequency_spectrum_fft_size;
	}

	public int getFrequency_spectrum_sample_rate() {
		return frequency_spectrum_sample_rate;
	}

	public void setFrequency_spectrum_sample_rate(int frequency_spectrum_sample_rate) {
		this.frequency_spectrum_sample_rate = frequency_spectrum_sample_rate;
	}

	public int getFrequency_spectrum_overlap() {
		return frequency_spectrum_overlap;
	}

	public void setFrequency_spectrum_overlap(int frequency_spectrum_overlap) {
		this.frequency_spectrum_overlap = frequency_spectrum_overlap;
	}

	public double getFrequency_spectrum_trim_threshold() {
		return frequency_spectrum_trim_threshold;
	}

	public void setFrequency_spectrum_trim_threshold(double frequency_spectrum_trim_threshold) {
		this.frequency_spectrum_trim_threshold = frequency_spectrum_trim_threshold;
	}

	public String getFrequency_spectrum_window() {
		return frequency_spectrum_window;
	}

	public void setFrequency_spectrum_window(String frequency_spectrum_window) {
		this.frequency_spectrum_window = frequency_spectrum_window;
	}

	public int getFrequency_spectrum_peak_frequency_threshold() {
		return frequency_spectrum_peak_frequency_threshold;
	}

	public void setFrequency_spectrum_peak_frequency_threshold(int frequency_spectrum_peak_frequency_threshold) {
		this.frequency_spectrum_peak_frequency_threshold = frequency_spectrum_peak_frequency_threshold;
	}

	public double getFrequency_spectrum_peak_magnitude_threshold() {
		return frequency_spectrum_peak_magnitude_threshold;
	}

	public void setFrequency_spectrum_peak_magnitude_threshold(double frequency_spectrum_peak_magnitude_threshold) {
		this.frequency_spectrum_peak_magnitude_threshold = frequency_spectrum_peak_magnitude_threshold;
	}

	public int getFrequency_spectrum_peak_peak_tester_x_size() {
		return frequency_spectrum_peak_peak_tester_x_size;
	}

	public void setFrequency_spectrum_peak_peak_tester_x_size(int frequency_spectrum_peak_peak_tester_x_size) {
		this.frequency_spectrum_peak_peak_tester_x_size = frequency_spectrum_peak_peak_tester_x_size;
	}

	public int getFrequency_spectrum_peak_peak_tester_y_size() {
		return frequency_spectrum_peak_peak_tester_y_size;
	}

	public void setFrequency_spectrum_peak_peak_tester_y_size(int frequency_spectrum_peak_peak_tester_y_size) {
		this.frequency_spectrum_peak_peak_tester_y_size = frequency_spectrum_peak_peak_tester_y_size;
	}

	public int getFingerprint_x_size_min() {
		return fingerprint_x_size_min;
	}

	public void setFingerprint_x_size_min(int fingerprint_x_size_min) {
		this.fingerprint_x_size_min = fingerprint_x_size_min;
	}

	public int getFingerprint_x_size_max() {
		return fingerprint_x_size_max;
	}

	public void setFingerprint_x_size_max(int fingerprint_x_size_max) {
		this.fingerprint_x_size_max = fingerprint_x_size_max;
	}

	public int getFingerprint_y_size() {
		return fingerprint_y_size;
	}

	public void setFingerprint_y_size(int fingerprint_y_size) {
		this.fingerprint_y_size = fingerprint_y_size;
	}

	public int getFingerprint_limit_associate_per_anchor() {
		return fingerprint_limit_associate_per_anchor;
	}

	public void setFingerprint_limit_associate_per_anchor(int fingerprint_limit_associate_per_anchor) {
		this.fingerprint_limit_associate_per_anchor = fingerprint_limit_associate_per_anchor;
	}

	public byte[] toDataBytes() {
		try {
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			ObjectOutputStream out = new ObjectOutputStream(baos);
			out.writeObject(this);
			out.close();
			return baos.toByteArray();
		} catch (Exception e) {
			return null;
		}
	}
}
