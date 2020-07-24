package io.choerodon.devops.api.vo;

import io.swagger.annotations.ApiModelProperty;

/**
 * Maven发布jar到maven仓库的配置对象
 * 这里只有一个字段也存在对象是为了之后拓展方便
 *
 * @author zmf
 * @since 2020/6/12
 */
public class MavenDeployRepoSettings {
    /**
     * 做成set而不是long类型是处于拓展性考虑
     */
    @ApiModelProperty("nexus的maven仓库在制品库的主键id")
    private Long nexusRepoIds;

    public Long getNexusRepoIds() {
        return nexusRepoIds;
    }

    public void setNexusRepoIds(Long nexusRepoIds) {
        this.nexusRepoIds = nexusRepoIds;
    }
}
