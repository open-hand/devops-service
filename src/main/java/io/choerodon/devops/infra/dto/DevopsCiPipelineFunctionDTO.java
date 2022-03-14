package io.choerodon.devops.infra.dto;

import io.swagger.annotations.ApiModelProperty;

import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import io.choerodon.mybatis.annotation.ModifyAudit;
import io.choerodon.mybatis.annotation.VersionAudit;
import io.choerodon.mybatis.domain.AuditDomain;

@ModifyAudit
@VersionAudit
@Table(name = "devops_ci_pipeline_function")
public class DevopsCiPipelineFunctionDTO extends AuditDomain {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
    @ApiModelProperty("devops流水线id/为0代表平台默认")
    private Long devopsPipelineId;
    @ApiModelProperty("函数名称")
    private String name;

    @ApiModelProperty("函数脚本")
    private String script;

    public String getScript() {
        return script;
    }

    public void setScript(String script) {
        this.script = script;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getDevopsPipelineId() {
        return devopsPipelineId;
    }

    public void setDevopsPipelineId(Long devopsPipelineId) {
        this.devopsPipelineId = devopsPipelineId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
