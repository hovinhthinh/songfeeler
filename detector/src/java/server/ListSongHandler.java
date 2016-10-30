package server;

import detector.Detector;
import gnu.trove.iterator.TIntIterator;
import gnu.trove.list.TIntList;
import org.apache.log4j.Logger;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.handler.AbstractHandler;
import org.json.JSONArray;
import org.json.JSONObject;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Created by thinhhv on 22/08/2014.
 */
public class ListSongHandler extends AbstractHandler {
	private static final Logger LOG = Logger.getLogger(ListSongHandler.class);
	private static final String NO_RESPONSE = new JSONObject() {{
		try {
			put("Verdict", "No");
			put("List", new JSONArray());
		} catch (Exception e) {
		}
	}}.toString();

	private Detector detector;

	protected ListSongHandler(Detector detector) {
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
		LOG.info("listing songs");
		request.setHandled(true);
		String callback = request.getParameter("callback");
		try {
			TIntList list = detector.listSong();
			JSONObject jsonResponse = new JSONObject();
			jsonResponse.put("Verdict", "Yes");
			jsonResponse.put("List", new JSONArray());

			for (TIntIterator i = list.iterator(); i.hasNext(); ) {
				jsonResponse.append("List", i.next());
			}
			httpServletResponse.getWriter().print(getJSONPResponseString(callback, jsonResponse));
		} catch (Exception e) {
			httpServletResponse.getWriter().print(getJSONPResponseString(callback, NO_RESPONSE));
		}
		httpServletResponse.setStatus(HttpServletResponse.SC_OK);
	}
}
