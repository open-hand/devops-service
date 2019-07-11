package io.choerodon.devops.api.vo.iam.entity;

/**
 * @author lizongwei
 * @date 2019/7/1
 */
public class DevopsEnvApplicationE {

    private Long appId;
    private Long envId;

    public DevopsEnvApplicationE() {
    }

    public DevopsEnvApplicationE(Long appId, Long envId) {
        this.appId = appId;
        this.envId = envId;
    }

    public Long getAppId() {
        return appId;
    }

    public void setAppId(Long appId) {
        this.appId = appId;
    }

    public Long getEnvId() {
        return envId;
    }

    public void setEnvId(Long envId) {
        this.envId = envId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DevopsEnvApplicationE that = (DevopsEnvApplicationE) o;
        return appId.equals(that.appId) &&
                envId.equals(that.envId);
    }

    @Override
    public int hashCode() {
        int result = 1;
        result = result * appId.intValue() * envId.intValue();
        return result;
    }
}
