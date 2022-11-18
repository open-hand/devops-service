package io.choerodon.devops.api.vo.pipeline;

import io.swagger.annotations.ApiModelProperty;
import org.hzero.starter.keyencrypt.core.Encrypt;

public class DevopsCiSonarQualityGateConditionVO {
    @Encrypt
    private Long id;

    @ApiModelProperty("质量门类型")
    private String gatesMetric;
    @ApiModelProperty("质量门比较操作")
    private String gatesOperator;
    @ApiModelProperty("质量门值")
    private String gatesValue;
    @ApiModelProperty("质量门检测范围")
    private Integer gatesScope;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getGatesMetric() {
        return gatesMetric;
    }

    public void setGatesMetric(String gatesMetric) {
        this.gatesMetric = gatesMetric;
    }

    public String getGatesOperator() {
        return gatesOperator;
    }

    public void setGatesOperator(String gatesOperator) {
        this.gatesOperator = gatesOperator;
    }

    public String getGatesValue() {
        return gatesValue;
    }

    public void setGatesValue(String gatesValue) {
        this.gatesValue = gatesValue;
    }

    public Integer getGatesScope() {
        return gatesScope;
    }

    public void setGatesScope(Integer gatesScope) {
        this.gatesScope = gatesScope;
    }
}
