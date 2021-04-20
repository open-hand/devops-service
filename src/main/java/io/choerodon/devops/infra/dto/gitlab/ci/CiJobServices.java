package io.choerodon.devops.infra.dto.gitlab.ci;


import io.swagger.annotations.ApiModelProperty;

/**
 * Created by wangxiang on 2021/4/19
 */
public class CiJobServices {
    @ApiModelProperty("services里面的镜像名称")
    private String name;
    @ApiModelProperty("别名")
    private String alias;


    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAlias() {
        return alias;
    }

    public void setAlias(String alias) {
        this.alias = alias;
    }
}
