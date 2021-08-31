package io.choerodon.devops.api.vo;

import io.choerodon.devops.infra.dto.iam.IamUserDTO;
import io.swagger.annotations.ApiModelProperty;
import org.hzero.starter.keyencrypt.core.Encrypt;

import java.util.Date;

/**
 * @author shanyu
 * @date 2021/8/18
 */
public class DevopsDeployAppCenterVO {
    @ApiModelProperty("应用服务id")
    @Encrypt
    private Long id;

    @ApiModelProperty("应用服务名称")
    private String name;

    @ApiModelProperty("应用服务code")
    private String code;

    @ApiModelProperty("应用服务所属项目id")
    @Encrypt
    private Long projectId;

    @ApiModelProperty("对象id")
    @Encrypt
    private Long objectId;

    @ApiModelProperty("环境Id")
    @Encrypt
    private Long envId;

    @ApiModelProperty("主机Id")
    @Encrypt
    private Long hostId;

    @ApiModelProperty("部署操作类型")
    private String operationType;

    @ApiModelProperty("部署制品类型")
    private String rdupmType;

    @ApiModelProperty("环境名称")
    private String envName;

    @ApiModelProperty("制品来源")
    private String chartSource;

    @ApiModelProperty("应用状态")
    private String status;

    private Boolean envActive;
    private Boolean envConnected;

    @ApiModelProperty("实例的所有pod数量")
    private Integer podCount;

    @ApiModelProperty("实例的运行中的pod的数量")
    private Integer podRunningCount;

    @ApiModelProperty("应用服务数据库纪录的版本号")
    private Long objectVersionNumber;

    @ApiModelProperty("应用创建时间")
    private Date creationDate;

    @ApiModelProperty("应用服务最近更新时间")
    private Date lastUpdateDate;

    @ApiModelProperty("创建者用户名")
    private Long createdBy;

    @ApiModelProperty("最近更新者用户名")
    private Long lastUpdatedBy;

    private IamUserDTO creator;

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

    public Long getEnvId() {
        return envId;
    }

    public void setEnvId(Long envId) {
        this.envId = envId;
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

    public String getEnvName() {
        return envName;
    }

    public void setEnvName(String envName) {
        this.envName = envName;
    }

    public String getChartSource() {
        return chartSource;
    }

    public void setChartSource(String chartSource) {
        this.chartSource = chartSource;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Long getObjectVersionNumber() {
        return objectVersionNumber;
    }

    public void setObjectVersionNumber(Long objectVersionNumber) {
        this.objectVersionNumber = objectVersionNumber;
    }

    public Date getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(Date creationDate) {
        this.creationDate = creationDate;
    }

    public Date getLastUpdateDate() {
        return lastUpdateDate;
    }

    public void setLastUpdateDate(Date lastUpdateDate) {
        this.lastUpdateDate = lastUpdateDate;
    }

    public Long getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(Long createdBy) {
        this.createdBy = createdBy;
    }

    public Long getLastUpdatedBy() {
        return lastUpdatedBy;
    }

    public void setLastUpdatedBy(Long lastUpdatedBy) {
        this.lastUpdatedBy = lastUpdatedBy;
    }

    public IamUserDTO getCreator() {
        return creator;
    }

    public void setCreator(IamUserDTO creator) {
        this.creator = creator;
    }

    public Boolean getEnvActive() {
        return envActive;
    }

    public void setEnvActive(Boolean envActive) {
        this.envActive = envActive;
    }

    public Boolean getEnvConnected() {
        return envConnected;
    }

    public void setEnvConnected(Boolean envConnected) {
        this.envConnected = envConnected;
    }

    public Integer getPodCount() {
        return podCount;
    }

    public void setPodCount(Integer podCount) {
        this.podCount = podCount;
    }

    public Integer getPodRunningCount() {
        return podRunningCount;
    }

    public void setPodRunningCount(Integer podRunningCount) {
        this.podRunningCount = podRunningCount;
    }
}

