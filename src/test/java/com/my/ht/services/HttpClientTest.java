package com.my.ht.services;

import com.my.ht.Main;
import com.my.ht.wrapper.HttpClient3Wrapper;
import com.my.ht.wrapper.HttpClient4Wrapper;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.concurrent.FutureCallback;
import org.apache.http.impl.nio.client.CloseableHttpAsyncClient;
import org.apache.http.impl.nio.client.HttpAsyncClients;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.CountDownLatch;

public class HttpClientTest {

	@Test
	public void simpleTest() throws Exception {

		// run the htt server on local match
		Main.main(new String[]{"--port=8081"});

		String url = "http://127.0.0.1:8081/rnd?c=1024";

		try {
			System.out.println("begin http client 3.0.1 testing");
			byte[] bytes = HttpClient3Wrapper.getHttpResponseFrom(url);
			System.out.println("return buffer size: " + bytes.length);
		} catch (Exception e) {
			e.printStackTrace();
		}

		try {
			System.out.println("begin http client 4.5.3 testing");
			byte[] bytes = HttpClient4Wrapper.getByCloseableHttpClient(url);
			if (bytes != null) {
				System.out.println("return buffer size: " + bytes.length);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		System.out.println("begin http async client 4.1.3 testing");
		RequestConfig requestConfig = RequestConfig.custom()
				.setSocketTimeout(3000)
				.setConnectTimeout(3000).build();
		CloseableHttpAsyncClient httpclient = HttpAsyncClients.custom()
				.setDefaultRequestConfig(requestConfig)
				.build();

		httpclient.start();
		final HttpGet request = new HttpGet(url);

		final CountDownLatch latch = new CountDownLatch(1);
		httpclient.execute(request, new FutureCallback<HttpResponse>() {

			@Override
			public void completed(final HttpResponse response) {
				latch.countDown();
				try {
					final HttpEntity entity = response.getEntity();
					if (entity != null) {
						InputStream instream = entity.getContent();
						byte[] bytes = getResponseBytes(instream);
						if ((bytes == null) || (bytes.length <= 0)) {
							System.out.println("failed");
						} else {
							System.out.println("return buffer size: " + bytes.length);
						}
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}

			@Override
			public void failed(final Exception ex) {
				latch.countDown();
				System.out.println(request.getRequestLine() + "->" + ex);
			}

			@Override
			public void cancelled() {
				latch.countDown();
				System.out.println(request.getRequestLine() + " cancelled");
			}

		});
		latch.await();
		System.out.println("Shutting down");
		httpclient.close();
	}

	static private byte[] getResponseBytes(InputStream instream) throws IOException {
		ByteArrayOutputStream outstream = new ByteArrayOutputStream(bufSize);
		try {
			int l;
			byte[] tmp = new byte[baSize];
			while ((l = instream.read(tmp)) != -1) {
				outstream.write(tmp, 0, l);
			}
			return outstream.toByteArray();
		} finally {
			instream.close();
			outstream.close();
		}
	}

	final static private int bufSize = 1024 * 128;
	final static private int baSize = 1024 * 16;
}
