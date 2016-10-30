package server;

import com.sun.org.apache.xml.internal.security.utils.Base64;
import detector.Detector;
import org.apache.log4j.Logger;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.handler.AbstractHandler;
import org.imgscalr.Scalr;
import org.json.JSONObject;
import util.Crawler;
import util.miscellaneous.Mp3ZingVn;
import util.miscellaneous.StringUtils;
import util.song.model.FrequencySpectrum;
import util.song.model.FrequencySpectrumDescriptor;
import util.song.model.FrequencySpectrumSerializableObject;

import javax.imageio.ImageIO;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.awt.image.BufferedImage;
import java.io.*;

/**
 * Created by thinhhv on 22/08/2014.
 */
public class AnalyzeSongByUrlHandler extends AbstractHandler {
	private static final Logger LOG = Logger.getLogger(AnalyzeSongByUrlHandler.class);
	private static final FrequencySpectrumDescriptor songDescriptor = FrequencySpectrumDescriptor.getDefaultFrequencySpectrumDescriptorForSong();
	private static final String NO_RESPONSE = new JSONObject() {{
		try {
			JSONObject songDetailJson = new JSONObject();
			try {
				songDetailJson.put("Url", "");
				songDetailJson.put("Title", "");
				songDetailJson.put("Author", "");
				songDetailJson.put("Artist", "");
				songDetailJson.put("Image", "");
				songDetailJson.put("Lyrics", "");
				songDetailJson.put("Fingerprint", "");
				songDetailJson.put("FingerprintHash", "");
			} catch (Exception e1) {
			}
			put("Verdict", "No");
			put("SongDetail", songDetailJson);
		} catch (Exception e2) {
		}
	}}.toString();

	private Detector detector;

	protected AnalyzeSongByUrlHandler(Detector detector) {
		this.detector = detector;
	}

	private String getJSONPResponseString(String callback, Object response) {
		if (callback == null) {
			return response.toString();
		} else {
			return callback + "(" + response.toString() + ");";
		}
	}

	@Override
	public void handle(String s, Request request, HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) throws IOException, ServletException {
		request.setHandled(true);
		String url = request.getParameter("url");
		LOG.info("analyzing song by url handler: " + url);
		String callback = request.getParameter("callback");
		if (!Mp3ZingVn.isAcceptedUrl(url)) {
			httpServletResponse.getWriter().print(getJSONPResponseString(callback, NO_RESPONSE));
		} else {
			Mp3ZingVn.ZingSong zingSong = Mp3ZingVn.ZingSong.from(url);
			if (zingSong.getSongTitle() == null || zingSong.getAuthorName() == null || zingSong.getArtistName() == null) {
				httpServletResponse.getWriter().print(getJSONPResponseString(callback, NO_RESPONSE));
			} else {
				try {
					JSONObject responseJson = new JSONObject();
					responseJson.put("Verdict", "Yes");
					JSONObject songDetailJson = new JSONObject();
					songDetailJson.put("Url", url);
					songDetailJson.put("Title", zingSong.getSongTitle());
					songDetailJson.put("Author", zingSong.getAuthorName());
					songDetailJson.put("Artist", zingSong.getArtistName());
					String imageUrl = zingSong.getArtistImageUrl();
					String imageString = "";
					String miniImageString = "";
					if (imageUrl != null) {
						byte[] bytes = Crawler.getContentBytesFromUrl(imageUrl);
						if (bytes != null) {
							BufferedImage img = ImageIO.read(new ByteArrayInputStream(bytes));
							img = Scalr.resize(img, 400);
							ByteArrayOutputStream imgBytes = new ByteArrayOutputStream();
							ImageIO.write(img, "jpg", imgBytes);
							imageString = Base64.encode(imgBytes.toByteArray());

							BufferedImage miniImg = ImageIO.read(new ByteArrayInputStream(bytes));
							miniImg = Scalr.resize(miniImg, 40);
							ByteArrayOutputStream miniImgBytes = new ByteArrayOutputStream();
							ImageIO.write(miniImg, "jpg", miniImgBytes);
							miniImageString = Base64.encode(miniImgBytes.toByteArray());
						}
					}
					songDetailJson.put("Image", imageString);
					songDetailJson.put("MiniImage", miniImageString);
					songDetailJson.put("Lyrics", zingSong.getLyricsString() == null ? "" : zingSong.getLyricsString());

					try {
						byte[] mp3DataBytes = Mp3ZingVn.getSongMp3DataBytes(url);

						FrequencySpectrumSerializableObject sobj = FrequencySpectrum.createFrequencySpectrumFromMp3DataBytes(
								songDescriptor, mp3DataBytes
						).toSerializableObject();

						String fingerprintBase64String = Base64.encode(sobj.toDataBytes());
						songDetailJson.put("Fingerprint", fingerprintBase64String);
						songDetailJson.put("FingerprintHash", StringUtils.getStringHashCode(fingerprintBase64String));
					} catch (Exception e) {
						songDetailJson.put("Fingerprint", "");
						songDetailJson.put("FingerprintHash", "");
					}
					responseJson.put("SongDetail", songDetailJson);
					PrintWriter out = new PrintWriter(new BufferedWriter(new OutputStreamWriter(httpServletResponse.getOutputStream(), "UTF-8")));
					out.print(getJSONPResponseString(callback, responseJson));
					out.close();
				} catch (Exception e) {
					httpServletResponse.getWriter().print(getJSONPResponseString(callback, NO_RESPONSE));
				}
			}
		}
		httpServletResponse.setStatus(HttpServletResponse.SC_OK);
	}
}
