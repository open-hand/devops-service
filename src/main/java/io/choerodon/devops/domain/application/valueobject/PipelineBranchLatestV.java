package io.choerodon.devops.domain.application.valueobject;

/**
 * Created by Zenger on 2018/4/14.
 */
public class PipelineBranchLatestV {

    private Long id;
    private String ref;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getRef() {
        return ref;
    }

    public void setRef(String ref) {
        this.ref = ref;
    }
}
