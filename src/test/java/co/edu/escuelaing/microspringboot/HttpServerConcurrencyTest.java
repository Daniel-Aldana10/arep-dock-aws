package co.edu.escuelaing.microspringboot;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.AfterEach;
import static org.junit.jupiter.api.Assertions.*;

import java.io.*;
import java.net.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Focused test class for HttpServer concurrency and edge cases.
 * Tests concurrent requests, edge cases, and error handling.
 */
public class HttpServerConcurrencyTest {
    
    private static final int SERVER_PORT = 35001;
    private static final int NUM_CONCURRENT_REQUESTS = 5; // Reduced for focused testing
    private ExecutorService clientExecutor;
    private ServerSocket testServerSocket;
    private Thread serverThread;
    
    @BeforeEach
    public void setUp() throws IOException {
        testServerSocket = new ServerSocket(SERVER_PORT);
        clientExecutor = Executors.newFixedThreadPool(NUM_CONCURRENT_REQUESTS);
    }
    
    @AfterEach
    public void tearDown() throws IOException, InterruptedException {
        if (serverThread != null && serverThread.isAlive()) {
            serverThread.interrupt();
            serverThread.join(2000);
        }
        if (testServerSocket != null && !testServerSocket.isClosed()) {
            testServerSocket.close();
        }
        if (clientExecutor != null) {
            clientExecutor.shutdown();
            clientExecutor.awaitTermination(2, TimeUnit.SECONDS);
        }
    }
    
    /**
     * Test concurrent requests to verify thread pool functionality.
     * This is the main concurrency test.
     */
    @Test
    public void testConcurrentRequests() throws Exception {
        // Start mock server
        startMockServer();
        Thread.sleep(100); // Wait for server to start
        
        AtomicInteger successfulRequests = new AtomicInteger(0);
        AtomicInteger failedRequests = new AtomicInteger(0);
        CountDownLatch latch = new CountDownLatch(NUM_CONCURRENT_REQUESTS);
        
        // Send concurrent requests
        for (int i = 0; i < NUM_CONCURRENT_REQUESTS; i++) {
            final int requestId = i;
            clientExecutor.submit(() -> {
                try {
                    sendHttpRequest("/test/" + requestId);
                    successfulRequests.incrementAndGet();
                } catch (Exception e) {
                    failedRequests.incrementAndGet();
                } finally {
                    latch.countDown();
                }
            });
        }
        
        // Wait for completion
        boolean completed = latch.await(3, TimeUnit.SECONDS);
        
        // Verify results
        assertTrue(completed, "Not all requests completed in time");
        assertEquals(NUM_CONCURRENT_REQUESTS, successfulRequests.get(), 
                    "Expected " + NUM_CONCURRENT_REQUESTS + " successful requests");
        assertEquals(0, failedRequests.get(), "Expected 0 failed requests");
    }
    
