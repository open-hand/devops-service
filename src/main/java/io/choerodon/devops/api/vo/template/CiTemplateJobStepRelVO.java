package io.choerodon.devops.api.vo.template;

import javax.validation.constraints.NotNull;

import io.swagger.annotations.ApiModelProperty;
import org.hzero.starter.keyencrypt.core.Encrypt;

/**
 * 流水线任务模板与步骤模板关系表(CiTemplateJobStepRel)实体类
 *
 * @author hao.wang08@hand-china.com
 * @since 2021-12-01 15:58:17
 */

public class CiTemplateJobStepRelVO {


    @Encrypt
    private Long id;

    @ApiModelProperty(value = "层级Id", required = true)
    @NotNull
    private Long ciTemplateJobId;

    @ApiModelProperty(value = "层级Id", required = true)
    @NotNull
    private Long ciTemplateStepId;

    @ApiModelProperty(value = "顺序", required = true)
    @NotNull
    private Long sequence;

    private CiTemplateStepVO ciTemplateStepVO;

    public CiTemplateStepVO getCiTemplateStepVO() {
        return ciTemplateStepVO;
    }

    public void setCiTemplateStepVO(CiTemplateStepVO ciTemplateStepVO) {
        this.ciTemplateStepVO = ciTemplateStepVO;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getCiTemplateJobId() {
        return ciTemplateJobId;
    }

    public void setCiTemplateJobId(Long ciTemplateJobId) {
        this.ciTemplateJobId = ciTemplateJobId;
    }

    public Long getCiTemplateStepId() {
        return ciTemplateStepId;
    }

    public void setCiTemplateStepId(Long ciTemplateStepId) {
        this.ciTemplateStepId = ciTemplateStepId;
    }

    public Long getSequence() {
        return sequence;
    }

    public void setSequence(Long sequence) {
        this.sequence = sequence;
    }

}

