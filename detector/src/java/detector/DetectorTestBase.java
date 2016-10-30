package detector;

import util.EngineConfiguration;
import util.song.SongFingerprint;

import static org.junit.Assert.assertTrue;

/**
 * Created by thinhhv on 09/08/2014.
 */
public abstract class DetectorTestBase {
	private static final int MATCH_THRESHOLD = Integer.parseInt(EngineConfiguration.getInstance().get("detector.match-threshold"));

	protected void assertMatched(SongFingerprint songFingerprint, SongFingerprint recordFingerprint) {
		int score = Detector.getMatcherScore(songFingerprint, recordFingerprint);
		assertTrue("threshold = " + MATCH_THRESHOLD + " ; matched score = " + score, score >= MATCH_THRESHOLD);
	}

	protected void assertNotMatched(SongFingerprint songFingerprint, SongFingerprint recordFingerprint) {
		int score = Detector.getMatcherScore(songFingerprint, recordFingerprint);
		assertTrue("threshold = " + MATCH_THRESHOLD + " ; matched score = " + score, score < MATCH_THRESHOLD);
	}

	protected void assertMatched(String message, SongFingerprint songFingerprint, SongFingerprint recordFingerprint) {
		int score = Detector.getMatcherScore(songFingerprint, recordFingerprint);
		assertTrue("message: {" + message + "} ; threshold = " + MATCH_THRESHOLD + " ; matched score = " + score, score >= MATCH_THRESHOLD);
	}

	protected void assertNotMatched(String message, SongFingerprint songFingerprint, SongFingerprint recordFingerprint) {
		int score = Detector.getMatcherScore(songFingerprint, recordFingerprint);
		assertTrue("message: {" + message + "} ; threshold = " + MATCH_THRESHOLD + " ; matched score = " + score, score < MATCH_THRESHOLD);
	}

	protected void pairedAssert(SongFingerprint songFingerprint[], SongFingerprint recordFingerprint[][]) {
		for (int i = 0; i < songFingerprint.length; ++i)
			for (int j = 0; j < recordFingerprint.length; ++j) {
				if (i == j) {
					for (int k = 0; k < recordFingerprint[j].length; ++k) {
						assertMatched("matched fail at: [" + i + "] - [" + j + "]" + "[" + k + "]", songFingerprint[i], recordFingerprint[j][k]);
					}
				} else {
					for (int k = 0; k < recordFingerprint[j].length; ++k) {
						assertNotMatched("not matched fail at: [" + i + "] - [" + j + "]" + "[" + k + "]", songFingerprint[i], recordFingerprint[j][k]);
					}
				}
			}
	}
}
