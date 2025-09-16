package co.edu.escuelaing.microspringboot;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.io.TempDir;
import static org.junit.jupiter.api.Assertions.*;

import java.net.URI;
import java.net.URISyntaxException;
import java.lang.reflect.Method;
import java.nio.file.Path;
import java.nio.file.Files;
import java.io.IOException;
import java.util.Map;

public class HttpServerTest {
    
    @RestController
    public static class TestController {
        @GetMapping("/test")
        public static String testMethod() {
            return "test response";
        }
        
        @GetMapping("/greeting")
        public static String greeting(@RequestParam(value = "name", defaultValue = "World") String name) {
            return "Hello " + name;
        }
        
        @GetMapping("/params")
        public static String withParams(@RequestParam("id") String id, @RequestParam("type") String type) {
            return "ID: " + id + ", Type: " + type;
        }
        
        @GetMapping("/error")
        public static String errorMethod() {
            throw new RuntimeException("Test error");
        }
    }
    
    @TempDir
    Path tempDir;
    
    @BeforeEach
    void setUp() {
        // Limpiar servicios antes de cada test
        HttpServer.services.clear();
        HttpServer.requests.clear();
        
        // Restaurar directorio por defecto
        HttpServer.ROOT_DIRECTORY = "target/classes/webroot";
    }
    
    @AfterEach
    void tearDown() {
        // Limpiar después de cada test
        HttpServer.services.clear();
        HttpServer.requests.clear();
    }
    
    // ========== TESTS DE REGISTRO DE SERVICIOS ==========
    
    @Test
    void testServiceRegistration() {
        // Test básico de registro de servicios
        Method testMethod = null;
        try {
            testMethod = TestController.class.getMethod("testMethod");
        } catch (NoSuchMethodException e) {
            fail("Method should exist");
        }
        
        HttpServer.get("/test", testMethod);
        
        assertTrue(HttpServer.services.containsKey("/test"));
        assertEquals(testMethod, HttpServer.services.get("/test"));
    }
    
    // ========== TESTS DE ARCHIVOS ESTÁTICOS ==========
    
    @Test
    void testStaticFilesPathSetting() {
        // Test que se puede cambiar el directorio de archivos estáticos
        String originalPath = HttpServer.ROOT_DIRECTORY;
        
        HttpServer.staticfiles("/custom");
        assertEquals("target/classes/custom", HttpServer.ROOT_DIRECTORY);
        
        // Restaurar el valor original
        HttpServer.ROOT_DIRECTORY = originalPath;
    }
    
    // ========== TESTS DE MIME TYPES ==========
    
    @Test
    void testGetTypeWithCommonFiles() {
        // Test que getType retorna MIME types correctos para archivos comunes
        assertEquals("text/html; charset=utf-8", HttpServer.getType(Path.of("test.html")));
        assertEquals("text/css; charset=utf-8", HttpServer.getType(Path.of("style.css")));
        assertEquals("application/javascript; charset=utf-8", HttpServer.getType(Path.of("script.js")));
        assertEquals("image/png", HttpServer.getType(Path.of("image.png")));
        assertEquals("image/jpeg", HttpServer.getType(Path.of("photo.jpg")));
    }
    
    @Test
    void testGetTypeEdgeCases() {
        // Test casos borde para getType
        assertEquals("application/octet-stream", HttpServer.getType(null));
        assertEquals("application/octet-stream", HttpServer.getType(Path.of("filename")));
        assertEquals("application/octet-stream", HttpServer.getType(Path.of("unknown.xyz")));
    }
    
    // ========== TESTS DE GREETING SERVICE ==========
    
    @Test
    void testGreetingServiceBasic() throws URISyntaxException {
        // Test básico de greetingService
        URI uri = new URI("/test?name=John");
        String response = HttpServer.greetingService(uri, false);
        
        assertTrue(response.contains("HTTP/1.1 200 OK"));
        assertTrue(response.contains("Hello John"));
    }
    
    @Test
    void testGreetingServiceWithTime() throws URISyntaxException {
        // Test que greetingService incluye la fecha cuando time=true
        URI uri = new URI("/test?name=John");
        String response = HttpServer.greetingService(uri, true);
        
        assertTrue(response.contains("HTTP/1.1 200 OK"));
        assertTrue(response.contains("Hello John"));
        assertTrue(response.contains("today's date is"));
    }
    
    @Test
    void testGreetingServiceErrorCases() throws URISyntaxException {
        // Test casos de error para greetingService
        URI uri1 = new URI("/test");
        String response1 = HttpServer.greetingService(uri1, false);
        assertTrue(response1.contains("HTTP/1.1 400 Bad Request"));
        
        URI uri2 = new URI("/test?");
        String response2 = HttpServer.greetingService(uri2, false);
        assertTrue(response2.contains("HTTP/1.1 400 Bad Request"));
    }
    
    // ========== TESTS DE CARGA DE COMPONENTES ==========
    
    @Test
    void testLoadComponent() {
        // Test que loadComponent carga controladores correctamente
        HttpServer.loadComponent(TestController.class);
        
        assertTrue(HttpServer.services.containsKey("/test"));
        assertTrue(HttpServer.services.containsKey("/greeting"));
        assertTrue(HttpServer.services.containsKey("/params"));
        assertTrue(HttpServer.services.containsKey("/error"));
    }

    // ========== TESTS DE INVOCACIÓN DE SERVICIOS ==========
    
    @Test
    void testInvokeServiceBasic() throws URISyntaxException {
        // Test básico de invokeService
        HttpServer.loadComponent(TestController.class);
        
        URI uri = new URI("/test");
        String response = HttpServer.invokeService(uri);
        
        assertTrue(response.contains("HTTP/1.1 200 OK"));
        assertTrue(response.contains("test response"));
    }
    
    @Test
    void testInvokeServiceWithParameters() throws URISyntaxException {
        // Test que invokeService funciona con parámetros
        HttpServer.loadComponent(TestController.class);
        
        URI uri = new URI("/greeting?name=John");
        String response = HttpServer.invokeService(uri);
        
        assertTrue(response.contains("HTTP/1.1 200 OK"));
        assertTrue(response.contains("Hello John"));
    }
    
    @Test
    void testInvokeServiceErrorCases() throws URISyntaxException {
        // Test casos de error para invokeService
        URI uri1 = new URI("/nonexistent");
        String response1 = HttpServer.invokeService(uri1);
        assertTrue(response1.contains("HTTP/1.1 404 Not Found"));
        
        HttpServer.loadComponent(TestController.class);
        URI uri2 = new URI("/error");
        String response2 = HttpServer.invokeService(uri2);
        assertTrue(response2.contains("HTTP/1.1 500 Internal Server Error"));
    }
}
