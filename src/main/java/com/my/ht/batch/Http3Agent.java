package com.my.ht.batch;

import com.my.ht.batch.genric.HttpAgent;
import com.my.ht.wrapper.HttpClient3Wrapper;

/**
 * xzhou on 2017/4/26.
 */
public class Http3Agent implements HttpAgent {
    @Override
    public byte[] getHttpResponseFrom(String url) {
        return HttpClient3Wrapper.getHttpResponseFrom(url);
    }
}
