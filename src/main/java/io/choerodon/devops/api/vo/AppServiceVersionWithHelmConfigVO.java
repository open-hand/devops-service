package io.choerodon.devops.api.vo;

import io.swagger.annotations.ApiModelProperty;

import io.choerodon.devops.infra.dto.AppServiceVersionDTO;

/**
 * @author zmf
 * @since 2020/12/2
 */
public class AppServiceVersionWithHelmConfigVO extends AppServiceVersionDTO {
    @ApiModelProperty("helm的仓库配置/如果有用户名密码，则包含用户名密码")
    private ConfigVO helmConfig;

    public ConfigVO getHelmConfig() {
        return helmConfig;
    }

    public void setHelmConfig(ConfigVO helmConfig) {
        this.helmConfig = helmConfig;
    }
}
