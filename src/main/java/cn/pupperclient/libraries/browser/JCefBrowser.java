package cn.pupperclient.libraries.browser;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import cn.pupperclient.PupperLogger;
import cn.pupperclient.utils.Multithreading;

import net.ccbluex.liquidbounce.mcef.MCEF;
import net.ccbluex.liquidbounce.mcef.MCEFDownloadManager;
import net.ccbluex.liquidbounce.mcef.MCEFPlatform;
import net.ccbluex.liquidbounce.mcef.cef.MCEFBrowser;

public class JCefBrowser {

	private static MCEFBrowser browser;
	private static boolean unsupport;
	
	public static void init() {
		
		MCEF.INSTANCE.initialize();
		
		if(browser == null) {
            String url = "https://cn.bing.com";
            boolean transparent = true;
			browser = MCEF.INSTANCE.createBrowser(url, transparent, 60);
			browser.resize(1280, 720);
		}
	}
	
	public static void close() {
		
		if(browser != null) {
			browser.close();
			browser = null;
		}
		
		if(MCEF.INSTANCE.isInitialized()) {
			MCEF.INSTANCE.shutdown();
		}
		
		if (MCEFPlatform.getPlatform().isWindows()) {
			String processName = "jcef_helper.exe";
			try {
				ProcessBuilder processBuilder = new ProcessBuilder("tasklist");
				Process process = processBuilder.start();

				BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
				String line;
				boolean isRunning = false;
				while ((line = reader.readLine()) != null) {
					if (line.contains(processName)) {
						isRunning = true;
						break;
					}
				}
				reader.close();

				if (isRunning) {
					MCEF.INSTANCE.getLogger().warn("JCEF is still running, killing to avoid lingering processes.");
					ProcessBuilder killProcess = new ProcessBuilder("taskkill", "/F", "/IM", processName);
					killProcess.start();
				}
			} catch (Exception e) {
				MCEF.INSTANCE.getLogger()
						.error("Unable to check if JCEF is still running. There may be lingering processes.", e);
			}
		}
	}
	
	public static void download() {
		try {
			MCEFDownloadManager resourceManager = MCEF.INSTANCE.newResourceManager();
			
			if(!resourceManager.isSystemCompatible()) {
				unsupport = true;
				PupperLogger.error("JCEF", "The computer does not support the browser");
				return;
			}
			
			if(resourceManager.requiresDownload()) {
				Multithreading.runAsync(() -> {
					try {
						resourceManager.downloadJcef();
					} catch (IOException e) {
						e.printStackTrace();
					}
				});
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static MCEFBrowser getBrowser() {
		return browser;
	}

	public static boolean isUnsupport() {
		return unsupport;
	}
}
