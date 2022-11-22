package io.choerodon.devops.infra.dto;


import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.annotations.ApiModelProperty;
import org.hzero.starter.keyencrypt.core.Encrypt;

import io.choerodon.mybatis.annotation.ModifyAudit;
import io.choerodon.mybatis.annotation.VersionAudit;

/**
 * sonar质量门条件
 * 〈〉
 *
 * @author lihao
 * @since 2022/11/18
 */
@Table(name = "devops_ci_sonar_quality_gate_condition")
@ModifyAudit
@VersionAudit
@JsonInclude(value = JsonInclude.Include.NON_NULL)
public class DevopsCiSonarQualityGateConditionDTO {
    @Id
    @Encrypt
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @ApiModelProperty("质量门条件在sonar的id")
    private String sonarConditionId;
    @ApiModelProperty("关联的gateId")
    private Long gateId;

    @ApiModelProperty("质量门类型")
    private String gatesMetric;
    @ApiModelProperty("质量门比较操作")
    private String gatesOperator;
    @ApiModelProperty("质量门值")
    private String gatesValue;
    @ApiModelProperty("质量门检测范围")
    private String gatesScope;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getSonarConditionId() {
        return sonarConditionId;
    }

    public void setSonarConditionId(String sonarConditionId) {
        this.sonarConditionId = sonarConditionId;
    }

    public Long getGateId() {
        return gateId;
    }

    public void setGateId(Long gateId) {
        this.gateId = gateId;
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

    public String getGatesScope() {
        return gatesScope;
    }

    public void setGatesScope(String gatesScope) {
        this.gatesScope = gatesScope;
    }
}
