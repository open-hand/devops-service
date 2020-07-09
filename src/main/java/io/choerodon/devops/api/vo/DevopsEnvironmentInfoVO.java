package io.choerodon.devops.api.vo;

import io.choerodon.devops.infra.dto.DevopsEnvironmentDTO;
import io.choerodon.devops.infra.enums.EnvironmentGitopsStatus;
import io.swagger.annotations.ApiModelProperty;

/**
 * 展示环境信息及一些相关的信息
 *
 * @author zmf
 */
public class DevopsEnvironmentInfoVO {
//    @Encrypt(DevopsEnvironmentDTO.ENCRYPT_KEY)
    private Long id;
    private String code;
    private String name;
    private Boolean connect;
    private Boolean synchronize;
    private Long clusterId;
    private String clusterName;
    private Boolean skipCheckPermission;
    private String description;
    private Long objectVersionNumber;
    private Boolean active;
    @ApiModelProperty("环境是否失败")
    private Boolean fail;
    private Long groupId;
    private String gitlabUrl;
    /**
     * value from {@link EnvironmentGitopsStatus}
     */
    private String gitopsStatus;

    public Boolean getFail() {
        return fail;
    }

    public void setFail(Boolean fail) {
        this.fail = fail;
    }

    public Long getGroupId() {
        return groupId;
    }

    public void setGroupId(Long groupId) {
        this.groupId = groupId;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Boolean getConnect() {
        return connect;
    }

    public void setConnect(Boolean connect) {
        this.connect = connect;
    }

    public Boolean getSynchronize() {
        return synchronize;
    }

    public void setSynchronize(Boolean synchronize) {
        this.synchronize = synchronize;
    }

    public Long getClusterId() {
        return clusterId;
    }

    public void setClusterId(Long clusterId) {
        this.clusterId = clusterId;
    }

    public String getClusterName() {
        return clusterName;
    }

    public void setClusterName(String clusterName) {
        this.clusterName = clusterName;
    }

    public String getGitopsStatus() {
        return gitopsStatus;
    }

    public void setGitopsStatus(String gitopsStatus) {
        this.gitopsStatus = gitopsStatus;
    }

    public Boolean getSkipCheckPermission() {
        return skipCheckPermission;
    }

    public void setSkipCheckPermission(Boolean skipCheckPermission) {
        this.skipCheckPermission = skipCheckPermission;
    }

    public Long getObjectVersionNumber() {
        return objectVersionNumber;
    }

    public void setObjectVersionNumber(Long objectVersionNumber) {
        this.objectVersionNumber = objectVersionNumber;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Boolean getActive() {
        return active;
    }

    public void setActive(Boolean active) {
        this.active = active;
    }

    public String getGitlabUrl() {
        return gitlabUrl;
    }

    public void setGitlabUrl(String gitlabUrl) {
        this.gitlabUrl = gitlabUrl;
    }
}
