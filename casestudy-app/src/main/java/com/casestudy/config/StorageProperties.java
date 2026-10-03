package com.casestudy.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "casestudy")
public class StorageProperties {

    /**
     * in-memory | mysql | mysql-redis
     */
    private String storage = "in-memory";

    public String getStorage() {
        return storage;
    }

    public void setStorage(String storage) {
        this.storage = storage;
    }
}
