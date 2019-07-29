package io.choerodon.devops.api.vo;

/**
 * @author lizongwei
 * @date 2019/7/1
 */
public class DevopsEnvApplicationVO {

    private Long appServiceId;
    private Long envId;

    public Long getAppServiceId() {
        return appServiceId;
    }

    public void getAppServiceId(Long appServiceId) {
        this.appServiceId = appServiceId;
    }

    public Long getEnvId() {
        return envId;
    }

    public void setEnvId(Long envId) {
        this.envId = envId;
    }
}
