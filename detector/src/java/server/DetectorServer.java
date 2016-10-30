package server;

import detector.Detector;
import org.apache.log4j.Logger;
import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.server.handler.ContextHandler;
import org.eclipse.jetty.server.handler.HandlerList;
import org.eclipse.jetty.util.thread.QueuedThreadPool;
import util.EngineConfiguration;
import util.song.Song;
import util.song.SongFingerprint;
import util.song.model.FrequencySpectrumSerializableObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * Created by thinhhv on 17/08/2014.
 */
public class DetectorServer {
	private static final Logger LOG = Logger.getLogger(DetectorServer.class);
	private static final String THREAD_POOL_MIN = "detector-server.thread-pool-min";
	private static final String THREAD_POOL_MAX = "detector-server.thread-pool-max";
	private static final String HANDLER_PORT = "detector-server.handler-port";
	private static final String DETECT_PORT = "detector-server.detect-port";
	private static final String DETECT_LIMIT = "detector-server.detect-connection-limit";

	private static final String ADD_SONG_CONTEXT_PATH = "handler.add-song.context-path";
	private static final String REMOVE_SONG_CONTEXT_PATH = "handler.remove-song.context-path";
	private static final String LIST_SONG_CONTEXT_PATH = "handler.list-song.context-path";
	private static final String DETECT_SONG_CONTEXT_PATH = "handler.detect-song.context-path";
	private static final String ANALYZE_SONG_BY_URL_CONTEXT_PATH = "handler.analyze-song-by-url.context-path";
	private static final String ANALYZE_SONG_BY_UPLOADING_CONTEXT_PATH = "handler.analyze-song-by-uploading.context-path";
	private static final String CONTAIN_SONG_CONTEXT_PATH = "handler.contain-song.context-path";
	private static final String PING_CONTEXT_PATH = "handler.ping.context-path";

	private Server server;
	private Detector detector;
	private ServerSocket serverSocket;

	static {
		com.sun.org.apache.xml.internal.security.Init.init();
	}

	public DetectorServer() throws Exception {
		detector = new Detector();

		QueuedThreadPool threadPool = new QueuedThreadPool();
		threadPool.setMaxThreads(Integer.parseInt(EngineConfiguration.getInstance().get(THREAD_POOL_MAX)));
		threadPool.setMinThreads(Integer.parseInt(EngineConfiguration.getInstance().get(THREAD_POOL_MIN)));
		server = new Server(threadPool);
		server.setAttribute("org.eclipse.jetty.server.Request.maxFormContentSize", "-1");

		ServerConnector connector = new ServerConnector(server);
		connector.setPort(Integer.parseInt(EngineConfiguration.getInstance().get(HANDLER_PORT)));
		server.addConnector(connector);

		HandlerList handlerList = new HandlerList();
		handlerList.setHandlers(getHandlers());
		server.setHandler(handlerList);
	}

	public static void main(String[] args) throws Exception {
		DetectorServer server = new DetectorServer();
		server.start();
		String line;
		int counter = 0;
		int bound = 0;
		if (args.length > 0) bound = Integer.parseInt(args[0]);
		if (bound > 0) {
			BufferedReader in = new BufferedReader(new InputStreamReader(new FileInputStream("data/Mp3ZingVn.out.filtered_2014-08"), "UTF-8"));
			while (null != (line = in.readLine())) {
				if (line.isEmpty()) continue;
				String[] arr = line.split("\t");
				String url = arr[0];
				String name = arr[3];
				String id = url.substring(url.lastIndexOf("/") + 1, url.lastIndexOf("."));
				File sobj = new File("data/sobj/" + id + ".mp3.sobj");
				if (!sobj.exists()) continue;
				FrequencySpectrumSerializableObject obj = FrequencySpectrumSerializableObject.fromFile(sobj);
				SongFingerprint fingerprint = SongFingerprint.createFingerprintFromFrequencySpectrumSerializableObject(
						obj
				);
				Song song = new Song();
				counter++;
				if (counter > bound) break;
				song.setSongId(counter);
				song.setSongTitle(name);
				song.setFingerprint(fingerprint);
				server.detector.addSong(song);
			}
			in.close();
		}
	}

	private Handler[] getHandlers() {
		ContextHandler addHandler = new ContextHandler();
		addHandler.setContextPath(EngineConfiguration.getInstance().get(ADD_SONG_CONTEXT_PATH));
		addHandler.setHandler(new AddSongHandler(detector));

		ContextHandler removeHandler = new ContextHandler();
		removeHandler.setContextPath(EngineConfiguration.getInstance().get(REMOVE_SONG_CONTEXT_PATH));
		removeHandler.setHandler(new RemoveSongHandler(detector));

		ContextHandler listHandler = new ContextHandler();
		listHandler.setContextPath(EngineConfiguration.getInstance().get(LIST_SONG_CONTEXT_PATH));
		listHandler.setHandler(new ListSongHandler(detector));

		ContextHandler detectHandler = new ContextHandler();
		detectHandler.setContextPath(EngineConfiguration.getInstance().get(DETECT_SONG_CONTEXT_PATH));
		detectHandler.setHandler(new DetectSongHandler(detector));

		ContextHandler analyzeByUrlHandler = new ContextHandler();
		analyzeByUrlHandler.setContextPath(EngineConfiguration.getInstance().get(ANALYZE_SONG_BY_URL_CONTEXT_PATH));
		analyzeByUrlHandler.setHandler(new AnalyzeSongByUrlHandler(detector));

		ContextHandler analyzeByUploadingHandler = new ContextHandler();
		analyzeByUploadingHandler.setContextPath(EngineConfiguration.getInstance().get(ANALYZE_SONG_BY_UPLOADING_CONTEXT_PATH));
		analyzeByUploadingHandler.setHandler(new AnalyzeSongByUploadingHandler(detector));

		ContextHandler containSongHandler = new ContextHandler();
		containSongHandler.setContextPath(EngineConfiguration.getInstance().get(CONTAIN_SONG_CONTEXT_PATH));
		containSongHandler.setHandler(new ContainSongHandler(detector));

		ContextHandler pingHandler = new ContextHandler();
		pingHandler.setContextPath(EngineConfiguration.getInstance().get(PING_CONTEXT_PATH));
		pingHandler.setHandler(new PingHandler());

		return new Handler[]{
				addHandler, removeHandler, listHandler, detectHandler, analyzeByUrlHandler, analyzeByUploadingHandler, containSongHandler, pingHandler
		};
	}

	public void start() throws Exception {
		server.start();
		serverSocket = new ServerSocket(
				Integer.parseInt(EngineConfiguration.getInstance().get(DETECT_PORT)),
				Integer.parseInt(EngineConfiguration.getInstance().get(DETECT_LIMIT))
		);
		new Thread(new Runnable() {
			@Override
			public void run() {
				while (true) {
					Socket sc = null;
					try {
						sc = serverSocket.accept();
						sc.setSoLinger(true, 10000);
						sc.setReceiveBufferSize(4096);
						final Socket sc1 = sc;
						final Detector detector1 = detector;
						new Thread(
								new Runnable() {
									@Override
									public void run() {
										detector1.detect(sc1);
									}
								}
						).start();
					} catch (Exception e) {
					}
				}
			}
		}).start();

		LOG.info("detector server started. detectPort = " +
						EngineConfiguration.getInstance().get(DETECT_PORT) + " ; handlerPort = " +
						EngineConfiguration.getInstance().get(HANDLER_PORT)
		);
	}
}
