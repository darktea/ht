package com.my.ht.wrapper;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.MultiThreadedHttpConnectionManager;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.params.HttpConnectionManagerParams;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * wrapper for apache http client 3.0.1 version
 */
public class HttpClient3Wrapper {
    final static private int bufSize = 1024 * 128;
    final static private int baSize = 1024 * 16;

    private static MultiThreadedHttpConnectionManager connectionManagerSearch = new MultiThreadedHttpConnectionManager();

    static {
        connectionManagerSearch = new MultiThreadedHttpConnectionManager();
        HttpConnectionManagerParams paramsSearch = new HttpConnectionManagerParams();
        paramsSearch.setDefaultMaxConnectionsPerHost(1000);
        paramsSearch.setMaxTotalConnections(1000);
        paramsSearch.setConnectionTimeout(3000); //连接超时
        paramsSearch.setSoTimeout(2000); //读数据超时
        connectionManagerSearch.setParams(paramsSearch);
    }

    static public byte[] getHttpResponseFrom(String url) {

        HttpClient client = new HttpClient(connectionManagerSearch);

        HttpMethod method = new GetMethod(url);
        int resultCode = 1;
        try {
            client.executeMethod(method);
            resultCode = method.getStatusCode();
        } catch (Exception e) {
            method.releaseConnection();
            throw new HttpClientException(1, url, e);
        }

        if (resultCode != 200) {
            throw new HttpClientException(resultCode, url);
        }

        byte[] responseBody = null;
        try {
            responseBody = getResponseBytes(method.getResponseBodyAsStream());
        } catch (IOException e) {
            throw new HttpClientException(1, e);
        } finally {
            method.releaseConnection();
        }
        return responseBody;
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
