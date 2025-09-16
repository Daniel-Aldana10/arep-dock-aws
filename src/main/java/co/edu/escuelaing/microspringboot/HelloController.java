/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package co.edu.escuelaing.microspringboot;

/**
 *
 * @author daniel.aldana-b
 */
@RestController
public class HelloController {

	@GetMapping("/hello")
	public static String index() {
		return "Greetings from Spring Boot!";
	}
}
