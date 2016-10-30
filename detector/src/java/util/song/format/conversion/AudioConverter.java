package util.song.format.conversion;

import org.apache.log4j.Logger;
import util.EngineConfiguration;

import javax.sound.sampled.*;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;

/**
 * Created by thinhhv on 07/08/2014.
 */

public class AudioConverter {
	private static final Logger LOG = Logger.getLogger(AudioConverter.class);

	private static final String SAMPLE_RATE = "frequency-spectrum.sample-rate";
	private static final AudioFormat.Encoding ENCODING = AudioFormat.Encoding.PCM_SIGNED;
	private static final int SAMPLE_SIZE = 16;
	private static final int NUM_CHANNELS = 1;


	private static AudioFileFormat.Type findTargetType(String strExtension) {
		AudioFileFormat.Type[] aTypes = AudioSystem.getAudioFileTypes();
		for (int i = 0; i < aTypes.length; i++) {
			if (aTypes[i].getExtension().equals(strExtension)) {
				return aTypes[i];
			}
		}
		return null;
	}

	public static void convertToNormalizedFile(File inputFile, File outputFile)
			throws UnsupportedAudioFileException, IOException, IllegalArgumentException {
		AudioInputStream inputStream = null;
		ByteArrayOutputStream temporaryOutputStream = null;
		if (inputFile.getName().toLowerCase().endsWith(".mp3")) {
			LOG.info("temporary converting");

			AudioInputStream temporaryInputStream = AudioSystem.getAudioInputStream(inputFile);
			LOG.info("source format: " + temporaryInputStream.getFormat());

			temporaryInputStream = convertToTemporaryFormat(temporaryInputStream);
			LOG.info("target format: " + temporaryInputStream.getFormat());

			int nWrittenBytes = 0;
			AudioFileFormat.Type targetFileType = findTargetType("au");

			temporaryOutputStream = new ByteArrayOutputStream();
			nWrittenBytes = AudioSystem.write(temporaryInputStream, targetFileType, temporaryOutputStream);
			LOG.info("Written bytes: " + nWrittenBytes);

			inputStream = AudioSystem.getAudioInputStream(new ByteArrayInputStream(temporaryOutputStream.toByteArray()));
			temporaryOutputStream.reset();
			temporaryOutputStream = null;
		} else if (inputFile.getName().toLowerCase().endsWith(".wav")) {
			inputStream = AudioSystem.getAudioInputStream(inputFile);
		} else throw new UnsupportedAudioFileException("format not supported");

		LOG.info("converting");

		LOG.info("source format: " + inputStream.getFormat());

		AudioInputStream desiredInputStream = convertToDesiredFormat(inputStream);
		LOG.info("target format: " + inputStream.getFormat());

		int nWrittenBytes = 0;
		AudioFileFormat.Type targetFileType = findTargetType("wav");
		nWrittenBytes = AudioSystem.write(desiredInputStream, targetFileType, outputFile);
		LOG.info("Written bytes: " + nWrittenBytes);
	}

	/* inputType: .mp3 | .wav */
	public static void convertToNormalizedFile(String inputType, byte[] dataBytes, File outputFile)
			throws UnsupportedAudioFileException, IOException, IllegalArgumentException {
		AudioInputStream inputStream = null;
		ByteArrayOutputStream temporaryOutputStream = null;
		if (inputType.toLowerCase().endsWith(".mp3")) {
			LOG.info("temporary converting");

			AudioInputStream temporaryInputStream = AudioSystem.getAudioInputStream(new ByteArrayInputStream(dataBytes));
			LOG.info("source format: " + temporaryInputStream.getFormat());

			temporaryInputStream = convertToTemporaryFormat(temporaryInputStream);
			LOG.info("target format: " + temporaryInputStream.getFormat());

			int nWrittenBytes = 0;
			AudioFileFormat.Type targetFileType = findTargetType("au");

			temporaryOutputStream = new ByteArrayOutputStream();
			nWrittenBytes = AudioSystem.write(temporaryInputStream, targetFileType, temporaryOutputStream);
			LOG.info("Written bytes: " + nWrittenBytes);

			inputStream = AudioSystem.getAudioInputStream(new ByteArrayInputStream(temporaryOutputStream.toByteArray()));
			temporaryOutputStream.reset();
			temporaryOutputStream = null;
		} else if (inputType.toLowerCase().endsWith(".wav")) {
			inputStream = AudioSystem.getAudioInputStream(new ByteArrayInputStream(dataBytes));
		} else throw new UnsupportedAudioFileException("format not supported");

		LOG.info("converting");

		LOG.info("source format: " + inputStream.getFormat());

		AudioInputStream desiredInputStream = convertToDesiredFormat(inputStream);
		LOG.info("target format: " + inputStream.getFormat());

		int nWrittenBytes = 0;
		AudioFileFormat.Type targetFileType = findTargetType("wav");
		nWrittenBytes = AudioSystem.write(desiredInputStream, targetFileType, outputFile);
		LOG.info("Written bytes: " + nWrittenBytes);
	}

	private static int calculateFrameSize(int nChannels, int nSampleSizeInBits) {
		return ((nSampleSizeInBits + 7) / 8) * nChannels;
	}

	private static AudioInputStream convertToTemporaryFormat(
			AudioInputStream sourceStream) {

		AudioFormat sourceFormat = sourceStream.getFormat();
		AudioFormat targetFormat = new AudioFormat(
				ENCODING,
				sourceFormat.getSampleRate(),
				SAMPLE_SIZE,
				sourceFormat.getChannels(),
				calculateFrameSize(sourceFormat.getChannels(), SAMPLE_SIZE),
				sourceFormat.getFrameRate(),
				sourceFormat.isBigEndian());
		return AudioSystem.getAudioInputStream(targetFormat, sourceStream);
	}

	private static AudioInputStream convertToDesiredFormat(
			AudioInputStream sourceStream) {
		int sampleRate = Integer.parseInt(EngineConfiguration.getInstance().get(SAMPLE_RATE));
		int sampleSize = SAMPLE_SIZE;
		int numChannels = NUM_CHANNELS;

		AudioFormat sourceFormat = sourceStream.getFormat();
		AudioFormat targetFormat = new AudioFormat(
				ENCODING,
				sampleRate,
				sampleSize,
				numChannels,
				calculateFrameSize(numChannels,
						sampleSize),
				sampleRate,
				sourceFormat.isBigEndian());
		return AudioSystem.getAudioInputStream(targetFormat, sourceStream);
	}

	public static void main(String[] args) throws Exception {
		args = new String[]{"samples/o1.mp3", "samples/o.wav"};
		convertToNormalizedFile(new File(args[0]), new File(args[1]));
	}
}

