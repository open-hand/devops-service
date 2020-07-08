package io.choerodon.devops.api.vo;

import java.util.List;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

import io.swagger.annotations.ApiModelProperty;

import io.choerodon.devops.infra.annotation.WillDeleted;

/**
 *
 * @author wanghao
 * @Date 2020/4/2 17:00
 */
public class DevopsCiStageVO {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ApiModelProperty("阶段名称")
    @NotEmpty(message = "error.stage.name.cannot.be.null")
    private String name;
    @ApiModelProperty("阶段所属流水线id")
    private Long ciPipelineId;
    @ApiModelProperty("阶段顺序")
    @NotNull(message = "error.stage.sequence.cannot.be.null")
    private Long sequence;

    private Long objectVersionNumber;

    private List<DevopsCiJobVO> jobList;
    private String type;

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
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

    public List<DevopsCiJobVO> getJobList() {
        return jobList;
    }

    public void setJobList(List<DevopsCiJobVO> jobList) {
        this.jobList = jobList;
    }

    public Long getObjectVersionNumber() {
        return objectVersionNumber;
    }

    public void setObjectVersionNumber(Long objectVersionNumber) {
        this.objectVersionNumber = objectVersionNumber;
    }
}
