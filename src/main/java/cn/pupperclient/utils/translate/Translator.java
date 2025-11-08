package cn.pupperclient.utils.translate;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.StringJoiner;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import cn.pupperclient.utils.TimerUtils;

public class Translator {

	private static String authCache;
	private static TimerUtils timer;

	public static final String AUTO_DETECT = "";
	public static final String ENGLISH = "en";
	public static final String JAPANESE = "ja";
	public static final String CHINESE_SIMPLIFIED = "zh-Hans";
	public static final String CHINESE_TRADITIONAL = "zh-Hant";
	public static final String POLISH = "pl";

	private static String auth() throws Exception {

		if (timer == null) {
			timer = new TimerUtils();
		}

		if (timer.delay(300 * 1000) || authCache == null) {

			URL url = new URI("https://edge.microsoft.com/translate/auth").toURL();
			HttpURLConnection con = (HttpURLConnection) url.openConnection();
			con.setRequestMethod("GET");

			BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
			String inputLine;
			StringBuffer content = new StringBuffer();

			while ((inputLine = in.readLine()) != null) {
				content.append(inputLine);
			}

			in.close();

			authCache = content.toString();
			return authCache;
		}

		return authCache;
	}

	public static String translate(String text, String from, String to) throws Exception {

		URL url = new URI("https://api.cognitive.microsofttranslator.com/translate?from=" + from + "&to=" + to
				+ "&api-version=3.0&includeSentenceLength=true").toURL();
		HttpURLConnection con = (HttpURLConnection) url.openConnection();
		con.setRequestMethod("POST");
		con.setRequestProperty("Content-Type", "application/json");
		con.setRequestProperty("authorization", "Bearer " + auth());

		StringJoiner sj = new StringJoiner(",", "[", "]");
		sj.add("{\"Text\":\"" + text + "\"}");

		String jsonInputString = sj.toString();

		con.setDoOutput(true);
		OutputStream os = con.getOutputStream();
		byte[] input = jsonInputString.getBytes(StandardCharsets.UTF_8);
		os.write(input, 0, input.length);

		BufferedReader br = new BufferedReader(new InputStreamReader(con.getInputStream(), StandardCharsets.UTF_8));
		String responseLine;
		StringBuilder responseContent = new StringBuilder();
		while ((responseLine = br.readLine()) != null) {
			responseContent.append(responseLine.trim());
		}
		br.close();

		Gson gson = new Gson();
		JsonArray jsonArray = gson.fromJson(responseContent.toString(), JsonArray.class);

		StringBuilder sb = new StringBuilder();
		for (JsonElement json : jsonArray) {
			JsonObject jsonObject = json.getAsJsonObject();
			JsonArray translations = jsonObject.getAsJsonArray("translations");
			for (JsonElement trans : translations) {
				JsonObject translation = trans.getAsJsonObject();
				sb.append(translation.get("text").getAsString());
				sb.append(",");
			}
		}

		return sb.toString().replaceAll(",$", "");
	}
}
