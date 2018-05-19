package io.choerodon.devops.infra.dataobject.harbor;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Project {
    @JsonProperty("project_name")
    private String name;
    @JsonProperty("public")
    private Integer _public;

    public Project(String name, Integer _public) {
        this.name = name;
        this._public = _public;
    }

    public Project() {
    }

    public Integer get_public() {
        return _public;
    }

    public void set_public(Integer _public) {
        this._public = _public;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
