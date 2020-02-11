package io.choerodon.devops.infra.dto.gitlab;

import io.swagger.annotations.ApiModelProperty;

/**
 * @author zmf
 * @since 2/3/20
 */
public class GitlabUserWithPasswordDTO extends GitLabUserDTO {
    @ApiModelProperty("gitlab用户密码")
    private String password;

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
