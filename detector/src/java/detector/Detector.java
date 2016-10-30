package detector;

import gnu.trove.iterator.TIntIntIterator;
import gnu.trove.iterator.TIntIterator;
import gnu.trove.iterator.TIntObjectIterator;
import gnu.trove.list.TIntList;
import gnu.trove.map.TIntIntMap;
import gnu.trove.map.TIntObjectMap;
import gnu.trove.map.hash.TIntIntHashMap;
import org.apache.log4j.Logger;
import util.EngineConfiguration;
import util.Pair;
import util.chart.BarChartPlotter;
import util.chart.ScatterPlotter;
import util.song.Song;
import util.song.SongFingerprint;
import util.song.model.FrequencySpectrum;
import util.song.model.FrequencySpectrumDescriptor;

import java.io.DataOutputStream;
import java.io.File;
import java.net.Socket;
import java.util.*;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * Created by thinhhv on 03/08/2014.
 */
public class Detector {
    private static final Logger LOG = Logger.getLogger(Detector.class);
    private static final String DETECT_CONNECTION_LIMIT = "detector-server.detect-connection-limit";
    private static final String DETECT_CONNECTION_RESERVED = "detector-server.detect-connection-reserved";
    private static final String THREAD_PER_DETECT = "detector.thread-per-detect";
    private static final String DETECT_TOTAL_TIME_LIMIT = "detector.detect-total-time-limit";


    private DetectorTable detectorTable;
    private ExecutorService executorService;

    private ArrayBlockingQueue<DetectStream> detectStreamQueue;

    public Detector() {
        this.detectorTable = new DetectorTable();
        executorService = Executors.newFixedThreadPool(
                Integer.parseInt(EngineConfiguration.getInstance().get(DETECT_CONNECTION_LIMIT)) *
                        Integer.parseInt(EngineConfiguration.getInstance().get(THREAD_PER_DETECT))
        );
        int detectStreamQueueSize = Integer.parseInt(EngineConfiguration.getInstance().get(DETECT_CONNECTION_LIMIT)) +
                Integer.parseInt(EngineConfiguration.getInstance().get(DETECT_CONNECTION_RESERVED));

        detectStreamQueue = new ArrayBlockingQueue<DetectStream>(detectStreamQueueSize);
        for (int i = 0; i < detectStreamQueueSize; ++i) {
            detectStreamQueue.add(new DetectStream());
        }
    }

    public static int getMatcherScore(SongFingerprint songFingerprint, SongFingerprint recordFingerprint) {
        Detector detector = new Detector();
        Song song = new Song();
        song.setSongId(1);
        song.setFingerprint(songFingerprint);
        detector.getDetectorTable().addSong(song);
        return detector.getDetectorTable().match(recordFingerprint, 0).get(1).max();
    }

    public static void plotTimeMatcher(SongFingerprint songFingerprint, SongFingerprint recordFingerprint) {
        Detector detector = new Detector();
        Song song = new Song();
        song.setSongId(1);
        song.setFingerprint(songFingerprint);
        detector.getDetectorTable().addSong(song);
        TIntList list = detector.getDetectorTable().match(recordFingerprint, 0).get(1);
        int index = 0;
        int value = 0;

        int run = 0;
        if (list != null) {
            for (TIntIterator it = list.iterator(); it.hasNext(); ) {
                int v = it.next();
                if (v > value) {
                    value = v;
                    index = run;
                }
                ++run;
            }
        }
        plotTimeMatcher(songFingerprint, recordFingerprint, index);
    }

    public static void plotTimeMatcher(SongFingerprint songFingerprint, SongFingerprint recordFingerprint, int recordTryMatchIndex) {
        Detector detector = new Detector();
        Song song = new Song();
        song.setSongId(1);
        song.setFingerprint(songFingerprint);
        detector.getDetectorTable().addSong(song);

        List<Pair<Short, Short>> matchingList = new LinkedList<Pair<Short, Short>>();
        int[] hash = recordFingerprint.getHash(recordTryMatchIndex);
        short[] offset = recordFingerprint.getOffset(recordTryMatchIndex);
        for (Pair<Integer, Pair<int[], short[]>> pair : detector.getDetectorTable().hashTable) {
            int[] songHash = pair.second.first;
            short[] songOffset = pair.second.second;
            int startIndex = 0;
            for (int i = 0; i < hash.length; ++i) {
                int matchIndex = detector.getDetectorTable().binarySearch(songHash, hash[i], startIndex);
                startIndex = matchIndex;
                while (matchIndex < songHash.length && songHash[matchIndex] == hash[i]) {
                    matchingList.add(new Pair<Short, Short>(songOffset[matchIndex], offset[i]));
                    ++matchIndex;
                }
            }
        }
        float[] x = new float[matchingList.size()], y = new float[matchingList.size()];
        int num = 0;
        for (Pair<Short, Short> p : matchingList) {
            x[num] = p.first;
            y[num] = p.second;
            ++num;
        }
        int count = matchingList.size();
        matchingList = null;
        new ScatterPlotter("TimeMatcher: " + count, "SongTime", "RecordTime", x, y).plot();

    }

