package io.choerodon.devops.infra.dto.gitlab.ci;

import java.util.List;

import io.swagger.annotations.ApiModelProperty;

/**
 * @author zmf
 * @since 20-4-2
 */
public class CiJob {
    @ApiModelProperty("job的镜像")
    private String image;
    @ApiModelProperty("所属stage")
    private String stage;
    @ApiModelProperty("包含的脚本")
    private List<String> script;
    @ApiModelProperty("匹配条件")
    private OnlyExceptPolicy only;
    @ApiModelProperty("排除条件")
    private OnlyExceptPolicy except;
    @ApiModelProperty("缓存配置")
    private Cache cache;

    public String getStage() {
        return stage;
    }

    public void setStage(String stage) {
        this.stage = stage;
    }

    public List<String> getScript() {
        return script;
    }

    public void setScript(List<String> script) {
        this.script = script;
    }

    public OnlyExceptPolicy getOnly() {
        return only;
    }

    public void setOnly(OnlyExceptPolicy only) {
        this.only = only;
    }

    public OnlyExceptPolicy getExcept() {
        return except;
    }

    public void setExcept(OnlyExceptPolicy except) {
        this.except = except;
    }

    public Cache getCache() {
        return cache;
    }

    public void setCache(Cache cache) {
        this.cache = cache;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }
}
