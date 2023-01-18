package io.choerodon.devops.api.vo;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

import io.swagger.annotations.ApiModelProperty;
import org.hzero.starter.keyencrypt.core.Encrypt;

import io.choerodon.devops.api.validator.CreateGroup;
import io.choerodon.devops.api.validator.UpdateGroup;
import io.choerodon.devops.infra.dto.DockerComposeValueDTO;

/**
 * 〈功能简述〉
 * 〈〉
 *
 * @author wanghao
 * @since 2022/4/7 9:46
 */
public class DockerComposeDeployVO {
    @Encrypt
    @ApiModelProperty(value = "主机id", required = true, example = "1")
    @NotNull(groups = {CreateGroup.class})
    private Long hostId;
    @ApiModelProperty(value = "主机应用的名称", required = true, example = "DC应用")
    @NotBlank(groups = {CreateGroup.class, UpdateGroup.class})
    private String appName;
    @ApiModelProperty(value = "主机应用的code, 创建时需要", example = "dc-app")
    @NotBlank(groups = {CreateGroup.class})
    private String appCode;

    @ApiModelProperty(value = "部署指令", required = true, example = "docker-compose up -d")
    @NotBlank(groups = {CreateGroup.class, UpdateGroup.class})
    private String runCommand;

    @Encrypt
    @ApiModelProperty(value = "部署配置id, 回滚版本时需要", example = "1")
    private Long valueId;

    @Valid
    @ApiModelProperty(value = "部署配置信息，正常部署时需要")
    private DockerComposeValueDTO dockerComposeValueDTO;

    @ApiModelProperty("应用版本")
    private String version;

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public Long getValueId() {
        return valueId;
    }

    public void setValueId(Long valueId) {
        this.valueId = valueId;
    }

    public String getRunCommand() {
        return runCommand;
    }

    public void setRunCommand(String runCommand) {
        this.runCommand = runCommand;
    }

    public Long getHostId() {
        return hostId;
    }

    public void setHostId(Long hostId) {
        this.hostId = hostId;
    }

    public String getAppName() {
        return appName;
    }

    public void setAppName(String appName) {
        this.appName = appName;
    }

    public String getAppCode() {
        return appCode;
    }

    public void setAppCode(String appCode) {
        this.appCode = appCode;
    }

    public DockerComposeValueDTO getDockerComposeValueDTO() {
        return dockerComposeValueDTO;
    }

    public void setDockerComposeValueDTO(DockerComposeValueDTO dockerComposeValueDTO) {
        this.dockerComposeValueDTO = dockerComposeValueDTO;
    }
}
