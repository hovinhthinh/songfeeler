package illusion.songfeeler.detect;

import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.util.Log;

public class SampleRecorder extends Thread {
	private DetectStream detectStream;

	private boolean flagRecording;

	private AudioRecord recorder;

	public SampleRecorder(DetectStream detectStream) {
		this.detectStream = detectStream;
		flagRecording = false;
		int bufferSize = AudioRecord.getMinBufferSize(
				detectStream.getSampleRate(), AudioFormat.CHANNEL_IN_MONO,
				AudioFormat.ENCODING_PCM_16BIT);
		recorder = new AudioRecord(MediaRecorder.AudioSource.DEFAULT,
				detectStream.getSampleRate(), AudioFormat.CHANNEL_IN_MONO,
				AudioFormat.ENCODING_PCM_16BIT, bufferSize);
	}

	public void startRecording() {
		flagRecording = true;
		this.start();
	}

	public void stopRecording() {
		flagRecording = false;
	}

	public boolean isRecording() {
		return flagRecording;
	}

	@Override
	public void run() {
		try {
			recorder.startRecording();
			while (flagRecording
					&& detectStream.getRecordedSamplesCount() < detectStream
							.getScheduledSamplesCount()
					&& !detectStream.isDetected()) {
				int c = recorder.read(
						detectStream.getSamples(),
						detectStream.getRecordedSamplesCount(),
						detectStream.getScheduledSamplesCount()
								- detectStream.getRecordedSamplesCount());
				detectStream.setRecordedSamplesCount(detectStream
						.getRecordedSamplesCount() + c);
			}
		} catch (Exception e) {
			Log.e(getClass().getSimpleName(), e.getMessage());
		} finally {
			try {
				flagRecording = false;
				recorder.stop();
				recorder.release();
			} catch (Exception e) {
			}
		}
	}
}
