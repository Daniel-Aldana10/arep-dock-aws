/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 */

package co.edu.escuelaing.microspringboot;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author curso
 */
public class MicroSpringBoot {

    public static void main(String[] args) {
        try {
            HttpServer.runServer(args);
        } catch (IOException ex) {
            Logger.getLogger(MicroSpringBoot.class.getName()).log(Level.SEVERE, null, ex);
        } catch (URISyntaxException ex) {
            Logger.getLogger(MicroSpringBoot.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
