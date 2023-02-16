package io.choerodon.devops.api.vo.pipeline;

import io.swagger.annotations.ApiModelProperty;

/**
 * @author hao.wang@zknow.com
 * @since 2023-02-16 16:49:20
 */
public class ConfigFileRelVO {

    @ApiModelProperty(value = "devops_config_file.id", required = true)
    private Long configFileId;
    @ApiModelProperty(value = "配置文件下载路径", required = true)
    private String configFilePath;


    public Long getConfigFileId() {
        return configFileId;
    }

    public void setConfigFileId(Long configFileId) {
        this.configFileId = configFileId;
    }

    public String getConfigFilePath() {
        return configFilePath;
    }

    public void setConfigFilePath(String configFilePath) {
        this.configFilePath = configFilePath;
    }
}
