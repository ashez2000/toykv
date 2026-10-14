package io.github.ashez2000.toykv;

import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.Arrays;
import java.util.concurrent.Executors;

public class App {

    public static void main(String[] args) throws IOException {
        String port = args[0];
        String[] peers = Arrays.copyOfRange(args, 1, args.length);
        HttpServer server = HttpServer.create(new InetSocketAddress(Integer.parseInt(port)), 0);
        server.setExecutor(Executors.newVirtualThreadPerTaskExecutor());
        server.createContext("/", new KvHandler(peers));
        server.start();
        System.out.println("Server started on port " + port);
    }

}
