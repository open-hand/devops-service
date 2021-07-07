package io.choerodon.devops.infra.dto;

import javax.persistence.*;

import io.swagger.annotations.ApiModelProperty;

import io.choerodon.mybatis.annotation.ModifyAudit;
import io.choerodon.mybatis.annotation.VersionAudit;
import io.choerodon.mybatis.domain.AuditDomain;

/**
 * 〈功能简述〉
 * 〈〉
 *
 * @author wanghao
 * @since 2021/6/8 11:05
 */
@Table(name = "devops_deployment")
@ModifyAudit
@VersionAudit
public class DevopsDeploymentDTO extends AuditDomain {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
    @ApiModelProperty("deployment名称")
    private String name;
    @ApiModelProperty("所属项目id")
    private Long projectId;
    @ApiModelProperty("部署环境id")
    private Long envId;
    @ApiModelProperty("commandId")
    private Long commandId;
    @ApiModelProperty("所属实例id")
    private Long instanceId;

    @Transient
    private String content;

    public DevopsDeploymentDTO() {
    }

    public DevopsDeploymentDTO(String name, Long projectId, Long envId, Long commandId) {
        this.name = name;
        this.projectId = projectId;
        this.envId = envId;
        this.commandId = commandId;
    }

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

    public Long getProjectId() {
        return projectId;
    }

    public void setProjectId(Long projectId) {
        this.projectId = projectId;
    }

    public Long getEnvId() {
        return envId;
    }

    public void setEnvId(Long envId) {
        this.envId = envId;
    }

    public Long getCommandId() {
        return commandId;
    }

    public void setCommandId(Long commandId) {
        this.commandId = commandId;
    }

    public Long getInstanceId() {
        return instanceId;
    }

    public void setInstanceId(Long instanceId) {
        this.instanceId = instanceId;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }
}
