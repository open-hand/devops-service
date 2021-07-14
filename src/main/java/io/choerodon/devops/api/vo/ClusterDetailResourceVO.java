package io.choerodon.devops.api.vo;

import io.choerodon.devops.infra.dto.DevopsClusterDTO;
import java.util.Date;
import org.hzero.starter.keyencrypt.core.Encrypt;

import javax.validation.constraints.NotNull;

/**
 * 集群资源资源VO
 *
 * @author xingxingwu.hand-china.com 2021/07/14 16:17
 */
public class ClusterDetailResourceVO {
    @Encrypt
    private Long id;
    private Long organizationId;
    private Long projectId;
    private String projectIdName;
    private String name;
    private String code;
    private String type;
    private String status;
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
