package server;

import detector.Detector;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.handler.AbstractHandler;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Created by thinhhv on 22/08/2014.
 */
public class DetectSongHandler extends AbstractHandler {
	private Detector detector;

	protected DetectSongHandler(Detector detector) {
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
		/* TODO */
	}
}
