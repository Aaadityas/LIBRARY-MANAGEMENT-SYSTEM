// File: src/LibraryServer.java
import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpExchange;
import java.io.*;
import java.net.InetSocketAddress;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import com.google.gson.Gson;

// Book class
class Book {
    String title;
    String author;
    boolean isIssued;

    public Book(String title, String author) {
        this.title = title;
        this.author = author;
        this.isIssued = false;
    }
}

// Main Server
public class LibraryServer {
    private static List<Book> books = new ArrayList<>();
    private static Gson gson = new Gson();

    public static void main(String[] args) throws IOException {
        HttpServer server = HttpServer.create(new InetSocketAddress(8000), 0);

        // Serve frontend HTML
        server.createContext("/", (exchange -> {
            File file = new File("data/index.html");
            if(file.exists()) {
                byte[] bytes = Files.readAllBytes(file.toPath());
                exchange.getResponseHeaders().set("Content-Type", "text/html");
                exchange.sendResponseHeaders(200, bytes.length);
                OutputStream os = exchange.getResponseBody();
                os.write(bytes);
                os.close();
            } else {
                String response = "index.html not found!";
                exchange.sendResponseHeaders(404, response.getBytes().length);
                OutputStream os = exchange.getResponseBody();
                os.write(response.getBytes());
                os.close();
            }
        }));

        // Add Book
        server.createContext("/addBook", (exchange -> {
            if ("POST".equals(exchange.getRequestMethod())) {
                InputStreamReader isr = new InputStreamReader(exchange.getRequestBody(), "utf-8");
                Book book = gson.fromJson(isr, Book.class);
                books.add(book);

                String response = gson.toJson(book);
                exchange.getResponseHeaders().set("Content-Type", "application/json");
                exchange.sendResponseHeaders(200, response.getBytes().length);
                OutputStream os = exchange.getResponseBody();
                os.write(response.getBytes());
                os.close();
            } else {
                exchange.sendResponseHeaders(405, -1);
            }
        }));

        // List Books
        server.createContext("/listBooks", (exchange -> {
            if ("GET".equals(exchange.getRequestMethod())) {
                String response = gson.toJson(books);
                exchange.getResponseHeaders().set("Content-Type", "application/json");
                exchange.sendResponseHeaders(200, response.getBytes().length);
                OutputStream os = exchange.getResponseBody();
                os.write(response.getBytes());
                os.close();
            } else {
                exchange.sendResponseHeaders(405, -1);
            }
        }));

        // Issue Book
        server.createContext("/issueBook", (exchange -> {
            if ("POST".equals(exchange.getRequestMethod())) {
                InputStreamReader isr = new InputStreamReader(exchange.getRequestBody(), "utf-8");
                Book reqBook = gson.fromJson(isr, Book.class);
                boolean found = false;
                for (Book b : books) {
                    if (b.title.equalsIgnoreCase(reqBook.title) && !b.isIssued) {
                        b.isIssued = true;
                        found = true;
                        String response = gson.toJson(b);
                        exchange.getResponseHeaders().set("Content-Type", "application/json");
                        exchange.sendResponseHeaders(200, response.getBytes().length);
                        OutputStream os = exchange.getResponseBody();
                        os.write(response.getBytes());
                        os.close();
                        break;
                    }
                }
                if(!found) exchange.sendResponseHeaders(404, -1);
            }
        }));

        // Return Book
        server.createContext("/returnBook", (exchange -> {
            if ("POST".equals(exchange.getRequestMethod())) {
                InputStreamReader isr = new InputStreamReader(exchange.getRequestBody(), "utf-8");
                Book reqBook = gson.fromJson(isr, Book.class);
                boolean found = false;
                for (Book b : books) {
                    if (b.title.equalsIgnoreCase(reqBook.title) && b.isIssued) {
                        b.isIssued = false;
                        found = true;
                        String response = gson.toJson(b);
                        exchange.getResponseHeaders().set("Content-Type", "application/json");
                        exchange.sendResponseHeaders(200, response.getBytes().length);
                        OutputStream os = exchange.getResponseBody();
                        os.write(response.getBytes());
                        os.close();
                        break;
                    }
                }
                if(!found) exchange.sendResponseHeaders(404, -1);
            }
        }));

        server.setExecutor(null); // default executor
        System.out.println("Library Server running at http://localhost:8000");
        server.start();
    }
}