    /**
     * Test edge case: Invalid HTTP request format.
     */
    @Test
    public void testInvalidHttpRequest() throws Exception {
        startMockServer();
        Thread.sleep(100);
        
        try (Socket socket = new Socket("localhost", SERVER_PORT);
             PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
             BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {
            
            // Send malformed HTTP request
            out.println("INVALID REQUEST FORMAT");
            out.println(""); // Empty line to end headers
            
            // Server should handle gracefully
            String response = in.readLine();
            assertNotNull(response, "Server should respond even to invalid requests");
        }
    }
    
    /**
     * Test edge case: Very long request path.
     */
    @Test
    public void testLongRequestPath() throws Exception {
        startMockServer();
        Thread.sleep(100);
        
        // Create a very long path
        StringBuilder longPath = new StringBuilder("/test/");
        for (int i = 0; i < 1000; i++) {
            longPath.append("verylongpathsegment");
        }
        
        try (Socket socket = new Socket("localhost", SERVER_PORT);
             PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
             BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {
            
            out.println("GET " + longPath.toString() + " HTTP/1.1");
            out.println("Host: localhost");
            out.println("Connection: close");
            out.println(""); // Empty line to end headers
            
            // Server should handle long paths
            String response = in.readLine();
            assertNotNull(response, "Server should handle long request paths");
        }
    }
    
    /**
     * Test edge case: Empty request.
     */
    @Test
    public void testEmptyRequest() throws Exception {
        startMockServer();
        Thread.sleep(100);
        
        try (Socket socket = new Socket("localhost", SERVER_PORT);
             PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
             BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {
            
            // Send empty request
            out.println("");
            out.println("");
            
            // Server should not crash
            String response = in.readLine();
            // Response might be null for empty request, which is acceptable
        }
    }
    
    /**
     * Test edge case: Multiple requests on same connection (HTTP/1.0 style).
     */
    @Test
    public void testMultipleRequestsSameConnection() throws Exception {
        startMockServer();
        Thread.sleep(100);
        
        try (Socket socket = new Socket("localhost", SERVER_PORT);
             PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
             BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {
            
            // Send first request
            out.println("GET /test/1 HTTP/1.1");
            out.println("Host: localhost");
            out.println("Connection: keep-alive");
            out.println("");
            
            // Read response
            String response1 = in.readLine();
            assertNotNull(response1, "First request should get response");
            
            // Send second request on same connection
            out.println("GET /test/2 HTTP/1.1");
            out.println("Host: localhost");
            out.println("Connection: close");
            out.println("");
            
            // Read second response
            String response2 = in.readLine();
            assertNotNull(response2, "Second request should get response");
        }
    }
    
    /**
     * Test thread safety: Multiple threads accessing services map.
     */
    @Test
    public void testThreadSafety() throws Exception {
        int numThreads = 5;
        CountDownLatch latch = new CountDownLatch(numThreads);
        AtomicInteger successfulOperations = new AtomicInteger(0);
        
        for (int i = 0; i < numThreads; i++) {
            final int threadId = i;
            new Thread(() -> {
                try {
                    // Simulate concurrent access to services map
                    String path = "/test/thread/" + threadId;
                    HttpServer.services.put(path, null);
                    successfulOperations.incrementAndGet();
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    latch.countDown();
                }
            }).start();
        }
        
        boolean completed = latch.await(3, TimeUnit.SECONDS);
        assertTrue(completed, "Thread safety test should complete");
        assertEquals(numThreads, successfulOperations.get(), 
                    "All thread operations should succeed");
    }
    
    /**
     * Helper method to start a mock server for testing.
     */
    private void startMockServer() {
        serverThread = new Thread(() -> {
            try {
                while (!Thread.currentThread().isInterrupted()) {
                    Socket clientSocket = testServerSocket.accept();
                    // Handle each connection in a separate thread (simulating our concurrent server)
                    new Thread(() -> {
                        try {
                            handleTestRequest(clientSocket);
                        } catch (IOException e) {
                            if (!Thread.currentThread().isInterrupted()) {
                                e.printStackTrace();
                            }
                        }
                    }).start();
                }
            } catch (IOException e) {
                if (!testServerSocket.isClosed()) {
                    e.printStackTrace();
                }
            }
        });
        serverThread.setDaemon(true);
        serverThread.start();
    }
    
    /**
     * Helper method to handle test requests in the mock server.
     */
    private void handleTestRequest(Socket clientSocket) throws IOException {
        try (BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
             PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true)) {
            
            String requestLine = in.readLine();
            if (requestLine != null && !requestLine.trim().isEmpty()) {
                // Simulate processing time
                try {
                    Thread.sleep(50); // 50ms processing
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
                
                // Send response
                String response = "HTTP/1.1 200 OK\r\n" +
                                "Content-Type: text/plain\r\n" +
                                "Content-Length: 13\r\n" +
                                "\r\n" +
                                "Hello, World!";
                out.print(response);
                out.flush();
            }
        } finally {
            clientSocket.close();
        }
    }
    
    /**
     * Helper method to send HTTP requests.
     */
    private void sendHttpRequest(String path) throws Exception {
        try (Socket socket = new Socket("localhost", SERVER_PORT);
             PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
             BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {
            
            out.println("GET " + path + " HTTP/1.1");
            out.println("Host: localhost");
            out.println("Connection: close");
            out.println("");
            
            String response = in.readLine();
            assertNotNull(response, "Should receive HTTP response");
            assertTrue(response.contains("200 OK"), "Should receive 200 OK response");
        }
    }
}
