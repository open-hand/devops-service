package io.choerodon.devops.api.vo;

public class DevopsMergeRequestVO {

    private String objectKind;

    private GitlabUserVO user;

    private ProjectReqVO project;

    private ObjectAttributesVO objectAttributes;

    public ProjectReqVO getProject() {
        return project;
    }

    public void setProject(ProjectReqVO project) {
        this.project = project;
    }

    public ObjectAttributesVO getObjectAttributes() {
        return objectAttributes;
    }

    public void setObjectAttributes(ObjectAttributesVO objectAttributesDTODTO) {
        this.objectAttributes = objectAttributesDTODTO;
    }

    public String getObjectKind() {
        return objectKind;
    }

    public void setObjectKind(String objectKind) {
        this.objectKind = objectKind;
    }

    public GitlabUserVO getUser() {
        return user;
    }

    public void setUser(GitlabUserVO user) {
        this.user = user;
    }
}
