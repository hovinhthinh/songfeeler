package detector;

import org.apache.log4j.Logger;
import util.Crawler;
import util.EngineConfiguration;
import util.Compressor;

import java.io.DataOutputStream;

/**
 * Created by thinhhv on 09/09/2014.
 */
public class DetectorWorker implements Runnable {
	private static final Logger LOG = Logger.getLogger(DetectorWorker.class);
	private static final String TRY_MATCH_TIME = "detector.try-match-time";
	private static final String RUNNING_BLOCK = "detector.worker.running-block";

	private static final String WEB_SERVER_ADDRESS = "web-server.address";
	private static final String WEB_SERVER_PORT = "web-server.port";
	private static final String WEB_SERVER_GET_SONG_PATH = "web-server.handler.get-song-info.context-path";
	private static final String GET_SONG_PREFIX =
			"http://" + EngineConfiguration.getInstance().get(WEB_SERVER_ADDRESS) + ":" + EngineConfiguration.getInstance().get(WEB_SERVER_PORT)
					+ EngineConfiguration.getInstance().get(WEB_SERVER_GET_SONG_PATH);
	private static final String WEB_SERVER_INCREASE_DETECTED_PATH = "web-server.handler.increase-detected.context-path";
	private static final String INCREASE_DETECTED_PREFIX =
			"http://" + EngineConfiguration.getInstance().get(WEB_SERVER_ADDRESS) + ":" + EngineConfiguration.getInstance().get(WEB_SERVER_PORT)
					+ EngineConfiguration.getInstance().get(WEB_SERVER_INCREASE_DETECTED_PATH);

	private DetectorTable detectorTable;
	private int startIndex, jumpStep;
	private DetectStream detectStream;
	private DataOutputStream out;
	private int[] fingerprintProcessedCursor;
	private int try_match_time;
	private int running_block;

	public DetectorWorker(DetectStream detectStream, DetectorTable detectorTable, int startIndex, int jumpStep) throws Exception {
		this.detectStream = detectStream;
		this.detectorTable = detectorTable;
		this.startIndex = startIndex;
		this.jumpStep = jumpStep;

		out = detectStream.out;

		try_match_time = Integer.parseInt(EngineConfiguration.getInstance().get(TRY_MATCH_TIME));
		running_block = Integer.parseInt(EngineConfiguration.getInstance().get(RUNNING_BLOCK));
		fingerprintProcessedCursor = new int[try_match_time];
	}

	@Override
	public void run() {
		try {
			while (detectStream.streaming && !detectStream.detected && !Thread.currentThread().isInterrupted()) {
				synchronized (detectStream.newDataNotifyObject) {
					detectStream.newDataNotifyObject.wait();
				}
				boolean dataAvailable;
				do {
					if (detectStream.detected || Thread.currentThread().isInterrupted()) return;
					dataAvailable = false;
					for (int i = 0; i < try_match_time; ++i) {
						for (int runBlock = 0; runBlock < running_block; ++runBlock) {
							if (fingerprintProcessedCursor[i] >= detectStream.fingerprintCursor[i]) break;
							if (detectStream.detected || Thread.currentThread().isInterrupted()) return;
							dataAvailable = true;
							for (int k = startIndex; k < detectStream.detectSize; k += jumpStep) {
								if (detectStream.detected || Thread.currentThread().isInterrupted()) return;

								int[] hash = detectorTable.hashTable.get(k).second.first;
								short[] offset = detectorTable.hashTable.get(k).second.second;

								int pos = detectorTable.binarySearch(hash, detectStream.hash[i][fingerprintProcessedCursor[i]], 0);
								while (pos < hash.length && hash[pos] == detectStream.hash[i][fingerprintProcessedCursor[i]]) {
									if (detectStream.detected || Thread.currentThread().isInterrupted()) return;
									boolean detect = detectStream.add(i, k, offset[pos] - detectStream.offset[i][fingerprintProcessedCursor[i]]);
									if (detect) {
										synchronized (out) {
											if (!detectStream.detected) {
												detectStream.detected = true;
												final int id = detectorTable.hashTable.get(k).first;
												LOG.info("detected: key = " + id + "; elapsed time = " + detectStream.getElapsedTime() + " ms");

												String songInfo = Crawler.getContentFromUrl(GET_SONG_PREFIX + id);
												if (songInfo == null) {
													songInfo = "";
												}

												byte[] dataBytes = songInfo.getBytes("UTF-8");
												LOG.info("get db info: data length = " + dataBytes.length + "; elapsed time = " + detectStream.getElapsedTime() + " ms");
												LOG.info("compressing data");
												dataBytes = Compressor.compressByteArray(dataBytes);
												out.writeInt(dataBytes.length);
												out.write(dataBytes);
												out.flush();

												synchronized (detectStream.detectedNotifyObject) {
													detectStream.detectedNotifyObject.notify();
												}
												new Thread(new Runnable() {
													@Override
													public void run() {
														Crawler.getContentFromUrl(INCREASE_DETECTED_PREFIX + id, "PUT");
													}
												}).start();
												return;
											}
										}
									}
									++pos;
								}
							}
							++fingerprintProcessedCursor[i];
							detectStream.increaseProcessedSignalCount(startIndex);
						}
					}
				} while (dataAvailable);
			}
			if (detectStream.detected || Thread.currentThread().isInterrupted()) return;

			if (detectStream.nullCounter.decrementAndGet() == 0) {
				synchronized (out) {
					if (!detectStream.detected) {
						out.writeInt(0);
						out.flush();
						detectStream.detected = true;
						LOG.info("detected: key = " + 0);
						synchronized (detectStream.detectedNotifyObject) {
							detectStream.detectedNotifyObject.notify();
						}
					}
				}
			}
		} catch (InterruptedException e) {
		} catch (Exception e) {
			e.printStackTrace();
			LOG.info("error in detector worker");
		}
	}
}
