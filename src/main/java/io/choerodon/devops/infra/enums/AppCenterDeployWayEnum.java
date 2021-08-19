package io.choerodon.devops.infra.enums;

/**
 * @Author: scp
 * @Description:
 * @Date: Created in 2021/8/18
 * @Modified By:
 */
public enum AppCenterDeployWayEnum {
    CONTAINER("container", "容器部署"),
    HOST("host", "主机部署");

    private String code;
    private String value;

    public String getCode() {
        return code;
    }

    public String getValue() {
        return value;
    }

    AppCenterDeployWayEnum(String code, String value) {
        this.value = value;
        this.code = code;
    }

}
