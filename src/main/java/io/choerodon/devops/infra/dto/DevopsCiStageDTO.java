package io.choerodon.devops.infra.dto;

import io.swagger.annotations.ApiModelProperty;

import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

/**
 *
 * @author wanghao
 * @Date 2020/4/2 17:00
 */
@Table(name = "devops_ci_stage")
public class DevopsCiStageDTO {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ApiModelProperty("阶段名称")
    private String name;
    @ApiModelProperty("阶段所属流水线id")
    private Long ciPipelineId;
    @ApiModelProperty("阶段顺序")
    private Long sequence;

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

    public Long getCiPipelineId() {
        return ciPipelineId;
    }

    public void setCiPipelineId(Long ciPipelineId) {
        this.ciPipelineId = ciPipelineId;
    }

    public Long getSequence() {
        return sequence;
    }

    public void setSequence(Long sequence) {
        this.sequence = sequence;
    }
}
