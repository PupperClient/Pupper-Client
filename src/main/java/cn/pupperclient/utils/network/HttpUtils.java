package cn.pupperclient.utils.network;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Objects;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

public class HttpUtils {

	private final static String ACCEPTED_RESPONSE = "application/json";
	private final static Gson gson = new Gson();

	public static JsonObject readJson(HttpURLConnection connection) {
		return gson.fromJson(readResponse(connection), JsonObject.class);
	}

	public static void postJson(String url, Object request) {

		HttpURLConnection connection = setupConnection(url, "Mozilla/5.0", 5000, false);
        assert connection != null;

        connection.setDoOutput(true);
		connection.addRequestProperty("Content-Type", ACCEPTED_RESPONSE);
		connection.addRequestProperty("Accept", ACCEPTED_RESPONSE);

		try {
			connection.setRequestMethod("POST");
			connection.getOutputStream().write(gson.toJson(request).getBytes(StandardCharsets.UTF_8));
		} catch (IOException ignored) {
		}

        readJson(connection);
    }

	public static String readResponse(HttpURLConnection connection) {

		String redirection = connection.getHeaderField("Location");

		if (redirection != null) {
			return readResponse(Objects.requireNonNull(setupConnection(redirection, "Mozilla/5.0", 5000, false)));
		}

		StringBuilder response = new StringBuilder();

		try (BufferedReader br = new BufferedReader(new InputStreamReader(
				connection.getResponseCode() >= 400 ? connection.getErrorStream() : connection.getInputStream()))) {
			String line;
			while ((line = br.readLine()) != null) {
				response.append(line).append('\n');
			}
		} catch (IOException ignored) {
		}

		return response.toString();
	}

	public static JsonObject readJson(String url, Map<String, String> headers, String userAgents) {

		try {
			HttpURLConnection connection = setupConnection(url, userAgents, 5000, false);
            assert connection != null;
			if (headers != null) {
				for (String header : headers.keySet()) {
                    connection.addRequestProperty(header, headers.get(header));
				}
			}

			InputStream is = connection.getResponseCode() != 200 ? connection.getErrorStream()
					: connection.getInputStream();
			BufferedReader rd = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8));

			return gson.fromJson(readResponse(rd), JsonObject.class);
		} catch (IOException ignored) {
		}

		return null;
	}

	public static JsonObject readJson(String url, Map<String, String> headers) {
		return readJson(url, headers, "Mozilla/5.0");
	}

	private static String readResponse(BufferedReader br) {

		try {
			StringBuilder sb = new StringBuilder();
			String line;

			while ((line = br.readLine()) != null) {
				sb.append(line);
			}

			return sb.toString();
		} catch (IOException ignored) {
		}

		return null;
	}

	public static HttpURLConnection setupConnection(String url, String userAgent, int timeout, boolean useCaches) {

		try {
			HttpURLConnection connection = ((HttpURLConnection) URI.create(url).toURL().openConnection());

			connection.setRequestMethod("GET");
			connection.setUseCaches(useCaches);
			connection.addRequestProperty("User-Agent", userAgent);
			connection.setRequestProperty("Accept-Language", "en-US");
			connection.setRequestProperty("Accept-Charset", "UTF-8");
			connection.setReadTimeout(timeout);
			connection.setConnectTimeout(timeout);
			connection.setDoOutput(true);

			return connection;
		} catch (Exception ignored) {
		}

		return null;
	}
}
