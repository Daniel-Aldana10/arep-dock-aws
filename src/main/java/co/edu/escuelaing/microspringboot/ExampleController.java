package co.edu.escuelaing.microspringboot;


import java.util.HashMap;
import java.util.Map;

@RestController
public class ExampleController {
    private static final Map<String, String> users = new HashMap<>();
    @GetMapping("/user")
    public static String info(@RequestParam("name") String name,
                             @RequestParam(value = "age", defaultValue = "0") String age) {
        users.put(name, age);
        return "Hello " + name  + ", you are " + age + " years old";
    }
    @GetMapping("/userInfo")
    public static String getUser(@RequestParam("name") String name) {
        String age = users.get(name);
        return (age != null) ? "User " + name + " retrieved value: age is " + age : "User data not found for " + name;
    }
}
