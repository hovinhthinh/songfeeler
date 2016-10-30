package util.miscellaneous;

import util.Crawler;

import java.io.*;
import java.util.ArrayList;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by thinhhv on 12/09/2014.
 */
public class Mp3ZingVnDownloader {

	private static final int numThreads = 4;

	/* <inputFile> <rootFolder> <mode 1:mp3 2:image>*/
	public static void main(String[] args) throws Exception {
		args = new String[]{
				"data/Mp3ZingVn.out.filtered_2014-08",
				"data/img",
				"2"
		};
		if (args[2].equals("1")) {
			downloadMp3(args);
		}
		if (args[2].equals("2")) {
			downloadImage(args);
		}
	}

	public static void downloadImage(String args[]) throws Exception {
		File root = new File(args[1]);
		BufferedReader in = new BufferedReader(new InputStreamReader(new FileInputStream(args[0])));
		PrintWriter out = new PrintWriter(new OutputStreamWriter(new FileOutputStream(new File(root, "fail"))));

		ExecutorService executorService = Executors.newFixedThreadPool(numThreads);
		ArrayList<Future> futures = new ArrayList<Future>();
		AtomicInteger counter = new AtomicInteger(0);
		for (int i = 0; i < numThreads; ++i) {
			futures.add(executorService.submit(new ImageWorker(in, out, counter, root)));
		}
		for (Future f : futures) f.get();
		in.close();
		out.close();
		System.out.println("done.");
		executorService.shutdown();
	}

	public static void downloadMp3(String[] args) throws Exception {
		File root = new File(args[1]);
		BufferedReader in = new BufferedReader(new InputStreamReader(new FileInputStream(args[0])));
		PrintWriter out = new PrintWriter(new OutputStreamWriter(new FileOutputStream(new File(root, "fail"))));

		ExecutorService executorService = Executors.newFixedThreadPool(numThreads);
		ArrayList<Future> futures = new ArrayList<Future>();
		AtomicInteger counter = new AtomicInteger(0);
		for (int i = 0; i < numThreads; ++i) {
			futures.add(executorService.submit(new Mp3Worker(in, out, counter, root)));
		}
		for (Future f : futures) f.get();
		in.close();
		out.close();
		System.out.println("done.");
		executorService.shutdown();
	}

	private static class Mp3Worker implements Callable<Void> {
		BufferedReader in;
		PrintWriter out;
		AtomicInteger counter;
		File root;

		public Mp3Worker(BufferedReader in, PrintWriter out, AtomicInteger counter, File root) {
			this.in = in;
			this.out = out;
			this.counter = counter;
			this.root = root;
		}

		@Override
		public Void call() throws Exception {
			String line;
			while (null != (line = in.readLine())) {
				if (line.isEmpty()) continue;
				int pos = line.indexOf("\t");
				if (pos < 0) pos = line.indexOf(" ");

				String url;
				if (pos < 0) url = line;
				else url = line.substring(0, pos);
				if (!Mp3ZingVn.isAcceptedUrl(url)) continue;
				System.out.println("processed: " + counter.incrementAndGet() + "\t" + url);
				String dataId = url.substring(url.lastIndexOf("/") + 1, url.lastIndexOf("."));
				File fo = new File(root, dataId + ".mp3");
				if (fo.exists()) continue;
				byte[] bytes = Mp3ZingVn.getSongMp3DataBytes(url);
				if (bytes == null) {
					out.println(url);
					out.flush();
				} else {
					FileOutputStream outFile = new FileOutputStream(fo);
					outFile.write(bytes);
					outFile.close();
				}
			}
			return null;
		}
	}

	private static class ImageWorker implements Callable<Void> {
		BufferedReader in;
		PrintWriter out;
		AtomicInteger counter;
		File root;

		public ImageWorker(BufferedReader in, PrintWriter out, AtomicInteger counter, File root) {
			this.in = in;
			this.out = out;
			this.counter = counter;
			this.root = root;
		}

		@Override
		public Void call() throws Exception {
			String line;
			while (null != (line = in.readLine())) {
				if (line.isEmpty()) continue;
				String[] arr = line.split("\t");
				String url = arr[0];
				String img = arr[8];

				System.out.println("processed: " + counter.incrementAndGet() + "\t" + img);
				String dataId = url.substring(url.lastIndexOf("/") + 1, url.lastIndexOf("."));
				File fo = new File(root, dataId + ".img");
				if (fo.exists()) continue;
				byte[] bytes = Crawler.getContentBytesFromUrl(img);
				if (bytes == null) {
					out.println(url + " " + img);
					out.flush();
				} else {
					FileOutputStream outFile = new FileOutputStream(fo);
					outFile.write(bytes);
					outFile.close();
				}
			}
			return null;
		}
	}

}
