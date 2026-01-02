package cn.pupperclient;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class PupperLogger {

	private static final Logger logger = LogManager.getLogger("PupperClient Client");

	public static void info(String prefix, String message) {
        logger.info("[PupperClient/INFO] [{}] {}", prefix, message);
	}

	public static void warn(String prefix, String message) {
        logger.warn("[PupperClient/WARN] [{}] {}", prefix, message);
	}

	public static void error(String prefix, String message) {
        logger.error("[PupperClient/ERROR] [{}] {}", prefix, message);
	}

	public static void error(String prefix, String message, Exception e) {
        logger.error("[PupperClient/ERROR] [{}] {}", prefix, message, e);
	}

	public static Logger getLogger() {
		return logger;
	}
}
