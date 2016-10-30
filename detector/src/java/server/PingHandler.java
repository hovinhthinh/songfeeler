package server;

import org.apache.log4j.Logger;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.handler.AbstractHandler;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Created by thinhhv on 01/10/2014.
 */
public class PingHandler extends AbstractHandler {
	private static final Logger LOG = Logger.getLogger(PingHandler.class);

	@Override
	public void handle(String s, Request request, HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) throws IOException, ServletException {
		LOG.info("pinging");
		request.setHandled(true);
		httpServletResponse.setStatus(HttpServletResponse.SC_OK);
	}
}
