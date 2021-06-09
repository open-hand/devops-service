package io.choerodon.devops.api.vo;

import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;

import io.swagger.annotations.ApiModelProperty;

import io.choerodon.mybatis.domain.AuditDomain;

/**
 * 〈功能简述〉
 * 〈〉
 *
 * @author wanghao
 * @since 2021/6/8 11:05
 */
public class DevopsDeploymentVO extends AuditDomain {
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

    private Long envResourceId;

    private Long resourceDetailId;

    public Long getResourceDetailId() {
        return resourceDetailId;
    }

    public void setResourceDetailId(Long resourceDetailId) {
        this.resourceDetailId = resourceDetailId;
    }

    public Long getEnvResourceId() {
        return envResourceId;
    }

    public void setEnvResourceId(Long envResourceId) {
        this.envResourceId = envResourceId;
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
}
