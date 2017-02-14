package org.example.service;

public class KeyGenToken {
    private int validityTime;

    public int getValidityTime() {
        return validityTime;
    }

    public String getAccessToken() {
        return accessToken;
    }

    private String accessToken;
}
