package com.cdancy.jenkins.rest.domain.job;

import javax.annotation.Generated;

import org.jclouds.javax.annotation.Nullable;

@Generated("com.google.auto.value.processor.AutoValueProcessor")
final class AutoValue_C7nBuildInfo extends C7nBuildInfo {

  @Nullable
  public String id;
  public final String status;
  public final long startTimeMillis;
  public final long durationTimeMillis;
  public final String username;
  public final String triggerType;


  public AutoValue_C7nBuildInfo(String id,
                                String status,
                                long startTimeMillis,
                                long durationTimeMillis,
                                String username,
                                String triggerType) {
    this.id = id;
    this.status = status;
    this.startTimeMillis = startTimeMillis;
    this.durationTimeMillis = durationTimeMillis;
    this.username = username;
    this.triggerType = triggerType;
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
}
