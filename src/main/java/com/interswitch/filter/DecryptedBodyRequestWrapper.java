package com.interswitch.filter;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

import org.springframework.http.MediaType;

import jakarta.servlet.ReadListener;
import jakarta.servlet.ServletInputStream;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;

public class DecryptedBodyRequestWrapper extends HttpServletRequestWrapper {

	private final String decryptedBody;
	private final Map<String, String> customHeaders;

	public DecryptedBodyRequestWrapper(HttpServletRequest request, String decryptedBody) {
		super(request);
		this.decryptedBody = decryptedBody;
		this.customHeaders = new HashMap<>();
		this.customHeaders.put("Content-Type", MediaType.APPLICATION_JSON_VALUE);
	}

	@Override
	public int getContentLength() {
		return decryptedBody.getBytes(StandardCharsets.UTF_8).length;
	}

	@Override
	public long getContentLengthLong() {
		return getContentLength();
	}

	@Override
	public String getContentType() {
		return MediaType.APPLICATION_JSON_VALUE;
	}

	@Override
	public String getHeader(String name) {
		if ("Content-Type".equalsIgnoreCase(name)) {
			return MediaType.APPLICATION_JSON_VALUE;
		}
		return super.getHeader(name);
	}

	@Override
	public Enumeration<String> getHeaders(String name) {
		if ("Content-Type".equalsIgnoreCase(name)) {
			return Collections.enumeration(Collections.singletonList(MediaType.APPLICATION_JSON_VALUE));
		}
		return super.getHeaders(name);
	}

	@Override
	public ServletInputStream getInputStream() {
		ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(
				decryptedBody.getBytes(StandardCharsets.UTF_8));
		return new ServletInputStream() {
			@Override
			public boolean isFinished() {
				return byteArrayInputStream.available() == 0;
			}

			@Override
			public boolean isReady() {
				return true;
			}

			@Override
			public int read() {
				return byteArrayInputStream.read();
			}

			@Override
			public void setReadListener(ReadListener listener) {
			}
		};
	}

	@Override
	public BufferedReader getReader() {
		return new BufferedReader(new InputStreamReader(getInputStream(), StandardCharsets.UTF_8));
	}
}
