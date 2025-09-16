/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package co.edu.escuelaing.microspringboot;

import java.net.*;
import java.io.*;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.*;
import java.util.concurrent.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * A lightweight HTTP server that provides web framework functionality.
 * This class implements a web server that can handle REST services, static file serving,
 * and provides a simple API for registering service endpoints.
 * @author daniel.aldana-b
 */
public class HttpServer {
    //Map containing registered REST services mapped by their paths
    public static final Map<String, Method> services = new HashMap<>();
    public static final Map<String, List<Parameter>> requests = new HashMap<>();
    // Root directory for serving static files
    public static String ROOT_DIRECTORY = "target/classes/webroot";

    // Simple thread pool for handling concurrent requests
    private static ExecutorService executor;
    private static volatile boolean running = true;

    /**
     * Starts the HTTP server and begins listening for incoming connections.
     * The server runs continuously, accepting client connections and handling
     * HTTP requests until manually stopped.
     * 
     * @param args command line arguments (not used)
     * @throws IOException if an I/O error occurs during server operation
     * @throws URISyntaxException if there's an error parsing request URIs
     */
    public static void runServer(String[] args) throws IOException, URISyntaxException {
        // Initialize simple thread pool
        executor = Executors.newFixedThreadPool(10);
        
        // Register shutdown hook for graceful shutdown
        registerShutdownHook();
        
        ServerSocket serverSocket = null;
        try {
            serverSocket = new ServerSocket(getPort());
        } catch (IOException e) {
            System.err.println("Could not listen on port: 35000.");
            System.exit(1);
        }
        
        loadComponents(args);
        
        while (running) {
            try {
                Socket clientSocket = serverSocket.accept();
                
                // Submit each connection to the thread pool for concurrent processing
                executor.submit(() -> {
                    try {
                        handleClient(clientSocket);
                    } catch (Exception e) {
                        Logger.getLogger(HttpServer.class.getName()).log(Level.SEVERE, null, e);
                    }
                });
                
            } catch (IOException e) {
                if (running) {
                    System.err.println("Accept failed.");
                    System.exit(1);
                }
                // If running is false, this is expected during shutdown
                break;
            }
        }
        
        serverSocket.close();
    }
    
    /**
     * Handles an incoming HTTP request and generates the appropriate response.
     * This method routes requests to the appropriate handler based on the URI path:
     * @param uri    the request URI containing the path and query parameters
     * @param out    the writer to send responses to the client
     * @param socket the client socket used for file streaming
     * @throws IOException if an I/O error occurs when handling the request
     */
    public static void handleRequest(URI uri, PrintWriter out, Socket socket) throws IOException {
        
        if(uri != null && uri.getPath().startsWith("/app/helloget")){
            String output = greetingService(uri, false);
            invokeService(uri);
            out.println(output);
        }else if(uri != null && uri.getPath().startsWith("/app/hellopost")) {
            String output = greetingService(uri, true);
            out.println(output);
        }
        // Check for registered REST services
        else if(uri != null && services.containsKey(uri.getPath())) {
            String output = invokeService(uri);
            out.println(output);
        }
        else if (uri != null){
            // Handle static files
            InputStream resource;


            String path = uri.getPath();


            if (path.endsWith("/")) {
                path += "index.html";
            }
            String resourcePath = "webroot" + path;
            resource = HttpServer.class.getClassLoader().getResourceAsStream(resourcePath);
            if (resource != null) {
                String mime = getType(Path.of(path));
                String header = "HTTP/1.1 200 OK\r\n" +
                        "content-type: " + mime + "\r\n\r\n";
                OutputStream outputStream = socket.getOutputStream();
                outputStream.write(header.getBytes());
                resource.transferTo(outputStream);
                resource.close();
            } else {
                String outputLine = "HTTP/1.1 404 Not Found\r\n" +
                        "content-type: text/plain; charset=utf-8\r\n\r\n" +
                        "File not found: " + path;
                out.println(outputLine);
            }
        }

    }
    
