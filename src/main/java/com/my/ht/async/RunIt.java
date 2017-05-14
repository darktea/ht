package com.my.ht.async;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.concurrent.FutureCallback;
import org.apache.http.impl.nio.client.CloseableHttpAsyncClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicLong;

public class RunIt implements Callable<Long> {

    private static Logger logger = LoggerFactory.getLogger(RunIt.class);

    final static private int bufSize = 1024 * 128;
    final static private int baSize = 1024 * 16;

    private int count = 0;
    private CloseableHttpAsyncClient httpclient;

    public RunIt(int count, CloseableHttpAsyncClient httpclient) {
        this.count = count;
        this.httpclient = httpclient;
    }

    @Override
    public Long call() {

        final CountDownLatch latch = new CountDownLatch(count);
        BackIt backIt = new BackIt(latch);

        for (int i = 0; i < count; i++) {
            final HttpGet request = new HttpGet("http://127.0.0.1:8072/rnd?c=" + (i + 1));
            backIt.setRequest(request);
            httpclient.execute(request, backIt);
        }

        try {
            latch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        return backIt.getOkNumber();
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

    private class BackIt implements FutureCallback<HttpResponse> {

        private CountDownLatch latch;
        private HttpGet request;

        private AtomicLong atomicLong = new AtomicLong(0);

        public BackIt(CountDownLatch latch) {
            this.latch = latch;
        }

        public long getOkNumber() {
            return this.atomicLong.get();
        }

        public void setRequest(HttpGet request) {
            this.request = request;
        }

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
                        this.atomicLong.incrementAndGet();
                    }
                }
            } catch (Exception e) {
                logger.error("error on completed. ", e);
            }
        }

        @Override
        public void failed(final Exception ex) {
            latch.countDown();
            System.out.println(request.getRequestLine() + "->" + ex);
            this.atomicLong.incrementAndGet();
            ex.printStackTrace();
        }

        @Override
        public void cancelled() {
            latch.countDown();
            this.atomicLong.incrementAndGet();
            System.out.println(request.getRequestLine() + " cancelled");
        }

    }
}