package io.choerodon.devops.infra.dto.gitlab.ci;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModelProperty;

import io.choerodon.devops.infra.annotation.YamlProperty;

/**
 * @author zmf
 * @since 20-4-2
 */
public class CiJob {
    @ApiModelProperty("job的镜像")
    private String image;
    @ApiModelProperty("所属stage")
    private String stage;

    @ApiModelProperty("job的并发数")
    private Integer parallel;
    @ApiModelProperty("ci里面的services")
    private List<CiJobServices> services;
    @YamlProperty(value = "after_script")
    @JsonProperty("after_script")
    @ApiModelProperty("after_script")
    private List<String> afterScript;
    @ApiModelProperty("包含的脚本")
    private List<String> script;
    @ApiModelProperty("匹配条件")
    private OnlyExceptPolicy only;
    @ApiModelProperty("排除条件")
    private OnlyExceptPolicy except;
    @ApiModelProperty("缓存配置")
    private Cache cache;

    @ApiModelProperty("运行该job的时间")
    private String when;

    @ApiModelProperty("当when的值为delayed时，设置该字段，表示延时时间")
    @YamlProperty(value = "start_in")
    private String startIn;

    public Integer getParallel() {
        return parallel;
    }

    public void setParallel(Integer parallel) {
        this.parallel = parallel;
    }

    public List<String> getAfterScript() {
        return afterScript;
    }

    public void setAfterScript(List<String> afterScript) {
        this.afterScript = afterScript;
    }

    public List<CiJobServices> getServices() {
        return services;
    }

    public void setServices(List<CiJobServices> services) {
        this.services = services;
    }

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

    public String getWhen() {
        return when;
    }

    public void setWhen(String when) {
        this.when = when;
    }

    public String getStartIn() {
        return startIn;
    }

    public void setStartIn(String startIn) {
        this.startIn = startIn;
    }
}
