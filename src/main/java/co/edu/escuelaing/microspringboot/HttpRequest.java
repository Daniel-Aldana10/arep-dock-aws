/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package co.edu.escuelaing.microspringboot;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;

/**
 * Represents an HTTP request with parsed query parameters.
 * This class wraps a URI and provides convenient access to query parameters
 * extracted from the request URL.
 * 
 * @author daniel.aldana-b
 */
public class HttpRequest {
    /** The original URI of the request */
    private URI requri = null;
    /** Map containing parsed query parameters (key-value pairs) */
    private Map<String, String> queryParams = new HashMap<>();
    
    /**
     * Constructs a new HttpRequest with the specified URI.
     * Automatically parses query parameters from the URI's query string.
     * 
     * @param uri the URI containing the request path and query parameters
     */
    public HttpRequest(URI uri) {
        this.requri = uri;
        parseQueryParams();
    }
    
    /**
     * Parses query parameters from the URI's query string.
     * Extracts key-value pairs from the query string and stores them in the queryParams map.
     * Query parameters should be in the format "key1=value1&key2=value2".
     */
    private void parseQueryParams() {
        if (requri != null && requri.getQuery() != null) {
            String query = requri.getQuery();
            String[] pairs = query.split("&");
            for (String pair : pairs) {
                String[] keyValue = pair.split("=");
                if (keyValue.length == 2) {
                    queryParams.put(keyValue[0], keyValue[1]);
                }
            }
        }
    }
    
    /**
     * Retrieves the value of a query parameter by its name.
     * 
     * @param paramName the name of the query parameter to retrieve
     * @return the value of the parameter, or null if the parameter doesn't exist
     */
    public String getValue(String paramName) {
        return queryParams.get(paramName);
    }
    
    /**
     * Retrieves the value of a query parameter by its name.
     * This method is an alias for getValue() and provides the same functionality.
     * @param paramName the name of the query parameter to retrieve
     * @return the value of the parameter, or null if the parameter doesn't exist
     */
    public String getValues(String paramName) {
        return queryParams.get(paramName);
    }
    
    /**
     * Retrieves the path component of the request URI.
     * Returns the path without query parameters or fragment.
     * @return the path component of the URI, or an empty string if the URI is null
     */
    public String getPath() {
        return requri != null ? requri.getPath() : "";
    }
}
