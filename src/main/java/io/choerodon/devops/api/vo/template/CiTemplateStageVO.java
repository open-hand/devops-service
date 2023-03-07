package io.choerodon.devops.api.vo.template;

import java.util.List;
import javax.validation.constraints.NotNull;

import io.swagger.annotations.ApiModelProperty;
import org.hzero.starter.keyencrypt.core.Encrypt;

/**
 * @author hao.wang08@hand-china.com
 * @since 2021-12-01 17:12:19
 */
public class CiTemplateStageVO {

    @Encrypt
    private Long id;
    @ApiModelProperty(value = "阶段名称", required = true)
    @NotNull
    private String name;
    @ApiModelProperty(value = "流水线模板id", required = true)
    @Encrypt
    private Long pipelineTemplateId;

    @ApiModelProperty(value = "顺序", required = true)
    @NotNull
    private Long sequence;

    @ApiModelProperty("任务模板是否可见")
    private Boolean visibility;

    private List<CiTemplateJobVO> ciTemplateJobVOList;

    public List<CiTemplateJobVO> getCiTemplateJobVOList() {
        return ciTemplateJobVOList;
    }

    public void setCiTemplateJobVOList(List<CiTemplateJobVO> ciTemplateJobVOList) {
        this.ciTemplateJobVOList = ciTemplateJobVOList;
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

    public Long getPipelineTemplateId() {
        return pipelineTemplateId;
    }

    public void setPipelineTemplateId(Long pipelineTemplateId) {
        this.pipelineTemplateId = pipelineTemplateId;
    }

    public Boolean getVisibility() {
        return visibility;
    }

    public void setVisibility(Boolean visibility) {
        this.visibility = visibility;
    }
}
