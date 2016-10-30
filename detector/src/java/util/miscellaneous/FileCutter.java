package util.miscellaneous;

import java.io.*;

/**
 * Created by thinhhv on 27/08/2014.
 */
public class FileCutter {
	private static int BLOCK_SIZE = 100000;
	private static int set_counter = 0;

	private static void flush(String[] buffer, int counter, File folder) throws Exception {
		File set_folder = new File(folder, "set_" + set_counter);
		if (!set_folder.exists()) set_folder.mkdirs();
		File data = new File(set_folder, "data_" + set_counter);
		PrintWriter out = new PrintWriter(new BufferedWriter(new OutputStreamWriter(new FileOutputStream(data))));
		for (int i = 0; i < counter; ++i) {
			out.println(buffer[i]);
		}
		out.close();
		set_counter++;
	}

	/* use for cut a large file into many parts */
	/* <input-file> <output-folder> [block_size-number_of_lines_per_block] */
	public static void main(String[] args) throws Exception {
//		args = new String[] {
//				"D:\\songfeeler\\Detector\\data\\mp3.zing.vn_urls\\mp3.zing.vn_urls",
//				"D:\\songfeeler\\Detector\\data\\mp3.zing.vn_urls\\data_sets"
//		};
		BufferedReader in = new BufferedReader(new InputStreamReader(new FileInputStream(args[0])));
		File folder = new File(args[1]);
		if (args.length > 2) BLOCK_SIZE = Integer.parseInt(args[2]);

		String line;
		String[] buffer = new String[BLOCK_SIZE];
		int counter = 0;
		while (null != (line = in.readLine())) {
			if (line.isEmpty()) continue;
			buffer[counter++] = line;
			if (counter == BLOCK_SIZE) {
				flush(buffer, counter, folder);
				counter = 0;
			}
		}
		if (counter > 0) {
			flush(buffer, counter, folder);
		}
		in.close();
	}
}
