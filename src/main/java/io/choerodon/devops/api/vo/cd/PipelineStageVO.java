package io.choerodon.devops.api.vo.cd;

import java.util.List;
import javax.validation.Valid;

import io.swagger.annotations.ApiModelProperty;
import org.hzero.starter.keyencrypt.core.Encrypt;

/**
 * @author
 * @since 2022-11-24 16:12:34
 */
public class PipelineStageVO {

    @Encrypt
    private Long id;
    @ApiModelProperty(value = "所属流水线Id,devops_pipeline.id", required = true)
    @Encrypt
    private Long pipelineId;
    @ApiModelProperty(value = "所属版本Id,devops_pipeline_version.id", required = true)
    @Encrypt
    private Long versionId;
    @ApiModelProperty(value = "名称", required = true)
    private String name;
    @ApiModelProperty(value = "阶段顺序", required = true)
    private Integer sequence;
    @ApiModelProperty(value = "阶段下任务信息", required = true)
    @Valid
    private List<PipelineJobVO> jobList;

    public Integer getSequence() {
        return sequence;
    }

    public void setSequence(Integer sequence) {
        this.sequence = sequence;
    }

    public List<PipelineJobVO> getJobList() {
        return jobList;
    }

    public void setJobList(List<PipelineJobVO> jobList) {
        this.jobList = jobList;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getPipelineId() {
        return pipelineId;
    }

    public void setPipelineId(Long pipelineId) {
        this.pipelineId = pipelineId;
    }

    public Long getVersionId() {
        return versionId;
    }

    public void setVersionId(Long versionId) {
        this.versionId = versionId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
