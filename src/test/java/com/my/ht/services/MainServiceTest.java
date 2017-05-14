package com.my.ht.services;

import com.my.ht.Main;
import org.junit.Test;

import static org.junit.Assert.assertFalse;

import java.net.URL;

public class MainServiceTest {

    @Test
    public void testMainIt() throws Exception {

        URL cpUrl = Thread.currentThread().getContextClassLoader().getResource("");
        assertFalse(cpUrl == null);

        System.out.println("Classpath is: [" + cpUrl.getPath() + "]");
        Main.main(new String[]{"--base.dir=" + cpUrl.getPath(), "--src.file=test.file"});

    }
}
