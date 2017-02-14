package org.example.service;

public class RegistrationResponse {
    private String clientId;
    private String clientName;
    private String callBackURL;
    private String clientSecret;
    private String isSaasApplication;
    private String appOwner;
    private String jsonString;

    public String getClientId() {
        return clientId;
    }

    public String getClientName() {
        return clientName;
    }

    public String getCallBackURL() {
        return callBackURL;
    }

    public String getClientSecret() {
        return clientSecret;
    }

    public String getIsSaasApplication() {
        return isSaasApplication;
    }

    public String getAppOwner() {
        return appOwner;
    }

    public String getJsonString() {
        return jsonString;
    }
}
