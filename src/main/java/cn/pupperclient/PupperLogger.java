package cn.pupperclient;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class PupperLogger {

	private static final Logger logger = LogManager.getLogger("Pupper Client");

	public static void info(String prefix, String message) {
		logger.info("[Pupper/INFO] [" + prefix + "] " + message);
	}

	public static void warn(String prefix, String message) {
		logger.warn("[Pupper/WARN] [" + prefix + "] " + message);
	}

	public static void error(String prefix, String message) {
		logger.error("[Pupper/ERROR] [" + prefix + "] " + message);
	}

	public static void error(String prefix, String message, Exception e) {
		logger.error("[Pupper/ERROR] [" + prefix + "] " + message, e);
	}

	public static Logger getLogger() {
		return logger;
	}
}
