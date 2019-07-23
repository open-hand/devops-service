package io.choerodon.devops.domain.application.valueobject;


import java.util.Date;

import io.choerodon.devops.infra.dto.DevopsCustomizeResourceContentDTO;
import io.choerodon.devops.infra.dto.DevopsEnvCommandDTO;

/**
 * Created by Sheep on 2019/6/26.
 */

public class DevopsCustomizeResource {

    private Long Id;
    private Long projectId;
    private Long envId;
    private DevopsCustomizeResourceContentDTO devopsCustomizeResourceContentDTO;
    private String k8sKind;
    private String filePath;
    private DevopsEnvCommandDTO devopsEnvCommandDTO;
    private String name;
    private String description;
    private Long createBy;
    private Long lastUpdateBy;
    private Date lastUpdateDate;


    public DevopsCustomizeResource() {
    }


    public DevopsCustomizeResource(Long projectId, Long envId, Long contentId, Long commandId, String k8sKind, String name, String filePath, Long createBy) {
        this.projectId = projectId;
        this.envId = envId;
        this.devopsCustomizeResourceContentDTO = new DevopsCustomizeResourceContentDTO();
        this.devopsCustomizeResourceContentDTO.setId(contentId);
        this.devopsEnvCommandDTO = new DevopsEnvCommandDTO();
        this.devopsEnvCommandDTO.setId(commandId);
        this.k8sKind = k8sKind;
        this.name = name;
        this.filePath = filePath;
        this.createBy = createBy;
    }

    public DevopsEnvCommandDTO getDevopsEnvCommandDTO() {
        return devopsEnvCommandDTO;
    }

    public void setDevopsEnvCommandDTO(DevopsEnvCommandDTO devopsEnvCommandDTO) {
        this.devopsEnvCommandDTO = devopsEnvCommandDTO;
    }

    public Long getId() {
        return Id;
    }

    public void setId(Long id) {
        Id = id;
    }

    public Long getProjectId() {
        return projectId;
    }

    public void setProjectId(Long projectId) {
        this.projectId = projectId;
    }

    public Long getEnvId() {
        return envId;
    }

    public void setEnvId(Long envId) {
        this.envId = envId;
    }

    public DevopsCustomizeResourceContentDTO getDevopsCustomizeResourceContentDTO() {
        return devopsCustomizeResourceContentDTO;
    }

    public void setDevopsCustomizeResourceContentDTO(DevopsCustomizeResourceContentDTO devopsCustomizeResourceContentDTO) {
        this.devopsCustomizeResourceContentDTO = devopsCustomizeResourceContentDTO;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getK8sKind() {
        return k8sKind;
    }

    public void setK8sKind(String k8sKind) {
        this.k8sKind = k8sKind;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public Long getCreateBy() {
        return createBy;
    }

    public void setCreateBy(Long createBy) {
        this.createBy = createBy;
    }

    public Long getLastUpdateBy() {
        return lastUpdateBy;
    }

    public void setLastUpdateBy(Long lastUpdateBy) {
        this.lastUpdateBy = lastUpdateBy;
    }

    public Date getLastUpdateDate() {
        return lastUpdateDate;
    }

    public void setLastUpdateDate(Date lastUpdateDate) {
        this.lastUpdateDate = lastUpdateDate;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DevopsCustomizeResource that = (DevopsCustomizeResource) o;
        return k8sKind.equals(that.k8sKind) &&
                name.equals(that.name);
    }
}
