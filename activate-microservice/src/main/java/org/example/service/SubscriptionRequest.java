package org.example.service;

public class SubscriptionRequest {
    private String tier;
    private String apiIdentifier;
    private String applicationId;

    public String getTier() {
        return tier;
    }

    public void setTier(String tier) {
        this.tier = tier;
    }

    public String getApiIdentifier() {
        return apiIdentifier;
    }

    public void setApiIdentifier(String apiIdentifier) {
        this.apiIdentifier = apiIdentifier;
    }

    public String getApplicationId() {
        return applicationId;
    }

    public void setApplicationId(String applicationId) {
        this.applicationId = applicationId;
    }

    private SubscriptionRequest() {}

    public static SubscriptionRequest newInstance(String tier, String apiId, String appId) {
        SubscriptionRequest sr = new SubscriptionRequest();
        sr.setTier(tier);
        sr.setApiIdentifier(apiId);
        sr.setApplicationId(appId);
        return sr;
    }
}
