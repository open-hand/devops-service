package io.choerodon.devops.api.vo.polaris;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import io.swagger.annotations.ApiModelProperty;

/**
 * @author zmf
 * @since 2/14/20
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class PolarisScanSummaryVO {
    @ApiModelProperty("通过的数量")
    private Long successes;
    @ApiModelProperty("警告的数量")
    private Long warnings;
    @ApiModelProperty("错误的数量")
    private Long errors;

    public Long getSuccesses() {
        return successes;
    }

    public void setSuccesses(Long successes) {
        this.successes = successes;
    }

    public Long getWarnings() {
        return warnings;
    }

    public void setWarnings(Long warnings) {
        this.warnings = warnings;
    }

    public Long getErrors() {
        return errors;
    }

    public void setErrors(Long errors) {
        this.errors = errors;
    }

    @Override
    public String toString() {
        return "PolarisScanSummaryVO{" +
                "successes=" + successes +
                ", warnings=" + warnings +
                ", errors=" + errors +
                '}';
    }
}
