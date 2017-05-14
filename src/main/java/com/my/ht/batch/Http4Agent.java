package com.my.ht.batch;

import com.my.ht.batch.genric.HttpAgent;
import com.my.ht.wrapper.HttpClient3Wrapper;
import com.my.ht.wrapper.HttpClient4Wrapper;

/**
 * xzhou on 2017/4/26.
 */
public class Http4Agent implements HttpAgent {
    @Override
    public byte[] getHttpResponseFrom(String url) {
        return HttpClient4Wrapper.getByCloseableHttpClient(url);
    }
}
