package com.my.ht;

import com.my.ht.services.ArgsServices;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import org.eclipse.jetty.http.HttpStatus;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.AbstractHandler;
import org.eclipse.jetty.server.nio.SelectChannelConnector;
import org.eclipse.jetty.util.ByteArrayOutputStream2;
import org.eclipse.jetty.util.IO;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Writer;
import java.util.Arrays;
import java.util.concurrent.TimeUnit;

@SpringBootApplication
public class Main implements CommandLineRunner {

    private static final Logger logger = LoggerFactory.getLogger(Main.class);

    @Autowired
    private ArgsServices argsServices;

    public static void main(String[] args) throws Exception {
        SpringApplication app = new SpringApplication(Main.class);
        app.setWebEnvironment(false);
        app.run(args);
    }

    @Override
    public void run(String... args) throws Exception {

        int port = 8072;

        logger.info("port: [" + this.argsServices.getPort() + "]");
        if ((argsServices.getPort() == null) || argsServices.getPort().isEmpty()) {
            logger.error("sample: java -jar ht-1.0-SNAPSHOT.jar --port=8072");
        } else {
            port = Integer.parseInt(argsServices.getPort());
        }

        final SelectChannelConnector connector = new SelectChannelConnector();
        final Server server = new Server(port);
        server.addConnector(connector);
        server.setHandler(new RandomDataHandler());

        server.start();
        final int localPort = connector.getLocalPort();
        logger.info("the local port: " + localPort);
    }

    static class RandomDataHandler extends AbstractHandler {

        public RandomDataHandler() {
            super();
        }

        @Override
        public void handle(
                final String target,
                final Request baseRequest,
                final HttpServletRequest request,
                final HttpServletResponse response) throws IOException, ServletException {
            if (target.equals("/rnd")) {
                rnd(request, response);
            } else if (target.equals("/echo")) {
                echo(request, response);
            } else {
                response.setStatus(HttpStatus.NOT_FOUND_404);
                final Writer writer = response.getWriter();
                writer.write("Target not found: " + target);
                writer.flush();
            }
        }

        private void rnd(
                final HttpServletRequest request,
                final HttpServletResponse response) throws IOException {
            int count = 100;
            final String s = request.getParameter("c");
            try {
                count = Integer.parseInt(s);
            } catch (final NumberFormatException ex) {
                response.setStatus(500);
                final Writer writer = response.getWriter();
                writer.write("Invalid query format: " + request.getQueryString());
                writer.flush();
                return;
            }

            response.setStatus(200);
            response.setContentLength(count);

            final OutputStream outstream = response.getOutputStream();
            final byte[] tmp = new byte[1024];
            final int r = Math.abs(Arrays.hashCode(tmp));
            int remaining = count;
            while (remaining > 0) {
                try {
                    TimeUnit.MILLISECONDS.sleep(50);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                final int chunk = Math.min(tmp.length, remaining);
                for (int i = 0; i < chunk; i++) {
                    tmp[i] = (byte) ((r + i) % 96 + 32);
                }
                outstream.write(tmp, 0, chunk);
                remaining -= chunk;
            }
            outstream.flush();
        }

        private void echo(
                final HttpServletRequest request,
                final HttpServletResponse response) throws IOException {

            final ByteArrayOutputStream2 buffer = new ByteArrayOutputStream2();
            final InputStream instream = request.getInputStream();
            if (instream != null) {
                IO.copy(instream, buffer);
                buffer.flush();
            }
            final byte[] content = buffer.getBuf();
            final int len = buffer.getCount();

            response.setStatus(200);
            response.setContentLength(len);

            final OutputStream outstream = response.getOutputStream();
            outstream.write(content, 0, len);
            outstream.flush();
        }

    }
}
