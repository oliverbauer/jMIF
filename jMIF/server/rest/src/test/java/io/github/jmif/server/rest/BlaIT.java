package io.github.jmif.server.rest;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClientBuilder;
import org.junit.Assert;
import org.junit.Test;

public class BlaIT {

	@Test
	public void test() throws Exception {
		final HttpResponse httpResponse = HttpClientBuilder.create().build().execute(new HttpGet("http://localhost:8080/jmif/get/0"));
		Assert.assertNotNull(httpResponse);
	}

}
