package io.choerodon.devops.api.vo;

import io.swagger.annotations.ApiModelProperty;
import org.hzero.starter.keyencrypt.core.Encrypt;

/**
 * Creator: Runge
 * Date: 2018/9/4
 * Time: 14:49
 * Description:
 */
public class DevopsEnvGroupVO {
    @Encrypt
    private Long id;
    @ApiModelProperty("项目id")
    private Long projectId;
    @ApiModelProperty("group名称")
    private String name;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getProjectId() {
        return projectId;
    }

    public void setProjectId(Long projectId) {
        this.projectId = projectId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

}
