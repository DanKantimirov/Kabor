package ru.kabor.demand.prediction.utils;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;

import javax.net.ssl.HttpsURLConnection;

import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** It contains methods for checking Google captcha */
public class VerifyCaptcha {

	private static final Logger LOG = LoggerFactory.getLogger(VerifyCaptcha.class);
	public static final String REQUEST_URL = "https://www.google.com/recaptcha/api/siteverify";
	private final static String USER_AGENT = "Mozilla/5.0";

	/**
	 * Sends request to Google for checking captcha
	 * 
	 * @param secretKey
	 *            secret key of site
	 * @param gRecaptchaResponse
	 *            users value from form
	 * @param isRealMode
	 *            false - for testing(just check whether field is empty or not),
	 *            true - for real mode
	 */
	public static boolean verify(String secretKey, String gRecaptchaResponse, Boolean isRealMode) {
		if (gRecaptchaResponse == null || "".equals(gRecaptchaResponse)) {
			return false;
		}

		if (isRealMode) {

			DataOutputStream outputStream = null;
			BufferedReader bufferedReader = null;

			try {
				URL url = new URL(REQUEST_URL);
				HttpsURLConnection connection = (HttpsURLConnection) url.openConnection();

				// add request header
				connection.setRequestMethod("POST");
				connection.setRequestProperty("User-Agent", USER_AGENT);
				connection.setRequestProperty("Accept-Language", "en-US,en;q=0.5");

				String postParams = "secret=" + secretKey + "&response=" + gRecaptchaResponse;

				// Send post request
				connection.setDoOutput(true);
				outputStream = new DataOutputStream(connection.getOutputStream());
				outputStream.writeBytes(postParams);
				outputStream.flush();

				bufferedReader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
				String inputLine;
				StringBuffer response = new StringBuffer();
				while ((inputLine = bufferedReader.readLine()) != null) {
					response.append(inputLine);
				}
				JSONObject jsonObject = new JSONObject(response.toString());
				return jsonObject.getBoolean("success");
			} catch (IOException | JSONException e) {
				LOG.error("Captcha exception", e);
				return false;
			} finally {
				if (outputStream != null) {
					try {
						outputStream.close();
					} catch (IOException e) {
						LOG.error("Captcha exception", e);
					}
				}
				if (bufferedReader != null) {
					try {
						bufferedReader.close();
					} catch (IOException e) {
						LOG.error("Captcha exception", e);
					}
				}
			}
		} else {
			return true;
		}
	}
}
