package co.edu.escuelaing.microspringboot;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.AfterEach;
import static org.junit.jupiter.api.Assertions.*;

import java.net.URI;
import java.net.URISyntaxException;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;

public class IntegrationControllerTest {
    

    @BeforeEach
    void setUp() {
        // Limpiar estado antes de cada test
        HttpServer.services.clear();
        HttpServer.requests.clear();
        
        // Cargar el controlador de integración
        HttpServer.loadComponent(IntegrationController.class);
    }
    
    @AfterEach
    void tearDown() {
        // Limpiar después de cada test
        HttpServer.services.clear();
        HttpServer.requests.clear();
    }
    
    @Test
    void testControllerLoading() {
        // Test que todos los endpoints del controlador se cargan correctamente
        assertTrue(HttpServer.services.containsKey("/hello"));
        assertTrue(HttpServer.services.containsKey("/greeting"));
        assertTrue(HttpServer.services.containsKey("/user"));
        assertTrue(HttpServer.services.containsKey("/math"));
        
        assertEquals(4, HttpServer.services.size());
    }
    
    @Test
    void testBasicEndpoints() throws URISyntaxException {
        // Test básico de endpoints principales
        URI uri1 = new URI("/hello");
        String response1 = HttpServer.invokeService(uri1);
        assertTrue(response1.contains("HTTP/1.1 200 OK"));
        assertTrue(response1.contains("Hello World!"));
        
        URI uri2 = new URI("/greeting?name=John");
        String response2 = HttpServer.invokeService(uri2);
        assertTrue(response2.contains("HTTP/1.1 200 OK"));
        assertTrue(response2.contains("Hello John"));
    }
    
    @Test
    void testParameterHandling() throws URISyntaxException {
        // Test manejo de parámetros en diferentes endpoints
        URI uri1 = new URI("/user?id=123&role=admin");
        String response1 = HttpServer.invokeService(uri1);
        assertTrue(response1.contains("HTTP/1.1 200 OK"));
        assertTrue(response1.contains("User ID: 123"));
        assertTrue(response1.contains("Role: admin"));
        
        URI uri2 = new URI("/math?a=5&b=3");
        String response2 = HttpServer.invokeService(uri2);
        assertTrue(response2.contains("HTTP/1.1 200 OK"));
        assertTrue(response2.contains("Result: 8"));
    }
    
    @Test
    void testEdgeCases() throws URISyntaxException {
        // Test casos borde
        URI uri1 = new URI("/math");
        String response1 = HttpServer.invokeService(uri1);
        assertTrue(response1.contains("HTTP/1.1 200 OK"));
        assertTrue(response1.contains("Result: 0"));
        
        URI uri2 = new URI("/math?a=abc&b=xyz");
        String response2 = HttpServer.invokeService(uri2);
        assertTrue(response2.contains("HTTP/1.1 200 OK"));
        assertTrue(response2.contains("Invalid numbers"));
    }
    
    @Test
    void testAnnotationsAndRegistration() {
        // Test que las anotaciones y registros funcionan correctamente
        assertTrue(IntegrationController.class.isAnnotationPresent(RestController.class));
        assertTrue(HttpServer.requests.containsKey("/greeting"));
        assertTrue(HttpServer.requests.containsKey("/user"));
        assertTrue(HttpServer.requests.containsKey("/math"));
        assertFalse(HttpServer.requests.containsKey("/hello"));
    }
    
    @Test
    void testErrorHandling() throws URISyntaxException {
        // Test manejo de errores
        URI uri = new URI("/nonexistent");
        String response = HttpServer.invokeService(uri);
        
        assertTrue(response.contains("HTTP/1.1 404 Not Found"));
        assertTrue(response.contains("Service not found"));
    }
    
   
}
