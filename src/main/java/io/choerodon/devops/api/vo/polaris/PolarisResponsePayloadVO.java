package io.choerodon.devops.api.vo.polaris;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import io.swagger.annotations.ApiModelProperty;

/**
 * @author zmf
 * @since 2/14/20
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class PolarisResponsePayloadVO {
    @ApiModelProperty("这次扫描相关的record的id")
    private Long recordId;
    @ApiModelProperty("扫描的结果")
    private PolarisScanResultVO polarisResult;

    public Long getRecordId() {
        return recordId;
    }

    public void setRecordId(Long recordId) {
        this.recordId = recordId;
    }

    public PolarisScanResultVO getPolarisResult() {
        return polarisResult;
    }

    public void setPolarisResult(PolarisScanResultVO polarisResult) {
        this.polarisResult = polarisResult;
    }
}