    /**
     * Determines the MIME type of a given file based on its extension.
     * Supports common web file types including HTML, CSS, JavaScript, images, and JSON.
     *
     * @param path the file path whose content type is to be determined
     * @return the MIME type as a string (e.g., "text/html"), or "application/octet-stream" if unknown
     */
    public static String getType(Path path){
        if (path == null || path.getFileName() == null) {
            return "application/octet-stream";
        }

        String fileName = path.getFileName().toString();
        int dotIndex = fileName.lastIndexOf('.');
        if (dotIndex < 0) {
            return "application/octet-stream";
        }

        String extension = fileName.substring(dotIndex + 1).toLowerCase(Locale.ROOT);

        return switch (extension) {
            case "html", "htm" -> "text/html; charset=utf-8";
            case "css" -> "text/css; charset=utf-8";
            case "js" -> "application/javascript; charset=utf-8";
            case "json" -> "application/json; charset=utf-8";
            case "png" -> "image/png";
            case "jpg", "jpeg" -> "image/jpeg";
            case "gif" -> "image/gif";
            case "svg" -> "image/svg+xml";
            case "ico" -> "image/x-icon";
            default -> "application/octet-stream";
        };
    }
    
    /**
     * Generates an HTTP response with a JSON greeting message.
     * This is a legacy service method that creates a JSON response with a greeting
     * and optionally includes the current date.
     *
     * @param uri  the request URI containing the query parameter (?name=value)
     * @param time if true, includes the current date in the response
     * @return an HTTP response string with status, headers, and JSON body
     */
    public static String greetingService(URI uri, boolean time){
        String user;
        try{
            user = uri.getQuery().split("=")[1];
        } catch (Exception e) {
            return "HTTP/1.1 400 Bad Request\r\n" + "content-type: text/plain; charset=utf-8\r\n"
                    + "\r\n" + "{\"msg\": \"Name not found\"}";
        }
        String response = "HTTP/1.1 200 OK \r\n" + "content-type: application/json; charset=utf-8\r\n"
                + "\r\n";
        response = response + "{\"msg\": \"Hello " + user;
        response = time? response + "today's date is" + LocalDate.now() + "\"}":response+ "\"}";
        System.out.println(response);
        return response;
    }
    
    /**
     * Registers a REST service endpoint with the specified path.
     * The service will be invoked when a request is made to the specified path.
     * 
     * @param path the URL path for the service (e.g., "/hello", "/api/users")
     * @param s the service implementation to handle requests to this path
     */
    public static void get(String path, Method s){
        services.put(path,s);
    }
    
    /**
     * Sets the root directory for serving static files.
     * The directory path is relative to the target/classes directory.
     * 
     * @param localFilesPath the path to the static files directory
     */
    public static void staticfiles(String localFilesPath){
        ROOT_DIRECTORY = "target/classes" + localFilesPath;
    }
    
    /**
     * Starts the HTTP server.
     * This is a convenience method that calls runServer().
     * 
     * @param args command line arguments passed to runServer()
     * @throws IOException if an I/O error occurs during server operation
     * @throws URISyntaxException if there's an error parsing request URIs
     */
    public static void start(String[] args) throws IOException, URISyntaxException{
        runServer(args);
    }
    
    /**
     * Invokes a registered REST service for the given URI.
     * Creates HttpRequest and HttpResponse objects and passes them to the service.
     * Returns a properly formatted HTTP response string.
     * 
     * @param uri the request URI containing the path and query parameters
     * @return a complete HTTP response string with headers and body, or a 404 error if service not found
     */
    public static String invokeService(URI uri){
        String key = uri.getPath();
        System.out.println("Invoking service for path: " + key);
        Method s = services.get(key);
        
        if (s != null) {
            try {
                // Get parameters for this method
                Parameter[] parameters = s.getParameters();
                Object[] args = new Object[parameters.length];
                
                // Parse query parameters
                HttpRequest httpRequest = new HttpRequest(uri);
                
                for (int i = 0; i < parameters.length; i++) {
                    Parameter p = parameters[i];
                    if (p.isAnnotationPresent(RequestParam.class)) {
                        RequestParam param = p.getAnnotation(RequestParam.class);
                        String value = httpRequest.getValue(param.value());
                        // Use defaultValue if parameter is not provided
                        if (value == null || value.isEmpty()) {
                            value = param.defaultValue();
                        }
                        args[i] = value;
                    } else {
                        // For non-annotated parameters, pass null
                        args[i] = null;
                    }
                }
                
                // Invoke the method
                Object result = s.invoke(null, args);
                
                // Return HTTP response
                return "HTTP/1.1 200 OK\r\n"
                        + "content-type: text/plain; charset=utf-8\r\n"
                        + "\r\n" + result.toString();
                        
            } catch (IllegalAccessException ex) {
                Logger.getLogger(HttpServer.class.getName()).log(Level.SEVERE, null, ex);
                return "HTTP/1.1 500 Internal Server Error\r\n"
                        + "content-type: text/plain; charset=utf-8\r\n"
                        + "\r\n" + "Internal Server Error: " + ex.getMessage();
            } catch (InvocationTargetException ex) {
                Logger.getLogger(HttpServer.class.getName()).log(Level.SEVERE, null, ex);
                return "HTTP/1.1 500 Internal Server Error\r\n"
                        + "content-type: text/plain; charset=utf-8\r\n"
                        + "\r\n" + "Internal Server Error: " + ex.getTargetException().getMessage();
            }
        }
        
        return "HTTP/1.1 404 Not Found\r\n" + "content-type: text/plain; charset=utf-8\r\n"
               + "\r\n" + "Service not found";
    }

