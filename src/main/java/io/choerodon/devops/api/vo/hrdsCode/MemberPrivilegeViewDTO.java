package io.choerodon.devops.api.vo.hrdsCode;

import io.swagger.annotations.ApiModelProperty;
import org.hzero.starter.keyencrypt.core.Encrypt;


public class MemberPrivilegeViewDTO {
    @Encrypt
    @ApiModelProperty("应用服务id")
    private Long repositoryId;

    @ApiModelProperty("权限")
    private Integer accessLevel;

    public Long getRepositoryId() {
        return repositoryId;
    }

    public MemberPrivilegeViewDTO setRepositoryId(Long repositoryId) {
        this.repositoryId = repositoryId;
        return this;
    }

    public Integer getAccessLevel() {
        return accessLevel;
    }

    public MemberPrivilegeViewDTO setAccessLevel(Integer accessLevel) {
        this.accessLevel = accessLevel;
        return this;
    }
}

