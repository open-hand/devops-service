package io.choerodon.devops.infra.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

/**
 * 用户环境所支持的资源类型
 *
 * @author zmf
 * @since 11/1/19
 */
public enum UserEnvSupportedResourceType {
    SERVICE("Service"),
    INGRESS("Ingress"),
    C7NHELMRELEASE("C7NHelmRelease"),
    CERTIFICATE("Certificate"),
    CONFIGMAP("ConfigMap"),
    ENDPOINTS("Endpoints"),
    PERSISTENT_VOLUME_CLAIM("PersistentVolumeClaim"),
    DEPLOYMENT("Deployment"),
    DAEMONSET("DaemonSet"),
    STATEFULSET("StatefulSet"),
    CRON_JOB("CronJob"),
    JOB("Job"),
    SECRET("Secret");

    @SuppressWarnings("unchecked")
    private static final JacksonJsonEnumHelper<UserEnvSupportedResourceType> enumHelper = new JacksonJsonEnumHelper(UserEnvSupportedResourceType.class);

    private String type;

    UserEnvSupportedResourceType(String type) {
        this.type = type;
    }

    public String getType() {
        return type;
    }

    @JsonCreator
    public static UserEnvSupportedResourceType forValue(String value) {
        return enumHelper.forValue(value);
    }

    @JsonValue
    public String toValue() {
        return enumHelper.toString(this);
    }

    @Override
    public String toString() {
        return enumHelper.toString(this);
    }
}
