package com.github.microprograms.osgi_framework_launcher;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;

import org.apache.felix.main.AutoProcessor;
import org.apache.felix.main.Main;
import org.osgi.framework.launch.Framework;
import org.osgi.framework.launch.FrameworkFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class OsgiFrameworkLauncher {
	private static final Logger log = LoggerFactory.getLogger(OsgiFrameworkLauncher.class);

	public static void main(String[] args) throws Exception {
		log.info("starting...");
		_startFelixAndWaitForStop();
		log.info("stoped");
		System.exit(0);
	}

	private static Framework _startFelixAndWaitForStop() throws Exception {
		Map<String, String> config = loadConfigProperties();
		FrameworkFactory factory = _getFrameworkFactory();
		Framework framework = factory.newFramework(config);
		framework.init();
		AutoProcessor.process(config, framework.getBundleContext());
		framework.start();
		framework.waitForStop(0);
		return framework;
	}

	private static Map<String, String> loadConfigProperties() {
		Map<String, String> config = Main.loadConfigProperties();
		URL moduleConfigDirUrl = OsgiFrameworkLauncher.class.getResource("/module/");
		if (moduleConfigDirUrl != null) {
			for (File x : new File(moduleConfigDirUrl.getFile()).listFiles()) {
				config.putAll(loadConfigProperties(x));
			}
		}
		return config;
	}

	private static Map<String, String> loadConfigProperties(File configFile) {
		log.info("loadConfigProperties, configFile={}", configFile);
		Map<String, String> config = new HashMap<>();
		Properties properties = new Properties();
		try (Reader r = new FileReader(configFile)) {
			properties.load(r);
			for (Entry<Object, Object> x : properties.entrySet()) {
				config.put(x.getKey().toString(), x.getValue().toString());
			}
		} catch (Exception e) {
			log.error("loadConfigProperties error", e);
		}
		return config;
	}

	private static FrameworkFactory _getFrameworkFactory() throws Exception {
		URL url = OsgiFrameworkLauncher.class.getClassLoader()
				.getResource("META-INF/services/org.osgi.framework.launch.FrameworkFactory");
		if (null == url) {
			throw new Exception("Could not find framework factory.");
		}

		try (InputStreamReader in = new InputStreamReader(url.openStream());
				BufferedReader reader = new BufferedReader(in);) {
			for (String line = reader.readLine(); line != null; line = reader.readLine()) {
				line = line.trim();
				if (line.length() > 0 && line.charAt(0) != '#') {
					return (FrameworkFactory) Class.forName(line).newInstance();
				}
			}
		}

		throw new Exception("Could not find framework factory.");
	}
}
