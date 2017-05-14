package com.my.ht.batch.genric;

import com.my.ht.wrapper.HttpClient3Wrapper;

/**
 * xzhou on 2017/4/26.
 */
public interface HttpAgent {
    byte[] getHttpResponseFrom(String url);
}
