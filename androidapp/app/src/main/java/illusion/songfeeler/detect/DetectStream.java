package illusion.songfeeler.detect;

public class DetectStream {
	private short[] samples;
	private int duration;
	private int sampleRate;

	/**
	 * this variable should be true if the detection completed regardless facing
	 * with error or not.
	 */
	private boolean detected;

	public boolean isDetected() {
		return detected;
	}

	public void setDetected(boolean detected) {
		this.detected = detected;
	}

	private int recordedSamplesCount;

	public DetectStream(int duration, int sampleRate) {
		this.duration = duration;
		this.sampleRate = sampleRate;
		samples = new short[duration * sampleRate];
		recordedSamplesCount = 0;
		detected = false;
	}

	public short[] getSamples() {
		return samples;
	}

	public int getDuration() {
		return duration;
	}

	public int getSampleRate() {
		return sampleRate;
	}

	public int getRecordedSamplesCount() {
		return recordedSamplesCount;
	}

	public void setRecordedSamplesCount(int recordedSamplesCount) {
		this.recordedSamplesCount = recordedSamplesCount;
	}

	public int getScheduledSamplesCount() {
		return sampleRate * duration;
	}

	public short getSample(int index) {
		if (index < 0 || index >= recordedSamplesCount) {
			throw new RuntimeException("Index out of bound: " + index);
		}
		return samples[index];
	}
}
