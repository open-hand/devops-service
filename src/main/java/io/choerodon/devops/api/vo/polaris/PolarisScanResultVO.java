package io.choerodon.devops.api.vo.polaris;

import io.swagger.annotations.ApiModelProperty;

/**
 * @author zmf
 * @since 2/14/20
 */
public class PolarisScanResultVO {
    @ApiModelProperty("主要数据")
    private PolarisScanAuditDataVO auditData;
    @ApiModelProperty("总结的数据")
    private PolarisScanSummaryVO summary;

    public PolarisScanAuditDataVO getAuditData() {
        return auditData;
    }

    public void setAuditData(PolarisScanAuditDataVO auditData) {
        this.auditData = auditData;
    }

    public PolarisScanSummaryVO getSummary() {
        return summary;
    }

    public void setSummary(PolarisScanSummaryVO summary) {
        this.summary = summary;
    }
}
