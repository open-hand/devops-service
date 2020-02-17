package io.choerodon.devops.infra.dto;

import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import io.swagger.annotations.ApiModelProperty;

import io.choerodon.mybatis.entity.BaseDTO;

/**
 * @author zmf
 * @since 2/17/20
 */
@Table(name = "devops_polaris_item")
public class DevopsPolarisItemDTO extends BaseDTO {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @ApiModelProperty("自增主键")
    private Long id;

    @ApiModelProperty("环境id / 可为空")
    private Long envId;

    @ApiModelProperty("集群namespace")
    private String namespace;

    @ApiModelProperty("资源名称")
    private String resourceName;

    @ApiModelProperty("资源类型")
    private String resourceKind;

    @ApiModelProperty("重视程度, 忽视/警告/报错等")
    private String severity;

    @ApiModelProperty("是否通过此校验")
    private Boolean isApproved;

    @ApiModelProperty("扫描纪录id")
    private Long recordId;

    @ApiModelProperty("item的类型名称")
    private String type;

    @ApiModelProperty("这个type所属的类型")
    private String category;

    @ApiModelProperty("通过/未通过这一项时的message")
    private String message;

    public DevopsPolarisItemDTO() {
    }

    public DevopsPolarisItemDTO(Long envId, String namespace, String resourceName, String resourceKind, Long recordId) {
        this.envId = envId;
        this.namespace = namespace;
        this.resourceName = resourceName;
        this.resourceKind = resourceKind;
        this.recordId = recordId;
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

    public String getSeverity() {
        return severity;
    }

    public void setSeverity(String severity) {
        this.severity = severity;
    }

    public Boolean getApproved() {
        return isApproved;
    }

    public void setApproved(Boolean approved) {
        isApproved = approved;
    }

    public Long getRecordId() {
        return recordId;
    }

    public void setRecordId(Long recordId) {
        this.recordId = recordId;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
