package io.choerodon.devops.infra.dto;

import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import io.choerodon.mybatis.annotation.ModifyAudit;
import io.choerodon.mybatis.annotation.VersionAudit;
import io.choerodon.mybatis.domain.AuditDomain;

/**
 * 代码扫描指标详情表(SonarAnalyseMeasure)实体类
 *
 * @author hao.wang@zknow.com
 * @since 2023-06-09 09:34:55
 */

@ApiModel("代码扫描指标详情表")
@VersionAudit
@ModifyAudit
@JsonInclude(value = JsonInclude.Include.NON_NULL)
@Table(name = "devops_sonar_analyse_measure")
public class SonarAnalyseMeasureDTO extends AuditDomain {
    public static final String FIELD_ID = "id";
    public static final String FIELD_RECORD_ID = "recordId";
    public static final String FIELD_METRIC = "metric";
    public static final String FIELD_VALUE = "value";
    private static final long serialVersionUID = 786188716070517060L;
    @Id
    @GeneratedValue
    private Long id;

    @ApiModelProperty(value = "devops_sonar_analyse_record.id", required = true)
    @NotNull
    private Long recordId;

    @ApiModelProperty(value = "键")
    private String metric;

    @ApiModelProperty(value = "值")
    private String metricValue;

    public SonarAnalyseMeasureDTO() {
    }

    public SonarAnalyseMeasureDTO(String metric, String metricValue) {
        this.metric = metric;
        this.metricValue = metricValue;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getRecordId() {
        return recordId;
    }

    public void setRecordId(Long recordId) {
        this.recordId = recordId;
    }

    public String getMetric() {
        return metric;
    }

    public void setMetric(String metric) {
        this.metric = metric;
    }

    public String getMetricValue() {
        return metricValue;
    }

    public void setMetricValue(String metricValue) {
        this.metricValue = metricValue;
    }

}

