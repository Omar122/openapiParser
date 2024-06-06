/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.omar.openapiparser1;

/**
 *
 * @author oalfuraydi
 */
public class Request {
    
    String request;
    String requestHeader;
    String url;
    String verb;

    public Request(String request, String requestHeader, String url, String verb) {
        this.request = request;
        this.requestHeader = requestHeader;
        this.url = url;
        this.verb = verb;
    }

    
    
    public String getVerrb() {
        return verb;
    }

    public void setVerrb(String verrb) {
        this.verb = verrb;
    }

  
    public String getRequest() {
        return request;
    }

    public void setRequest(String request) {
        this.request = request;
    }

    public String getRequestHeader() {
        return requestHeader;
    }

    public void setRequestHeader(String requestHeader) {
        this.requestHeader = requestHeader;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    @Override
    public String toString() {
        return "Request{" + "request=" + request + ", requestHeader=" + requestHeader + ", url=" + url + ", verb=" + verb + '}';
    }
    
    
    
}
