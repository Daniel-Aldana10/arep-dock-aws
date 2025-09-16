/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package co.edu.escuelaing.microspringboot;      

/**
 *
 * @author daniel.aldana-b
 */
@RestController
public class GreetingController {
    private static final String template = "Hello, %s!";
    
    @GetMapping("/greeting")
    public static String greeting(@RequestParam(value="name", defaultValue="World") String name){
        return "Hola " + name;
    }
}
