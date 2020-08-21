package io.choerodon.devops.api.vo;

/**
 * Created by wangxiang on 2020/7/27
 */
public class DevopsPipelineRecordRelVO extends BaseDomain{
    private Long id;

    private Long pipelineId;

    private Long ciPipelineRecordId;    // 纯cd流水线，这个值为0

    private Long cdPipelineRecordId;

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

    public Long getCiPipelineRecordId() {
        return ciPipelineRecordId;
    }

    public void setCiPipelineRecordId(Long ciPipelineRecordId) {
        this.ciPipelineRecordId = ciPipelineRecordId;
    }

    public Long getCdPipelineRecordId() {
        return cdPipelineRecordId;
    }

    public void setCdPipelineRecordId(Long cdPipelineRecordId) {
        this.cdPipelineRecordId = cdPipelineRecordId;
    }
}
