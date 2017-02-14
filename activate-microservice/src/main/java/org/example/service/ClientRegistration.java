package org.example.service;

public class ClientRegistration {
    private String callbackUrl;
    private String clientName;
    private String tokenScope;
    private String owner;
    private String grantType;
    private boolean saasApp;

    private ClientRegistration(String callbackUrl, String clientName, String tokenScope, String owner, String grantType, boolean saasApp) {
        this.callbackUrl = callbackUrl;
        this.clientName = clientName;
        this.tokenScope = tokenScope;
        this.owner = owner;
        this.grantType = grantType;
        this.saasApp = saasApp;
    }

    public static ClientRegistration newInstance() {
        return new ClientRegistration(
                "localhost",
                "rest_api_store",
                "Production",
                "admin",
                "password refresh_token",
                true);
    }
}
