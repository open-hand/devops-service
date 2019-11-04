package io.choerodon.devops.infra.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

/**
 * {@link io.choerodon.devops.api.vo.kubernetes.Metadata#setType(String)}的可选值
 *
 * @author zmf
 * @since 11/4/19
 */
public enum C7NHelmReleaseMetadataType {
    /**
     * 表明这个release是一个集群组件的类型，这个类型是没有应用服务和应用服务版本纪录的
     */
    CLUSTER_COMPONENT("cluster-component"),
    /**
     * 默认的release类型，type为null默认是这个
     */
    APP_SERVICE_VERSION("app-service-version");

    @SuppressWarnings("unchecked")
    private static final JacksonJsonEnumHelper<C7NHelmReleaseMetadataType> enumHelper = new JacksonJsonEnumHelper(C7NHelmReleaseMetadataType.class);

    private String type;

    C7NHelmReleaseMetadataType(String type) {
        this.type = type;
    }

    public String getType() {
        return type;
    }

    @JsonCreator
    public static C7NHelmReleaseMetadataType forValue(String value) {
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
