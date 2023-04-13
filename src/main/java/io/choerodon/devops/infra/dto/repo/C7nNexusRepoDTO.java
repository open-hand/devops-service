package io.choerodon.devops.infra.dto.repo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

/**
 * choerodon nexus仓库DTO
 *
 * @author weisen.yang@hand-china.com 2020/7/2
 */
@ApiModel("nexus仓库DTO")
public class C7nNexusRepoDTO {
    @ApiModelProperty(value = "服务配置Id")
    private Long configId;
    @ApiModelProperty(value = "主键Id")
    private Long repositoryId;
    @ApiModelProperty(value = "仓库名称")
    private String neRepositoryName;

    @ApiModelProperty(value = "内部url")
    private String internalUrl;

    @ApiModelProperty(value = "仓库的版本策略")
    private String versionPolicy;

    public String getInternalUrl() {
        return internalUrl;
    }

    public void setInternalUrl(String internalUrl) {
        this.internalUrl = internalUrl;
    }

    public String getVersionPolicy() {
        return versionPolicy;
    }

    public void setVersionPolicy(String versionPolicy) {
        this.versionPolicy = versionPolicy;
    }

    public Long getConfigId() {
        return configId;
    }

    public void setConfigId(Long configId) {
        this.configId = configId;
    }

    public Long getRepositoryId() {
        return repositoryId;
    }

    public void setRepositoryId(Long repositoryId) {
        this.repositoryId = repositoryId;
    }

    public String getNeRepositoryName() {
        return neRepositoryName;
    }

    public void setNeRepositoryName(String neRepositoryName) {
        this.neRepositoryName = neRepositoryName;
    }

    @Override
    public String toString() {
        return "C7nNexusRepoDTO{" +
                "configId=" + configId +
                ", repositoryId=" + repositoryId +
                ", neRepositoryName='" + neRepositoryName + '\'' +
                '}';
    }
}
