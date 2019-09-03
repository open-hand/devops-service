package io.choerodon.devops.infra.dto;


import javax.persistence.*;

import io.choerodon.mybatis.entity.BaseDTO;

/**
 * Created by Sheep on 2019/6/26.
 */

@Table(name = "devops_customize_resource")
public class DevopsCustomizeResourceDTO extends BaseDTO {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private Long projectId;
    private Long envId;
    private Long contentId;
    private Long commandId;
    private String k8sKind;
    private String name;
    private String filePath;
    private String description;


    @Transient
    private String commandStatus;
    @Transient
    private String commandError;
    @Transient
    private String resourceContent;

    public DevopsCustomizeResourceDTO() {
    }

    public DevopsCustomizeResourceDTO(Long envId, String k8sKind, String name) {
        this.envId = envId;
        this.k8sKind = k8sKind;
        this.name = name;
    }

    public DevopsCustomizeResourceDTO(Long projectId, Long envId, Long contentId, Long commandId, String k8sKind, String name, String filePath, Long createBy) {
        this.projectId = projectId;
        this.envId = envId;
        this.contentId = contentId;
        this.commandId = commandId;
        this.k8sKind = k8sKind;
        this.name = name;
        this.filePath = filePath;
        super.setCreatedBy(createBy);
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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

    public Long getContentId() {
        return contentId;
    }

    public void setContentId(Long contentId) {
        this.contentId = contentId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Long getCommandId() {
        return commandId;
    }

    public void setCommandId(Long commandId) {
        this.commandId = commandId;
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


    public String getCommandStatus() {
        return commandStatus;
    }

    public void setCommandStatus(String commandStatus) {
        this.commandStatus = commandStatus;
    }

    public String getCommandError() {
        return commandError;
    }

    public void setCommandError(String commandError) {
        this.commandError = commandError;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public String getResourceContent() {
        return resourceContent;
    }

    public void setResourceContent(String resourceContent) {
        this.resourceContent = resourceContent;
    }
}
