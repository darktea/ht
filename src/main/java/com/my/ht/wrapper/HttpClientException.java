package com.my.ht.wrapper;

/**
 * xzhou on 2017/4/25.
 */
public class HttpClientException extends RuntimeException {

    private int errorCode;

    public HttpClientException() {
    }

    public HttpClientException(int errorCode, String s) {
        super(s);
        this.errorCode = errorCode;
    }

    public HttpClientException(int errorCode, Exception e) {
        super(e);
        this.errorCode = errorCode;
    }

    public HttpClientException(int errorCode, String s, Exception e) {
        super(s, e);
        this.errorCode = errorCode;
    }
}