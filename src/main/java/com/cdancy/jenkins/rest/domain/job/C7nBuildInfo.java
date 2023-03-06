
package com.cdancy.jenkins.rest.domain.job;

import com.google.auto.value.AutoValue;
import org.jclouds.javax.annotation.Nullable;
import org.jclouds.json.SerializedNames;

@AutoValue
public abstract class C7nBuildInfo {


    C7nBuildInfo() {
    }

    @SerializedNames({"id", "status", "startTimeMillis", "durationTimeMillis", "username", "triggerType"})
    public static C7nBuildInfo create(String id,
                                      String status,
                                      long startTimeMillis,
                                      long durationTimeMillis,
                                      String username,
                                      String triggerType) {
        return new AutoValue_C7nBuildInfo(id,
                status,
                startTimeMillis,
                durationTimeMillis,
                username,
                triggerType);
    }

    @Nullable
    public abstract String id();

    public abstract String status();

    public abstract long startTimeMillis();

    public abstract long durationTimeMillis();

    public abstract String username();

    public abstract String triggerType();
}
