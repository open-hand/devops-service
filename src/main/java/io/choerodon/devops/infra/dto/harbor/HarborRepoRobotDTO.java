package io.choerodon.devops.infra.dto.harbor;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

/**
 * description
 *
 * @author mofei.li@hand-china.com 2020/06/11 11:04
 */
@ApiModel("Harbor仓库机器人DTO")
public class HarborRepoRobotDTO {
    @ApiModelProperty(value = "账户名")
    private String name;
    @ApiModelProperty(value = "账户token")
    private String token;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }
}
