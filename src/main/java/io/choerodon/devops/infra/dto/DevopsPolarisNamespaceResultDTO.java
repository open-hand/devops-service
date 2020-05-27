package io.choerodon.devops.infra.dto;

import javax.persistence.*;

import io.swagger.annotations.ApiModelProperty;

import io.choerodon.mybatis.annotation.ModifyAudit;
import io.choerodon.mybatis.annotation.VersionAudit;
import io.choerodon.mybatis.domain.AuditDomain;

/**
 * polaris扫描结果的Namespace这一级别的数据
 *
 * @author zmf
 * @since 2/17/20
 */
@ModifyAudit
@VersionAudit
@Table(name = "devops_polaris_namespace_result")
public class DevopsPolarisNamespaceResultDTO extends AuditDomain {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @ApiModelProperty("自增id")
    private Long id;

    @ApiModelProperty("环境id / 可为空")
    private Long envId;

    @ApiModelProperty("集群namespace")
    private String namespace;

    @ApiModelProperty("扫描纪录id")
    private Long recordId;

    @ApiModelProperty("此namespace详细扫描纪录id")
    private Long detailId;

    @ApiModelProperty("是否有error级别的检测项")
    private Boolean hasErrors;

    @Transient
    @ApiModelProperty("详情json")
    private String detail;

    public DevopsPolarisNamespaceResultDTO() {
    }

    public DevopsPolarisNamespaceResultDTO(Long envId, String namespace, Long recordId, Boolean hasErrors) {
        this.envId = envId;
        this.namespace = namespace;
        this.recordId = recordId;
        this.hasErrors = hasErrors;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getEnvId() {
        return envId;
    }

    public void setEnvId(Long envId) {
        this.envId = envId;
    }

    public String getNamespace() {
        return namespace;
    }

    public void setNamespace(String namespace) {
        this.namespace = namespace;
    }

    public Long getRecordId() {
        return recordId;
    }

    public void setRecordId(Long recordId) {
        this.recordId = recordId;
    }

    public Long getDetailId() {
        return detailId;
    }

    public void setDetailId(Long detailId) {
        this.detailId = detailId;
    }

    public Boolean getHasErrors() {
        return hasErrors;
    }

    public void setHasErrors(Boolean hasErrors) {
        this.hasErrors = hasErrors;
    }

    public String getDetail() {
        return detail;
    }

    public void setDetail(String detail) {
        this.detail = detail;
    }
}
