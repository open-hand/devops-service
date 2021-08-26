package io.choerodon.devops.infra.enums.k8s;

/**
 * 〈功能简述〉
 * 〈Service 类型枚举类〉
 *
 * @author wanghao
 * @since 2021/6/15 11:00
 */
public enum ServiceTypeEnum {
    CLUSTER_IP("ClusterIP"),
    NODE_PORT("NodePort"),
    LOAD_BALANCER("LoadBalancer");

    private String value;

    ServiceTypeEnum(String value) {
        this.value = value;
    }

    public String value() {
        return this.value;
    }


}
