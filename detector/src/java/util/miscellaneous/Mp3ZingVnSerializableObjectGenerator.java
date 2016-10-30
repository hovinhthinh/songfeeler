package util.miscellaneous;

import util.song.model.FrequencySpectrum;
import util.song.model.FrequencySpectrumDescriptor;

import java.io.File;
import java.util.ArrayList;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by thinhhv on 12/09/2014.
 */
public class Mp3ZingVnSerializableObjectGenerator {
	private static final int num_threads = 8;
	private static File nullObj = new File("null");

	/* <rootFolder> */
	public static void main(String[] args) throws Exception {
		args = new String[]{"data/song"};
		File root = new File(args[0]);
		ExecutorService executorService = Executors.newScheduledThreadPool(num_threads);
		ArrayBlockingQueue<File> queue = new ArrayBlockingQueue<File>(16);
		ArrayList<Future> futures = new ArrayList<Future>();
		AtomicInteger counter = new AtomicInteger();
		for (int i = 0; i < num_threads; ++i) futures.add(executorService.submit(new GeneratorWorker(queue, counter)));
		for (File f : root.listFiles()) {
			if (!f.getName().toLowerCase().endsWith(".mp3") && !f.getName().toLowerCase().endsWith(".wav")) continue;
			queue.put(f);
		}
		for (int i = 0; i < num_threads; ++i) queue.put(nullObj);

		for (Future f : futures) f.get();
		System.out.println("done.");
		executorService.shutdown();
	}

	private static class GeneratorWorker implements Callable<Void> {
		ArrayBlockingQueue<File> queue;
		AtomicInteger counter;

		public GeneratorWorker(ArrayBlockingQueue<File> queue, AtomicInteger counter) {
			this.counter = counter;
			this.queue = queue;
		}

		@Override
		public Void call() throws Exception {
			File next;
			while (nullObj != (next = queue.take())) {
				try {
					File out = new File("data/sobj/" + next.getName() + ".sobj");
					System.out.println("processed: " + counter.incrementAndGet() + "\t" + next.getName());
					if (out.exists()) continue;
					if (next.length() > 10000000) {
						continue;
					}
					FrequencySpectrum spectrum = FrequencySpectrum.createFrequencySpectrumFromFile(
							FrequencySpectrumDescriptor.getDefaultFrequencySpectrumDescriptorForSong(),
							next
					);
					if (spectrum != null) {
						spectrum.toSerializableObject().toFile(out);
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			return null;
		}
	}
}
