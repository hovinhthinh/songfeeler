package util.song;

/**
 * Created by thinhhv on 23/07/2014.
 */
public class Song {
	private int songId;
	private String artistName;
	private String artistImageUrl;
	private String authorName;
	private String songTitle;
	private String genre;
	private String[] lyrics; /* splited by lines */
	private SongFingerprint fingerprint;

	public String getGenre() {
		return genre;
	}

	public void setGenre(String genre) {
		this.genre = genre;
	}

	public String getAuthorName() {
		return authorName;
	}

	public void setAuthorName(String authorName) {
		this.authorName = authorName;
	}

	public int getSongId() {
		return songId;
	}

	public void setSongId(int songId) {
		this.songId = songId;
	}

	public String getArtistName() {
		return artistName;
	}

	public void setArtistName(String artistName) {
		this.artistName = artistName;
	}

	public String getSongTitle() {
		return songTitle;
	}

	public void setSongTitle(String songTitle) {
		this.songTitle = songTitle;
	}

	public String[] getLyrics() {
		return lyrics;
	}

	public void setLyrics(String[] lyrics) {
		this.lyrics = lyrics;
	}

	public SongFingerprint getFingerprint() {
		return fingerprint;
	}

	public void setFingerprint(SongFingerprint fingerprint) {
		this.fingerprint = fingerprint;
	}

	public String getArtistImageUrl() {
		return artistImageUrl;
	}

	public void setArtistImageUrl(String artistImageUrl) {
		this.artistImageUrl = artistImageUrl;
	}
}
