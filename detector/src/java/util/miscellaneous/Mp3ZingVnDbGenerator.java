package util.miscellaneous;

import com.sun.org.apache.xml.internal.security.utils.Base64;
import org.imgscalr.Scalr;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;

/**
 * Created by thinhhv on 13/11/2014.
 */
public class Mp3ZingVnDbGenerator {
	private static final String CONNECTION_STRING = "jdbc:sqlserver://localhost:1433;databaseName=Songfeeler;IntegratedSecurity=True";
	private static final String DRIVER = "com.microsoft.sqlserver.jdbc.SQLServerDriver";

	private static final Connection getConnection() {
		try {
			Class.forName(DRIVER);
			return DriverManager.getConnection(CONNECTION_STRING);
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	public static byte[] getContentBytesFromFile(File f) {
		try {
			ByteArrayOutputStream out = new ByteArrayOutputStream();
			InputStream is = new FileInputStream(f);
			byte[] buffer = new byte[1024 * 8];
			int c;
			while ((c = is.read(buffer)) >= 0) {
				out.write(buffer, 0, c);
			}
			return out.toByteArray();
		} catch (Exception e) {
			return null;
		}
	}

	/* <input> <sobj_root> <img_root> <top> */
	public static void main(String[] args) throws Exception {
		args = new String[]{
				"data/Mp3ZingVn.out.filtered_2014-08",
				"data/sobj",
				"data/img",
				"100000"

		};
		File sobjRoot = new File(args[1]);
		File imgRoot = new File(args[2]);
		BufferedReader in = new BufferedReader(new InputStreamReader(new FileInputStream(args[0]), "UTF-8"));

		int top = Integer.parseInt(args[3]);
		String line;
		int count = 0;
		Connection conn = getConnection();
		int countLine = 0;
		while (null != (line = in.readLine())) {
			if (line.isEmpty()) continue;
			++countLine;
			if (countLine < 7520) continue;
			String arr[] = line.split("\t");
			DbSong song = new DbSong();

			song.url = arr[0];
			song.title = arr[3];
			song.author = arr[6];
			song.artist = arr[7];

			String dataId = song.url.substring(song.url.lastIndexOf("/") + 1, song.url.lastIndexOf("."));

			File sobjFile = new File(sobjRoot, dataId + ".mp3.sobj");
			if (!sobjFile.exists()) continue;
			song.fingerprint = getContentBytesFromFile(sobjFile);
			song.fingerprintHash = StringUtils.getStringHashCode(Base64.encode(song.fingerprint));
			File imgFile = new File(imgRoot, dataId + ".img");
			if (imgFile.exists()) {
				byte[] bytes = getContentBytesFromFile(imgFile);
				try {
					BufferedImage img = ImageIO.read(new ByteArrayInputStream(bytes));
					img = Scalr.resize(img, 400);
					ByteArrayOutputStream imgBytes = new ByteArrayOutputStream();
					ImageIO.write(img, "jpg", imgBytes);
					song.image = imgBytes.toByteArray();

					BufferedImage miniImg = ImageIO.read(new ByteArrayInputStream(bytes));
					miniImg = Scalr.resize(miniImg, 40);
					ByteArrayOutputStream miniImgBytes = new ByteArrayOutputStream();
					ImageIO.write(miniImg, "jpg", miniImgBytes);
					song.miniImage = miniImgBytes.toByteArray();
				} catch (Exception e) {
					song.image = null;
					song.miniImage = null;
					System.out.println("Unsupported Image Type: " + song.url);
				}
			}

			if (arr.length >= 10 && !arr[9].equals("null")) {
				song.lyrics = arr[9];
			}
			PreparedStatement cmd = conn
					.prepareStatement("INSERT INTO Songs VALUES (?,?,?,?,?,?,?,?,?,0)");
			cmd.setString(1, song.title);
			cmd.setString(2, song.url);
			cmd.setString(3, song.author);
			cmd.setString(4, song.artist);
			cmd.setBytes(5, song.image);
			cmd.setBytes(6, song.miniImage);
			cmd.setString(7, song.lyrics);
			cmd.setBytes(8, song.fingerprint);
			cmd.setLong(9, song.fingerprintHash);
			try {
				int r = cmd.executeUpdate();
				if (r > 0) ++count;
				if (count % 10 == 0) {
					System.out.println("processed: " + count);
				}
			} catch (Exception e) {
				System.out.println("Duplicated: " + song.url);
			}

			if (count == top) break;
		}
		in.close();
		conn.close();
	}

	private static class DbSong {
		public String url;
		public String title;
		public String author;
		public String artist;
		public byte[] image; // can Null
		public byte[] miniImage; // can Null
		public String lyrics; // can Null
		public byte[] fingerprint;
		public long fingerprintHash;
	}
}
