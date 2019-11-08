package io.choerodon.devops.infra.enums;

/**
 * PersistentVolume的类型
 *
 * @author zmf
 * @since 11/7/19
 */
public enum PersistentVolumeType {
    NFS("NFS"),
    HOST_PATH("HostPath");

    private String type;

    PersistentVolumeType(String type) {
        this.type = type;
    }

    public static PersistentVolumeType forType(String type) {
        for (PersistentVolumeType pvType : values()) {
            if (pvType.type.equals(type)) {
                return pvType;
            }
        }
        return null;
    }

    public String getType() {
        return type;
    }
}
