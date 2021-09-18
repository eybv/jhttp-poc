package com.github.eybv.jhttp;

import com.github.eybv.jhttp.autoconfigure.ArgumentResolverAutoConfigurer;
import com.github.eybv.jhttp.autoconfigure.RequestMappingAutoConfigurer;
import com.github.eybv.jhttp.error.HttpException;
import com.github.eybv.jhttp.error.NotFoundException;
import com.github.eybv.jhttp.handler.RequestHandler;
import com.github.eybv.jhttp.reader.Http11RequestReader;
import com.github.eybv.jhttp.reader.HttpRequestReader;
import com.github.eybv.jhttp.writer.Http11ResponseWriter;
import com.github.eybv.jhttp.writer.HttpResponseWriter;

import javax.net.ServerSocketFactory;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Logger;

public class HttpServer {

    private final static Logger logger = Logger.getLogger(HttpServer.class.getName());

    private final ServerSocket serverSocket;

    private final ExecutorService executorService;

    private final Map<String, Map<String, RequestHandler>> bindings = new HashMap<>();

    private volatile boolean stopped = false;

    public HttpServer(int port) throws IOException {
        this(ServerSocketFactory.getDefault(), port);
    }

    public HttpServer(ServerSocketFactory socketFactory, int port) throws IOException {
        this.serverSocket = socketFactory.createServerSocket(port);
        this.executorService = Executors.newFixedThreadPool(64);
    }

    public void autoConfigure(String... packages) {
        final var argumentResolverAutoConfigurer = new ArgumentResolverAutoConfigurer();
        final var resolvers = argumentResolverAutoConfigurer.scanPackages(packages);
        final var requestMappingAutoConfigurer = new RequestMappingAutoConfigurer(resolvers);
        final var bindings = requestMappingAutoConfigurer.scanPackages(packages);
        this.bindings.putAll(bindings);
    }

    public void serveForever() {
        logger.info(String.format("Server started at port %s", serverSocket.getLocalPort()));
        try {
            while (!stopped && !Thread.currentThread().isInterrupted()) {
                Socket socket = serverSocket.accept();
                executorService.submit(new ConnectionHandler(socket));
            }
        } catch (IOException e) {
            logger.severe(e.getMessage());
        }
    }

    public synchronized void stop() {
        stopped = true;
        logger.info("Server stops...");
        logger.info("ExecutorService closes...");
        var aborted = executorService.shutdownNow().size();
        logger.info(String.format("%s connections interrupted.", aborted));
        try {
            logger.info("Socket closes...");
            serverSocket.close();
        } catch (IOException e) {
            logger.severe(e.getMessage());
        }
        logger.info("Server has been stopped :)");
    }

    private class ConnectionHandler implements Runnable {

        private final HttpRequestReader reader;
        private final HttpResponseWriter writer;

        private final Socket socket;

        public ConnectionHandler(Socket socket) throws IOException {
            this.reader = new Http11RequestReader(socket.getInputStream());
            this.writer = new Http11ResponseWriter(socket.getOutputStream());
            this.socket = socket;
        }

        private RequestHandler match(HttpRequest request) {
            return Optional
                    .ofNullable(bindings.get(request.getMethod()))
                    .map(paths -> paths.get(request.getUri().getPath()))
                    .orElseThrow(NotFoundException::new);
        }

        @Override
        public void run() {
            try {
                final var request = reader.read();
                final var response = HttpResponse.createDefault();

                logger.info(String.format("Request from %s: %s %s",
                        socket.getInetAddress().getHostAddress(),
                        request.getMethod(),
                        request.getUri().toString()));

                match(request).handle(request, response);
                writer.write(response);

            } catch (HttpException e) {
                final var response = HttpResponse.from(e);
                writer.write(response);
            } catch (Exception e) {
                if (socket.isConnected()) {
                    writer.write(HttpResponse.internalServerError());
                }
                e.printStackTrace();
            } finally {
                try {
                    socket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

    }

}
