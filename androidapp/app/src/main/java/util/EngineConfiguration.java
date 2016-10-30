package util;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

/**
 * Created by thinhhv on 23/07/2014.
 */
public class EngineConfiguration {
	private static final String ENGINE_CONF_PATH = "/conf/engine-config.xml";
	private static final String SERVER_CONF_PATH = "/conf/server-config.xml";
	private static EngineConfiguration conf = null;
	private Properties prop;

	private EngineConfiguration() {
		prop = new Properties();
		try {
			prop.loadFromXML(getClass().getResourceAsStream(ENGINE_CONF_PATH));
			prop.loadFromXML(getClass().getResourceAsStream(SERVER_CONF_PATH));
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	public static EngineConfiguration getInstance() {
		if (conf == null) conf = new EngineConfiguration();
		return conf;
	}

	public String get(String key) {
		return prop.getProperty(key);
	}
}
