package com.my.ht.async;

import org.apache.http.config.ConnectionConfig;
import org.apache.http.impl.nio.client.CloseableHttpAsyncClient;
import org.apache.http.impl.nio.client.HttpAsyncClients;
import org.apache.http.impl.nio.conn.PoolingNHttpClientConnectionManager;
import org.apache.http.impl.nio.reactor.DefaultConnectingIOReactor;
import org.apache.http.impl.nio.reactor.IOReactorConfig;
import org.apache.http.nio.reactor.IOReactorExceptionHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.concurrent.*;

/**
 * xzhou on 2017/4/27.
 */
public class Batch {

    private static Logger logger = LoggerFactory.getLogger(Batch.class);

    final private static int taskNumber = 200;         // 并行线程数
    final private static int count = 1000;             // 每个线程循环次数
    final private static int warm = 1000;              // warm循环次数


    private final static ExecutorService exec = Executors.newCachedThreadPool();

    public static void main(final String[] args) throws Exception {

//        IOReactorConfig ioReactorConfig = IOReactorConfig.custom()
//                .setIoThreadCount(Runtime.getRuntime().availableProcessors())
//                .setConnectTimeout(3000)
//                .setSoTimeout(3000)
//                .build();
//
//        ConnectingIOReactor ioReactor = new DefaultConnectingIOReactor(ioReactorConfig);
//        PoolingNHttpClientConnectionManager cm = new PoolingNHttpClientConnectionManager(ioReactor);
//        cm.setMaxTotal(1000);
//        cm.setDefaultMaxPerRoute(1000);
//
//        CloseableHttpAsyncClient httpclient = HttpAsyncClients.custom().setConnectionManager(cm).build();

        final IOReactorConfig ioReactorConfig = IOReactorConfig.custom()
                .setIoThreadCount(20)
                .setConnectTimeout(3000)
                .setSoTimeout(3000)
                .setSoKeepAlive(true)
                .build();
        final ConnectionConfig connectionConfig = ConnectionConfig.custom()
                .setBufferSize(8 * 1024)
                .setFragmentSizeHint(8 * 1024)
                .build();
        DefaultConnectingIOReactor ioreactor = new DefaultConnectingIOReactor(ioReactorConfig);
        ioreactor.setExceptionHandler(new DefaultIOReactorExceptionHandler());//设置异常处理器
        PoolingNHttpClientConnectionManager mgr = new PoolingNHttpClientConnectionManager(ioreactor);
        mgr.setDefaultConnectionConfig(connectionConfig);
        mgr.setMaxTotal(1000);
        mgr.setDefaultMaxPerRoute(1000);
        CloseableHttpAsyncClient httpclient = HttpAsyncClients.custom().setConnectionManager(mgr).build();

        try {
            httpclient.start();

            CompletionService<Long> completionService = new ExecutorCompletionService<Long>(exec);
            RunIt warmIt = new RunIt(warm, httpclient);
            completionService.submit(warmIt);
            Future<Long> fWarm = completionService.take();
            Long rWarm = fWarm.get();
            System.out.println("warm it: " + rWarm);

            long s = System.currentTimeMillis();

            for (int i = 0; i < taskNumber; ++i) {
                RunIt runIt = new RunIt(count, httpclient);
                completionService.submit(runIt);
            }
            long all = 0;
            for (int i = 0; i < taskNumber; ++i) {
                Future<Long> f = completionService.take();
                Long r = f.get();
                all += r;
            }

            long tt = (System.currentTimeMillis() - s);

            System.out.println("http result consumer thread done");
            System.out.println("[QPS]: " + all * 1000 / tt + " [Count]: " + all + " [avg Time (ms)]: " + tt / (all));

            exec.shutdown();
        } finally {
            httpclient.close();
        }
    }

    static private class DefaultIOReactorExceptionHandler implements IOReactorExceptionHandler {
        @Override
        public boolean handle(IOException ex) {
            logger.error(ex.getMessage(), ex);
            return true;
        }

        @Override
        public boolean handle(RuntimeException ex) {
            logger.error(ex.getMessage(), ex);
            return true;//确保reactor永远处于work状态
        }
    }
}
