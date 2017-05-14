package com.my.ht.batch;

import com.my.ht.batch.genric.HttpBatchRun;

/**
 * xzhou on 2017/4/26.
 */
public class BatchIt {

    final private static long taskNumber = 200;         // 并行线程数
    final private static long count = 1000;             // 每个线程循环次数
    final private static long warm = 500;               // warm循环次数

    public static void main(String[] args) throws Exception {

        HttpBatchRun httpBatchRun3 = new HttpBatchRun(new Http3Agent(), taskNumber, count, warm);
        httpBatchRun3.batch();

        HttpBatchRun httpBatchRun4 = new HttpBatchRun(new Http4Agent(), taskNumber, count, warm);
        httpBatchRun4.batch();

        System.exit(0);
    }
}
