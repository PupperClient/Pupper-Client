package cn.pupperclient.utils;

public class OS {

	private static final String OS_NAME = System.getProperty("os.name").toLowerCase();
	
	public static boolean isWindows() {
		return OS_NAME.contains("win");
	}
	
	public static boolean isMacOS() {
		return OS_NAME.contains("mac");
	}
	
	public static boolean isLinux() {
		return OS_NAME.contains("nix") || OS_NAME.contains("nux") || OS_NAME.contains("aix");
	}
}
