package com.my.ht.batch.genric;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.Callable;

public class HttpRun implements Callable<HitResult> {

    private static Logger logger = LoggerFactory.getLogger(HttpRun.class);

    private long count = 0;
    private String url = "";
    private HttpAgent httpAgent;
    private long responseSize = 0;

    public HttpRun(HttpAgent httpAgent, long count, String url) {
        this.count = count;
        this.url = url;
        this.httpAgent = httpAgent;
    }

    public void setResponseSize(long responseSize) {
        this.responseSize = responseSize;
    }

    @Override
    public HitResult call() {
        long hits = 0;
        long all = 0;
        long puts = 0;
        for (int i = 0; i < this.count; ++i) {
            long s = System.currentTimeMillis();
            try {
                byte[] bytes = httpAgent.getHttpResponseFrom(url);
                if (responseSize != 0 && bytes.length != responseSize) {
                    logger.error("bad response size.");
                }
            } catch (Exception ex) {
                logger.error("error.", ex);
            }
            long tt = (System.currentTimeMillis() - s);
            hits += tt;
            all++;
        }

        HitResult hr = new HitResult();
        hr.hits = hits;
        hr.all = all;
        hr.puts = hits / all;
        return hr;
    }
}
