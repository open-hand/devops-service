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
    @ApiModelProperty("主机资源类型")
    private String object_type;
    @ApiModelProperty("主机资源标识，唯一性索引")
    private String object;
    @ApiModelProperty("资源版本")
    private String objectVersion;
    @ApiModelProperty("操作类型")
    private String commandType;
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

    public String getObject_type() {
        return object_type;
    }

    public void setObject_type(String object_type) {
        this.object_type = object_type;
    }

    public String getObject() {
        return object;
    }

    public void setObject(String object) {
        this.object = object;
    }

    public String getObjectVersion() {
        return objectVersion;
    }

    public void setObjectVersion(String objectVersion) {
        this.objectVersion = objectVersion;
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
