
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;

import java.io.*;
import java.util.*;
// import java.util.Scanner;

import java.nio.file.Files;

import java.net.URI;

import com.sun.net.httpserver.*;
// import com.sun.net.httpserver.HttpExchange;
// import com.sun.net.httpserver.HttpHandler;
// import com.sun.net.httpserver.HttpServer;

public class Main {

    public static void main(String[] args) throws Exception {
        HttpServer server = HttpServer.create(new InetSocketAddress(8000), 0);
        server.createContext("/text", new Text());
        server.createContext("/html", new HTML());
        server.createContext("/run", new POST());
        server.createContext("/static", new StaticFiles());
        server.setExecutor(null); // creates a default executor
        System.out.println("Server is running on local host with port 8000");
        server.start();
    }

    static class Text implements HttpHandler {
        //THIS IS HOW YOU HANDLE TEXT
        public void handle(HttpExchange t) throws IOException {
            String response = "This is the response";
            t.sendResponseHeaders(200, response.length());
            OutputStream out = t.getResponseBody();
            out.write(response.getBytes());
            out.close();
        }
    }

    static class POST implements HttpHandler {
        public void handle(HttpExchange t) throws IOException {
            if (t.getRequestMethod().equalsIgnoreCase("POST")) {
                // THIS IS HOW YOU HANDLE POST AND GET POST DATA
                StringBuilder buf = new StringBuilder();

                int b;
                InputStream is = t.getRequestBody();
                while ((b = is.read()) != -1) {
                    buf.append((char) b);
                }
                is.close();
                String response = buf.toString();
                System.out.println(response); //RESPONSE IS A STRING VERSION OF THE BODY

            }
        }
    }

    static class HTML implements HttpHandler {
        //        @Override
        public void handle(HttpExchange t) throws IOException {
            // String response = ReadHtml("saves/ide.html");\
            // URI uri = ex.getRequestURI();
            String name = new File("saves/index.html").getName();
            File path = new File("saves/" + name);

            Headers h = t.getResponseHeaders();
            // Could be more clever about the content type based on the filename here.
            h.add("Content-Type", "text/html");

            OutputStream out = t.getResponseBody();

            if (path.exists()) {
                t.sendResponseHeaders(200, path.length());
                out.write(Files.readAllBytes(path.toPath()));

            } else {
                System.err.println("File not found: " + path.getAbsolutePath());
                t.sendResponseHeaders(404, 0);
                out.write("404 File not found.".getBytes());
            }

            out.close();
        }
    }

    static class StaticFiles implements HttpHandler {
        //This handles css and js files

        public void handle(HttpExchange t) throws IOException {

            String root = ".";
            URI uri = t.getRequestURI();
            System.out.println("looking for: " + root + uri.getPath());
            String path = uri.getPath();
            File file = new File(root + path).getCanonicalFile();

            if (!file.isFile()) {
                // Object does not exist or is not a file: reject with 404 error.
                String response = "404 (Not Found)\n";
                t.sendResponseHeaders(404, response.length());
                OutputStream os = t.getResponseBody();
                os.write(response.getBytes());
                os.close();
            } else {
                // Object exists and is a file: accept with response code 200.
                String mime = "text/html";
                if (path.substring(path.length() - 3).equals(".js"))
                    mime = "application/javascript";
                if (path.substring(path.length() - 3).equals("css"))
                    mime = "text/css";

                Headers h = t.getResponseHeaders();
                h.set("Content-Type", mime);
                t.sendResponseHeaders(200, 0);

                OutputStream os = t.getResponseBody();
                FileInputStream fs = new FileInputStream(file);
                final byte[] buffer = new byte[0x10000];
                int count = 0;
                while ((count = fs.read(buffer)) >= 0) {
                    os.write(buffer, 0, count);
                }
                fs.close();
                os.close();
            }
        }
    }
}