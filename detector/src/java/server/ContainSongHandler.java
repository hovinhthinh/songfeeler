package server;

import detector.Detector;
import org.apache.log4j.Logger;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.handler.AbstractHandler;
import org.json.JSONObject;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Created by thinhhv on 15/09/2014.
 */
public class ContainSongHandler extends AbstractHandler {
	private static final Logger LOG = Logger.getLogger(ListSongHandler.class);
	private static final String NO_RESPONSE = new JSONObject() {{
		try {
			put("Verdict", "No");
			put("Result", "No");
		} catch (Exception e) {
		}
	}}.toString();

	private Detector detector;

	protected ContainSongHandler(Detector detector) {
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
		LOG.info("containing songs");
		request.setHandled(true);
		int key = Integer.parseInt(request.getParameter("key"));
		String callback = request.getParameter("callback");
		try {
			JSONObject jsonResponse = new JSONObject();
			jsonResponse.put("Verdict", "Yes");
			boolean contain = detector.containsSong(key);
			jsonResponse.put("Result", contain ? "Yes" : "No");

			httpServletResponse.getWriter().print(getJSONPResponseString(callback, jsonResponse));
		} catch (Exception e) {
			httpServletResponse.getWriter().print(getJSONPResponseString(callback, NO_RESPONSE));
		}
		httpServletResponse.setStatus(HttpServletResponse.SC_OK);
	}
}
