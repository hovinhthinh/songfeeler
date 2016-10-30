package util;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.PropertyConfigurator;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

/**
 * Created by thinhhv on 23/07/2014.
 */
public class EngineConfiguration {
	private static final String LOG4J_CONF_PATH = "/conf/log4j.properties";
	private static final String ENGINE_CONF_PATH = "/conf/engine-config.xml";
	private static final String SERVER_CONF_PATH = "conf/server-config.xml";
	private static EngineConfiguration conf = null;
	private Properties prop;

	static {
		PropertyConfigurator.configure(EngineConfiguration.class.getResourceAsStream(LOG4J_CONF_PATH));
	}

	private EngineConfiguration() {
		prop = new Properties();
		try {
			prop.loadFromXML(getClass().getResourceAsStream(ENGINE_CONF_PATH));
			prop.loadFromXML(new FileInputStream(SERVER_CONF_PATH));
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
