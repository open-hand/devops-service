package io.choerodon.devops.infra.dto;

import javax.persistence.Id;
import javax.persistence.Table;

import io.choerodon.mybatis.domain.AuditDomain;
import io.swagger.annotations.ApiModelProperty;

import io.choerodon.mybatis.annotation.ModifyAudit;
import io.choerodon.mybatis.annotation.VersionAudit;

/**
 * devops主机部署应用表
 */
@Table(name = "devops_deploy_app_center_host")
@ModifyAudit
@VersionAudit
public class DevopsDeployAppCenterHostDTO extends AuditDomain {
    @Id
    private Long id;

    @ApiModelProperty(name = "name")
    private String name;

    @ApiModelProperty(name = "code")
    private String code;

    @ApiModelProperty(name = "项目id")
    private Long projectId;

    @ApiModelProperty(name = "部署对象id")
    private Long objectId;

    @ApiModelProperty(name = "hostId")
    private Long hostId;

    @ApiModelProperty(name = "操作类型")
    private String operationType;

    @ApiModelProperty(name = "jar类型")
    private String jarSource;

    @ApiModelProperty(name = "chart/jar/docker")
    private String rdupmType;


    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public Long getProjectId() {
        return projectId;
    }

    public void setProjectId(Long projectId) {
        this.projectId = projectId;
    }

    public Long getObjectId() {
        return objectId;
    }

    public void setObjectId(Long objectId) {
        this.objectId = objectId;
    }

    public Long getHostId() {
        return hostId;
    }

    public void setHostId(Long hostId) {
        this.hostId = hostId;
    }

    public String getOperationType() {
        return operationType;
    }

    public void setOperationType(String operationType) {
        this.operationType = operationType;
    }

    public String getRdupmType() {
        return rdupmType;
    }

    public void setRdupmType(String rdupmType) {
        this.rdupmType = rdupmType;
    }

    public String getJarSource() {
        return jarSource;
    }

    public void setJarSource(String jarSource) {
        this.jarSource = jarSource;
    }
}
