package util.miscellaneous;

import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.log4j.Logger;
import util.Crawler;
import util.TParser;

import java.util.regex.Pattern;

/**
 * Created by thinhhv on 23/08/2014.
 */
public class Mp3ZingVn {
	private static final Logger LOG = Logger.getLogger(Mp3ZingVn.class);
	private static final Pattern ACCEPT_URL_PATTERN = Pattern.compile(
			"http://mp3\\.zing\\.vn/bai-hat/[\\w\\-]++/\\w++\\.html"
	);

	public static boolean isAcceptedUrl(String url) {
		return ACCEPT_URL_PATTERN.matcher(url).matches();
	}

	public static int getSongTotalPlay(String url) {
		try {
			if (!isAcceptedUrl(url)) throw new Exception("invalid mp3.zing.vn song url format");
			String content = Crawler.getContentFromUrl(url);
			String dataId = TParser.getContent(content, "data-id=\"", "\"");
			content = Crawler.getContentFromUrl("http://mp3.zing.vn/ajax/info/total-play?id=" + dataId + "&type=song");
			return Integer.parseInt(TParser.getContent(content, ":", "}"));
		} catch (Exception ex) {
			LOG.error(ex.getMessage());
			return -1;
		}
	}

	public static byte[] getSongMp3DataBytes(String url) {
		try {
			if (!isAcceptedUrl(url)) throw new Exception("invalid mp3.zing.vn song url format");
			String content = Crawler.getContentFromUrl(url);
			String dataId = TParser.getContent(content, "http://mp3.zing.vn/html5/song/", "\"");
			String mp3Link = "http://mp3.zing.vn/html5/song/" + dataId;
			return Crawler.getContentBytesFromUrl(mp3Link);
		} catch (Exception ex) {
			LOG.error(ex.getMessage());
			return null;
		}
	}

	public static boolean isHighQualitySong(String url) {
		try {
			if (!isAcceptedUrl(url)) throw new Exception("invalid mp3.zing.vn song url format");
			String content = Crawler.getContentFromUrl(url);
			String dataId = TParser.getContent(content, "http://mp3.zing.vn/html5/song/", "\"");
			content = Crawler.getContentFromUrl("http://mp3.zing.vn/xml/song-xml/" + dataId);
			return content.contains("<![CDATA[require vip]]>");
		} catch (Exception ex) {
			LOG.error(ex.getMessage());
			return false;
		}
	}

	public static void main(String[] args) throws Exception {
//		System.out.println(getSongTotalPlay("http://mp3.zing.vn/bai-hat/Bon-Chu-Lam-Truc-Nhan-Truong-Thao-Nhi/ZW6BFZUA.html"));
//		System.out.println(getSongMp3DataBytes("http://mp3.zing.vn/bai-hat/Mo-Ho-Bui-Anh-Tuan/ZW6CFIWI.html").length);
//		ZingSong song = ZingSong.from("http://mp3.zing.vn/bai-hat/Mo-Ho-Bui-Anh-Tuan/ZW6CFIWI.html");
//		System.out.println(song.toString());
	}

	public static class ZingSong {
		private String url;
		private String timestamp;
		private String authorName;
		private String artistName;
		private String artistImageUrl;
		private String songTitle;
		private int totalPlay;
		private String genre;
		private boolean highQuality;
		private String[] lyrics; /* splited by lines */

