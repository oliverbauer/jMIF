package io.github.jmif.server.rest;

import java.io.BufferedReader;
import java.io.InputStreamReader;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClientBuilder;
import org.junit.Assert;
import org.junit.Test;

public class ServiceIT {

	@Test
	public void testGet_nothing() throws Exception {
		final HttpResponse httpResponse = HttpClientBuilder.create().build().execute(new HttpGet("http://localhost:8080/jmif/get/0"));
		Assert.assertNotNull(httpResponse);
		Assert.assertEquals(HttpStatus.SC_ACCEPTED, httpResponse.getStatusLine().getStatusCode());
		
		final var entity = httpResponse.getEntity();
		Assert.assertNotNull(entity);
		
		String result = null;
		try (final BufferedReader reader = new BufferedReader(new InputStreamReader(entity.getContent()));) {
			result = reader.readLine();
		}
		Assert.assertNotNull(result);
	}

	@Test
	public void testGetProfile_nothing() throws Exception {
		final HttpResponse httpResponse = HttpClientBuilder.create().build().execute(new HttpGet("http://localhost:8080/jmif/getProfiles"));
		Assert.assertNotNull(httpResponse);
		Assert.assertEquals(HttpStatus.SC_ACCEPTED, httpResponse.getStatusLine().getStatusCode());
		
		final var entity = httpResponse.getEntity();
		Assert.assertNotNull(entity);
		
		String result = null;
		try (final BufferedReader reader = new BufferedReader(new InputStreamReader(entity.getContent()));) {
			result = reader.readLine();
		}
		Assert.assertNotNull(result);
	}
}
