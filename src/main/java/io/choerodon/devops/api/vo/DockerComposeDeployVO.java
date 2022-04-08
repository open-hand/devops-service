package io.choerodon.devops.api.vo;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

import io.swagger.annotations.ApiModelProperty;
import org.hzero.starter.keyencrypt.core.Encrypt;

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
    @ApiModelProperty("主机id")
    @NotNull
    private Long hostId;
    @ApiModelProperty("主机应用的名称")
    @NotBlank
    private String name;
    @ApiModelProperty("主机应用的code")
    @NotBlank
    private String code;

    @ApiModelProperty(value = "部署指令", required = true)
    @NotBlank
    private String runCommand;

    @Encrypt
    @ApiModelProperty("部署配置id")
    private Long valueId;

    private DockerComposeValueDTO dockerComposeValueDTO;

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

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public DockerComposeValueDTO getDockerComposeValueDTO() {
        return dockerComposeValueDTO;
    }

    public void setDockerComposeValueDTO(DockerComposeValueDTO dockerComposeValueDTO) {
        this.dockerComposeValueDTO = dockerComposeValueDTO;
    }
}
