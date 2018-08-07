package io.choerodon.devops.domain.application.valueobject;

import java.util.List;

public class GitOpsMetaData {
    private String commit;
    private List<Error> errors;

    public String getCommit() {
        return commit;
    }

    public void setCommit(String commit) {
        this.commit = commit;
    }

    public List<Error> getErrors() {
        return errors;
    }

    public void setErrors(List<Error> errors) {
        this.errors = errors;
    }
}
