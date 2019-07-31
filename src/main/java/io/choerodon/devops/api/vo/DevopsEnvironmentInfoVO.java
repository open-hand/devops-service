package io.choerodon.devops.api.vo;

import io.choerodon.devops.infra.enums.EnvironmentGitopsStatus;

/**
 * 展示环境信息及一些相关的信息
 *
 * @author zmf
 */
public class DevopsEnvironmentInfoVO {
    private Long id;
    private String code;
    private String name;
    private Boolean connect;
    private Boolean synchronize;
    private Long clusterId;
    private String clusterName;
    private Boolean isSkipCheckPermission;
    /**
     * value from {@link EnvironmentGitopsStatus}
     */
    private String gitopsStatus;

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
        return isSkipCheckPermission;
    }

    public void setSkipCheckPermission(Boolean skipCheckPermission) {
        isSkipCheckPermission = skipCheckPermission;
    }
}
