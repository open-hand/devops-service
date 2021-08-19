package io.choerodon.devops.infra.dto;

import io.swagger.annotations.ApiModelProperty;
import org.hzero.starter.keyencrypt.core.Encrypt;

import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import java.util.Date;

/**
 * @author shanyu
 * @date 2021/8/18
 */
public class DevopsDeployAppCenterHostDTO {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @ApiModelProperty("应用服务名称")
    private String name;

    @ApiModelProperty("应用服务code")
    private String code;

    @ApiModelProperty("应用服务所属项目id")
    private Long projectId;

    @ApiModelProperty("对象id")
    private Long objectId;

    @ApiModelProperty("环境Id")
    @Encrypt
    private Long hostId;

    @ApiModelProperty("部署操作类型")
    private String operationType;

    @ApiModelProperty("部署制品类型")
    private String rdupmType;

    @ApiModelProperty("制品来源")
    private String jarSource;

    @ApiModelProperty("应用服务数据库纪录的版本号")
    private Long objectVersionNumber;

    @ApiModelProperty("应用创建时间")
    private Date creationDate;

    @ApiModelProperty("应用服务最近更新时间")
    private Date lastUpdateDate;

    @ApiModelProperty("创建者用户名")
    private String createdBy;

    @ApiModelProperty("最近更新者用户名")
    private String lastUpdatedBy;

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

    public String getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    public String getLastUpdatedBy() {
        return lastUpdatedBy;
    }

    public void setLastUpdatedBy(String lastUpdatedBy) {
        this.lastUpdatedBy = lastUpdatedBy;
    }
}

