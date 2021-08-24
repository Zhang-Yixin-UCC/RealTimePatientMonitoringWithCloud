package com.example.bulletinboard;

//Application configuration
//Stores the address of the server.
public class AppConfig {
//    change this address to the real server address
//    10.0.2.2 = localhost
    private final String serverUrl = "http://10.0.2.2:6122/";

    public String getServerUrl() {
        return serverUrl;
    }
}
