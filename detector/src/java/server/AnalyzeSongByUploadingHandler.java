package server;

import com.sun.org.apache.xml.internal.security.utils.Base64;
import detector.Detector;
import org.apache.log4j.Logger;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.handler.AbstractHandler;
import org.json.JSONObject;
import util.miscellaneous.StringUtils;
import util.song.model.FrequencySpectrum;
import util.song.model.FrequencySpectrumDescriptor;
import util.song.model.FrequencySpectrumSerializableObject;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;

/**
 * Created by thinhhv on 03/09/2014.
 */
public class AnalyzeSongByUploadingHandler extends AbstractHandler {
	private static final Logger LOG = Logger.getLogger(AnalyzeSongByUploadingHandler.class);
	private static final FrequencySpectrumDescriptor songDescriptor = FrequencySpectrumDescriptor.getDefaultFrequencySpectrumDescriptorForSong();
	private static final String NO_RESPONSE = new JSONObject() {{
		try {
			put("Verdict", "No");
			put("Fingerprint", "");
			put("FingerprintHash", "");
		} catch (Exception e) {
		}
	}}.toString();
	private Detector detector;

	protected AnalyzeSongByUploadingHandler(Detector detector) {
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
		LOG.info("analyzing song by uploading");

		request.setHandled(true);

		httpServletResponse.addHeader("Access-Control-Allow-Origin", "*");
		httpServletResponse.addHeader("Access-Control-Allow-Methods", "POST");
		httpServletResponse.addHeader("Access-Control-Max-Age", "30000");

		try {
			StringBuilder sb = new StringBuilder();
			BufferedReader reader = new BufferedReader(new InputStreamReader(request.getInputStream()));
			char[] buffer = new char[1024 * 8];
			int count = 0;
			while ((count = reader.read(buffer)) >= 0) {
				sb.append(buffer, 0, count);
			}
			JSONObject requestJson = new JSONObject(sb.toString());

			JSONObject responseJson = new JSONObject();
			responseJson.put("Verdict", "Yes");
			String contentType = requestJson.getString("Type");
			byte[] content = Base64.decode(requestJson.getString("Content"));

			FrequencySpectrumSerializableObject sobj = null;
			if (contentType.equals("mp3")) {
				sobj = FrequencySpectrum.createFrequencySpectrumFromMp3DataBytes(
						songDescriptor, content
				).toSerializableObject();
			} else if (contentType.equals("wav")) {
				sobj = FrequencySpectrum.createFrequencySpectrumFromWavDataBytes(
						songDescriptor, content
				).toSerializableObject();
			} else {
				throw new Exception("invalid content type");
			}

			String fingerprintBase64String = Base64.encode(sobj.toDataBytes());
			responseJson.put("Fingerprint", fingerprintBase64String);
			responseJson.put("FingerprintHash", StringUtils.getStringHashCode(fingerprintBase64String));
			PrintWriter out = new PrintWriter(new BufferedWriter(new OutputStreamWriter(httpServletResponse.getOutputStream(), "UTF-8")));
			out.print(getJSONPResponseString(null, responseJson));
			out.close();
		} catch (Exception e) {
			httpServletResponse.getWriter().print(getJSONPResponseString(null, NO_RESPONSE));
		}

		httpServletResponse.setStatus(HttpServletResponse.SC_OK);
	}
}