		public static ZingSong from(String url) {
			ZingSong song = new ZingSong();
			song.setTotalPlay(-1);
			song.setUrl(url);
			try {
				if (!isAcceptedUrl(url))
					throw new Exception("invalid mp3.zing.vn song url format");
				String content = Crawler.getContentFromUrl(url);


				String str = TParser.getContent(content, "<h1(\\s)+?class=\"detail-title\">", "</h1>");
				if (str != null) song.setSongTitle(TParser.simpleRemoveTag(str));

				str = TParser.getContent(content, "<span>-</span><h2><a[^>]*?>", "</a>");
				if (str != null) song.setArtistName(TParser.simpleRemoveTag(str));

				str = TParser.getContent(content, "property=\"og:image\"(\\s)+?content=\"", "\"(\\s)+?/>");
				if (str != null) {
					if (str.equals("http://image.mp3.zdn.vn/")) str = null;
					song.setArtistImageUrl(str);
				}

				str = TParser.getContent(content, "<p(\\s)+?class=\"song-info\">Sáng(\\s)+?tác:", "\\|");
				if (str != null) song.setAuthorName(TParser.simpleRemoveTag(str));

				str = TParser.getContent(content, "<span(\\s)+?class=\"lyric-author\">[^>]*?></span></span>", "</p>");
				if (str != null) {
					String[] lyrics = str.split("<br\\s+?/>");
					for (int i = 0; i < lyrics.length; ++i) lyrics[i] = lyrics[i].replaceAll("\\s++", " ").trim();
					song.setLyrics(lyrics);
				}

				str = TParser.getContent(content, "Thể(\\s)+?loại:", "\\|");
				if (str != null) song.setGenre(TParser.simpleRemoveTag(str));

				try {
					String dataId = TParser.getContent(content, "data-id=\"", "\"");
					String contentTotalPlay = Crawler.getContentFromUrl("http://mp3.zing.vn/ajax/info/total-play?id=" + dataId + "&type=song");
					song.setTotalPlay(Integer.parseInt(TParser.getContent(contentTotalPlay, ":", "}")));
				} catch (Exception e) {
				}
				try {
					String dataId = TParser.getContent(content, "http://mp3.zing.vn/html5/song/", "\"");
					String contentQuality = Crawler.getContentFromUrl("http://mp3.zing.vn/xml/song-xml/" + dataId);
					song.setHighQuality(contentQuality.contains("<![CDATA[require vip]]>"));
				} catch (Exception e) {
				}
			} catch (Exception ex) {
			}

			return song;
		}

		public static ZingSong from(String url, String timestamp) {
			ZingSong song = from(url);
			song.setTimestamp(timestamp);
			return song;
		}

		public boolean isHighQuality() {
			return highQuality;
		}

		public void setHighQuality(boolean highQuality) {
			this.highQuality = highQuality;
		}

		public String getUrl() {
			return url;
		}

		public void setUrl(String url) {
			this.url = url;
		}

		public String getTimestamp() {
			return timestamp;
		}

		public void setTimestamp(String timestamp) {
			this.timestamp = timestamp;
		}

		public String getArtistName() {
			return artistName;
		}

		public void setArtistName(String artistName) {
			this.artistName = artistName;
		}

		public String getArtistImageUrl() {
			return artistImageUrl;
		}

		public void setArtistImageUrl(String artistImageUrl) {
			this.artistImageUrl = artistImageUrl;
		}

		public String getAuthorName() {
			return authorName;
		}

		public void setAuthorName(String authorName) {
			this.authorName = authorName;
		}

		public String getSongTitle() {
			return songTitle;
		}

		public void setSongTitle(String songTitle) {
			this.songTitle = songTitle;
		}

		public int getTotalPlay() {
			return totalPlay;
		}

		public void setTotalPlay(int totalPlay) {
			this.totalPlay = totalPlay;
		}

		public String getGenre() {
			return genre;
		}

		public void setGenre(String genre) {
			this.genre = genre;
		}

		public String[] getLyrics() {
			return lyrics;
		}

		public void setLyrics(String[] lyrics) {
			this.lyrics = lyrics;
		}

		public String getLyricsString() {
			if (lyrics == null) return null;
			StringBuilder sb = new StringBuilder();
			for (int i = 0; i < lyrics.length; ++i) {
				sb.append(lyrics[i]);
				if (i < lyrics.length - 1) sb.append("|");
			}
			return sb.toString();
		}

		@Override
		public String toString() {
			return StringEscapeUtils.unescapeHtml4(url + "\t" +
					timestamp + "\t" +
					highQuality + "\t" +
					songTitle + "\t" +
					totalPlay + "\t" +
					genre + "\t" +
					authorName + "\t" +
					artistName + "\t" +
					artistImageUrl + "\t" +
					getLyricsString());
		}
	}
}
