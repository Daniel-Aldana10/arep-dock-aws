/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package co.edu.escuelaing.microspringboot;

/**
 *
 * @author daniel.aldana-b
 */
public interface Service {
    public String invoke(HttpRequest req, HttpResponse res);
}
