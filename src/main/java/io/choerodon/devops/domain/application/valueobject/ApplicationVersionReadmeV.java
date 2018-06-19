package io.choerodon.devops.domain.application.valueobject;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Component
@Scope("prototype")
public class ApplicationVersionReadmeV {

    private Long id;
    private Long versionId;
    private String readme;


    public ApplicationVersionReadmeV() {
    }

    public ApplicationVersionReadmeV(Long versionId) {
        this.versionId = versionId;
    }

    public ApplicationVersionReadmeV(String readme) {
        this.readme = readme;
    }

    public ApplicationVersionReadmeV(Long versionId, String readme) {
        this.versionId = versionId;
        this.readme = readme;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getVersionId() {
        return versionId;
    }

    public void setVersionId(Long versionId) {
        this.versionId = versionId;
    }

    public String getReadme() {
        return readme;
    }

    public void setReadme(String readme) {
        this.readme = readme;
    }
}
