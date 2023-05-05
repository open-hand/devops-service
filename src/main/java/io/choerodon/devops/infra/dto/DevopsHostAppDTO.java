package io.choerodon.devops.infra.dto;

import io.choerodon.devops.infra.enums.deploy.RdupmTypeEnum;
import io.choerodon.mybatis.annotation.ModifyAudit;
import io.choerodon.mybatis.annotation.VersionAudit;
import io.choerodon.mybatis.domain.AuditDomain;
import io.swagger.annotations.ApiModelProperty;

import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.validation.constraints.NotBlank;

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
    /**
     * {@link RdupmTypeEnum}
     */
    @ApiModelProperty("制品类型")
    private String rdupmType;
    /**
     * {@link io.choerodon.devops.infra.enums.deploy.OperationTypeEnum}
     */
    @ApiModelProperty("操作类型")
    private String operationType;
    @ApiModelProperty(value = "部署指令", required = true)
    @NotBlank
    private String runCommand;
    @ApiModelProperty(value = "当前生效的配置id,为docker_compose部署类型时才需要")
    private Long effectValueId;
    @ApiModelProperty(value = "应用版本号，该字段添加时间之前生成的应用版本号为1，之后的版本号为2")
    private String version;
    @ApiModelProperty(value = "工作路径")
    private String workDir;

    public DevopsHostAppDTO() {

    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public DevopsHostAppDTO(Long hostId, String code) {
        this.hostId = hostId;
        this.code = code;
    }

    public DevopsHostAppDTO(Long projectId, Long hostId, String name, String code, String rdupmType, String operationType, String workDir) {
        this.projectId = projectId;
        this.hostId = hostId;
        this.name = name;
        this.code = code;
        this.rdupmType = rdupmType;
        this.operationType = operationType;
        this.workDir = workDir;
    }


    public Long getEffectValueId() {
        return effectValueId;
    }

    public void setEffectValueId(Long effectValueId) {
        this.effectValueId = effectValueId;
    }

    public String getRunCommand() {
        return runCommand;
    }

    public void setRunCommand(String runCommand) {
        this.runCommand = runCommand;
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

    public String getRdupmType() {
        return rdupmType;
    }

    public void setRdupmType(String rdupmType) {
        this.rdupmType = rdupmType;
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

    public String getWorkDir() {
        return workDir;
    }

    public DevopsHostAppDTO setWorkDir(String workDir) {
        this.workDir = workDir;
        return this;
    }
}
