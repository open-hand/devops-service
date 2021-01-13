package io.choerodon.devops.infra.dto.gitlab;

import io.swagger.annotations.ApiModelProperty;

public class Project {
    @ApiModelProperty("gitlab项目id")
    private Integer id;
    @ApiModelProperty("ci文件位置")
    private String ciConfigPath;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getCiConfigPath() {
        return ciConfigPath;
    }

    public void setCiConfigPath(String ciConfigPath) {
        this.ciConfigPath = ciConfigPath;
    }
}
