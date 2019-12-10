package io.choerodon.devops.infra.enums;

/**
 * 资源删除通知事件类型枚举类
 */
public enum NotifyEventEnum {

    INSTANCE("instance"),
    INGRESS("ingress"),
    CONFIGMAP("configMap"),
    CERTIFICATE("certificate"),
    SECRET("secret"),
    SERVICE("service");

    private final String value;

    NotifyEventEnum(String value) {
        this.value = value;
    }

    public String value() {
        return value;
    }
}
