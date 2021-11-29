package io.choerodon.devops.api.vo;

import javax.validation.constraints.NotNull;

import io.swagger.annotations.ApiModelProperty;
import org.hzero.starter.keyencrypt.core.Encrypt;

import io.choerodon.devops.infra.dto.DevopsCiSonarConfigDTO;

/**
 * 〈功能简述〉
 * 〈〉
 *
 * @author wanghao
 * @since 2021/11/29 11:38
 */
public class DevopsCiStepVO {

    private Long id;
    @ApiModelProperty("步骤名称")
    private String name;
    /**
     * {@link io.choerodon.devops.infra.enums.DevopsCiStepTypeEnum}
     */
    @ApiModelProperty("步骤类型")
    private String type;
    @ApiModelProperty("步骤脚本")
    private String script;

    @ApiModelProperty("步骤顺序")
    @NotNull(message = "error.step.sequence.cannot.be.null")
    private Long sequence;

    @Encrypt
    @ApiModelProperty("步骤所属任务id")
    private Long devopsCiJobId;
    @Encrypt
    @ApiModelProperty("步骤配置信息id")
    private Long configId;

    @ApiModelProperty("步骤为代码扫描时需要，保存代码扫描相关信息")
    private DevopsCiSonarConfigDTO devopsCiSonarConfigDTO;

    public DevopsCiSonarConfigDTO getDevopsCiSonarConfigDTO() {
        return devopsCiSonarConfigDTO;
    }

    public void setDevopsCiSonarConfigDTO(DevopsCiSonarConfigDTO devopsCiSonarConfigDTO) {
        this.devopsCiSonarConfigDTO = devopsCiSonarConfigDTO;
    }

    public Long getSequence() {
        return sequence;
    }

    public void setSequence(Long sequence) {
        this.sequence = sequence;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getScript() {
        return script;
    }

    public void setScript(String script) {
        this.script = script;
    }

    public Long getDevopsCiJobId() {
        return devopsCiJobId;
    }

    public void setDevopsCiJobId(Long devopsCiJobId) {
        this.devopsCiJobId = devopsCiJobId;
    }

    public Long getConfigId() {
        return configId;
    }

    public void setConfigId(Long configId) {
        this.configId = configId;
    }
}
