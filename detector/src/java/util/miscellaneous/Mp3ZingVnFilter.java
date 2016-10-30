package util.miscellaneous;

import java.io.*;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by thinhhv on 02/09/2014.
 */
public class Mp3ZingVnFilter {
	private static final String INPUT = "data/Mp3ZingVn.out";
	private static final String OUTPUT = "data/Mp3ZingVn.out.filtered";

	private static final HashMap<String, Mp3ZingVn.ZingSong> map = new HashMap<String, Mp3ZingVn.ZingSong>();

	private static final void put(Mp3ZingVn.ZingSong song) {
		if (!song.isHighQuality()) return;
		if (song.getTotalPlay() < 100000) return;
		String key = (song.getUrl().substring(song.getUrl().lastIndexOf("/") + 1)).split("\\.")[0].toUpperCase();
		if (map.containsKey(key)) {
			Mp3ZingVn.ZingSong msong = map.get(key);
			if (song.getTimestamp().compareTo(msong.getTimestamp()) > 0 || (msong.getUrl().contains("--") && !song.getUrl().contains("--"))) {
				map.put(key, song);
			}
		} else {
			map.put(key, song);
		}
	}

	public static void main(String[] args) throws Exception {
		BufferedReader in = new BufferedReader(new InputStreamReader(new FileInputStream(INPUT), "UTF-8"));
		PrintWriter out = new PrintWriter(new BufferedWriter(new OutputStreamWriter(new FileOutputStream(OUTPUT), "UTF-8")));
		String line;
		while (null != (line = in.readLine())) {
			if (line.isEmpty()) continue;
			Mp3ZingVn.ZingSong song = new Mp3ZingVn.ZingSong();
			String[] fields = line.split("\t");

			for (int i = 0; i < fields.length; ++i) if (fields[i].equals("null")) fields[i] = null;
			try {
				song.setUrl(fields[0]);
				song.setTimestamp(fields[1]);
				song.setHighQuality(Boolean.parseBoolean(fields[2]));
				song.setSongTitle(fields[3]);
				song.setTotalPlay(Integer.parseInt(fields[4]));
				song.setGenre(fields[5]);
				song.setAuthorName(fields[6]);
				song.setArtistName(fields[7]);
				song.setArtistImageUrl(fields[8]);
				if (fields[9].isEmpty()) fields[9] = null;
				song.setLyrics(fields[9].split("\\|"));
			} catch (Exception e) {
			}
			put(song);
		}
		in.close();
		for (Map.Entry<String, Mp3ZingVn.ZingSong> entry : map.entrySet()) {
			Mp3ZingVn.ZingSong song = entry.getValue();
			out.println(song.toString());
		}
		out.close();
	}
}
