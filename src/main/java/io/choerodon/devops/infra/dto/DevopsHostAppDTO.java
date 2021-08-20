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
 * 〈功能简述〉
 * 〈〉
 *
 * @author wanghao
 * @Date 2021/7/1 9:22
 */
@Table(name = "devops_host_app")
@ModifyAudit
@VersionAudit
public class DevopsHostAppDTO extends AuditDomain {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @ApiModelProperty("项目id")
    private Long projectId;
    @ApiModelProperty("主机id")
    private Long hostId;
    @ApiModelProperty("应用名称")
    private String name;
    @ApiModelProperty("应用编码")
    private String code;
    @ApiModelProperty("进程id")
    private String pid;
    @ApiModelProperty("占用端口")
    private String ports;
    @ApiModelProperty("部署来源")
    private String sourceType;
    @ApiModelProperty("进程状态")
    private String status;
    @ApiModelProperty("制品类型")
    private String rdupmType;
    /**
     * {@link io.choerodon.devops.infra.enums.deploy.OperationTypeEnum}
     */
    @ApiModelProperty("操作类型")
    private String operationType;
    @ApiModelProperty("来源配置")
    private String sourceConfig;
    @ApiModelProperty("部署配置")
    private String value;


    public DevopsHostAppDTO() {
    }

    public DevopsHostAppDTO(Long hostId, String name) {
        this.hostId = hostId;
        this.name = name;
    }

    public DevopsHostAppDTO(Long hostId, String name, String sourceType, String rdupmType, String operationType, String sourceConfig, String value) {
        this.hostId = hostId;
        this.name = name;
        this.sourceType = sourceType;
        this.rdupmType = rdupmType;
        this.operationType = operationType;
        this.sourceConfig = sourceConfig;
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public Long getProjectId() {
        return projectId;
    }

    public void setProjectId(Long projectId) {
        this.projectId = projectId;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getOperationType() {
        return operationType;
    }

    public void setOperationType(String operationType) {
        this.operationType = operationType;
    }

    public String getSourceConfig() {
        return sourceConfig;
    }

    public void setSourceConfig(String sourceConfig) {
        this.sourceConfig = sourceConfig;
    }

    public String getRdupmType() {
        return rdupmType;
    }

    public void setRdupmType(String rdupmType) {
        this.rdupmType = rdupmType;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
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

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPid() {
        return pid;
    }

    public void setPid(String pid) {
        this.pid = pid;
    }

    public String getPorts() {
        return ports;
    }

    public void setPorts(String ports) {
        this.ports = ports;
    }

    public String getSourceType() {
        return sourceType;
    }

    public void setSourceType(String sourceType) {
        this.sourceType = sourceType;
    }
}
