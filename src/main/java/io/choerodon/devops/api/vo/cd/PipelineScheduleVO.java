package io.choerodon.devops.api.vo.cd;

import javax.validation.constraints.NotEmpty;

import io.swagger.annotations.ApiModelProperty;
import org.hzero.starter.keyencrypt.core.Encrypt;

import io.choerodon.devops.api.vo.CommonScheduleVO;

/**
 * @author hao.wang@zknow.com
 * @since 2022-11-29 16:40:28
 */
public class PipelineScheduleVO extends CommonScheduleVO {

    @Encrypt
    private Long id;
    @ApiModelProperty(value = "所属流水线id，devops_pipeline.id", required = true)
    private Long pipelineId;
    @ApiModelProperty(value = "定时任务名称", required = true)
    @NotEmpty(message = "{devops.pipeline.schedule.name.cannot.be.null}")
    private String name;

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

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

}
