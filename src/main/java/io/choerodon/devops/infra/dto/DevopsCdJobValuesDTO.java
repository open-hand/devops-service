package io.choerodon.devops.infra.dto;

import io.swagger.annotations.ApiModelProperty;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import io.choerodon.mybatis.annotation.ModifyAudit;
import io.choerodon.mybatis.annotation.VersionAudit;
import io.choerodon.mybatis.domain.AuditDomain;

/**
 *
 * @author wanghao
 * @Date 2020/4/2 17:00
 */
@ModifyAudit
@VersionAudit
@Table(name= "devops_cd_job_values")
public class DevopsCdJobValuesDTO extends AuditDomain {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
    @ApiModelProperty("流水线任务job id")
    private Long cicdJobId;
    @ApiModelProperty("流水线gitlab-ci.yaml配置文件")
    private String value;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getCicdJobId() {
        return cicdJobId;
    }

    public void setCicdJobId(Long cicdJobId) {
        this.cicdJobId = cicdJobId;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return "CiCdJobValuesDTO{" +
                "id=" + id +
                ", cicdJobId=" + cicdJobId +
                ", value='" + value + '\'' +
                '}';
    }
}
