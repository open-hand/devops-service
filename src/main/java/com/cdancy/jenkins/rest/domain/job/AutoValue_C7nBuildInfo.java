package com.cdancy.jenkins.rest.domain.job;


import org.jclouds.javax.annotation.Nullable;

final class AutoValue_C7nBuildInfo extends C7nBuildInfo {

    public final String branch;
    public final String status;
    public final long startTimeMillis;
    public final long durationTimeMillis;
    public final String username;
    public final String triggerType;
    @Nullable
    public String id;


    public AutoValue_C7nBuildInfo(String id,
                                  String status,
                                  long startTimeMillis,
                                  long durationTimeMillis,
                                  String username,
                                  String triggerType,
                                  String branch) {
        this.id = id;
        this.status = status;
        this.startTimeMillis = startTimeMillis;
        this.durationTimeMillis = durationTimeMillis;
        this.username = username;
        this.triggerType = triggerType;
        this.branch = branch;
    }

    @Override
    public String id() {
        return id;
    }

    @Override
    public String status() {
        return status;
    }

    @Override
    @Nullable
    public long startTimeMillis() {
        return startTimeMillis;
    }

    @Override
    @Nullable
    public long durationTimeMillis() {
        return durationTimeMillis;
    }

    @Nullable
    public String username() {
        return username;
    }

    @Nullable
    public String triggerType() {
        return triggerType;
    }

    @Override
    public String branch() {
        return branch;
    }
}
