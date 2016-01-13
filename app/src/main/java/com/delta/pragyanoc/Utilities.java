package com.delta.pragyanoc;

/**
 * Created by rb on 12/1/16.
 */
public class Utilities {
    private static String GOOGLE_PROJ_ID = "128273290200";
    private static String GCM_URL = "http://api.pragyan.org/oc/gcm/register";
    private static String LOGIN_URL = "http://api.pragyan.org/oc/login";
    private static String PROFILE_URL = "http://api.pragyan.org/oc/profile/getdetails";
    private static String ALL_PROFILE_URL = "http://api.pragyan.org/oc/profile/getalldetails";
    private static String ALL_TASKS_URL = "http://api.pragyan.org/oc/task/all";
    public static String LOGGING = "LOGGING";

    public static String getAllTasksUrl() {
        return ALL_TASKS_URL;
    }

    public static void setAllTasksUrl(String allTasksUrl) {
        ALL_TASKS_URL = allTasksUrl;
    }

    public static String getAllProfileUrl() {
        return ALL_PROFILE_URL;
    }

    public static void setAllProfileUrl(String allProfileUrl) {
        ALL_PROFILE_URL = allProfileUrl;
    }

    public static String getProfileUrl() {
        return PROFILE_URL;
    }

    public static void setProfileUrl(String profileUrl) {
        PROFILE_URL = profileUrl;
    }

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
