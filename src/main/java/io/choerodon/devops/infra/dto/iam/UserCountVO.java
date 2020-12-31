package io.choerodon.devops.infra.dto.iam;

import io.swagger.annotations.ApiModelProperty;

/**
 * @author zmf
 * @since 2020/12/30
 */
public class UserCountVO {
    @ApiModelProperty("用户的数量")
    private Integer count;

    public UserCountVO() {
    }

    public UserCountVO(Integer count) {
        this.count = count;
    }

    public Integer getCount() {
        return count;
    }

    public void setCount(Integer count) {
        this.count = count;
    }
}
