package io.choerodon.devops.api.vo;

import io.choerodon.devops.infra.dto.AppServiceVersionDTO;

import java.util.List;
import java.util.Objects;

/**
 * @author zhaotianxin
 * @since 2019/8/13
 */
public class AppServiceGroupInfoVO {
    private Long id;
    private String name;
    private String code;
    private Long projectId;
    private String type;
    private List<AppServiceVersionDTO> versions;
    private Boolean share;
    private String  projectName;

    public List<AppServiceVersionDTO> getVersions() {
        return versions;
    }

    public void setVersions(List<AppServiceVersionDTO> versions) {
        this.versions = versions;
    }

    public Boolean getShare() {
        return share;
    }

    public void setShare(Boolean share) {
        this.share = share;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

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

    public String getProjectName() {
        return projectName;
    }

    public void setProjectName(String projectName) {
        this.projectName = projectName;
    }
}
