package io.choerodon.devops.api.vo;

import io.swagger.annotations.ApiModelProperty;

/**
 * 〈功能简述〉
 * 〈〉
 *
 * @author wanghao
 * @since 2020/7/13 14:22
 */
public class AuditCheckVO {
    @ApiModelProperty("资源类型，取值范围[stage,job]")
    private String sourceType;
    @ApiModelProperty("资源id，取值范围[stage_id,job_id]")
    private Long sourceId;

    public String getSourceType() {
        return sourceType;
    }

    public void setSourceType(String sourceType) {
        this.sourceType = sourceType;
    }

    public Long getSourceId() {
        return sourceId;
    }

    public void setSourceId(Long sourceId) {
        this.sourceId = sourceId;
    }
}
