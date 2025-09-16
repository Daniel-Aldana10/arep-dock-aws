/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package co.edu.escuelaing.microspringboot;

/**
 * Represents an HTTP response with configurable status code, status message, and content type.
 * This class provides a way to configure HTTP response properties before sending the response
 * to the client.
 * 
 * @author daniel.aldana-b
 */
public class HttpResponse {
    //The MIME type and character encoding of the response body
    private String contentType = "text/plain; charset=utf-8";
    // The HTTP status code (e.g., 200, 404, 500)
    private int statusCode = 200;
    // The HTTP status message (e.g., "OK", "Not Found", "Internal Server Error")
    private String statusMessage = "OK";
    
    /**
     * Sets the content type of the HTTP response.
     * The content type specifies the MIME type and character encoding of the response body.
     * @param contentType the MIME type and optional character encoding
     */
    public void setContentType(String contentType) {
        this.contentType = contentType;
    }
    
    /**
     * Sets the HTTP status code of the response.
     * Common status codes
     * @param statusCode the HTTP status code to set
     */
    public void setStatusCode(int statusCode) {
        this.statusCode = statusCode;
    }
    
    /**
     * Sets the HTTP status message of the response.
     * The status message provides a human-readable description of the status code.
     * @param statusMessage the HTTP status message to set
     */
    public void setStatusMessage(String statusMessage) {
        this.statusMessage = statusMessage;
    }
    
    /**
     * Gets the current content type of the HTTP response.
     * @return the MIME type and character encoding of the response
     */
    public String getContentType() {
        return contentType;
    }
    
    /**
     * Gets the current HTTP status code of the response.
     * @return the HTTP status code
     */
    public int getStatusCode() {
        return statusCode;
    }
    
    /**
     * Gets the current HTTP status message of the response.
     * 
     * @return the HTTP status message
     */
    public String getStatusMessage() {
        return statusMessage;
    }
}
