package cn.pupperclient.utils.language;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public final class I18n {

	private static final Map<String, String> translateMap = new HashMap<>();
	private static Language currentLanguage;

	private I18n() {
	}

	public static void setLanguage(Language language) {
		currentLanguage = language;
		load(language);
	}

	private static void load(Language language) {

		String resourcePath = String.format("assets/pupper/languages/%s.lang", language.getId());
		translateMap.clear();

		try (BufferedReader reader = new BufferedReader(new InputStreamReader(
            Objects.requireNonNull(I18n.class.getClassLoader().getResourceAsStream(resourcePath), "Language file not found: " + resourcePath), StandardCharsets.UTF_8))) {

			reader.lines().filter(line -> !line.isEmpty() && !line.startsWith("#")).map(line -> line.split("=", 2))
					.filter(parts -> parts.length == 2)
					.forEach(parts -> translateMap.put(parts[0].trim(), parts[1].trim()));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static String get(String key) {
		return translateMap.getOrDefault(key, "null");
	}

	public static Language getCurrentLanguage() {
		return currentLanguage;
	}
}
