/*
 * Copyright (c) 2016, WSO2 Inc. (http://wso2.com) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.example.service;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.net.ssl.*;
import javax.ws.rs.*;
import javax.ws.rs.core.Response;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.Key;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.X509Certificate;
import java.sql.*;
import java.util.Base64;
import java.util.HashMap;

@Path("/activate")
public class MyService {

    private static Logger log = LoggerFactory.getLogger(MyService.class);


    @GET
    @Path("/{license}")
    @Produces("application/json")
    public Response post(@PathParam("license") String license) {
        skipCertErrors();

        log.info("License: " + license);

        // Decode license key and find out info about requested APIs
        log.info("Decoded: " + new String(Base64.getDecoder().decode(license.getBytes())));

        String[] requestedAPIs = (new String(Base64.getDecoder().decode(license.getBytes()))).split(",");

        log.info("Requested APIs");
        for (String api : requestedAPIs) {
            log.info("Key: " + api);
        }

        // Get APIs from DB
        HashMap<String, String> apiKeys = new HashMap<String, String>();
        Connection conn = null;
        Statement stmt = null;
        ResultSet rs = null;
        try {
            conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/scg?user=scg&password=scg");
            stmt = conn.createStatement();
            rs = stmt.executeQuery("SELECT * FROM apis");

            while (rs.next()) {
                apiKeys.put(rs.getString(3), rs.getString(2));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            // some housekeeping
            try {
                if (rs != null) {
                        rs.close();
                }
                if (stmt != null) {
                        stmt.close();
                }
                if (conn != null) {
                    conn.close();
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

        log.info("Need to create a subscription for following APIs");
        for (String api : requestedAPIs) {
            log.info(apiKeys.get(api));
        }

        Gson gson = new GsonBuilder().create();
        HttpsURLConnection connection = null;
        try {

            // Client registration before calling the API
            URL registerClient = new URL("http://localhost:9763/client-registration/v0.11/register");
            ClientRegistration registrationRequest = ClientRegistration.newInstance();

            HttpURLConnection regCon = (HttpURLConnection) registerClient.openConnection();
            regCon.setRequestMethod("POST");
            regCon.setRequestProperty("Authorization", "Basic YWRtaW46YWRtaW4=");
            regCon.setRequestProperty("Content-Type", "application/json");
            regCon.setDoOutput(true);
            regCon.setRequestProperty("Content-Length", gson.toJson(registrationRequest).getBytes().length + "");

            OutputStreamWriter wr = new OutputStreamWriter(regCon.getOutputStream());
            wr.write(gson.toJson(registrationRequest).toString());
            wr.flush();

            // We need clientId and secret
            RegistrationResponse registrationResponse = gson.fromJson(
                    new InputStreamReader(regCon.getInputStream()), RegistrationResponse.class);
            log.info(registrationResponse.getClientId() + ", " + registrationResponse.getClientSecret());
            wr.close();

            // Get All available APIs
            URL getAllAPIs = new URL("https://127.0.0.1:9443/api/am/store/v0.11/apis");
            connection = (HttpsURLConnection) getAllAPIs.openConnection();
            connection.setRequestMethod("GET");
            connection.setRequestProperty("Content-Type", "application/json");
            connection.setDoOutput(true);
            connection.getResponseCode();

            ApiList apis = gson.fromJson(new InputStreamReader(connection.getInputStream()), ApiList.class);

            log.info("All APIs available locally");
            for (ApiInfo api : apis.getList()) {
                log.info(api.getName() + ", " + api.getId());
            }

            // Get token to get default application Id
            URL tokenAPI = new URL("https://127.0.0.1:8243/token");
            HttpsURLConnection tokCon = (HttpsURLConnection) tokenAPI.openConnection();
            tokCon.setRequestMethod("POST");

            String authHeader = registrationResponse.getClientId() + ":" +registrationResponse.getClientSecret();
            tokCon.setRequestProperty("Authorization",
                    "Basic " + Base64.getEncoder().encodeToString(authHeader.getBytes())
            );
            String getTokenParams = "grant_type=password&username=admin&password=admin&scope=apim:subscribe";
            tokCon.setRequestProperty("Content-Length", getTokenParams.length() + "");
            tokCon.setDoOutput(true);

            wr = new OutputStreamWriter(tokCon.getOutputStream());
            wr.write(getTokenParams);
            wr.flush();

            Token token = gson.fromJson(new InputStreamReader(tokCon.getInputStream()), Token.class);

            log.info("Access Token: " + token.getAccess_token());

            // Get default application id
            URL getAllApps = new URL("https://127.0.0.1:9443/api/am/store/v0.11/applications");
            HttpsURLConnection appsCon = (HttpsURLConnection) getAllApps.openConnection();
            appsCon.setRequestMethod("GET");
            appsCon.setRequestProperty("Authorization", "Bearer " + token.getAccess_token());
            appsCon.setDoOutput(true);
            appsCon.getResponseCode();

            ApplicationList appList = gson.fromJson(
                    new InputStreamReader(appsCon.getInputStream()), ApplicationList.class);

            String defaultAppID = "";
            for (ApplicationInfo appInfo : appList.getList()) {
                if (appInfo.getName().equals("DefaultApplication")) {
                    defaultAppID = appInfo.getApplicationId();
                }
                log.info("App Info >> " + appInfo.getApplicationId() + ", " + appInfo.getName());
            }

            // Subscribe to APIs using the above application
            log.info("Subscribing to APIs");
            for (String rid : requestedAPIs) {

                URL subscribeURL = new URL("https://127.0.0.1:9443/api/am/store/v0.11/subscriptions");
                HttpsURLConnection subCon = (HttpsURLConnection) subscribeURL.openConnection();
                subCon.setRequestMethod("POST");
                subCon.setRequestProperty("Authorization", "Bearer " + token.getAccess_token());
                subCon.setDoOutput(true);

                SubscriptionRequest subscriptionRequest = SubscriptionRequest.newInstance(
                        "Gold", apis.getApiID(apiKeys.get(rid)), defaultAppID);

                subCon.setRequestProperty("Content-Type", "application/json");
                subCon.setRequestProperty("Content-Length", gson.toJson(subscriptionRequest).getBytes().length + "");

                wr = new OutputStreamWriter(subCon.getOutputStream());
                wr.write(gson.toJson(subscriptionRequest).toString());
                wr.flush();

                // Subscription response
                StringWriter writer = new StringWriter();
                IOUtils.copy(subCon.getInputStream(), writer, "UTF-8");
                log.info("Subscription response: " + writer.toString());
            }

            // Generate access token and send
            URL genToken = new URL(
                    "https://127.0.0.1:9443/api/am/store/v0.11/applications/generate-keys?applicationId="
                            + defaultAppID);
            HttpsURLConnection genTokCon = (HttpsURLConnection) genToken.openConnection();
            genTokCon.setRequestMethod("POST");
            genTokCon.setRequestProperty("Authorization", "Bearer " + token.getAccess_token());
            genTokCon.setDoOutput(true);

            KeyGenRequest keyGenRequest = KeyGenRequest.newInstance();
            genTokCon.setRequestProperty("Content-Type", "application/json");
            genTokCon.setRequestProperty("Content-Length", gson.toJson(keyGenRequest).getBytes().length + "");


            wr = new OutputStreamWriter(genTokCon.getOutputStream());
            wr.write(gson.toJson(keyGenRequest).toString());
            wr.flush();

            KeyGenResponse keyGenResponse = gson.fromJson(
                    new InputStreamReader(genTokCon.getInputStream()), KeyGenResponse.class);
            log.info("Access token: " + keyGenResponse.getToken().getAccessToken());

            return Response.status(Response.Status.OK)
                    .header("Access-Control-Allow-Origin", "*")
                    .entity(keyGenResponse)
                    .build();


        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }

    @GET
    @Produces("application/json")
    @Path("/playerinfo")
    public PlayerInfo getPlayerInfo() {
        return PlayerInfo.newInstance();
    }

    private void skipCertErrors() {
        // Skipping self signed cert validation
        TrustManager[] trustAllCerts = new TrustManager[] {
                new X509TrustManager() {
                    public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                        return new X509Certificate[0];
                    }
                    public void checkClientTrusted(
                            java.security.cert.X509Certificate[] certs, String authType) {
                    }
                    public void checkServerTrusted(
                            java.security.cert.X509Certificate[] certs, String authType) {
                    }
                }
        };
        SSLContext sc = null;
        try {
            sc = SSLContext.getInstance("SSL");
            sc.init(null, trustAllCerts, new java.security.SecureRandom());
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (KeyManagementException e) {
            e.printStackTrace();
        }
        HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
        HostnameVerifier allHostsValid = new HostnameVerifier() {
            public boolean verify(String hostname, SSLSession session) {
                return true;
            }
        };
        HttpsURLConnection.setDefaultHostnameVerifier(allHostsValid);
    }
}
