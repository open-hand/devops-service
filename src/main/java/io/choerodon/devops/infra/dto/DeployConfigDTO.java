package io.choerodon.devops.infra.dto;

import io.choerodon.mybatis.annotation.ModifyAudit;
import io.choerodon.mybatis.annotation.VersionAudit;
import io.choerodon.mybatis.domain.AuditDomain;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import org.hzero.starter.keyencrypt.core.Encrypt;

import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

/**
 * 主机部署文件配置表
 *
 * @author jian.zhang02@hand-china.com 2021-08-19 15:43:01
 */
@ApiModel("主机部署文件配置表")
@VersionAudit
@ModifyAudit
@Table(name = "devops_deploy_config")
public class DeployConfigDTO extends AuditDomain {

    public static final String FIELD_ID = "id";
    public static final String FIELD_PROJECT_ID = "projectId";
    public static final String FIELD_DEPLOY_RECORD_ID = "deployRecordId";
    public static final String FIELD_HOST_ID = "hostId";
    public static final String FIELD_DEPLOY_OBJECT_KEY = "deployObjectKey";
    public static final String FIELD_INSTANCE_NAME = "instanceName";
    public static final String FIELD_CONFIG_ID = "configId";
    public static final String FIELD_MOUNT_PATH = "mountPath";
    public static final String FIELD_CONFIG_GROUP = "configGroup";
    public static final String FIELD_CONFIG_CODE = "configCode";

    //
    // 业务方法(按public protected private顺序排列)
    // ------------------------------------------------------------------------------

    //
    // 数据库字段
    // ------------------------------------------------------------------------------


    @ApiModelProperty("主键ID")
    @Id
    @GeneratedValue
    @Encrypt
    private Long id;
    @ApiModelProperty("租户ID")
    @NotNull
    private Long organizationId;
    @ApiModelProperty(value = "项目ID", required = true)
    @NotNull
    private Long projectId;
    @ApiModelProperty(value = "部署记录ID", required = true)
    @NotNull
    private Long deployRecordId;
    @ApiModelProperty(value = "主机ID", required = true)
    @NotNull
    private Long hostId;
    @ApiModelProperty(value = "部署对象", required = true)
    private String deployObjectKey;
    @ApiModelProperty(value = "实例ID", required = true)
    @NotBlank
    private Long instanceId;
    @ApiModelProperty(value = "实例名称", required = true)
    @NotBlank
    private String instanceName;
    @ApiModelProperty(value = "配置文件ID", required = true)
    @NotNull
    private Long configId;
    @ApiModelProperty(value = "挂载路径", required = true)
    @NotBlank
    private String mountPath;
    @ApiModelProperty(value = "配置分组", required = true)
    @NotBlank
    private String configGroup;
    @ApiModelProperty(value = "配置编码", required = true)
    @NotBlank
    private String configCode;

	//
    // 非数据库字段
    // ------------------------------------------------------------------------------
    //
    // getter/setter
    // ------------------------------------------------------------------------------

    /**
     * @return 主键ID
     */
	public Long getId() {
		return id;
	}

	public DeployConfigDTO setId(Long id) {
		this.id = id;
        return this;
	}

    public Long getOrganizationId() {
        return organizationId;
    }

    public DeployConfigDTO setOrganizationId(Long organizationId) {
        this.organizationId = organizationId;
        return this;
    }

    /**
     * @return 项目ID
     */
	public Long getProjectId() {
		return projectId;
	}

	public DeployConfigDTO setProjectId(Long projectId) {
		this.projectId = projectId;
        return this;
	}
    /**
     * @return 部署记录ID
     */
	public Long getDeployRecordId() {
		return deployRecordId;
	}

	public DeployConfigDTO setDeployRecordId(Long deployRecordId) {
		this.deployRecordId = deployRecordId;
        return this;
	}
    /**
     * @return 主机ID
     */
	public Long getHostId() {
		return hostId;
	}

	public DeployConfigDTO setHostId(Long hostId) {
		this.hostId = hostId;
        return this;
	}
    /**
     * @return 部署对象
     */
	public String getDeployObjectKey() {
		return deployObjectKey;
	}

	public DeployConfigDTO setDeployObjectKey(String deployObjectKey) {
		this.deployObjectKey = deployObjectKey;
        return this;
	}

    public Long getInstanceId() {
        return instanceId;
    }

    public DeployConfigDTO setInstanceId(Long instanceId) {
        this.instanceId = instanceId;
        return this;
    }

    /**
     * @return 实例名称
     */
	public String getInstanceName() {
		return instanceName;
	}

	public DeployConfigDTO setInstanceName(String instanceName) {
		this.instanceName = instanceName;
        return this;
	}
    /**
     * @return 配置文件ID
     */
	public Long getConfigId() {
		return configId;
	}

	public DeployConfigDTO setConfigId(Long configId) {
		this.configId = configId;
        return this;
	}
    /**
     * @return 挂载路径
     */
	public String getMountPath() {
		return mountPath;
	}

	public DeployConfigDTO setMountPath(String mountPath) {
		this.mountPath = mountPath;
        return this;
	}
    /**
     * @return 配置分组
     */
	public String getConfigGroup() {
		return configGroup;
	}

	public DeployConfigDTO setConfigGroup(String configGroup) {
		this.configGroup = configGroup;
        return this;
	}
    /**
     * @return 配置编码
     */
	public String getConfigCode() {
		return configCode;
	}

	public DeployConfigDTO setConfigCode(String configCode) {
		this.configCode = configCode;
        return this;
	}

}

