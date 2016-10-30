package util.miscellaneous;

import org.apache.log4j.Logger;

import java.io.*;
import java.util.ArrayList;
import java.util.concurrent.*;

/**
 * Created by thinhhv on 26/08/2014.
 */
/* TODO */
public class Mp3ZingVnCrawler {
	private static final Logger LOG = Logger.getLogger(Mp3ZingVnCrawler.class);
	private static final int CRAWLING_DELAY = 250;
	private static int NUM_THREADS = 4;

	/* <input-file> [num_threads]*/
	public static void main(String[] args) throws Exception {
//		args = new String[]{"data/mp3.zing.vn_urls/data_sets/set_0/data_0", "1"};
		ConcurrentLinkedQueue<String> inUrlQueue = new ConcurrentLinkedQueue<String>();
		File inFile = new File(args[0]);
		File outFile = new File(inFile.getAbsolutePath() + ".out");
		if (outFile.exists()) {
			LOG.info(outFile.getAbsolutePath() + " existed");
			return;
		}
		if (args.length > 1) {
			NUM_THREADS = Integer.parseInt(args[1]);
		}
		PrintWriter out = new PrintWriter(new BufferedWriter(new OutputStreamWriter(new FileOutputStream(outFile), "UTF-8")));
		BufferedReader in = new BufferedReader(new InputStreamReader(new FileInputStream(inFile)));

		String line;

		while (null != (line = in.readLine())) {
			line = line.trim().replaceAll("\\s++", " ");
			if (line.isEmpty()) continue;
			inUrlQueue.add(line);
		}
		in.close();
		ExecutorService executorService = Executors.newFixedThreadPool(NUM_THREADS);

		ArrayList<Future> futures = new ArrayList<Future>();
		for (int i = 0; i < NUM_THREADS; ++i) {
			futures.add(executorService.submit(new Worker(inUrlQueue, out)));
		}
		new Thread(new Monitor(inUrlQueue)).start();
		for (Future f : futures) f.get();
		executorService.shutdown();

		out.close();
	}

	private static class Worker implements Callable<Void> {
		private ConcurrentLinkedQueue<String> inUrlQueue;
		private PrintWriter out;

		public Worker(ConcurrentLinkedQueue<String> inUrlQueue, PrintWriter out) {
			this.inUrlQueue = inUrlQueue;
			this.out = out;
		}

		@Override
		public Void call() throws Exception {
			String line;
			while (null != (line = inUrlQueue.poll())) {
				int pos = line.indexOf(' ');
				String url = line.substring(0, pos);
				String timestamp = line.substring(pos + 1);
				Mp3ZingVn.ZingSong song = Mp3ZingVn.ZingSong.from(url, timestamp);
//				LOG.info(song.toString());
				synchronized (out) {
					out.println(song.toString());
					out.flush();
				}
				try {
					Thread.sleep(CRAWLING_DELAY);
				} catch (Exception ex) {
				}
			}
			return null;
		}
	}

	private static class Monitor implements Runnable {
		private static final Logger LOG = Logger.getLogger(Monitor.class);
		private static final int SLEEP = 10;
		private ConcurrentLinkedQueue inUrlQueue;

		public Monitor(ConcurrentLinkedQueue inUrlQueue) {
			this.inUrlQueue = inUrlQueue;
		}

		@Override
		public void run() {
			int initialSize = inUrlQueue.size();
			int countSecond = 0;
			while (!inUrlQueue.isEmpty()) {
				try {
					Thread.sleep(SLEEP * 1000);
				} catch (Exception e) {
				}
				countSecond += SLEEP;
				int processed = initialSize - inUrlQueue.size();
				LOG.info("processed: " + processed + " | remaining: " + inUrlQueue.size() + " | speed: " + (processed / countSecond));
			}
		}
	}
}
