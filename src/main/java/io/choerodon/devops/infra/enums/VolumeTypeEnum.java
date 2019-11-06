package io.choerodon.devops.infra.enums;

/**
 * 卷类型枚举
 */
public enum VolumeTypeEnum {

    NFS("nfs"),
    HOSTPATH("HostPath");

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
}
