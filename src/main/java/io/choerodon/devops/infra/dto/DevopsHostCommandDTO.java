package io.choerodon.devops.infra.dto;

import io.choerodon.mybatis.annotation.ModifyAudit;
import io.choerodon.mybatis.annotation.VersionAudit;
import io.choerodon.mybatis.domain.AuditDomain;
import io.swagger.annotations.ApiModelProperty;

import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;

/**
 * 〈功能简述〉
 * 〈〉
 *
 * @author wanghao
 * @Date 2021/6/28 11:41
 */
@Table(name = "devops_host_command")
@ModifyAudit
@VersionAudit
public class DevopsHostCommandDTO extends AuditDomain {

    @Id
    @GeneratedValue
    private Long id;
    @ApiModelProperty("操作主机id")
    private Long hostId;
    /**
     * {@link io.choerodon.devops.infra.enums.host.HostResourceType}
     */
    @ApiModelProperty("实例类型,docker、jar")
    private String instanceType;
    @ApiModelProperty("实例id")
    private Long instanceId;

    /**
     * {@link io.choerodon.devops.infra.enums.host.HostCommandEnum}
     */
    @ApiModelProperty("操作类型")
    private String commandType;
    /**
     * {@link io.choerodon.devops.infra.enums.host.HostCommandStatusEnum}
     */
    @ApiModelProperty("操作状态")
    private String status;
    @ApiModelProperty("错误信息")
    private String error;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getHostId() {
        return hostId;
    }

    public void setHostId(Long hostId) {
        this.hostId = hostId;
    }

    public String getInstanceType() {
        return instanceType;
    }

    public void setInstanceType(String instanceType) {
        this.instanceType = instanceType;
    }

    public Long getInstanceId() {
        return instanceId;
    }

    public void setInstanceId(Long instanceId) {
        this.instanceId = instanceId;
    }

    public String getCommandType() {
        return commandType;
    }

    public void setCommandType(String commandType) {
        this.commandType = commandType;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }
}
