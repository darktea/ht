package com.my.ht.services;

import com.my.ht.wrapper.HttpClient3Wrapper;
import com.my.ht.wrapper.HttpClient4Wrapper;
import org.apache.http.HttpResponse;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.concurrent.FutureCallback;
import org.apache.http.impl.nio.client.CloseableHttpAsyncClient;
import org.apache.http.impl.nio.client.HttpAsyncClients;
import org.junit.Test;

import java.util.concurrent.CountDownLatch;

/**
 * xzhou on 2017/4/25.
 */
public class HttpClientTest {

    @Test
    public void simpleTest() throws Exception {

        String url = "http://127.0.0.1:8072/rnd?c=1024";

        try {
            System.out.println("begin http client 3.0.1 testing");
            byte[] bytes = HttpClient3Wrapper.getHttpResponseFrom(url);
            System.out.println("return buffer size: " + bytes.length);
        } catch (Exception e) {
            e.printStackTrace();
        }

        try {
            System.out.println("begin http client 4.3.3 testing");
            byte[] bytes = HttpClient4Wrapper.getByCloseableHttpClient(url);
            if (bytes != null) {
                System.out.println("return buffer size: " + bytes.length);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

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
                System.out.println(request.getRequestLine() + "->" + response.getStatusLine());
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
}
