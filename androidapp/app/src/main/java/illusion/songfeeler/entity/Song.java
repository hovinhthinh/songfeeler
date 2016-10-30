package illusion.songfeeler.entity;

public class Song {
	private int id;
	private String title;
	private String url;
	private String author;
	private String artist;
	private String lyrics; /* Splited by | */
	private byte[] image;

	public Song(String title, String url, String author, String artist,
			String lyrics, byte[] image) {
		super();
		this.title = title;
		this.url = url;
		this.author = author;
		this.artist = artist;
		this.lyrics = lyrics;
		this.image = image;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public String getAuthor() {
		return author;
	}

	public void setAuthor(String author) {
		this.author = author;
	}

	public String getArtist() {
		return artist;
	}

	public void setArtist(String artist) {
		this.artist = artist;
	}

	public String getLyrics() {
		return lyrics;
	}

	public void setLyrics(String lyrics) {
		this.lyrics = lyrics;
	}

	public byte[] getImage() {
		return image;
	}

	public void setImage(byte[] image) {
		this.image = image;
	}
}
