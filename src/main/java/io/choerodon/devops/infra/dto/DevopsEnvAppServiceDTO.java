package io.choerodon.devops.infra.dto;

import java.util.Objects;
import javax.persistence.Id;
import javax.persistence.Table;

@Table(name = "devops_env_app_service")
public class DevopsEnvAppServiceDTO {
    // 这个表没有主键，这个@Id注解是防止启动报错
    @Id
    private Long appServiceId;
    private Long envId;

    public DevopsEnvAppServiceDTO() {
    }

    public DevopsEnvAppServiceDTO(Long appServiceId, Long envId) {
        this.appServiceId = appServiceId;
        this.envId = envId;
    }

    public Long getAppServiceId() {
        return appServiceId;
    }

    public void setAppServiceId(Long appServiceId) {
        this.appServiceId = appServiceId;
    }

    public Long getEnvId() {
        return envId;
    }

    public void setEnvId(Long envId) {
        this.envId = envId;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }

        if (!(obj instanceof DevopsEnvAppServiceDTO)) {
            return false;
        }

        DevopsEnvAppServiceDTO that = (DevopsEnvAppServiceDTO) obj;

        return Objects.equals(this.appServiceId, that.appServiceId) && Objects.equals(this.envId, that.envId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(appServiceId, envId);
    }
}
