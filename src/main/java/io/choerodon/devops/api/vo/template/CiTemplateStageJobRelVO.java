package io.choerodon.devops.api.vo.template;

import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import org.hzero.starter.keyencrypt.core.Encrypt;

import io.choerodon.mybatis.annotation.ModifyAudit;
import io.choerodon.mybatis.annotation.VersionAudit;
import io.choerodon.mybatis.domain.AuditDomain;

/**
 * 流水线阶段与任务模板的关系表(CiTemplateStageJobRel)实体类
 *
 * @author hao.wang08@hand-china.com
 * @since 2021-12-01 15:58:20
 */


public class CiTemplateStageJobRelVO {

    @Encrypt
    private Long id;

    @ApiModelProperty(value = "流水线模板阶段id", required = true)
    @NotNull
    @Encrypt
    private Long ciTemplateStageId;

    @ApiModelProperty(value = "流水线模板id", required = true)
    @NotNull
    @Encrypt
    private Long ciTemplateJobId;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getCiTemplateStageId() {
        return ciTemplateStageId;
    }

    public void setCiTemplateStageId(Long ciTemplateStageId) {
        this.ciTemplateStageId = ciTemplateStageId;
    }

    public Long getCiTemplateJobId() {
        return ciTemplateJobId;
    }

    public void setCiTemplateJobId(Long ciTemplateJobId) {
        this.ciTemplateJobId = ciTemplateJobId;
    }

}

