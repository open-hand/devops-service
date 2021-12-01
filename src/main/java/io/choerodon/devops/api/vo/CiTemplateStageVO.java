package io.choerodon.devops.api.vo;

import java.util.List;

import io.swagger.annotations.ApiModelProperty;

/**
 * @author hao.wang08@hand-china.com
 * @since 2021-12-01 17:12:19
 */
public class CiTemplateStageVO {


    private Long id;
    @ApiModelProperty(value = "阶段名称", required = true)
    private String name;
    @ApiModelProperty(value = "流水线模板id", required = true)
    private Long pipelineTemplateId;

    private List<CiTemplateJobVO> ciTemplateJobVOList;

    public List<CiTemplateJobVO> getCiTemplateJobVOList() {
        return ciTemplateJobVOList;
    }

    public void setCiTemplateJobVOList(List<CiTemplateJobVO> ciTemplateJobVOList) {
        this.ciTemplateJobVOList = ciTemplateJobVOList;
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
}
