package io.choerodon.devops.api.vo.template;

import io.swagger.annotations.ApiModelProperty;
import org.hzero.starter.keyencrypt.core.Encrypt;

import javax.persistence.GeneratedValue;
import javax.persistence.Id;

public class CiTplChartDeployCfgVO {


    @Id
    @GeneratedValue
    @Encrypt
    private Long id;

    @ApiModelProperty(value = "是否校验环境权限")
    private Boolean skipCheckPermission;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Boolean getSkipCheckPermission() {
        return skipCheckPermission;
    }

    public void setSkipCheckPermission(Boolean skipCheckPermission) {
        this.skipCheckPermission = skipCheckPermission;
    }

}
