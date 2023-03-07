package io.choerodon.devops.infra.dto;

import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;

import io.swagger.annotations.ApiModelProperty;
import org.hzero.starter.keyencrypt.core.Encrypt;

import io.choerodon.devops.infra.enums.host.HostResourceType;
import io.choerodon.mybatis.annotation.ModifyAudit;
import io.choerodon.mybatis.annotation.VersionAudit;
import io.choerodon.mybatis.domain.AuditDomain;

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
    @Encrypt
    @GeneratedValue
    private Long id;
    @ApiModelProperty("操作主机id")
    @Encrypt
    private Long hostId;
    /**
     * {@link HostResourceType}
     */
    @ApiModelProperty("实例类型,docker、jar")
    private String instanceType;
    @ApiModelProperty("实例id")
    @Encrypt
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
    @Encrypt
    @ApiModelProperty("操作关联的流水线任务记录id/nullable, 不为空则说明操作关联了流水线任务")
    private Long cdJobRecordId;

    @Encrypt
    @ApiModelProperty("操作关联的流水线记录id/nullable, 不为空则说明操作关联了流水线任务")
    private Long ciPipelineRecordId;

    public DevopsHostCommandDTO() {
    }

    public DevopsHostCommandDTO(Long hostId, String instanceType, Long instanceId, String commandType, String status) {
        this.hostId = hostId;
        this.instanceType = instanceType;
        this.instanceId = instanceId;
        this.commandType = commandType;
        this.status = status;
    }

    public Long getCdJobRecordId() {
        return cdJobRecordId;
    }

    public void setCdJobRecordId(Long cdJobRecordId) {
        this.cdJobRecordId = cdJobRecordId;
    }

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

    public Long getCiPipelineRecordId() {
        return ciPipelineRecordId;
    }

    public void setCiPipelineRecordId(Long ciPipelineRecordId) {
        this.ciPipelineRecordId = ciPipelineRecordId;
    }
}