    public static void printTimeDiffMatcher(SongFingerprint songFingerprint, SongFingerprint recordFingerprint) {
        Detector detector = new Detector();
        Song song = new Song();
        song.setSongId(1);
        song.setFingerprint(songFingerprint);
        detector.getDetectorTable().addSong(song);
        detector.getDetectorTable().match(recordFingerprint, 0);
    }

    public static void plotTimeDiffMatcher(SongFingerprint songFingerprint, SongFingerprint recordFingerprint) {
        Detector detector = new Detector();
        Song song = new Song();
        song.setSongId(1);
        song.setFingerprint(songFingerprint);
        detector.getDetectorTable().addSong(song);
        TIntList list = detector.getDetectorTable().match(recordFingerprint, 0).get(1);
        int index = 0;
        int value = 0;

        int run = 0;
        if (list != null) {
            for (TIntIterator it = list.iterator(); it.hasNext(); ) {
                int v = it.next();
                if (v > value) {
                    value = v;
                    index = run;
                }
                ++run;
            }
        }
        plotTimeDiffMatcher(songFingerprint, recordFingerprint, index);
    }

    public static void plotTimeDiffMatcher(SongFingerprint songFingerprint, SongFingerprint recordFingerprint, int recordTryMatchIndex) {
        Detector detector = new Detector();
        Song song = new Song();
        song.setSongId(1);
        song.setFingerprint(songFingerprint);
        detector.getDetectorTable().addSong(song);

        TIntIntMap countingMap = new TIntIntHashMap();
        int[] hash = recordFingerprint.getHash(recordTryMatchIndex);
        short[] offset = recordFingerprint.getOffset(recordTryMatchIndex);
        for (Pair<Integer, Pair<int[], short[]>> pair : detector.getDetectorTable().hashTable) {
            int[] songHash = pair.second.first;
            short[] songOffset = pair.second.second;
            int startIndex = 0;
            for (int i = 0; i < hash.length; ++i) {
                int matchIndex = detector.getDetectorTable().binarySearch(songHash, hash[i], startIndex);
                startIndex = matchIndex;
                while (matchIndex < songHash.length && songHash[matchIndex] == hash[i]) {
                    int diff = songOffset[matchIndex] - offset[i];
                    if (diff >= 0 && countingMap.containsKey(diff)) {
                        countingMap.put(diff, countingMap.get(diff) + 1);
                    } else countingMap.put(diff, 1);
                    ++matchIndex;
                }
            }
        }
        int[] x = new int[countingMap.size()], y = new int[countingMap.size()];
        int num = 0;
        for (TIntIntIterator it = countingMap.iterator(); it.hasNext(); ) {
            it.advance();
            x[num] = it.key();
            y[num] = it.value();
            ++num;
        }
        int count = countingMap.size();
        countingMap = null;
        new BarChartPlotter("TimeDiffMatcher: " + count, "Diff", "Count", x, y).plot();
    }

    public static void main(String[] args) throws Exception {
//		FrequencySpectrum.createFrequencySpectrumFromFile(
//				FrequencySpectrumDescriptor.getDefaultFrequencySpectrumDescriptorForSong(),
//				new File("samples/o1.mp3")
//			).toSerializableObject().toFile(new File("samples/o1.sobj"));

        SongFingerprint songFingerprint = SongFingerprint.createFingerprintFromFrequencySpectrum(
                FrequencySpectrum.createFrequencySpectrumFromFile(
                        FrequencySpectrumDescriptor.getDefaultFrequencySpectrumDescriptorForSong(),
                        new File("samples/o1.mp3")
                )
        );

//		SongFingerprint songFingerprint = SongFingerprint.createFingerprintFromFrequencySpectrum(
//				FrequencySpectrum.createFrequencySpectrumFromSerializableObject(
//						FrequencySpectrumSerializableObject.fromFile(new File("samples/o1.sobj"))
//				)
//		);

        SongFingerprint recordFingerprint = SongFingerprint.createFingerprintFromFrequencySpectrum(
                FrequencySpectrum.createFrequencySpectrumFromFile(
                        FrequencySpectrumDescriptor.getDefaultFrequencySpectrumDescriptorForRecord(),
                        new File("samples/t1.wav")
                )
        );
        printTimeDiffMatcher(songFingerprint, recordFingerprint);
    }

