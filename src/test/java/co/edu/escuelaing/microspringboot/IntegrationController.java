package co.edu.escuelaing.microspringboot;

@RestController
public class IntegrationController {
    @GetMapping("/hello")
    public static String hello() {
        return "Hello World!";
    }

    @GetMapping("/greeting")
    public static String greeting(@RequestParam(value = "name", defaultValue = "World") String name) {
        return "Hello " + name;
    }

    @GetMapping("/user")
    public static String user(@RequestParam("id") String id, @RequestParam("role") String role) {
        return "User ID: " + id + ", Role: " + role;
    }

    @GetMapping("/math")
    public static String math(@RequestParam(value = "a", defaultValue = "0") String a,
                              @RequestParam(value = "b", defaultValue = "0") String b) {
        try {
            int numA = Integer.parseInt(a);
            int numB = Integer.parseInt(b);
            return "Result: " + (numA + numB);
        } catch (NumberFormatException e) {
            return "Invalid numbers";
        }
    }
}