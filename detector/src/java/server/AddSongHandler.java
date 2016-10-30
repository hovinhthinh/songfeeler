package server;

import com.sun.org.apache.xml.internal.security.utils.Base64;
import detector.Detector;
import org.apache.log4j.Logger;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.handler.AbstractHandler;
import org.json.JSONObject;
import util.song.Song;
import util.song.SongFingerprint;
import util.song.model.FrequencySpectrum;
import util.song.model.FrequencySpectrumSerializableObject;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.Reader;

/**
 * Created by thinhhv on 22/08/2014.
 */
public class AddSongHandler extends AbstractHandler {
	private static final Logger LOG = Logger.getLogger(AddSongHandler.class);
	private static final String NO_RESPONSE = new JSONObject() {{
		try {
			put("Verdict", "No");
		} catch (Exception e) {
		}
	}}.toString();
	private Detector detector;

	protected AddSongHandler(Detector detector) {
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
		LOG.info("adding song");
		request.setHandled(true);
		try {
			Reader in = request.getReader();
			StringBuilder builder = new StringBuilder();
			char[] buffer = new char[1024 * 8];
			int c;
			while ((c = in.read(buffer)) != -1) {
				builder.append(buffer, 0, c);
			}
			JSONObject inJson = new JSONObject(builder.toString());
			int id = inJson.getInt("Key");
			byte[] dataBytes = Base64.decode(inJson.getString("Fingerprint"));
			FrequencySpectrum spectrum = FrequencySpectrum.createFrequencySpectrumFromSerializableObject(
					FrequencySpectrumSerializableObject.fromDataBytes(dataBytes)
			);

			SongFingerprint fingerprint = SongFingerprint.createFingerprintFromFrequencySpectrum(spectrum);
			Song song = new Song();
			song.setSongId(id);
			song.setFingerprint(fingerprint);

			boolean verdict = detector.addSong(song);
			JSONObject jsonResponse = new JSONObject();
			if (!verdict) {
				jsonResponse.put("Verdict", "No");
			} else {
				jsonResponse.put("Verdict", "Yes");
			}
			httpServletResponse.getWriter().print(getJSONPResponseString(null, jsonResponse));
		} catch (Exception e) {
			e.printStackTrace();
			httpServletResponse.getWriter().print(getJSONPResponseString(null, NO_RESPONSE));
		}
		httpServletResponse.setStatus(HttpServletResponse.SC_OK);
	}
}
