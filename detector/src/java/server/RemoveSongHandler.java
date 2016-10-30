package server;

import detector.Detector;
import org.apache.log4j.Logger;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.handler.AbstractHandler;
import org.json.JSONObject;
import util.song.Song;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Created by thinhhv on 22/08/2014.
 */
public class RemoveSongHandler extends AbstractHandler {
	private static final Logger LOG = Logger.getLogger(RemoveSongHandler.class);
	private static final String NO_RESPONSE = new JSONObject() {{
		try {
			put("Verdict", "No");
		} catch (Exception e) {
		}
	}}.toString();
	private Detector detector;

	protected RemoveSongHandler(Detector detector) {
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
		LOG.info("removing song");
		request.setHandled(true);
		String callback = request.getParameter("callback");
		try {
			int id = Integer.parseInt(request.getParameter("key"));

			Song song = new Song();
			song.setSongId(id);

			boolean verdict = detector.removeSong(song);
			JSONObject jsonResponse = new JSONObject();
			if (!verdict) {
				jsonResponse.put("Verdict", "No");
			} else {
				jsonResponse.put("Verdict", "Yes");
			}
			httpServletResponse.getWriter().print(getJSONPResponseString(callback, jsonResponse));
		} catch (Exception e) {
			httpServletResponse.getWriter().print(getJSONPResponseString(callback, NO_RESPONSE));
		}
		httpServletResponse.setStatus(HttpServletResponse.SC_OK);
	}
}