    public static void loadComponents(String[] args) {
        try {
            List<Class<?>> classes = ComponentScanner.scanForControllers("co.edu.escuelaing.microspringboot");
            for (Class<?> cl : classes){
                loadComponent(cl);
            }
        } catch (ClassNotFoundException | IOException ex) {
            Logger.getLogger(HttpServer.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    static void loadComponent(Class<?> c){
        if(!c.isAnnotationPresent(RestController.class)) {
            return;
        }
        Method[] methods = c.getDeclaredMethods();
        for(Method m : methods){
            if(!m.isAnnotationPresent(GetMapping.class)){
                continue;
            }
            String mapping = m.getAnnotation(GetMapping.class).value();
            System.out.println(mapping);
            services.put(mapping, m);
            checkMethodParameters(m, mapping);
        }
    }
    private static void checkMethodParameters(Method method, String mapping) {
        Parameter[] params = method.getParameters();
        for (Parameter p : params) {
            if (p.isAnnotationPresent(RequestParam.class)) {
                requests.computeIfAbsent(mapping, l -> new ArrayList<>()).add(p);
            }
        }
    }
    /**
     * Handles a client connection in a separate thread.
     * This method processes the HTTP request and sends the response.
     */
    private static void handleClient(Socket clientSocket) throws IOException, URISyntaxException {
        PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);
        BufferedReader in = new BufferedReader(
                new InputStreamReader(clientSocket.getInputStream()));
        
        String inputLine;
        boolean firstLine = true;
        URI requri = null;
        
        while ((inputLine = in.readLine()) != null) {
            if (firstLine) {
                requri = new URI(inputLine.split(" ")[1]);
                System.out.println("Path: " + requri.getPath() + " - Thread: " + Thread.currentThread().getName());
                firstLine = false;
            }
            System.out.println("Received: " + inputLine);
            if (!in.ready()) {
                break;
            }
        }
        
        try {
            handleRequest(requri, out, clientSocket);
        } catch (Exception ex) {
            Logger.getLogger(HttpServer.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        out.close();
        in.close();
        clientSocket.close();
    }
    /**
     * Registers a shutdown hook to gracefully shut down the server.
     * This hook will be called when the JVM is shutting down (Ctrl+C, System.exit, etc.)
     */
    private static void registerShutdownHook() {
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            // Set running flag to false to stop accepting new connections
            running = false;
            
            // Shutdown thread pool gracefully
            if (executor != null) {
                executor.shutdown();
                
                try {
                    // Wait for existing tasks to complete (max 30 seconds)
                    if (!executor.awaitTermination(30, TimeUnit.SECONDS)) {
                        executor.shutdownNow();
                        
                        // Wait a bit more for forced shutdown
                        if (!executor.awaitTermination(5, TimeUnit.SECONDS)) {
                            // Thread pool did not terminate even after forced shutdown
                        }
                    }
                } catch (InterruptedException e) {
                    executor.shutdownNow();
                    Thread.currentThread().interrupt();
                }
            }
        }, "HttpServer-ShutdownHook"));
    }
    
    /**
     * Gets the port number from environment variable or returns default.
     * @return the port number to use for the server
     */
    private static int getPort() {
        String portEnv = System.getenv("PORT");
        if (portEnv != null) {
            try {
                return Integer.parseInt(portEnv);
            } catch (NumberFormatException e) {
                System.err.println("Variable PORT is not a number using default value 35000.");
            }
        }
        return 35000;
    }

    public static void main(String[] args) throws IOException, URISyntaxException  {
        runServer(args);
    }
}