    public DetectorTable getDetectorTable() {
        return detectorTable;
    }

    public TIntList listSong() {
        return detectorTable.listSong();
    }

    public boolean addSong(Song song) {
        return detectorTable.addSong(song);
    }

    public boolean removeSong(Song song) {
        return detectorTable.removeSong(song);
    }

    public boolean removeSong(int key) {
        return detectorTable.removeSong(key);
    }

    public boolean containsSong(Song song) {
        return detectorTable.containsSong(song);
    }

    public boolean containsSong(int key) {
        return detectorTable.containsSong(key);
    }

    public Song getSong(int id) {
        return detectorTable.getSong(id);
    }

    public List<Pair<Song, Integer>> detect(SongFingerprint pattern) {
        LOG.info("detecting");
        TIntObjectMap<TIntList> matchResult = detectorTable.match(pattern);
        List<Pair<Song, Integer>> result = new LinkedList<Pair<Song, Integer>>();
        for (TIntObjectIterator<TIntList> it = matchResult.iterator(); it.hasNext(); ) {
            it.advance();
            result.add(new Pair<Song, Integer>(detectorTable.getSong(it.key()), it.value().max()));
        }
        Collections.sort(result, new Comparator<Pair<Song, Integer>>() {
            @Override
            public int compare(Pair<Song, Integer> o1, Pair<Song, Integer> o2) {
                return o2.second - o1.second;
            }
        });
        return result;
    }

    public void detect(Socket sc) {
        long elapsedTime = -1;
        int signalCount = -1;
        String processedSignalCountString = null;
        LOG.info("incoming request");
        boolean flag = detectorTable.lock.readLock().tryLock();
        if (!flag) {
            try {
                DataOutputStream out = new DataOutputStream(sc.getOutputStream());
                out.writeInt(0);
                out.flush();
                return;
            } catch (Exception e) {

            }
        }
        int detectTotalTimeLimit = Integer.parseInt(EngineConfiguration.getInstance().get(DETECT_TOTAL_TIME_LIMIT)) * 1000;
        DetectStream detectStream = null;
        try {
            LOG.info("initiating for incoming detect");
            detectStream = detectStreamQueue.poll();
            if (detectStream == null) {
                DataOutputStream out = new DataOutputStream(sc.getOutputStream());
                out.writeInt(0);
                out.flush();
                return;
            }
            detectStream.init(sc, detectorTable.hashTable.size());
            int thread_per_detect = Integer.parseInt(EngineConfiguration.getInstance().get(THREAD_PER_DETECT));

            ArrayList<Future> futures = new ArrayList<Future>();
            for (int i = 0; i < thread_per_detect; i++) {
                futures.add(executorService.submit(new DetectorWorker(detectStream, detectorTable, i, thread_per_detect)));
            }
            new Thread(new DetectStreamProcessor(detectStream)).start();
            LOG.info("detecting");

            if (!detectStream.detected)
                synchronized (detectStream.detectedNotifyObject) {
                    detectStream.detectedNotifyObject.wait(detectTotalTimeLimit);
                }
            for (Future f : futures) {
                f.cancel(true);
            }
            if (!detectStream.detected) {
                LOG.info("network error OR detect time too long");
                throw new Exception();
            }
        } catch (Exception e) {
            try {
                synchronized (detectStream.out) {
                    if (!detectStream.detected) {
                        detectStream.out.writeInt(0);
                        detectStream.out.flush();
                        detectStream.detected = true;
                    }
                }
            } catch (Exception ex) {
            }
        } finally {
            LOG.info("closing socket");
            try {
                Thread.sleep(500);
            } catch (Exception e) {
            }
            try {
                sc.getInputStream().close();
            } catch (Exception e) {
            }
            try {
                sc.getOutputStream().close();
            } catch (Exception e) {
            }
            try {
                sc.close();
            } catch (Exception e) {
            }
            try {
                Thread.sleep(500);
                if (detectStream != null) {
                    signalCount = detectStream.getSignalCount();
                    processedSignalCountString = detectStream.getProcessedSignalCountString();
                    elapsedTime = detectStream.getElapsedTime();
                    detectStream.reset();
                    detectStreamQueue.add(detectStream);
                }
            } catch (Exception e) {
            }
            detectorTable.lock.readLock().unlock();
            System.gc();
        }
        LOG.info("total detect time = " + (elapsedTime) + " ms");
        LOG.info("total signal count = " + signalCount + " ; processed = " + processedSignalCountString);
    }
}
