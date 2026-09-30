package io.github.ashez2000.toykv;

import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.net.InetSocketAddress;

public class App {

    public static void main(String[] args) throws IOException {
        HttpServer server = HttpServer.create(new InetSocketAddress(8000), 0);
        server.createContext("/", new KvHandler());
        server.start();
        System.out.println("Server started on port 8000");
    }

}
