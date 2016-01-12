package com.delta.pragyanoc;

/**
 * Created by rb on 12/1/16.
 */
public class Utilities {
    private static String GOOGLE_PROJ_ID = "128273290200";
    private static String GCM_URL = "http://api.pragyan.org/oc/gcm/register";
    private static String LOGIN_URL = "https://api.pragyan.org/oc/login";
    public static String LOGGING = "LOGGING";
    public static String getGoogleProjId() {
        return GOOGLE_PROJ_ID;
    }

    public static void setGoogleProjId(String googleProjId) {
        GOOGLE_PROJ_ID = googleProjId;
    }

    public static String getGcmUrl() {
        return GCM_URL;
    }

    public static void setGcmUrl(String gcmUrl) {
        GCM_URL = gcmUrl;
    }

    public static String getLoginUrl() {
        return LOGIN_URL;
    }

    public static void setLoginUrl(String loginUrl) {
        LOGIN_URL = loginUrl;
    }
}
