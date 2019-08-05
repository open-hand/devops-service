package io.choerodon.devops.api.vo;

import io.choerodon.devops.api.vo.ProjectVO;
import io.choerodon.devops.infra.enums.Visibility;

/**
 * Created by younger on 2018/3/29.
 */
public class DevopsProjectVO {
    private Integer id;
    private Long devopsAppGroupId;
    private Long devopsEnvGroupId;
    private String path;
    private String name;
    private Visibility visibility;
    private ProjectVO projectE;
    private Boolean harborProjectIsPrivate;
    private String harborProjectUserName;
    private String harborProjectUserPassword;
    private String harborProjectUserEmail;


    public Long getDevopsAppGroupId() {
        return devopsAppGroupId;
    }

    public void setDevopsAppGroupId(Long devopsAppGroupId) {
        this.devopsAppGroupId = devopsAppGroupId;
    }

    public Long getDevopsEnvGroupId() {
        return devopsEnvGroupId;
    }

    public void setDevopsEnvGroupId(Long devopsEnvGroupId) {
        this.devopsEnvGroupId = devopsEnvGroupId;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public ProjectVO getProjectE() {
        return projectE;
    }

    public void setProjectE(ProjectVO projectE) {
        this.projectE = projectE;
    }

    public Visibility getVisibility() {
        return visibility;
    }

    public void setVisibility(Visibility visibility) {
        this.visibility = visibility;
    }

    public void initProjectE(Long id) {
        this.projectE = new ProjectVO(id);
    }

    public void initName(String name) {
        this.name = name;
    }

    public void initPath(String path) {
        this.path = path;
    }

    public void initVisibility(Visibility visibility) {
        this.visibility = visibility;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }


    public String getHarborProjectUserName() {
        return harborProjectUserName;
    }

    public void setHarborProjectUserName(String harborProjectUserName) {
        this.harborProjectUserName = harborProjectUserName;
    }

    public String getHarborProjectUserPassword() {
        return harborProjectUserPassword;
    }

    public void setHarborProjectUserPassword(String harborProjectUserPassword) {
        this.harborProjectUserPassword = harborProjectUserPassword;
    }

    public String getHarborProjectUserEmail() {
        return harborProjectUserEmail;
    }

    public void setHarborProjectUserEmail(String harborProjectUserEmail) {
        this.harborProjectUserEmail = harborProjectUserEmail;
    }

    public Boolean getHarborProjectIsPrivate() {
        return harborProjectIsPrivate;
    }

    public void setHarborProjectIsPrivate(Boolean harborProjectIsPrivate) {
        this.harborProjectIsPrivate = harborProjectIsPrivate;
    }
}
