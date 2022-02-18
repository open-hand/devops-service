package io.choerodon.devops.infra.enums.host;

/**
 * Created by wangxiang on 2022/2/18
 */
public enum DevopsHostDeployType {
    /**
     * 默认仓库镜像部署
     */
    DEFAULT("default"),
    /**
     * 自定义仓库镜像部署
     */
    CUSTOM("custom");

    private final String value;

    DevopsHostDeployType(String value) {
        this.value = value;
    }

    public String value() {
        return this.value;
    }
}
