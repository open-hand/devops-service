package io.choerodon.devops.domain.application.valueobject;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Component
@Scope("prototype")
public class ApplicationVersionReadmeV {

    private Long id;
    private String readme;


    public ApplicationVersionReadmeV() {
    }

    public ApplicationVersionReadmeV(Long id) {
        this.id = id;
    }

    public ApplicationVersionReadmeV(String readme, Long id) {
        this.readme = readme;
        this.id = id;
    }

    public ApplicationVersionReadmeV(String readme) {
        this.readme = readme;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }


    public String getReadme() {
        return readme;
    }

    public void setReadme(String readme) {
        this.readme = readme;
    }
}
