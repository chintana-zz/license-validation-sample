package org.example.service;

public class KeyGenRequest {
    private String validityTime;
    private String keyType;
    private String[] accessAllowDomains;

    private KeyGenRequest() {}

    public static KeyGenRequest newInstance() {
        KeyGenRequest kr = new KeyGenRequest();
        kr.validityTime = "3600000";
        kr.keyType = "PRODUCTION";
        kr.accessAllowDomains = new String[]{"ALL"};
        return kr;
    }
}
