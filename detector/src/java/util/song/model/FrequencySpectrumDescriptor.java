package util.song.model;

import util.EngineConfiguration;
import util.song.model.window.*;

/**
 * Created by thinhhv on 23/07/2014.
 */
public class FrequencySpectrumDescriptor {

	private static final String FFT_SIZE = "frequency-spectrum.fft-size";
	private static final String SAMPLE_RATE = "frequency-spectrum.sample-rate";
	private static final String OVERLAP = "frequency-spectrum.overlap";
	private static final String TRY_MATCH_TIME = "detector.try-match-time";
	private static final String TRY_MATCH_SHIFT = "detector.try-match-shift";
	private static final String WINDOW = "frequency-spectrum.window";

	private int fftSize;
	private int sampleRate;
	private int overlap;
	private Window windowFuntion;
	private int tryMatchTime;
	private int tryMatchShift;
	private Type type;

	public FrequencySpectrumDescriptor(Type type, int fftSize, int sampleRate, int overlap, Window windowFuntion, int tryMatchTime, int tryMatchShift) {
		this.type = type;
		this.fftSize = fftSize;
		this.sampleRate = sampleRate;
		this.overlap = overlap;
		this.windowFuntion = windowFuntion;
		this.tryMatchTime = tryMatchTime;
		this.tryMatchShift = tryMatchShift;
	}

	public static Window getDefaultWindow(int fftSize) {
		String windowStr = EngineConfiguration.getInstance().get(WINDOW).toUpperCase().trim();
		Window window = null;
		if (windowStr.equals("NULL")) {
		} else if (windowStr.equals("GAUSSIAN")) {
			window = new GaussianWindow(fftSize);
		} else if (windowStr.equals("HANNING")) {
			window = new HanningWindow(fftSize);
		} else if (windowStr.equals("BLACKMAN")) {
			window = new BlackmanWindow(fftSize);
		} else
			throw new WindowRuntimeException("invalid window function");
		return window;
	}

	public static FrequencySpectrumDescriptor getDefaultFrequencySpectrumDescriptorForRecord() {
		int fftSize = Integer.parseInt(EngineConfiguration.getInstance().get(FFT_SIZE));
		int sampleRate = Integer.parseInt(EngineConfiguration.getInstance().get(SAMPLE_RATE));
		int overlap = Integer.parseInt(EngineConfiguration.getInstance().get(OVERLAP));
		int tryMatchTime = Integer.parseInt(EngineConfiguration.getInstance().get(TRY_MATCH_TIME));
		int tryMatchShift = Integer.parseInt(EngineConfiguration.getInstance().get(TRY_MATCH_SHIFT));
		return new FrequencySpectrumDescriptor(Type.RECORD, fftSize, sampleRate, overlap, getDefaultWindow(fftSize), tryMatchTime, tryMatchShift);
	}

	public static FrequencySpectrumDescriptor getDefaultFrequencySpectrumDescriptorForSong() {
		int fftSize = Integer.parseInt(EngineConfiguration.getInstance().get(FFT_SIZE));
		int sampleRate = Integer.parseInt(EngineConfiguration.getInstance().get(SAMPLE_RATE));
		int overlap = Integer.parseInt(EngineConfiguration.getInstance().get(OVERLAP));
		return new FrequencySpectrumDescriptor(Type.SONG, fftSize, sampleRate, overlap, getDefaultWindow(fftSize), 1, 0);
	}

	public int getFftSize() {
		return fftSize;
	}

	public void setFftSize(int fftSize) {
		this.fftSize = fftSize;
	}

	public int getSampleRate() {
		return sampleRate;
	}

	public void setSampleRate(int sampleRate) {
		this.sampleRate = sampleRate;
	}

	public int getOverlap() {
		return overlap;
	}

	public void setOverlap(int overlap) {
		this.overlap = overlap;
	}

	public Window getWindowFuntion() {
		return windowFuntion;
	}

	public void setWindowFuntion(Window windowFuntion) {
		this.windowFuntion = windowFuntion;
	}

	public int getTryMatchTime() {
		return tryMatchTime;
	}

	public void setTryMatchTime(int tryMatchTime) {
		this.tryMatchTime = tryMatchTime;
	}

	public int getTryMatchShift() {
		return tryMatchShift;
	}

	public void setTryMatchShift(int tryMatchShift) {
		this.tryMatchShift = tryMatchShift;
	}

	public Type getType() {
		return type;
	}

	public static enum Type {
		SONG, RECORD;
	}
}
