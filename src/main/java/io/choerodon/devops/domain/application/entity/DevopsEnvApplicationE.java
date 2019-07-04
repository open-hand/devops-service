package io.choerodon.devops.domain.application.entity;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

/**
 * @author lizongwei
 * @date 2019/7/1
 */
@Component
@Scope("prototype")
public class DevopsEnvApplicationE {

    private Long appId;
    private Long envId;

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
