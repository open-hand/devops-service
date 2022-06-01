package io.choerodon.devops.api.vo;

import java.util.Date;
import javax.validation.constraints.NotNull;

import io.swagger.annotations.ApiModelProperty;
import org.hzero.starter.keyencrypt.core.Encrypt;

import io.choerodon.devops.infra.dto.DevopsClusterDTO;

/**
 * 集群资源资源VO
 *
 * @author xingxingwu.hand-china.com 2021/07/14 16:17
 */
public class ClusterDetailResourceVO {
    @Encrypt
    private Long id;
    @ApiModelProperty("组织id")
    private Long organizationId;
    @ApiModelProperty("项目id")
    private Long projectId;
    @ApiModelProperty("项目id或名称")
    private String projectIdName;
    @ApiModelProperty("集群名称")
    private String name;
    @ApiModelProperty("集群编码")
    private String code;
    @ApiModelProperty("集群类型")
    private String type;
    @ApiModelProperty("集群连接状态")
    private String status;
    @ApiModelProperty("集群创建日期")
    private Date creationDate;

    public static ClusterDetailResourceVO build(@NotNull DevopsClusterDTO clusterDTO, String projectIdName) {
        ClusterDetailResourceVO clusterDetailResourceVO = new ClusterDetailResourceVO();
        clusterDetailResourceVO.id = clusterDTO.getId();
        clusterDetailResourceVO.organizationId = clusterDTO.getOrganizationId();
        clusterDetailResourceVO.projectId = clusterDTO.getProjectId();
        clusterDetailResourceVO.projectIdName = projectIdName;
        clusterDetailResourceVO.name = clusterDTO.getName();
        clusterDetailResourceVO.code = clusterDTO.getCode();
        clusterDetailResourceVO.type = clusterDTO.getType();
        clusterDetailResourceVO.status = clusterDTO.getStatus();
        clusterDetailResourceVO.creationDate = clusterDTO.getCreationDate();
        return clusterDetailResourceVO;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getOrganizationId() {
        return organizationId;
    }

    public void setOrganizationId(Long organizationId) {
        this.organizationId = organizationId;
    }

    public Long getProjectId() {
        return projectId;
    }

    public void setProjectId(Long projectId) {
        this.projectId = projectId;
    }

    public String getProjectIdName() {
        return projectIdName;
    }

    public void setProjectIdName(String projectIdName) {
        this.projectIdName = projectIdName;
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

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Date getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(Date creationDate) {
        this.creationDate = creationDate;
    }
}
