package detector;

import org.junit.Test;
import util.song.SongFingerprint;
import util.song.model.FrequencySpectrum;
import util.song.model.FrequencySpectrumDescriptor;

import java.io.File;

/**
 * Created by thinhhv on 23/08/2014.
 */
public class DetectorTest extends DetectorTestBase {

	private static final File originalRoot = new File("data/testdata");
	private static final File recordRoot = new File(originalRoot, "records");
	private static final int NUM_TEST = 51;

	@Test
	public void test0() throws Exception {
		SongFingerprint[] original = new SongFingerprint[NUM_TEST];
		SongFingerprint[][] record = new SongFingerprint[NUM_TEST][3];
		for (int i = 0; i < NUM_TEST; ++i) {
			original[i] = SongFingerprint.createFingerprintFromFrequencySpectrum(
					FrequencySpectrum.createFrequencySpectrumFromFile(
							FrequencySpectrumDescriptor.getDefaultFrequencySpectrumDescriptorForSong(),
							new File(originalRoot, "o" + (i + 1) + ".mp3")
					)
			);
			for (int j = 0; j < 3; ++j) {
				record[i][j] = SongFingerprint.createFingerprintFromFrequencySpectrum(
						FrequencySpectrum.createFrequencySpectrumFromFile(
								FrequencySpectrumDescriptor.getDefaultFrequencySpectrumDescriptorForRecord(),
								new File(recordRoot, "r" + (i + 1) + "_" + (j + 1) + ".wav")
						)
				);
			}
		}
		pairedAssert(original, record);
	}
}
