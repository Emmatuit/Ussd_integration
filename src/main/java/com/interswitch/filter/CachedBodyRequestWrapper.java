package com.interswitch.filter;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.stream.Collectors;

import jakarta.servlet.ReadListener;
import jakarta.servlet.ServletInputStream;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;

public class CachedBodyRequestWrapper extends HttpServletRequestWrapper {

	private final String body;

	@SuppressWarnings("resource")
	public CachedBodyRequestWrapper(HttpServletRequest request) throws IOException {
		super(request);
		body = new BufferedReader(new InputStreamReader(request.getInputStream(), StandardCharsets.UTF_8)).lines()
				.collect(Collectors.joining("\n"));
	}

	public String getBody() {
		return body;
	}

	@Override
	public ServletInputStream getInputStream() {
		ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(body.getBytes(StandardCharsets.UTF_8));
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