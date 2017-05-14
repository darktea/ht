package com.my.ht.wrapper;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.conn.ConnectionKeepAliveStrategy;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.protocol.HttpContext;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * xzhou on 2017/4/26.
 */
public class HttpClient4Wrapper {

    final static private int bufSize = 1024 * 128;
    final static private int baSize = 1024 * 16;

    private static Logger logger = LoggerFactory.getLogger(HttpClient4Wrapper.class);

    private static PoolingHttpClientConnectionManager mgr;
    private static CloseableHttpClient httpclient;

    private static ScheduledExecutorService executorService = Executors.newScheduledThreadPool(1);

    static {

        mgr = new PoolingHttpClientConnectionManager();
        mgr.setMaxTotal(1000);
        mgr.setDefaultMaxPerRoute(1000);

        RequestConfig requestConfig = RequestConfig.custom()
                .setConnectionRequestTimeout(150)
                .setConnectTimeout(3000)
                .setSocketTimeout(2000)
                .build();

        ConnectionKeepAliveStrategy myStrategy = new ConnectionKeepAliveStrategy() {
            public long getKeepAliveDuration(HttpResponse response, HttpContext context) {
                return 30 * 1000; // keep alive for 30 seconds
            }
        };

        httpclient = HttpClients.custom().setConnectionManager(mgr).setDefaultRequestConfig(requestConfig).setKeepAliveStrategy(myStrategy).build();
        executorService.scheduleAtFixedRate(new IdleConnectionMonitor(mgr), 300, 600, TimeUnit.SECONDS);
    }

    public static class IdleConnectionMonitor implements Runnable {
        private final PoolingHttpClientConnectionManager connMgr;

        public IdleConnectionMonitor(PoolingHttpClientConnectionManager connMgr) {
            this.connMgr = connMgr;
        }

        @Override
        public void run() {
            try {
                logger.warn("PoolingHttpClientConnectionManager: " + connMgr.getTotalStats().toString());
                connMgr.closeExpiredConnections();
                connMgr.closeIdleConnections(120, TimeUnit.SECONDS);
            } catch (Exception e) {
                logger.warn("IdleConnectionMonitor error: ", e);
            }
        }
    }

    public static byte[] getByCloseableHttpClient(String url) {

        if (httpclient == null) {
            return null;
        }

        byte[] bytes = null;

        try {
            HttpContext context = HttpClientContext.create();
            HttpGet httpget = new HttpGet(url);

            CloseableHttpResponse response = httpclient.execute(httpget, context);
            try {
                int resultCode = 1;
                if ((response == null) || (response.getStatusLine() == null)) {
                    throw new HttpClientException(resultCode, url);
                }

                resultCode = response.getStatusLine().getStatusCode();
                if (resultCode != 200) {
                    throw new HttpClientException(resultCode, url);
                }

                final HttpEntity entity = response.getEntity();
                if (entity != null) {
                    InputStream instream = entity.getContent();
                    bytes = getResponseBytes(instream);
                }
            } finally {
                if (response != null) {
                    response.close();
                }
            }
        } catch (final Exception e) {
            logger.error("failed on closeableHttpClient", e);
        }

        return bytes;
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
}
