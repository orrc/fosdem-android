package org.fosdem.parsers;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

public abstract class BaseParser {
	protected InputStream stream;

	public InputStream getStream() {
		return stream;
	}

	public void setStream(InputStream stream) {
		this.stream = stream;
	}

	public BaseParser(InputStream s) {
		this.stream = s;
	}

	public BaseParser(String url) throws IOException {
		this.stream = openHttpConnection(url);
	}

	protected InputStream openHttpConnection(String urlString) throws IOException {
		InputStream in = null;
		int response = -1;

		URL url = new URL(urlString);

		HttpURLConnection conn = null;

		if (url.getProtocol().toLowerCase().equals("https")) {
			trustAllCerts();
			HttpsURLConnection https = (HttpsURLConnection) url.openConnection();
			conn = https;
		} else {
			conn = (HttpURLConnection) url.openConnection();
		}

		if (!(conn instanceof HttpURLConnection))
			throw new IOException("Not an HTTP connection");

		try	{
			conn.setReadTimeout(30000 /* milliseconds */);
			conn.setConnectTimeout(35000 /* milliseconds */);
			conn.setAllowUserInteraction(false);
			conn.setInstanceFollowRedirects(true);
			conn.setRequestMethod("GET");

			conn.connect();
			response = conn.getResponseCode();

			if (response == HttpsURLConnection.HTTP_OK) {
				in = conn.getInputStream();
			}
		} catch (Exception e) {
			throw new IOException("Error connecting");
		}
		return in;
	}

	/**
	 * Trust every certificate by installing an all-trusting custom TrustManager
	 * that does not validate certificate chains.
	 */
	private static void trustAllCerts() {
		TrustManager[] trustAllCerts = new TrustManager[] { new X509TrustManager() {
			public java.security.cert.X509Certificate[] getAcceptedIssuers() {
				return new java.security.cert.X509Certificate[] {};
			}

			public void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException {
			}

			public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException {
			}
		} };

		// install the manager
		try {
			SSLContext sc = SSLContext.getInstance("TLS");
			sc.init(null, trustAllCerts, new java.security.SecureRandom());
			HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}