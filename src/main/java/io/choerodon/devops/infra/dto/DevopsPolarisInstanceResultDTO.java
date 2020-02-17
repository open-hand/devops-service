package io.choerodon.devops.infra.dto;

import javax.persistence.*;

import io.swagger.annotations.ApiModelProperty;

import io.choerodon.mybatis.entity.BaseDTO;

/**
 * polaris扫描结果的Deployment这一级别的数据
 *
 * @author zmf
 * @since 2/17/20
 */
@Table(name = "devops_polaris_instance_result")
public class DevopsPolarisInstanceResultDTO extends BaseDTO {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @ApiModelProperty("自增id")
    private Long id;

    @ApiModelProperty("环境id / 可为空")
    private Long envId;

    @ApiModelProperty("实例id / 可为空")
    private Long instanceId;

    @ApiModelProperty("集群namespace")
    private String namespace;

    @ApiModelProperty("资源名称")
    private String resourceName;

    @ApiModelProperty("资源类型")
    private String resourceKind;

    @ApiModelProperty("扫描纪录id")
    private Long recordId;

    @ApiModelProperty("此条资源详细扫描纪录id")
    private Long detailId;

    @Transient
    @ApiModelProperty("详情json")
    private String detail;

    public DevopsPolarisInstanceResultDTO() {
    }

    public DevopsPolarisInstanceResultDTO(Long envId, Long instanceId, String namespace, String resourceName, String resourceKind, Long recordId, Long detailId, String detail) {
        this.envId = envId;
        this.instanceId = instanceId;
        this.namespace = namespace;
        this.resourceName = resourceName;
        this.resourceKind = resourceKind;
        this.recordId = recordId;
        this.detailId = detailId;
        this.detail = detail;
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

    public Long getInstanceId() {
        return instanceId;
    }

    public void setInstanceId(Long instanceId) {
        this.instanceId = instanceId;
    }

    public String getNamespace() {
        return namespace;
    }

    public void setNamespace(String namespace) {
        this.namespace = namespace;
    }

    public String getResourceName() {
        return resourceName;
    }

    public void setResourceName(String resourceName) {
        this.resourceName = resourceName;
    }

    public String getResourceKind() {
        return resourceKind;
    }

    public void setResourceKind(String resourceKind) {
        this.resourceKind = resourceKind;
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

    public String getDetail() {
        return detail;
    }

    public void setDetail(String detail) {
        this.detail = detail;
    }
}
