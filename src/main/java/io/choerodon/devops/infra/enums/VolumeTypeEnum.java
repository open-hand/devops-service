package io.choerodon.devops.infra.enums;

import com.fasterxml.jackson.annotation.JsonCreator;

/**
 * 卷类型枚举
 */
public enum VolumeTypeEnum {

    NFS("nfs"),
    HOSTPATH("HostPath"),
    LOCALPV("LocalPv");

    private String type;

    VolumeTypeEnum(String type) {
        this.type = type;
    }

    public String getType() {
        return type;
    }

    public static Boolean checkExist(String type) {
        VolumeTypeEnum volumeTypeEnum = VolumeTypeEnum.valueOf(type.toUpperCase());
        return volumeTypeEnum != null;
    }

    @SuppressWarnings("unchecked")
    private static final JacksonJsonEnumHelper<VolumeTypeEnum> enumHelper = new JacksonJsonEnumHelper(VolumeTypeEnum.class);

    @JsonCreator
    public static VolumeTypeEnum forValue(String value) {
        return enumHelper.forValue(value);
    }
}
