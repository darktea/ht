package com.my.ht.batch.genric;

import java.util.concurrent.*;

/**
 * xzhou on 2017/4/25.
 */
public class HttpBatchRun {

    private long taskNumber;        // 并行线程数
    private long count;             // 每个线程循环次数
    private long warm;              // warm循环次数
    private HttpAgent httpAgent;

    private final static String url = "http://127.0.0.1:8072/rnd?c=1024";

    private final static ExecutorService exec = Executors.newCachedThreadPool();

    public HttpBatchRun(HttpAgent httpAgent, long taskNumber, long count, long warm) {
        this.httpAgent = httpAgent;
        this.taskNumber = taskNumber;
        this.count = count;
        this.warm = warm;
    }

    public void batch() throws Exception {

        // init
        System.out.println("taskNumber: " + taskNumber + " count: " + count + " warm: " + warm);

        // warm
        CompletionService<HitResult> warmService = new ExecutorCompletionService<HitResult>(exec);
        warmService.submit(new HttpRun(httpAgent, warm, url));

        Future<HitResult> warmF = warmService.take();
        HitResult warmR = warmF.get();
        System.out.println("result: " + warmR.all + " avg rt: " + warmR.puts);

        // test begin
        CompletionService<HitResult> completionService = new ExecutorCompletionService<HitResult>(exec);

        long s = System.nanoTime();

        for (int i = 0; i < taskNumber; ++i) {
            HttpRun httpRun = new HttpRun(httpAgent, count, url);
            httpRun.setResponseSize(1024);
            completionService.submit(httpRun);
        }

        for (int i = 0; i < taskNumber; ++i) {
            Future<HitResult> f = completionService.take();
            HitResult r = f.get();
            System.out.println("result: " + r.all + " avg rt: " + r.puts);
        }

        long tt = (System.nanoTime() - s);
        System.out.println("taskNumber: " + taskNumber);
        System.out.println("count: " + count);
        System.out.println("time: " + tt / 1000000 + " ms");
        System.out.println("QPS: " + taskNumber * count * 1000000000 / tt);
    }
}
