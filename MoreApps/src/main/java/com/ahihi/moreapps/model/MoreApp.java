package com.ahihi.moreapps.model;


public class MoreApp {
    private String urlImage;
    private String appId;
    private String appName;

    public MoreApp() {
    }


    public void setUrlImage(String urlImage) {
        this.urlImage = urlImage;
    }

    public void setAppId(String appId) {
        this.appId = appId;
    }

    public void setAppName(String appName) {
        this.appName = appName;
    }

    public String getUrlImage() {
        return urlImage;
    }

    public String getAppId() {
        return appId;
    }

    public String getAppName() {
        return appName;
    }
}
