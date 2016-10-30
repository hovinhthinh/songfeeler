package illusion.songfeeler.detect;

import illusion.songfeeler.entity.Song;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.zip.GZIPInputStream;

import org.json.JSONObject;

import android.util.Base64;
import android.util.Log;

public class InputStreamProcessor extends Thread {

	private DetectStream detectStream;
	private DataInputStream dataInputStream;

	private boolean flagInputing;
	private boolean successful;

	/**
	 * This object return song info of detection. Null if detection is fail.
	 */
	private Song song;

	public InputStreamProcessor(DetectStream detectStream,
			InputStream inputStream) {
		this.detectStream = detectStream;
		this.dataInputStream = new DataInputStream(inputStream);
		successful = false;
		flagInputing = false;
		song = null;
	}

	public void startInputing() {
		flagInputing = true;
		this.start();
	}

	public void stopInputing() {
		flagInputing = false;
	}

	public boolean isSuccessful() {
		return successful;
	}

	public boolean isInputing() {
		return flagInputing;
	}

	public Song getResult() {
		return song;
	}

	@Override
	public void run() {
		try {
			int dataLength = dataInputStream.readInt();
			Log.d("dataByte", dataLength + "");
			byte[] dataBytes = new byte[dataLength];
			int readCount = 0;
			int c;
			while (readCount < dataLength) {
				c = dataInputStream.read(dataBytes, readCount, dataLength
						- readCount);
				if (c == -1) {
					throw new Exception("data bytes not enough");
				}
				readCount += c;
			}
			Log.d("dataByte", readCount + "");
			StringBuilder sb = new StringBuilder();
			if (readCount > 0) {
				char[] buffer = new char[1024 * 8];
				InputStreamReader in = new InputStreamReader(
						new GZIPInputStream(new ByteArrayInputStream(dataBytes)), "UTF-8");
				while ((c = in.read(buffer)) > 0) {
					sb.append(buffer, 0, c);
				}
				in.close();
			}

			JSONObject json = new JSONObject(sb.toString());

			String title = json.getString("Title");
			String url = json.getString("Url");
			String author = json.getString("Author");
			String artist = json.getString("Artist");
			String lyrics = json.getString("Lyrics");
			byte[] image = Base64.decode(json.getString("Image"),
					Base64.DEFAULT);

			this.song = new Song(title, url, author, artist, lyrics, image);
			successful = true;
		} catch (Exception e) {
			for (Object o : e.getStackTrace()) {
				Log.e(getClass().getSimpleName(), o.toString());
			}
		} finally {
			flagInputing = false;
			detectStream.setDetected(true);
		}
	}
}
