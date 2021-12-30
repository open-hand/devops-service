package io.choerodon.devops.infra.dto;

import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;

import io.swagger.annotations.ApiModelProperty;
import org.hzero.starter.keyencrypt.core.Encrypt;

import io.choerodon.mybatis.annotation.ModifyAudit;
import io.choerodon.mybatis.annotation.VersionAudit;
import io.choerodon.mybatis.domain.AuditDomain;

/**
 * 〈功能简述〉
 * 〈〉
 *
 * @author wanghao
 * @since 2021/11/29 11:38
 */
@Table(name = "devops_ci_step")
@ModifyAudit
@VersionAudit
public class DevopsCiStepDTO extends AuditDomain {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @ApiModelProperty("项目id")
    private Long projectId;
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

    public Long getSequence() {
        return sequence;
    }

    public void setSequence(Long sequence) {
        this.sequence = sequence;
    }

    public Long getProjectId() {
        return projectId;
    }

    public void setProjectId(Long projectId) {
        this.projectId = projectId;
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

}
