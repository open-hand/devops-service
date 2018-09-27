package io.choerodon.devops.domain.application.valueobject;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Error {
    private String id;
    private String path;
    private String commit;
    @JsonProperty(value = "error")
    private String errors;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getErrors() {
        return errors;
    }

    public void setErrors(String errors) {
        this.errors = errors;
    }

    public String getCommit() {
        return commit;
    }

    public void setCommit(String commit) {
        this.commit = commit;
    }
}
