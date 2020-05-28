package io.choerodon.devops.infra.dto;

import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import io.swagger.annotations.ApiModelProperty;

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
@Table(name= "devops_ci_content")
public class DevopsCiContentDTO extends AuditDomain {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
    @ApiModelProperty("流水线id")
    private Long ciPipelineId;
    @ApiModelProperty("gitlab-ci配置文件")
    private String ciContentFile;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getCiPipelineId() {
        return ciPipelineId;
    }

    public void setCiPipelineId(Long ciPipelineId) {
        this.ciPipelineId = ciPipelineId;
    }

    public String getCiContentFile() {
        return ciContentFile;
    }

    public void setCiContentFile(String ciContentFile) {
        this.ciContentFile = ciContentFile;
    }
}
